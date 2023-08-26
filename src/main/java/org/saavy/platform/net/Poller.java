/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.platform.net;

import org.saavy.dom.SaavyElement;
import org.saavy.platform.SaavyManager;
import org.saavy.platform.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rgsaavedra
 */
public class Poller implements Runnable {

    private long intervalInMillis = 30;
    private SaavyManager manager;
    private HashMap<String, Poll> polls;
    private ScheduledThreadPoolExecutor exec;
    private PollRun run;

    public Poller(SaavyManager manager) {
        exec = new ScheduledThreadPoolExecutor(1);
        this.manager = manager;
        polls = new HashMap<String, Poll>();
        run = new PollRun();
        exec.scheduleAtFixedRate(this, 0, intervalInMillis, TimeUnit.SECONDS);

    }

    public void registerPoll(String id, Poll poll) {
//        System.out.println("Poll("+id+")"+poll);
        poll.setID(id);
        polls.put(id, poll);
    }

    public void removePolls(String cannonicalID) {
        ArrayList<String> ids = new ArrayList<String>();
        for (String id : polls.keySet()) {
            if (id.startsWith(cannonicalID)) {

                ids.add(id);
            }
        }
        for (String id : ids) {
//            System.out.println("RemovingPolls:"+id);
            polls.remove(id);
        }
    }

    public void requestPoll(String string) {
        Poll poll = getPoll(string);
        if (poll != null) {
            invokePollNow(poll);
        }
    }
    private boolean forcePollersOnly = false;

    public void setForcePollersOnly(boolean b) {
        forcePollersOnly = b;
    }

    public void unRegisterPoll(String id) {
        polls.remove(id);
    }

    public Poll getPoll(String id) {

        return polls.get(id);
    }

    public long getIntervalInMillis() {
        return intervalInMillis * 1000;
    }

    public void setIntervalInMillis(long intervalInMillis) {
        this.intervalInMillis = intervalInMillis;
    }

