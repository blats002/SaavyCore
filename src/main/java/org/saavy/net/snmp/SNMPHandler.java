/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.net.snmp;

import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.MibParseException;
import org.saavy.platform.net.*;
import org.saavy.util.ResourceUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rgsaavedra
 */
public class SNMPHandler extends CommStackHandler<SNMPDetails> {

//    private SnmpSession snmpSession;
    private boolean connectedOnce = false;

    public SNMPHandler() {
        setLoginDetails(new SNMPDetails());
        MibUtil.unloadAllMibs();
    }

    @Override
    public void setLoginDetails(SNMPDetails loginDetails) {
        super.setLoginDetails(loginDetails);
    }

    public void loadMib(String path) {
        try {
            MibUtil.loadMib(new InputStreamReader(ResourceUtil.getResourceAsStream(path)), true);
        } catch (IOException ex) {
            Logger.getLogger(SNMPHandler.class.getName()).log(Level.SEVERE, "Error Loading:" + path, ex);
        } catch (MibParseException ex) {
            Logger.getLogger(SNMPHandler.class.getName()).log(Level.SEVERE, "Error Loading:" + path, ex);
        }
    }

    protected SnmpSession createSnmpSession() throws IOException, MibParseException {
        //OLD
        if (getLoginDetails().isModifiedOrNew()) {
            MibUtil.unloadAllMibs();
//            System.out.println("LOADING MIBS");
            if (!MibUtil.isMibFileLoaded()) {
                List<String> mibs = (List<String>) getLoginDetails().getProperty(SNMPEnum.MIB_FILES.toString());
                if (mibs != null) {
                    for (String path : mibs) {
                        loadMib(path);
                    }
                }
            }
            this.getLoginDetails().backupData();
        }
        //OLD
        //NEW
//        if (!MibUtil.isMibFileLoaded()) {
//            List<String> mibs = (List<String>) getLoginDetails().getProperty(SNMPEnum.MIB_FILES.toString());
//            if (mibs != null) {
//                for (String path : mibs) {
//                    loadMib(path);
//                }
//            }
//        }
        //NEW

        SnmpSession snmpSession = null;

        String host = (String) getLoginDetails().getProperty(SNMPEnum.HOST.toString());
        if (host == null) {
            throw new InvalidParameterException("Host cannot be null or empty");
        }
        Integer port = (Integer) getLoginDetails().getProperty(SNMPEnum.PORT.toString());
        if (port == null) {
            port = Integer.valueOf(SnmpConst.DEFAULT_SNMP_AGENT_PORT);
        }

        SnmpTarget snmpTarget = new SnmpTarget(host, port);
        int ver = SnmpConst.SNMPV1;

        SNMPEnum.Version version = (SNMPEnum.Version) getLoginDetails().getProperty(SNMPEnum.VERSION.toString());

        if (version != null) {
            if (version.equals(SNMPEnum.Version.V2C)) {
                ver = SnmpConst.SNMPV2;
            } else if (version.equals(SNMPEnum.Version.V3)) {
                ver = SnmpConst.SNMPV3;
            }
        }

        snmpTarget.setVersion(ver);
        String readCommunity = (String) getLoginDetails().getProperty(SNMPEnum.GET_COMMUNITY.toString());
        if (readCommunity == null) {
            readCommunity = "public";
        }
        String writeCommunity = (String) getLoginDetails().getProperty(SNMPEnum.SET_COMMUNITY.toString());
        if (writeCommunity == null) {
            writeCommunity = "private";
        }
        snmpTarget.setReadCommunity(readCommunity);
        snmpTarget.setWriteCommunity(writeCommunity);

//        if (snmpSession != null) {
//            snmpSession.close();
//            snmpSession = null;
//        }

//            MibUtil.unloadAllMibs();

        snmpSession = new SnmpSession(snmpTarget);

        snmpSession.setTimeout((int) TimeUnit.SECONDS.toMillis(getLoginDetails().getTimeout()));
        snmpSession.setRetries(getLoginDetails().getRetries());


        Integer timeout = (Integer) getLoginDetails().getProperty(SNMPEnum.TIMEOUT.toString());

        if (timeout != null) {
            snmpSession.setTimeout((int) TimeUnit.SECONDS.toMillis(timeout.intValue()));
        }

        Integer retries = (Integer) getLoginDetails().getProperty(SNMPEnum.RETRIES.toString());

        if (retries != null) {
            snmpSession.setRetries(retries.intValue());
        }

        if (snmpSession.getVersion() == SnmpConst.SNMPV3) {
            SNMPEnum.PrivacyProtocol priv = (SNMPEnum.PrivacyProtocol) getLoginDetails().getProperty(SNMPEnum.PRIVACY_PROTOCOL.toString());
            SNMPEnum.AuthenticationProtocol auth = (SNMPEnum.AuthenticationProtocol) getLoginDetails().getProperty(SNMPEnum.AUTHENTICATION_PROTOCOL.toString());

            String v3UserName = (String) getLoginDetails().getProperty(SNMPEnum.V3_USERNAME.toString());
            if (v3UserName == null) {
                v3UserName = "";
            }
            String authPass = (String) getLoginDetails().getProperty(SNMPEnum.AUTHENTICATION_PASSWORD.toString());

            String privPass = (String) getLoginDetails().getProperty(SNMPEnum.PRIVACY_PASSWORD.toString());


            int snmpPriv = SnmpConst.DES;
            if (priv != null && priv.equals(SNMPEnum.PrivacyProtocol.AES)) {
                snmpPriv = SnmpConst.AES;
            }

            String snmpAuth = SnmpConst.SHA;
            if (auth != null && auth.equals(SNMPEnum.AuthenticationProtocol.MD5)) {
                snmpAuth = SnmpConst.MD5;
            }
            snmpSession.setV3Params(v3UserName, snmpAuth, authPass, snmpPriv, privPass);
        }


//        }
        return snmpSession;
    }
    private final String INVALID_LOGIN = ".1.3.6.1.6.3.15.1.1";

