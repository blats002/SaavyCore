/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.net.snmp;


import com.ireasoning.protocol.snmp.*;
import org.saavy.dom.SaavyElement;
import org.saavy.platform.Module;
import org.saavy.platform.net.CommStack;
import org.saavy.platform.net.CommStackException;
import org.saavy.platform.net.ConnectionFailedException;
import org.saavy.platform.net.Packet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rgsaavedra
 */
public class SNMPStack extends CommStack<SNMPHandler> {

    public SNMPStack() {
        super(new SNMPHandler());
    }

    @Override
    public void init(SaavyElement init) {
        if (init.getName().equalsIgnoreCase("mibs")) {
            ArrayList<String> mibs = new ArrayList<String>();
            for (SaavyElement mib : init.getChildren("mib")) {
                if (mib.hasAttribute("path")) {
                    mibs.add(mib.getAttribute("path"));
                }
            }
            getHandler().getLoginDetails().setMibFiles(mibs);
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        if (snmpProcessor != null) {
            snmpProcessor.stopProcessing();
        }
        getHandler().close();
    }

    @Override
    public boolean isConnected() {
        return getHandler().isConnected();
    }
    private ArrayBlockingQueue<SNMPQueue> snmpQueues = new ArrayBlockingQueue<SNMPQueue>(100);
    private ArrayBlockingQueue<SNMPQueue> snmpQueuesPriority = new ArrayBlockingQueue<SNMPQueue>(100);
    private ArrayBlockingQueue<SNMPQueue> threadPool = new ArrayBlockingQueue<SNMPQueue>(1);

    private class SNMPQueue extends Thread {

        private Module module;
        private Packet packet;

        public SNMPQueue(Module module, Packet packet) {
            super("SNMPQueue:" + (packet != null ? packet.getPacketElement().getName() + ":from(" + packet.getFrom() + ")" : ""));
            this.module = module;
            this.packet = packet;
        }

        @Override
        public void run() {
//            System.out.println("Processing SNMP:" + threadPool.size());

            SaavyElement req = packet.getPacketElement();
            SaavyElement response = new SaavyElement();
            response.setAttribute("timestamp", Calendar.getInstance().getTime());
            response.setName(req.getName());
            SnmpSession session = null;
            try {

                if (module != null && module.getATLPManager().getCommStackManager().getPoller().isForcePollersOnly()) {
                    if (/*packet.getPacketElement().hasAttribute("force") && */packet.getPacketElement().getAttribute("force", "false").equalsIgnoreCase("false")) {
                        throw new CommStackException("SNMP  Currently down");
                    }
                }
                session = getHandler().createSnmpSession();
                Boolean successSet = null;

                for (SaavyElement snmp : req.getChildren("pdu")) {
                    boolean processNext = true;

                    if (snmp.hasAttribute("processif")) {
                        processNext = successSet != null && successSet.equals(snmp.getObjectAttribute("processif"));
                    }

                    if (processNext) {
                        String action = snmp.getAttribute("action");
                        if (action.equalsIgnoreCase("choiceset")) {
                            snmp.setAttribute("successset", successSet);
                        }
                        SaavyElement pdu = doAction(session, snmp);
                        response.addChildren(pdu);
                        if (!isLoggedIn()) {
                            setLoggedIn(true);
                        }
                        successSet = null;
                        if (action.equals("set") || action.equals("multipleset")) {
                            successSet = !pdu.hasAttribute("seterror");
                        }
                    } else {
                        successSet = null;
                    }

                }

            } catch (CommStackException ex) {
                response.setAttribute("error", "true");
                response.setAttribute("exception", ex);
//                    Logger.getLogger(SNMPStack.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                response.setAttribute("error", "true");
                response.setAttribute("exception", new ConnectionFailedException(ex.getMessage(), ex));
//                    Logger.getLogger(SNMPStack.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (response != null) {
                    response.setAttribute("to", packet.getFrom());
                    response.setAttribute("from", packet.getTo());
                    if (response.getAttribute("error").equalsIgnoreCase("true")) {
                        response.setName(packet.getPacketElement().getName());
                        response.addChildren(packet.getPacketElement());
                        Exception ex = (Exception) response.getObjectAttribute("exception");
                        if (module != null && module.getProperties().get("HOST").toString().equalsIgnoreCase("debug")) {
                            Logger.getLogger(SNMPStack.class.getName()).log(Level.SEVERE, response.getXML());
                        }
                    }
                    if (!sendAndWait) {
                        if (module != null) {
                            module.processSendResponse(Packet.createPacket(response), packet);
                        }
                    } else {
                        responseForWait = Packet.createPacket(response);
                    }
                }

                threadPool.remove(this);
//                System.out.println("Removed:" + threadPool.remove(SNMPQueue.this));
                if (session != null) {
                    session.close();
                }
            }
//            System.out.println("Processing DONE:" + threadPool.size());
        }

        public void processQueue() {
            start();
        }
        private boolean sendAndWait = false;
        private Packet responseForWait = null;

        private Packet processQueueAndWait() {
            try {
                sendAndWait = true;
                run();
                return responseForWait;
            } finally {
                sendAndWait = false;
            }
        }
    }