    public void close() {
        polls.clear();
        run.stopPollRun();
        exec.shutdownNow();
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public boolean isForcePollersOnly() {
        return forcePollersOnly;
    }

    private class PollRun extends Thread {

        public PollRun() {
            super("PollRun Thread");
            start();
        }
        private ArrayBlockingQueue<Poll> queue = new ArrayBlockingQueue<Poll>(100);

        public synchronized void addPoll(Poll poll) {
            try {
                queue.put(poll);
            } catch (InterruptedException ex) {
                Logger.getLogger(Poller.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (waiting) {
                synchronized (this) {
                    notify();
                }
            }
        }

        public void stopPollRun() {
            running = false;
            queue.clear();
            synchronized (this) {
                notifyAll();
            }
            queue = null;
        }

        @Override
        public synchronized void start() {
            running = true;
            super.start();
        }
        private boolean running = false;
        private boolean waiting = false;

        @Override
        public void run() {
//            System.out.println("Starting Poll");
            try {
                while (running) {
                    Poll poll = null;
                    try {
                        synchronized (this) {
                            if (queue.isEmpty()) {
                                waiting = true;
                                wait(1000 * 60 * 60);
                            } else {
                                wait(500);
                            }
                        }
                        waiting = false;
                        super.setPriority(Thread.MIN_PRIORITY);
                        if (queue != null) {
                            poll = queue.poll();
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Poller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (poll != null) {
                        super.setPriority(Thread.NORM_PRIORITY - 1);
                        for (SaavyElement element : poll.getPollElement().getChild("request").getChildren()) {
                            element.setAttribute("force", poll.isForcePoller());

                            if (!element.getAttribute("commstack", "false").equalsIgnoreCase("true")) {
                                poll.setPolling(true);
                                element.setAttribute("polling", "true");
                            }
                            element.setAttribute("from", poll.getID() + "@poll");
                            Packet pollElement = new Packet(element);
                            //System.out.println("Sending poll:"+element.getXML());
                            Module module = manager.getModule(poll.getModuleID());
                            if (module != null) {
                                module.send(pollElement);
                            }
                        }
                        HybridPacket packet = null;
                        for (SaavyElement hybrid : poll.getPollElement().getChild("hybridrequest").getChildren()) {
                            if (packet == null) {
                                packet = new HybridPacket(SaavyElement.createXML("<hybrid from='" + poll.getID() + "@poll' to='commstack'/>"));
                            }
                            packet.addPacket(Packet.createPacket(hybrid));
                        }

                        if (packet != null) {
                            Module module = manager.getModule(poll.getModuleID());
                            if (module != null) {
                                module.send(packet);
                            }
                        }

                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
            finally {
                running = false;
            }
//            System.out.println("Stopping Poll");
        }
    }

//    private void processPoll(Poll poll) {
//        for (SaavyElement element : poll.getPollElement().getChild("request").getChildren()) {
////            SaavyElement element = poll.getPollElement();
//
//            element.setAttribute("force", poll.isForcePoller());
//
//            if (!element.getAttribute("commstack", "false").equalsIgnoreCase("true")) {
//                poll.setPolling(true);
//                element.setAttribute("polling", "true");
//            }
//
//            //element.setAttribute("registered", poll.getRegistered());
//
//            element.setAttribute("from", poll.getID() + "@poll");
//            Packet pollElement = new Packet(element);
//            //System.out.println("Sending poll:"+element.getXML());
//            Module module = manager.getModule(poll.getModuleID());
//            if (module != null) {
//                module.send(pollElement);
//            }
//        }
//        HybridPacket packet = null;
//        for (SaavyElement hybrid : poll.getPollElement().getChild("hybridrequest").getChildren()) {
//            if (packet == null) {
//                packet = new HybridPacket(SaavyElement.createXML("<hybrid from='" + poll.getID() + "@poll' to='commstack'/>"));
//            }
//            packet.addPacket(Packet.createPacket(hybrid));
////                HybridPacket packet = new HybridPacket(SaavyElement.createXML("<login to='commstack'/>"));
////        if (snmp) packet.addPacket(Packet.createPacket(SaavyElement.createXML("<login to=\"snmp@commstack\"><pdu oid='.1.3.6.1.2.1.1.2.0' action='get'/></login>")));
////        if (cli) packet.addPacket(Packet.createPacket(SaavyElement.createXML("<login to=\"cli@commstack\"/>")));
////        send(packet);
//        }
//
//        if (packet != null) {
//            Module module = manager.getModule(poll.getModuleID());
//            if (module != null) {
//                module.send(packet);
//            }
//        }
//    }
    public void invokePollNow(SaavyElement pollE) {
        Poll poll = new Poll();
        poll.setModuleID(pollE.getAttribute("module"));
        poll.setPollElement(pollE);
        invokePollNow(poll);
    }

    public void invokePollNow(Poll poll) {
        if (!poll.isPolling()) {
//            exec.schedule(new PollRun(poll), 100, TimeUnit.MILLISECONDS);
            poll.setPolling(true);
            run.addPoll(poll);
        }
    }

    public boolean continuePoll(Poll poll) {
        ArrayList<String> comps = poll.continuePoll();
        if (comps.size() > 0) {
            Module module = manager.getModule(poll.getModuleID());
            for (String id : comps) {
                if (module.getSaavyComponent(id) != null && module.getSaavyComponent(id).continuePoll(poll)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public void run() {
//        long currentTime = System.currentTimeMillis();
//        System.out.println("Time:Started:");
        int count = 0;
        for (Poll poll : polls.values()) {
            //SaavyElement element = poll.getPollElement();
            long deltaTime = System.currentTimeMillis() - poll.getLastTimeSend();
            if (!pause && !poll.isPause() && !(forcePollersOnly && !poll.isForcePoller())) {
                if (deltaTime >= ((getIntervalInMillis()*80)/100)) {
                    if (continuePoll(poll)) {
                        invokePollNow(poll);
                        count++;
                    }
                }
            }
        }
//        if (count > 0) {
//            System.out.println("Time:Ended:" + (System.currentTimeMillis() - currentTime) + ":processed:" + count);
//        }
    }

    public void packetReceived(Packet packet) {
        String pollID = packet.getTo().replaceAll("\\@poll", "");
        Poll poll = getPoll(pollID);
        if (poll != null) {
//            Packet pollE = new Packet(packet.getPacketElement());
            Packet pollE = packet;
            Module module = manager.getModule(poll.getModuleID());
            for (String id : poll.getRegistered()) {
                pollE.setTo(id);
                if (module != null) {
//                    System.out.println("POLLStart:"+pollID);
                    module.send(pollE);
                }
            }
            pollE = null;
            poll.setPolling(false);
//            System.out.println("POLLFIN:"+pollID);
            poll.updateLastSend();
        }
    }
    private boolean pause = true;
}