    public void checkSNMPv3(SnmpPdu var) throws CommStackException {
        if (var == null) {
            throw new ConnectionLostException("Error Obtaining Data: Device returned null!");
        }
//		  System.out.println("CheckSNMPv3"+var+":"+var.getType()+":"+var.getTypeString());
//		  System.out.println("Pdu:"+var.getErrorString());
        if (var.getFirstVarBind().getName().toString().startsWith(INVALID_LOGIN)||(var.getErrorString().equals("Authorization Error"))) {
            throw new LoginFailedException("Invalid V3 Parameters!");
        }
    }

    public SnmpTableModel getTable(SnmpSession session, String oidString) throws CommStackException {
        SnmpOID oid = MibUtil.lookupOID(oidString);
        SnmpTableModel model = null;
        try {
            model = session.snmpGetTable(oid.toString());
        } catch (IOException ex) {
            throw new CommStackException("Error Obtaining Data: Error communicating to Device", ex);
        } catch (Exception ex) {
            //Logger.getLogger(SNMPHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new CommStackException("Error Obtaining Data", ex);
        }
        return model;
    }

    public SnmpVarBind[] createSNMPVarBinds(String[] oidStrings, String[] types, Object[] values) {
        SnmpVarBind[] binds = new SnmpVarBind[oidStrings.length];
        for (int x = 0; x < oidStrings.length; x++) {
            SnmpOID oid = MibUtil.lookupOID(oidStrings[x]);
            Object value = values[x];
            String type = types[x];
            SnmpDataType dataType = null;
            if (value == null) {
                dataType = new SnmpNull();
            } else if (value instanceof InetAddress) {
                dataType = new SnmpIpAddress((InetAddress) value);
            } else if (value instanceof String) {
                if (type.equalsIgnoreCase("ipaddress")) {
                    dataType = new SnmpIpAddress((String) value);
                } else if (type.equalsIgnoreCase("oid")) {
                    dataType = new SnmpOID((String) value);
                } else if (type.equalsIgnoreCase("hex")) {
                    dataType = new SnmpOctetString(SnmpOctetString.convertPhysAddress((String) value));
                } else {
                    dataType = new SnmpOctetString((String) value);
                }
            } else if (value instanceof Integer) {
                dataType = new SnmpInt((Integer) value);
            } else if (value instanceof Long) {
                dataType = new SnmpUInt((Long) value);
            } else if (value instanceof Character){
                dataType = new SnmpOctetString(String.valueOf(value));
            }
            binds[x] = new SnmpVarBind(oid, dataType);
        }
        return binds;
    }

