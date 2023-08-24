/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.platform.net;


import org.saavy.dom.SaavyElement;
import org.saavy.dom.SaavyHashMap;
import org.saavy.platform.ATLPManager;
import org.saavy.platform.Engine;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rgsaavedra
 */
public class CommStackManager {

    private ATLPManager manager;
    private SaavyHashMap<String, CommStack> mCommStack;
    private SaavyHashMap<String, CommStackListener> listeners;
    private SaavyHashMap<String, Poll> polls;
    private Poller poller;

    public CommStackManager(ATLPManager manager) {
        this.manager = manager;
        mCommStack = new SaavyHashMap<String, CommStack>();
        listeners = new SaavyHashMap<String, CommStackListener>();
        poller = new Poller(manager);
    }

    public void close() {
        poller.close();
        for (Engine commStack : mCommStack.values()) {
            commStack.stop();
        }
        queue = null;
        if (commstackProccessor != null) {
            synchronized (commstackProccessor) {
                commstackProccessor.notifyAll();
            }
        }
    }

    public CommStack getCommStack(String id) {
        return mCommStack.get(id);
    }

    public void addCommStack(String id, CommStack val) {
        this.mCommStack.put(id, val);
        val.setId(id);
        val.start();
//        threadPool = this.mCommStack.size();
    }

    public boolean isCommStackExist(String id) {
        return this.mCommStack.containsKey(id);
    }

    public CommStackListener getCommStackListener(String id) {
        return listeners.get(id);
    }

    public void addCommStackListener(String id, CommStackListener l) {
        listeners.put(id, l);
    }

    public void removeCommStackListner(String id) {
        listeners.remove(id);
    }

    public void removePolls(String cannonicalID) {
        if (getPoller() != null) {
            getPoller().removePolls(cannonicalID);
        }
    }
    private ThreadQueue commstackProccessor;

    public void start() {
        if (queue == null) {
            queue = new ArrayBlockingQueue<Packet>(100);
        }
    }
    private int threadRun = 0;
    private int threadPool = 1;

    public int getThreadPool() {
        if (this.threadPool < 1) {
            this.threadPool = 1;
        }
        return threadPool;
    }

    public void setThreadPool(int threadPool) {
        this.threadPool = threadPool;
    }

    public Poller getPoller() {
        return poller;
    }

//    void processSendResponse(Module module, Packet responsePacket, Packet requestPacket) {
//        if (requestPacket.getPacketElement().hasAttribute("dialog") && requestPacket.getPacketElement().getObjectAttribute("dialog") instanceof JDialog) {
//            responsePacket.getPacketElement().setAttribute("dialog", requestPacket.getPacketElement().getObjectAttribute("dialog"));
//        }
//        
//    }
    public class ThreadQueue extends Thread {

        CommStackManager manager;

        public ThreadQueue(CommStackManager manager) {
            super("CommStackManager Thread Queue");
            this.manager = manager;
        }
        private boolean waiting = false;
        private boolean threadFull = false;

