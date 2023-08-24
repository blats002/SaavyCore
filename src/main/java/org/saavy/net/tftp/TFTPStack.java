package org.saavy.net.tftp;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.commons.net.tftp.*;
import org.saavy.dom.SaavyElement;
import org.saavy.platform.Module;
import org.saavy.platform.net.CommStack;
import org.saavy.platform.net.Packet;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rgsaavedra
 */
public class TFTPStack extends CommStack<TFTPHandler> {

    public TFTPStack() {
        super(new TFTPHandler());
    }

    @Override
    public void start() {
        processor = new TFTPProcessor();
    }
    private TFTPProcessor processor;

    public class TFTPProcessor extends Thread {

        private File fileParent;
        private boolean running = false;
        private String address;
        private TFTP.TYPE type;
        private TFTPPacket tftpPacket;
        private Module module;

        public void start(File file, String address, TFTP.TYPE type) {
            this.fileParent = file;
            this.address = address;
            this.type = type;
            start();
        }
        private boolean close = false;

        public void kill() {
            close = true;
            getHandler().getClient().close();
        }

        @Override
        public void run() {
//            System.out.println("TFTP Processor started");
            running = true;
            TFTPClient client = getHandler().getClient();
            try {
                client.open(TFTPClient.DEFAULT_PORT);
                if (module != null) {
                    for (Packet packet : tftpPacket.getPacketsOk()) {
//                        System.out.println("Sending PacketsOk:" + packet.getPacketElement().getXML());
                        module.send(packet);
                    }
                }
                org.apache.commons.net.tftp.TFTPPacket recv = null;
                while (!close) {
                    client.setSoTimeout(1000 * 60 * 10);
                    recv = org.apache.commons.net.tftp.TFTPPacket.newTFTPPacket(client.receive().newDatagram());
//                    System.out.println("Address:" + recv.getAddress());
//                    System.out.println("PORT:" + recv.getPort());
//                    System.out.println("Clas:" + recv.getClass().toString());

                    if (recv instanceof TFTPWriteRequestPacket && type.equals(TFTP.TYPE.UPLOAD)) {
                        TFTPErrorPacket error = new TFTPErrorPacket(recv.newDatagram().getAddress(),
                                recv.newDatagram().getPort(),
                                TFTPErrorPacket.ILLEGAL_OPERATION,
                                "Illegal Operation!");
                        client.send(error);
                    } else if (recv instanceof TFTPReadRequestPacket && type.equals(TFTP.TYPE.DOWNLOAD)) {
                        TFTPErrorPacket error = new TFTPErrorPacket(recv.newDatagram().getAddress(),
                                recv.newDatagram().getPort(),
                                TFTPErrorPacket.ILLEGAL_OPERATION,
                                "Illegal Operation!");
                        client.send(error);
                    } else if (!getAddress().equalsIgnoreCase(recv.getAddress().getHostAddress())) {
                        TFTPErrorPacket error = new TFTPErrorPacket(recv.newDatagram().getAddress(),
                                recv.newDatagram().getPort(),
                                TFTPErrorPacket.ACCESS_VIOLATION,
                                "Access violation!");
                        client.send(error);
                    }
//                    else {
//                        continue;
//                    }

                    client.setSoTimeout(10000);
                    client.setMaxTimeouts(5);
                    if (recv instanceof TFTPReadRequestPacket) {
                        TFTPReadRequestPacket readPacket = (TFTPReadRequestPacket) recv;
//                    System.out.println("FileName:" + readPacket.getFilename());
//                    System.out.println("Mode:" + readPacket.getMode());

                        // We're sending a fileParent
                        FileInputStream input = null;
                        // Try to open local fileParent for reading
                        try {
//                            File fileParent = new File(readPacket.getFilename());
                            input = new FileInputStream(fileParent);
//                        int maxLength = TFTPDataPacket.MAX_DATA_LENGTH;
                            long blockData = 0;
                            long size = fileParent.length();


                            long blocks = (size / TFTPDataPacket.MAX_DATA_LENGTH);
                            long extra = size - (TFTPDataPacket.MAX_DATA_LENGTH * blocks);
                            TFTPDataPacket dataPacket = null;

                            while (blockData <= blocks) {
                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                                for (long x = 0; x < (blockData == blocks ? extra : TFTPDataPacket.MAX_DATA_LENGTH); x++) {
                                    bytes.write(input.read());
                                }

                                dataPacket = new TFTPDataPacket(recv.getAddress(), recv.getPort(), (int) blockData + 1, bytes.toByteArray(), 0, (int) (blockData == blocks ? extra : TFTPDataPacket.MAX_DATA_LENGTH));
                                client.send(dataPacket);

                                try {
                                    recv = org.apache.commons.net.tftp.TFTPPacket.newTFTPPacket(client.receive().newDatagram());
                                } catch (org.apache.commons.net.tftp.TFTPPacketException te) {
                                    break;
                                } 
                                
                                if (recv.getType() != org.apache.commons.net.tftp.TFTPPacket.ACKNOWLEDGEMENT) {
                                    break;
                                }
                                blockData++;
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            TFTPErrorPacket error = new TFTPErrorPacket(recv.newDatagram().getAddress(),
                                    recv.newDatagram().getPort(),
                                    TFTPErrorPacket.FILE_NOT_FOUND,
                                    "File not found!");
                            client.send(error);
                        } catch (IOException e) {
                            e.printStackTrace();
                            TFTPErrorPacket error = new TFTPErrorPacket(recv.newDatagram().getAddress(),
                                    recv.newDatagram().getPort(),
                                    TFTPErrorPacket.ILLEGAL_OPERATION,
                                    "Error Reading file");
                            client.send(error);
                        }
                    } else if (recv instanceof TFTPWriteRequestPacket) {
                        TFTPWriteRequestPacket writePacket = (TFTPWriteRequestPacket) recv;
//                    System.out.println("FileName:" + writePacket.getFilename());
//                    System.out.println("Type:" + writePacket.getType());
                        TFTPAckPacket ackPacket = new TFTPAckPacket(recv.getAddress(), recv.getPort(), 0);
                        client.send(ackPacket);
                        TFTPDataPacket dataIn = null;
                        File file = new File(getFile(), writePacket.getFilename());
//                    System.out.println("File:" + file.getAbsolutePath());
                        file.mkdirs();
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                        final FileOutputStream fileOut = new FileOutputStream(file);
                        final ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(100);
                        new Thread() {

                            @Override
                            public void run() {
                                while (true) {
                                    try {
                                        byte[] b = queue.take();
                                        if (b.length > 0) {
                                            fileOut.write(b);
                                        } else {
                                            fileOut.close();
                                            break;
                                        }
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(TFTPStack.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (IOException ex) {
                                        Logger.getLogger(TFTPStack.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }.start();
                        while (dataIn == null || dataIn.getDataLength() == TFTPDataPacket.MAX_DATA_LENGTH) {
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            recv = org.apache.commons.net.tftp.TFTPPacket.newTFTPPacket(client.receive().newDatagram());
                            if (recv instanceof TFTPDataPacket) {
                                dataIn = (TFTPDataPacket) recv;
//                                System.out.println("DataBlock:" + dataIn.getBlockNumber());
                                bytes.write(dataIn.getData(), dataIn.getDataOffset(), dataIn.getDataLength());
                                queue.add(bytes.toByteArray());
                                ackPacket = new TFTPAckPacket(dataIn.getAddress(), dataIn.getPort(), dataIn.getBlockNumber());
                                client.send(ackPacket);
                            } else {
                                TFTPErrorPacket error = new TFTPErrorPacket(recv.newDatagram().getAddress(),
                                        recv.newDatagram().getPort(),
                                        TFTPErrorPacket.UNDEFINED,
                                        "");
                                client.send(error);
                            }
                        }
                        queue.add(new byte[0]);
                    }
                }
            } catch (Exception ex) {
                if (!close) {
                    ex.printStackTrace();
                    if (tftpPacket != null && module != null) {
                        Packet response = Packet.createResponsePacket(tftpPacket.getPacketElement());
                        response.getPacketElement().setAttribute("error", true);
                        response.getPacketElement().setAttribute("exception", ex);
                        response.getPacketElement().addChildren(tftpPacket.getPacketElement());
                        module.processSendResponse(response, tftpPacket);
                    }
                }
            } finally {
                try {
                    if (client != null) {
                        client.close();
                    }
                } catch (Exception e) {
                }
                processor = new TFTPProcessor();
            }

            running = false;
//            System.out.println("TFTP Processor done");
        }

        public boolean isRunning() {
            return running;
        }

        public File getFile() {
            return fileParent;
        }

        public void setFile(File file) {
            this.fileParent = file;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public TFTP.TYPE getType() {
            return type;
        }

        public void setType(TFTP.TYPE type) {
            this.type = type;
        }

        public TFTPPacket getTftpPacket() {
            return tftpPacket;
        }

        public void setTftpPacket(TFTPPacket tftpPacket) {
            this.tftpPacket = tftpPacket;
        }

        public Module getModule() {
            return module;
        }

        public void setModule(Module module) {
            this.module = module;
        }
    }

    @Override
    public void stop() {
        if (processor.isRunning()) {
            processor.kill();
        }
    }

    @Override
    public boolean isConnected() {
        return processor.isRunning();
    }

    @Override
    public void doAction(Module module, Packet req) {
        if (req instanceof TFTPPacket) {
            TFTPPacket tftpPacket = (TFTPPacket) req;
            SaavyElement tftp = tftpPacket.getPacketElement().getChild("tftp");
            TFTP.TYPE type = (TFTP.TYPE) tftp.getObjectAttribute("type");
            if (type.equals(TFTP.TYPE.KILL)) {
                if (processor.isRunning()) {
                    processor.kill();
                }
            } else {
                if (!processor.isRunning()) {
                    processor.setTftpPacket(tftpPacket);
                    processor.setAddress(tftp.getAttribute("address"));
                    processor.setFile((File) tftp.getObjectAttribute("file"));
                    processor.setType((TFTP.TYPE) tftp.getObjectAttribute("type"));
                    processor.setModule(module);
                    synchronized (this) {
                        processor.start();
                    }
                } else {
                    Packet response = Packet.createResponsePacket(req.getPacketElement());
                    response.getPacketElement().setAttribute("error", true);
                    response.getPacketElement().setAttribute("exception", new Exception("TFTP Server is busy"));
                    response.getPacketElement().addChildren(req.getPacketElement());
                    if (module != null) {
                        module.processSendResponse(response, req);
                    }

                }
            }
        } else {
            Packet response = Packet.createResponsePacket(req.getPacketElement());
            response.getPacketElement().setAttribute("error", true);
            response.getPacketElement().setAttribute("exception", new Exception("Incompatible Packet"));
            response.getPacketElement().addChildren(req.getPacketElement());
            if (module != null) {
                module.processSendResponse(response, req);
            }
        }
    }

    public static void main(String a[]) {
        TFTPStack stack = new TFTPStack();
        stack.start();
        TFTPPacket packet = new TFTPPacket("test", "test");
        packet.addFile(new File("testtftp.txt"), "192.168.100.31", TFTP.TYPE.UPLOAD);
        stack.doAction(null, packet);
        while (stack.processor.isRunning()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TFTPStack.class.getName()).log(Level.SEVERE, null, ex);
            }
//            System.out.println("Still running");
        }
        stack.stop();
    }
}