    @Override
    public Packet forceAction(Module module, Packet request) {
        SNMPQueue que = new SNMPQueue(module, request);
        if (snmpProcessor == null) {
            snmpProcessor = new SNMPProcessor();
            snmpProcessor.start();
        }
        this.snmpProcessor.pause();
        Packet response = que.processQueueAndWait();
        this.snmpProcessor.releasePause();
        return response;
    }

    @Override
    public void doAction(Module module, Packet packet) {
        try {
            boolean prioritize = packet.getPacketElement().getAttribute("prioritize").equalsIgnoreCase("true");
            if (prioritize) {
                snmpQueuesPriority.put(new SNMPQueue(module, packet));
//                ArrayList<SNMPQueue> collection = new ArrayList<SNMPQueue>();
//                snmpQueues.drainTo(collection);
//                snmpQueues.put(new SNMPQueue(module, packet));
//                snmpQueues.addAll(collection);
            } else {
                snmpQueues.put(new SNMPQueue(module, packet));
            }
            if (snmpProcessor == null) {
                snmpProcessor = new SNMPProcessor();
                snmpProcessor.start();
            }
            if (snmpProcessor.waiting) {
                synchronized (snmpProcessor) {
                    snmpProcessor.notifyAll();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SNMPStack.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    private boolean running = false;
    private SNMPProcessor snmpProcessor;

    public class SNMPProcessor extends Thread {

        private boolean waiting = false;

        public SNMPProcessor() {
            super("SNMP Processor");
        }

        @Override
        public void run() {
            running = true;
            while (running) {
                try {
                    if (!reqPause && snmpQueues.isEmpty() && snmpQueuesPriority.isEmpty()) {
//                        Thread.sleep(1000);
                        synchronized (this) {
                            waiting = true;
                            while (!reqPause && snmpQueues.isEmpty()) {
                                wait(1000 * 30);
                            }
                        }
                    } else {
                        synchronized (this) {
                            wait(500);
                        }
                    }
                    waiting = false;

                    if (reqPause) {
                        reqPause = false;
                        pause = true;
                    }

                    SNMPQueue current = null;
                    if (!pause) {
                        current = snmpQueuesPriority.poll();

                        //                    current = snmpQueuesPriority.poll(1, TimeUnit.SECONDS);
                        if (current == null) {
                            //                        current = snmpQueues.poll(1, TimeUnit.MINUTES);
                            current = snmpQueues.poll();
                        }
                    }

                    if (current == null || current.module == null) {
                        continue;
                    }
//                    System.out.println("current:"+current.getName());
//                    System.out.println("Initializing:" + threadPool.size());
                    threadPool.put(current);
//                    System.out.println("SizeP:"+snmpQueuesPriority.size());
//                    System.out.println("SizeN:"+snmpQueues.size());
//                    snmpQueuesPriority.remove(current);
//                    snmpQueues.remove(current);
//                    System.out.println("SizeP:"+snmpQueuesPriority.size());
//                    System.out.println("SizeN:"+snmpQueues.size());
                    if (running) {
                        current.processQueue();
                    }
//                    current.run();
                } catch (Exception ex) {
                    Logger.getLogger(SNMPStack.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            running = false;
//            System.out.println("SNMP Processor Done");
        }

        public void stopProcessing() {
            running = false;
            snmpQueuesPriority.clear();
            snmpQueues.clear();
            snmpQueues.add(new SNMPQueue(null, null));
            synchronized (this) {
                notifyAll();
            }
        }
        private boolean reqPause = false;
        private boolean pause = false;

        private void pause() {
            reqPause = true;
            while (!pause) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }

        private void releasePause() {
            pause = false;
        }
    }

    public void convertVarBind(SaavyElement response, SnmpVarBind varBind) {
        if (varBind.getValue() instanceof SnmpIpAddress) {
//            if (varBind.getValue().getType() == SnmpIpAddress.IPADDRESS) {
//                SnmpIpAddress.convertPhysAddress(arg0);
//            }
//            response.setAttribute("syntax", new SnmpIpAddress(((SnmpIpAddress) varBind.getValue()).toString()));
            response.setAttribute("value", ((SnmpIpAddress) varBind.getValue()).toString());
        } else if (varBind.getValue() instanceof SnmpOctetString) {
//            new SnmpOctetString()
//            response.setAttribute("value", new String(((SnmpOctetString) varBind.getValue()).getValue()));
            response.setAttribute("value", new SnmpOctetString((SnmpOctetString) varBind.getValue()).toString());
            String val = response.getAttribute("value");
            if (val.length() > 0 && val.replaceAll("0x[0-9a-fA-F][0-9a-fA-F]\\s*", "").length() == 0) {
                response.setAttribute("hex", response.getObjectAttribute("value"));
            } else {
                response.setAttribute("hex", ((SnmpOctetString) varBind.getValue()).toHexString());
            }
//            System.out.println("SNMPOctetStringType:"+varBind.getValue().getTypeString());
            if (varBind.getValue().getType() == SnmpOctetString.BITS) {
                String translateValue = MibUtil.translateValue(varBind.getName(), response.getAttribute("hex"));
                if (!translateValue.equals(response.getAttribute("hex"))) {
                    response.setAttribute("syntax", translateValue.trim());
                }
            }
        } else if (varBind.getValue() instanceof SnmpUInt) {
            if (varBind.getValue() instanceof SnmpTimeTicks) {
                response.setAttribute("value", (Long) ((SnmpTimeTicks) varBind.getValue()).getValue());

                response.setAttribute("timestring", ((SnmpTimeTicks) varBind.getValue()).getTimeString());
            } else {
                response.setAttribute("value", (Long) ((SnmpUInt) varBind.getValue()).getValue());
            }
        } else if (varBind.getValue() instanceof SnmpInt) {
            response.setAttribute("value", (Integer) ((SnmpInt) varBind.getValue()).getValue());
            String translateValue = MibUtil.translateValue(varBind.getName(), response.getAttribute("value"));

            if (!translateValue.equals(response.getAttribute("value"))) {
                response.setAttribute("syntax", translateValue.trim());
            }
        } else {
            response.setAttribute("value", String.valueOf(varBind.getValue()));
        }
    }

    public SaavyElement convertVarBind(SnmpSession session, SnmpTableModel model, SaavyElement snmp) {
        SaavyElement table = new SaavyElement();
        table.setName("table");
        int columnCount = model.getColumnCount();
        int rowCount = model.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            SaavyElement rowElement = new SaavyElement();
            rowElement.setName("row");
            SnmpVarBind[] binds = model.getRow(row);
            for (int column = 0; column < columnCount; column++) {
                SaavyElement colElement = new SaavyElement();
                colElement.setName("column");
                SnmpVarBind vind = binds[column];

                String oidColumn = model.getColumnOID(column);
                if (vind.getValue() instanceof SnmpNull) {
//                    colElement.setAttribute("oid", oidColumn);
//                    colElement.setAttribute("oidname", MibUtil.translateOID(oidColumn, false));
//                    colElement.setAttribute("type", "No Such Name");
                } else {
                    colElement.setAttribute("oid", vind.getName().toString());
                    colElement.setAttribute("oidname", MibUtil.translateOID(vind.getName(), false)/*.replaceAll("\\.0$", "")*/);
                    colElement.setAttribute("type", vind.getValue().getTypeString());
                    convertVarBind(colElement, vind);
                    rowElement.addChildren(colElement);
                }


            }
            rowElement.setAttribute("index", rowElement.getChild("column").getAttribute("oidname").replaceAll("^\\w*?\\.", ""));
            if (snmp.hasAttribute("indexlookup")) {
                //GUIA-174
                String lookup = snmp.getAttribute("indexlookup");
                try {
                    if (rowElement.getAttribute("index").contains(".")) {
                        String[] indices = rowElement.getAttribute("index").split("\\.");
                        Pattern pattern = Pattern.compile("\\$(\\d+)");
                        Matcher matcher = pattern.matcher(lookup);
                        String indexOid = lookup;
                        while (matcher.find()) {
                            String number = matcher.group(1);
                            indexOid = indexOid.replaceAll("\\$" + Pattern.quote(number), indices[Integer.parseInt(number) - 1]);
                        }
                        SnmpPdu pdu = getHandler().get(session, indexOid);
                        convertVarBind(rowElement, pdu.getFirstVarBind());
                    } else {
                        String index = rowElement.getAttribute("index");
                        String indexOid = lookup.replaceAll("\\$", index);
                        SnmpPdu pdu = getHandler().get(session, indexOid);
                        convertVarBind(rowElement, pdu.getFirstVarBind());
                    }
                } catch (CommStackException ex) {
                }
                //GUIA-174
            }
            table.addChildren(rowElement);
//            System.out.println("AddedRow:" + (row + 1));
        }
        return table;
    }

    private SaavyElement doAction(SnmpSession session, SaavyElement snmp) throws CommStackException {
        String action = snmp.getAttribute("action");
        String oid = snmp.getAttribute("oid");
        String type = snmp.getAttribute("type", "");
        Object value = snmp.getObjectAttribute("value");
        SaavyElement pduElement = new SaavyElement();
        SnmpOID snmpOID = MibUtil.lookupOID(oid);
        if ((action != null && snmpOID != null) || action.equals("multipleset") || action.equals("choiceset")) {
            if (action.equals("choiceset")) {
                SaavyElement pduSuccess = snmp.getChildren().get(0);
                SaavyElement pduFailed = snmp.getChildren().get(1);
                Boolean successSet = (Boolean) snmp.getObjectAttribute("successset");
                if (successSet == null || successSet) {
                    if (pduSuccess != null) {
                        return doAction(session, pduSuccess);
                    } else {
                        return pduElement;
                    }
                } else {
                    if (pduFailed != null) {
                        return doAction(session, pduFailed);
                    } else {
                        return pduElement;
                    }
                }
            } else if (action.equals("multipleset")) {
                pduElement.setName("pdus");
                pduElement.setAttribute("type", "multipleset");
                SNMPPacket groupPacket = (SNMPPacket) snmp.getObjectAttribute("grouppacket");
                ArrayList<SaavyElement> pdus = groupPacket.getPacketElement().getChildren("pdu");
                int count = pdus.size();
                String[] oidStrings = new String[count];
                String[] types = new String[count];
                Object[] values = new Object[count];

                for (int x = 0; x < count; x++) {
                    SaavyElement pdu = pdus.get(x);
                    oidStrings[x] = pdu.getAttribute("oid");
                    types[x] = pdu.getAttribute("type", "");
                    values[x] = pdu.getObjectAttribute("value");
                }

                SnmpPdu pdu = getHandler().multipleSet(session, oidStrings, types, values);
                for (int x = 0; x < pdu.getVarBindCount(); x++) {
                    SnmpOID snmpOID2 = MibUtil.lookupOID(oidStrings[x]);
                    SaavyElement pduelem = new SaavyElement();
                    pduelem.setName("pdu");
                    SnmpVarBind var = pdu.getVarBind(x);
                    pduelem.setAttribute("oid", snmpOID2.toString());
//                    pduelem.setAttribute("actiontest", action);
//                    System.out.println("VarBind:"+var.toString());

                    if (var.getValue() instanceof SnmpNull) {
                        if(MibUtil.translateOID(snmpOID2, false) != null){
                            pduelem.setAttribute("oidname", MibUtil.translateOID(snmpOID2, false).replaceAll("\\.0$", ""));
                        }
                        pduelem.setAttribute("type", var.getValue().getTypeString());
                    } else {
                        if(MibUtil.translateOID(snmpOID2, false) != null){
                            pduelem.setAttribute("oidname", MibUtil.translateOID(snmpOID2, false).replaceAll("\\.0$", ""));
                        }
                        pduelem.setAttribute("type", var.getValue().getTypeString());
                        convertVarBind(pduelem, var);
                    }

//                    System.out.println("Type:"+var.getType());
//                    System.out.println("TypeString:"+var.getTypeString());


                    pduElement.addChildren(pduelem);
                }

                if (!pdu.getErrorStatusString().equalsIgnoreCase("No Error")) {
                    pduElement.setAttribute("seterror", pdu.getErrorStatusString());
                }


            } else if (action.equals("set")) {
                SnmpPdu pdu = getHandler().set(session, oid, type, value);
                pduElement.setName("pdu");
                pduElement.setAttribute("oid", snmpOID.toString());
                if (pdu.getFirstVarBind().getValue() instanceof SnmpNull) {
                    if(MibUtil.translateOID(snmpOID, false) != null){
                        pduElement.setAttribute("oidname", MibUtil.translateOID(snmpOID, false).replaceAll("\\.0$", ""));
                    }
                    pduElement.setAttribute("type", pdu.getFirstVarBind().getValue().getTypeString());
                } else {
                    if(MibUtil.translateOID(snmpOID, false) != null){
                        pduElement.setAttribute("oidname", MibUtil.translateOID(snmpOID, false).replaceAll("\\.0$", ""));
                    }
                    pduElement.setAttribute("type", pdu.getFirstVarBind().getValue().getTypeString());
                    convertVarBind(pduElement, pdu.getFirstVarBind());
                }

                if (!pdu.getErrorStatusString().equalsIgnoreCase("No Error")) {
                    pduElement.setAttribute("seterror", pdu.getErrorStatusString());
                }

            } else if (action.equals("get")) {
                SnmpPdu pdu = getHandler().get(session, oid);
                pduElement.setName("pdu");
                pduElement.setAttribute("oid", snmpOID.toString());
                if (pdu.getFirstVarBind().getValue() instanceof SnmpNull) {
                    if(MibUtil.translateOID(snmpOID, false) != null){
                        pduElement.setAttribute("oidname", MibUtil.translateOID(snmpOID, false).replaceAll("\\.0$", ""));
                    }
                    pduElement.setAttribute("type", pdu.getFirstVarBind().getValue().getTypeString());
                } else {
                    if(MibUtil.translateOID(snmpOID, false) != null){
                        pduElement.setAttribute("oidname", MibUtil.translateOID(snmpOID, false).replaceAll("\\.0$", ""));
                    }
                    pduElement.setAttribute("type", pdu.getFirstVarBind().getValue().getTypeString());
                    convertVarBind(pduElement, pdu.getFirstVarBind());
                }
            } else if (action.equals("getnext")) {
                SnmpPdu pdu = getHandler().getNext(session, oid);
                pduElement.setName("pdu");
                pduElement.setAttribute("oid", pdu.getFirstVarBind().getName().toString());
                if (pdu.getFirstVarBind().getValue() instanceof SnmpNull) {
                    if(MibUtil.translateOID(snmpOID, false) != null){
                        pduElement.setAttribute("oidname", MibUtil.translateOID(pdu.getFirstVarBind().getName(), false).replaceAll("\\.0$", ""));
                    }
                    pduElement.setAttribute("type", pdu.getFirstVarBind().getValue().getTypeString());
                } else {
                    if(MibUtil.translateOID(snmpOID, false) != null){
                        pduElement.setAttribute("oidname", MibUtil.translateOID(pdu.getFirstVarBind().getName(), false).replaceAll("\\.0$", ""));
                    }
                    pduElement.setAttribute("type", pdu.getFirstVarBind().getValue().getTypeString());
                    convertVarBind(pduElement, pdu.getFirstVarBind());
                }
            } else if (action.equals("gettable")) {
                SnmpTableModel model = getHandler().getTable(session, oid);
                pduElement.setName("pdu");
                pduElement.setAttribute("oid", snmpOID.toString());
                pduElement.setAttribute("oidname", MibUtil.translateOID(snmpOID, false));
                pduElement.setAttribute("type", "table");
                pduElement.addChildren(convertVarBind(session, model, snmp));
            } else if (action.equals("tree")) {
                SnmpVarBind[] vars = null;

                pduElement.setName("pdu");
                pduElement.setAttribute("oid", snmpOID.toString());
                pduElement.setAttribute("oidname", MibUtil.translateOID(snmpOID, false));
                pduElement.setAttribute("type", "tree");
                boolean hasValueLookup = snmp.hasAttribute("valuelookup");
                String oidlookup = snmp.getAttribute("valuelookup");
                try {
                    pduElement.setAttribute("indices", new ArrayList<String>());

                    vars = getHandler().getWorkAroundTable(session, oid);

                    String firstColumn = null;
                    for (SnmpVarBind bind : vars) {
                        SaavyElement node = SaavyElement.createXML("<node/>");

                        String oidname = MibUtil.translateOID(bind.getName(), false);
                        if (oidname != null) {
                            node.setAttribute("oidname", oidname);
                            if (firstColumn == null) {
                                firstColumn = node.getAttribute("oidname").replaceAll("\\..*", "");
                            }
                            if (!bind.getValue().getTypeString().equals("Null")) {
                                node.setAttribute("type", bind.getValue().getTypeString());
                                convertVarBind(node, bind);
                                if (hasValueLookup) {
                                    String valueLookUp = node.getAttribute("value");
                                    SnmpPdu pduLookup = getHandler().get(session, oidlookup.replaceAll("\\$", valueLookUp));
                                    if (pduLookup.getFirstVarBind().getType() != SnmpDataType.NULL) {
                                        SaavyElement temp = new SaavyElement();
                                        temp.setName("temp");
                                        convertVarBind(temp, pduLookup.getFirstVarBind());
                                        node.setAttribute("valuelookup", temp.getAttribute("value"));
                                    }
                                }
                                pduElement.addChildren(node);
                            }
                        }
                        node = null;
                    }
                    pduElement.setAttribute("indices", getIndicesFromTree(pduElement, firstColumn));
                } catch (CommStackException commStackException) {
                    if (!commStackException.getMessage().equalsIgnoreCase("Error Obtaining Data: Device returned null!")) {
                        throw commStackException;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(SNMPStack.class.toString()).log(Level.SEVERE, MibUtil.translateOID(snmpOID, true), ex);
                }
            }
        }
//        pduElement.setAttribute("actiontest", action);
        return pduElement;
    }

    public ArrayList<String> getIndicesFromTree(SaavyElement pdu, String firstColumnName) {
        ArrayList<String> indices = new ArrayList<String>();
        for (SaavyElement node : pdu.getChildren("node")) {
            String treeName = node.getAttribute("oidname").replaceAll("\\..*", "");
            if (treeName.equalsIgnoreCase(firstColumnName)) {
                indices.add(node.getAttribute("oidname").replaceAll(firstColumnName + "\\.", ""));
                //System.out.println("Adding:"+node.getAttribute("oidname"));
            } else {
                break;
            }
        }
        return indices;
    }
//    private void processSNMPPacket(final Module module, final Packet packet) {
//        
//
//    }
//    public static void main (String a[]){
////        Object test = new int[]{40,60,29,90};
////        StringBuffer hex = new StringBuffer();
////        if(test instanceof String){
////            for(int x = 0 ; x < test.toString().length();x++ ){
////                hex.append("0x").append(Integer.toHexString((int)test.toString().charAt(x)).toUpperCase()).append(" ");
////            }
////        }else if (test instanceof int[]){
////            int[] array = (int[])test;
////            for(int x = 0 ; x < array.length;x++ ){
////                hex.append("0x").append(Integer.toHexString(array[x]).toUpperCase()).append(" ");
////            }
////        }
////        System.out.println("Hex:"+hex.toString().trim());
//
//        System.out.println("Match:"+("="));
//    }
}