    public SnmpPdu multipleSet(SnmpSession session, String[] oidStrings, String[] types, Object[] values) throws CommStackException {
        SnmpVarBind[] binds = createSNMPVarBinds(oidStrings, types,values);
        SnmpPdu pdu = new SnmpPdu(SnmpConst.SET, binds);

        SnmpPdu pduResponsd = null;
        try {
            pduResponsd = session.snmpSetRequest(pdu);
        } catch (IOException ex) {
            throw new ConnectionLostException("Error Setting Data: Error communicating to Device", ex);
        } catch (Exception ex) {
            Logger.getLogger(SNMPHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new CommStackException("Error Obtaining Data", ex);
        }
        return pduResponsd;
    }

    public SnmpPdu set(SnmpSession session, String oidString, String type, Object value) throws CommStackException {
//        SnmpOID oid = MibUtil.lookupOID(oidString);
//        SnmpDataType dataType = null;
//        if (value == null) {
//            dataType = new SnmpNull();
//        } else if (value instanceof InetAddress) {
//            dataType = new SnmpIpAddress((InetAddress) value);
//        } else if (value instanceof String) {
//            if (type.equalsIgnoreCase("ipaddress")) {
//                dataType = new SnmpIpAddress((String) value);
//            } else if (type.equalsIgnoreCase("oid")) {
//                dataType = new SnmpOID((String) value);
//            } else if (type.equalsIgnoreCase("hex")) {
//                dataType = new SnmpOctetString(SnmpOctetString.convertPhysAddress((String) value));
//            } else {
//                dataType = new SnmpOctetString((String) value);
//            }
//        } else if (value instanceof Integer) {
//            dataType = new SnmpInt((Integer) value);
//        } else if (value instanceof Long) {
//            dataType = new SnmpUInt((Long) value);
//        }
        SnmpVarBind[] binds = createSNMPVarBinds(new String[]{oidString}, new String[]{type},new Object[]{value});
        SnmpPdu pdu = new SnmpPdu(SnmpConst.SET, binds);

        SnmpPdu pduResponsd = null;
        try {
            pduResponsd = session.snmpSetRequest(pdu);
        } catch (IOException ex) {
            throw new ConnectionLostException("Error Setting Data: Error communicating to Device", ex);
        } catch (Exception ex) {
            Logger.getLogger(SNMPHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new CommStackException("Error Obtaining Data", ex);
        }
        return pduResponsd;
    }

    public SnmpVarBind[] getWorkAroundTable(SnmpSession session, String oidString) throws CommStackException {
        SnmpOID oid = MibUtil.lookupOID(oidString);
        oidString = oid.toString();
//        SnmpPdu pdu = new SnmpPdu;
        SnmpVarBind[] vars;
        int x = 1;
        try {
            vars = session.snmpGetSubtree(oid);

//            do {
//                if (pdu == null) {
//                    pdu = session.snmpGetNextRequest(oid/*+ "." + x*/);
//                } else {
//                    SnmpPdu pdu2 = session.snmpGetNextRequest(pdu.getLastVarBind().getName());
//                    
//                    if (pdu2.getFirstVarBind().getName().compareTo(pdu.getLastVarBind().getName()) <= 0) {
//                        throw new CommStackException("Error retrieving Data from Device!");
//                    }
//
//                    pdu.addVarBinds(pdu2.getVarBinds());
//                }
//            } while (pdu.getLastVarBind().getName().startsWith(oidString));
//            pdu.removeVarBind(pdu.getVarBindCount() - 1);
            if (!connectedOnce) {
                connectedOnce = true;
            }
        } catch (IOException ex) {
            if (connectedOnce) {
                connectedOnce = false;
                throw new ConnectionLostException("Error Obtaining Data: Error communicating to Device", ex);
            } else {
                throw new ConnectionFailedException("Connection Failed", ex);
            }
        } catch (Exception ex) {
            //Logger.getLogger(SNMPHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new CommStackException("Error Obtaining Data", ex);
        }

        if (vars != null) {
            if (vars.length > 0) {
//                checkSNMPv3(vars[0]);
            }
        } else {
            throw new CommStackException("Error Obtaining Data: No Data Found");
        }
        return vars;
    }

    public SnmpPdu get(SnmpSession session, String oidString) throws CommStackException {
        SnmpOID oid = MibUtil.lookupOID(oidString);

        SnmpPdu pdu = null;

        try {
            pdu = session.snmpGetRequest(oid);

            if (!connectedOnce) {
                connectedOnce = true;
            }
        } catch (IOException ex) {
            if (connectedOnce) {
                throw new ConnectionLostException("Error Obtaining Data: Error communicating to Device", ex);
            } else {
                throw new ConnectionFailedException("Connection Failed", ex);
            }
        } catch (Exception ex) {
            //Logger.getLogger(SNMPHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new CommStackException("Error Obtaining Data", ex);
        }

        if (pdu != null) {
            checkSNMPv3(pdu);
        } else {
            throw new CommStackException("Error Obtaining Data: No Data Found");
        }
        return pdu;
    }

    public SnmpPdu getNext(SnmpSession session, String oidString) throws CommStackException {
        SnmpOID oid = MibUtil.lookupOID(oidString);

        SnmpPdu pdu = null;

        try {
            pdu = session.snmpGetNextRequest(oid);
				
            if (!connectedOnce) {
                connectedOnce = true;
            }
        } catch (IOException ex) {
            if (connectedOnce) {
                throw new ConnectionLostException("Error Obtaining Data: Error communicating to Device", ex);
            } else {
                throw new ConnectionFailedException("Connection Failed", ex);
            }
        } catch (Exception ex) {
            //Logger.getLogger(SNMPHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new CommStackException("Error Obtaining Data", ex);
        }

        if (pdu != null) {
            checkSNMPv3(pdu);
        } else {
            throw new CommStackException("Error Obtaining Data: No Data Found");
        }
        return pdu;
    }

    @Override
    public boolean isConnected() {
        return connectedOnce;
    }

    @Override
    public void close() {
        connectedOnce = false;
//        if (snmpSession != null) {
//            snmpSession.close();
//        }
    }
}