        @Override
        public void run() {

            while (queue != null) {
                /*<request id="">
                <method id="login" bean="loginCompo.loginbean" type="passive"/>
                 *  ......
                 *</request>
                 */

//                double freeMem = getFreeMemory();
//                System.out.println("FM:"+freeMem);

                Packet req = null;
                try {
//                    int tries = 0;

                    while ((getFreeMemory() <= 5/* && tries < 1*/) || (queue != null && threadRun >= getThreadPool())) {
                        synchronized (this) {
                            try {
                                if (getFreeMemory() <= 5/* && tries < 1*/) {
//                                    System.out.println("Explicit GC");
//                                    System.gc();
//                                    tries++;
                                    Thread.sleep(500);
                                    continue;
                                } else {
                                    threadFull = true;
                                }
//                                System.out.println("Waiting:"+threadRun);
                                this.wait(5000);
                            } catch (InterruptedException ex) {
                                //ignore
                            }
                        }
                    }

                    threadFull = false;
                    if (queue != null) {
                        synchronized (this) {
                            if (queue.isEmpty()) {
//                                System.out.println("Waiting queue empty");
                                waiting = true;
                                while(queue.isEmpty()){
                                    this.wait(1000*30);
                                }
                            } else {
                                this.wait(500);
                            }
                        }
                        waiting = false;
                        if (queue != null) {
                            req = queue.poll();
                        }
                    }
                    if (req == null) {
                        continue;
                    }

                    ArrayList<Packet> reqs = new ArrayList();
                    reqs.add(req);

                    for (Packet reqTemp : reqs) {
                        threadRun++;
//                        System.out.println("Processing Request:" + threadRun);
                        if (reqTemp.getPacketElement().getAttribute("polling").equalsIgnoreCase("true")) {
                            if (getPoller().isForcePollersOnly() && reqTemp.getPacketElement().hasAttribute("force") && reqTemp.getPacketElement().getAttribute("force", "false").equalsIgnoreCase("false")) {
//                                System.out.println("Skipping request:"+reqTemp.getPacketElement().getName());
                                threadRun--;
                                continue;
                            }
                        }
                        if (reqTemp instanceof HybridPacket) {
                            new HybridRequest(this, (HybridPacket) reqTemp).start();
                        } else {
                            new SingleRequest(this, reqTemp).start();
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(CommStackManager.class.getName()).log(Level.SEVERE, null, ex);
                    //fireError(false, new CommStackException(ex.getMessage(), ex));
                }
            }
//            System.out.println("Commstack manager done");
        }

        public void fireThreadEnded() {
            threadRun--;
            synchronized (this) {
                if (threadFull) {
                    threadFull = false;
                    notifyAll();
                }
                if (!queue.isEmpty() && commstackProccessor.waiting) {
                    commstackProccessor.notify();
                }
            }
//            System.out.println("Fire Thread Ended:" + threadRun);
        }

        private double getFreeMemory() {
            /*<request id="">
            <method id="login" bean="loginCompo.loginbean" type="passive"/>
             *  ......
             *</request>
             */
            long total2 = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            double freeMem = (((double) free) / (double) total2) * 100D;
//            double freeMem = ((Runtime.getRuntime().freeMemory() * 100D) / Runtime.getRuntime().totalMemory());
            return freeMem;
        }
    }

    public void responsePacket(String id, SaavyElement element) {
        CommStackListener list = getCommStackListener(id);
        if (list != null) {
            list.packetReceived(Packet.createPacket(element));
        }

    }

    public void sendErrorPacket(String id, CommStackException ex) {
        CommStackListener list = getCommStackListener(id);
        if (list != null) {
            list.errorOccured(ex);
        }

    }

    public void send(String module, Packet element) {
        if (manager != null && manager.getModule(module) != null) {
            manager.getModule(module).send(element);
        }

    }

    private void sendPacketToCommstack(Packet packet) {
        Packet responsePacket = null;
        String commstackID = packet.getTo().replaceAll("\\@.*", "");
        boolean polling = packet.getPacketElement().getAttribute("polling", "false").equalsIgnoreCase("true");
        if (isCommStackExist(commstackID)) {
            CommStack commStack = getCommStack(commstackID);
            commStack.doAction(manager.getModule(packet.getPacketElement().getAttribute("module")), packet);
        } else {
            Logger.getLogger(CommStackManager.class.toString()).log(Level.SEVERE, "CommStack(" + commstackID + ") not found!");
        }
    }
    private SaavyHashMap<Integer, HybridPacket> mapHybridResponse;
    private SaavyHashMap<Integer, HybridPacket> mapHybridRequest;
    private SaavyHashMap<Integer, ThreadQueue> mapThreadQueue;

    public void addToHybridRequest(Packet packet) {
        if (mapHybridResponse != null) {
            String id = packet.getTo().replaceAll("\\@.*", "");
            int hash = Integer.parseInt(id.replaceAll("\\..*", ""));
            int index = Integer.parseInt(id.replaceAll(".*\\.", ""));
            if (mapHybridResponse.containsKey(hash)) {
                HybridPacket responsePacket = mapHybridResponse.get(hash);
                int count = responsePacket.getPacketElement().getIntAttribute("count", 0);
                
                if (index >= responsePacket.getPackets().size()) {
                    responsePacket.addPacket(packet);
                } else {
                    responsePacket.addPacket(index, packet);
                }

//                System.out.println("Hash:" + id + ":" + count + ":" + responsePacket.getPackets().size()+":"+mapHybridResponse.size());

                if (count == 0) {
                    mapHybridResponse.remove(hash);
                    mapHybridRequest.remove(hash);
                    mapThreadQueue.remove(hash).fireThreadEnded();
                } else if (count == responsePacket.getPackets().size()) {
                    send(responsePacket.getPacketElement().getAttribute("module"), responsePacket);
//                    System.out.println("Hybrid:("+hash+")"+responsePacket.getPackets().size()+":"+mapHybridRequest.get(hash).getPacketElement().getAttribute("from"));
                    mapHybridResponse.remove(hash);
                    mapHybridRequest.remove(hash);
                    mapThreadQueue.remove(hash).fireThreadEnded();
                } else {
                    sendPartOfHybridRequest(index + 1, mapHybridRequest.get(hash), responsePacket);
                }

            } 

        }
    }

    private void sendPartOfHybridRequest(int x, HybridPacket packet, HybridPacket responsePacket) {
//        System.out.println("Hybrid("+responsePacket.hashCode()+")Count->"+responsePacket.getPacketElement().getIntAttribute("count", 0)+":Size -> "+responsePacket.getPackets().size()+":"+mapHybridRequest.get(responsePacket.hashCode() ).getPacketElement().getAttribute("from"));
        Packet packt = packet.getPackets().get(x);
        packt.setFrom(responsePacket.hashCode() + "." + x + "@commstack.hybrid");
        packt.getPacketElement().setAttribute("module", packet.getPacketElement().getAttribute("module"));
        sendPacketToCommstack(packt);
    }

    public class HybridRequest extends Thread {

        private HybridPacket packet;
        private ThreadQueue tQueue;

        public HybridRequest(ThreadQueue tQueue, HybridPacket packet) {
            super("Hybrid Request:" + packet.getFrom());
            this.packet = packet;
            this.tQueue = tQueue;
        }

        @Override
        public void run() {
//            System.out.println("HBRIDPacket before:"+threadRun);
            SaavyElement temp = new SaavyElement();

            temp.setName(packet.getPacketElement().getName());

            HybridPacket responsePacket = new HybridPacket(temp);
            
            responsePacket.setTo(packet.getFrom());
            responsePacket.setFrom(packet.getTo());

            if (mapHybridResponse == null) {
                mapHybridResponse = new SaavyHashMap<Integer, HybridPacket>();
                mapHybridRequest = new SaavyHashMap<Integer, HybridPacket>();
                mapThreadQueue = new SaavyHashMap<Integer, ThreadQueue>();
            }

            if (packet.getPackets().size() > 0) {
                responsePacket.getPacketElement().setAttribute("count", packet.getPackets().size());
                responsePacket.getPacketElement().setAttribute("module", packet.getPacketElement().getAttribute("module"));

                mapHybridResponse.put(responsePacket.hashCode(), responsePacket);
                mapHybridRequest.put(responsePacket.hashCode(), packet);
                mapThreadQueue.put(responsePacket.hashCode(), tQueue);

                try {
                    sendPartOfHybridRequest(0, packet, responsePacket);
                } catch (Exception ex) {
                    Logger.getLogger(CommStackManager.class.toString()).log(Level.SEVERE, packet.getPacketElement().getXML(), ex);
                    tQueue.fireThreadEnded();
                }
            } else {
                tQueue.fireThreadEnded();
            }
//            System.out.println("HBRIDPacket after:"+threadRun);
        }
    }

    public class SingleRequest extends Thread {

        private Packet packet;
        private ThreadQueue tQueue;

        public SingleRequest(ThreadQueue tQueue, Packet packet) {
            super("Single Request:" + packet.getTo());
            this.packet = packet;
            this.tQueue = tQueue;
        }

        @Override
        public void run() {
//            System.out.println("SingleRequest before:"+threadRun);
            try {
//                responsePacket = 
                sendPacketToCommstack(packet);
            } catch (Exception ex) {
                Logger.getLogger(CommStackManager.class.toString()).log(Level.SEVERE, packet.getPacketElement().getXML(), ex);
            } finally {
                tQueue.fireThreadEnded();
            }
//            System.out.println("SingleRequest after:"+threadRun);
            
        }
    }
    private ArrayBlockingQueue<Packet> queue;

    public synchronized void sendRequest(Packet commStackRequest) {
        try {
            //System.out.println("Sending:"+commStackRequest.getName()+":"+commStackRequest.getAttribute("id")+":"+commStackRequest.getAttribute("commstack"));
            if (queue == null) {
                start();
            }
            queue.put(commStackRequest);
            if(commstackProccessor == null){
                commstackProccessor = new ThreadQueue(this);
                commstackProccessor.start();
            }
            synchronized (commstackProccessor) {
                if (commstackProccessor.waiting) {
                    commstackProccessor.notify();
                }
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(CommStackManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
