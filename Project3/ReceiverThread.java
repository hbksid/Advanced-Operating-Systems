//package Koo_Toueg_Protocol;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiverThread implements Runnable {

    ServerSocket tcpServerChannel;
    HashMap<Integer, String> nodeConfiguration;
    VectorClock vectorClock;
    List<Integer> neighbours;

    int counter;
    int rollback_reply_counter;
    public ReceiverThread(ServerSocket tcpServerChannel,
                HashMap<Integer, String> nodeConfiguration,
                VectorClock lamportClock, List<Integer> neighbours) {
        this.vectorClock = lamportClock;
        this.nodeConfiguration = nodeConfiguration;
        this.tcpServerChannel = tcpServerChannel;
        this.neighbours = neighbours;
    }

    @Override
    public void run() {
        List<SctpChannel> sctpChannels = new ArrayList<>();

        boolean listener = true;
        //Loop to accept connection
        while (listener) {
            System.out.println("Waiting for incoming connection.");
            Socket tcpChannel = null;
            try {
                tcpChannel = tcpServerChannel.accept();
                //sctpChannel.configureBlocking(true);
                //Iterator itr = sctpChannel.getRemoteAddresses().iterator();
                //while (itr.hasNext()) {
                    // Handle the message
                    System.out.println("\n\nNode P" + Node.node_id + " received a message.");
                    //InetSocketAddress socketAddress = (InetSocketAddress)itr.next();
                    //System.out.println("Connected by "+Node.nodeId+" connection from : "+socketAddress.getHostName());

                    // Commenting below since we don't need to add channel to 
                    // conn_Map since it is defined in connectAll() in Main and 
                    // we are guaranteed that we will not receive a message from a 
                    // process that is not our neighbor
    //                String connectedHostName = socketAddress.getHostName();
    //                
    //                for (Entry entry: nodeConfiguration.entrySet()) {
    //                    if(entry.getValue().contains(connectedHostName)){
    //                        Node.conn_Map.put((Integer)entry.getKey(),sctpChannel);
    //                    }
    //                }

                    
                    
                    // Handle the message
                    // Get the message type
                    Messages msg = receiveMessage(tcpChannel);
                    System.out.println("Ready to process message.");
                    if (msg != null) {
                        // Switch statement based on the message type
                        Types_of_Messages msgType = msg.getMessageType();
                        int sender = msg.getSenderId();
                        int initiator = msg.getInstanceInitiatorId();
                        int LLR;
                        System.out.println("Received a " + msgType + 
                                " message from P" + sender + ".");
                            switch (msgType) {
                                case TAKE:
                                    // I know I am not the initiator so I get LLR 
                                    // from sender and determine if I also need to 
                                    // take a checkpoint
                                    LLR = msg.getLlrValue();
                                    int FLS = Node.local_FLS[sender];
                                    System.out.println("LLR=" + LLR + ", FLS=" + 
                                            FLS + ".");
                                    if (LLR >= FLS && FLS > -1 && !Node.already_taken) {
                                        // For TAKE messages where I WILL take checkpoint, 
                                        // I need to store who the sender is as my parent
                                        Node.parent_id = sender;
                                        
                                        System.out.println("Will take checkpoint.");
                                        // I need to take a tentative checkpoint
                                        // I need to stop all application messages
                                        // then record data for tentative 
                                        // checkpoint.
                                        // I check if I have cohort set and if so, 
                                        // I broadcast to them to also take 
                                        // checkpoint if necessary. If not, then 
                                        // go ahead and send REPLY message 
                                        // indicating checkpoint done.
                                        // Since I don't send reply back to 
                                        // the initiator/sender requesting 
                                        // checkpoint, I need to store something to 
                                        // remember that I am waiting on my cohort 
                                        // set to reply to my broadcast before 
                                        // sending my reply.

                                        // Updating node flags using already_taken 
                                        // and ready_to_take to prevent application 
                                        // messages. -1 passed for initiator to 
                                        // basically not set the initiator since I 
                                        // a process receiving a TAKE message will 
                                        // never be the initiator.
                                        System.out.println("Updating Node flags to "
                                                + "prevent from sending "
                                                + "application messages.");
                                        Node.updateFlags(-1, true, false);

                                        // Calling Node.take_A_Checkpoint but need 
                                        // to work with Siddharth since this should 
                                        // really be a tentative checkpoint
                                        System.out.println("Taking a ?!permanent?! "
                                                + "checkpoint. Initiator=P" + 
                                                initiator + ".");
                                        Node.take_A_Checkpoint(initiator);

                                        // Check if I have cohort set
                                        List<Integer> cohortSet = Node.getCohortMembers();
                                        int cohortSetSize = cohortSet.size();
                                        if (cohortSet.contains(sender)) {
                                           cohortSetSize--;
                                        }
                                        if (cohortSet.contains(initiator) && sender != initiator) {
                                           cohortSetSize--;
                                        }
                                        System.out.println("Cohort set size minus 1 "
                                                + "(for sender): " + cohortSetSize + ".");
                                        if (cohortSetSize > 0) {
                                            // Send TAKE message to all my cohorts 
                                            // EXCEPT to the sending process if it 
                                            // is also my cohort.
                                            System.out.println("Sending TAKE "
                                                    + "to all cohorts EXCEPT "
                                                    + "to the sending process "
                                                    + "if it is also my "
                                                    + "cohort.");
                                            for (int cohort: cohortSet) {
                                                if (cohort != sender && cohort != initiator) {
                                                    Node.cohort_list.add(cohort);
                                                    System.out.println(
                                                            "Sending TAKE to P" 
                                                            + cohort + ".");
                                                    Messages message = 
                                                            new Messages(
                                                            Types_of_Messages.
                                                            TAKE,
                                                            Node.node_id, 
                                                            cohort, 
                                                            initiator, 
                                                            "whoppiiee", 
                                                            Node.node_id);
                                                    message.setLlrValue(
                                                            Node.
                                                            local_LLR[cohort]);
                                                    Node.control_message.
                                                            add(message);
                                                }
                                                else {
                                                    System.out.println("Not "
                                                            + "sending to P" + 
                                                            cohort + " since "
                                                            + "it is the "
                                                            + "sender (" + 
                                                            sender + ") or "
                                                            + "initiator (" + 
                                                            initiator + ").");
                                                }
                                            }
                                        }
                                        else {
                                            System.out.println("No other "
                                                    + "checkpoints needed, "
                                                    + "sending reply to P" + sender 
                                                    + ".");
                                            Messages message = new Messages(
                                                    Types_of_Messages.REPLY, 
                                                    Node.node_id, sender, 
                                                    initiator, "whoppiiee", 
                                                    sender);
                                            Node.control_message.add(message);
                                        }
                                    }
                                    else {
                                        System.out.println("Will NOT take "
                                                + "checkpoint, sending reply to P" 
                                                + sender + ".");
                                        Messages message = new Messages(
                                                Types_of_Messages.REPLY, 
                                                Node.node_id, sender, initiator, 
                                                "whoppiiee", sender);
                                        Node.control_message.add(message);
                                    }
                                    break;
                                case MAKE:
                                    // MAKE messages are checkpoint protocol phase 
                                    // 2 messages where the initiator is indicating 
                                    // to all neighbors to make all tentative 
                                    // checkpoints permanent. Will need to add a 
                                    // makePermanent method to Node to do this.
                                    // Since the parent is flooding all neighbors, 
                                    // not just cohorts, we need to check if we 
                                    // have any tentative checkpoints. If not, then 
                                    // we do nothing. Part of the makePermanent 
                                    // method should include resetting arrays but don't reset 
                                    // flags until after broadcasting other messages.
                                    // Then I broadcast MAKE messages to all my 
                                    // neighbors knowing they may receive 
                                    // duplicates (this is to ensure FIFO 
                                    // guarantees all processes receive MAKE before 
                                    // an app message
                                    if (Node.isLastCheckpointTentative()) {
                                        // Make my checkpoint permanent then
                                        // broadcast MAKE to all my neighbors and I 
                                        // will receive reply for these.
                                        System.out.println("Making my checkpoint "
                                                + "permanent.");
                                        Node.makePermanent();
                                        
                                        // Set parent_id to sender
                                        Node.parent_id = sender;
                                        
                                        List<Integer> neighbors = Node.neighbours;
                                        System.out.println("Last checkpoint is "
                                                + "tentative, so broadcasting MAKE"
                                                + " message to all neighbors.");
                                        // Subtract 1 from neighbors since I will not send 
                                        // MAKE back to sender
                                        if (neighbors.size() - 1 > 0) {
                                            for (int neighbor: neighbors) {
                                                if (neighbor != sender) {
                                                    Node.temp_neighbor_list.add(neighbor);
                                                    System.out.println("Sending "
                                                            + "MAKE to P" + 
                                                            neighbor + ".");
                                                    Messages message = 
                                                            new Messages(
                                                            Types_of_Messages.MAKE, 
                                                            Node.node_id, neighbor, 
                                                            initiator, "whoppiiee", 
                                                            sender);
                                                    Node.control_message.
                                                            add(message);
                                                }
                                            }
                                        }
                                        else {
                                            System.out.println("I don't have any "
                                                    + "neighbors, so I send "
                                                    + "MAKE_REPLY right away");
                                            Messages message = new Messages(
                                                Types_of_Messages.MAKE_REPLY, 
                                                Node.node_id, sender, 
                                                initiator, "whoppiiee", 
                                                sender);
                                            Node.control_message.add(message);
                                        }
                                        
                                        // Now allow application messages
                                        Node.updateFlags(initiator, false, true);
                                    }
                                    else {
                                        System.out.println("Last checkpoint is not"
                                                + " tentative but still need to "
                                                + "send MAKE_REPLY.");
                                        Messages message = new Messages(
                                                Types_of_Messages.MAKE_REPLY, 
                                                Node.node_id, sender, 
                                                initiator, "whoppiiee", 
                                                sender);
                                        Node.control_message.add(message);
                                    }
                                    break;
                                case MAKE_REPLY:
                                    // Need to remove neighbor from 
                                    // temp_neighbor_list to check if I have 
                                    // received all replies
                                    System.out.println("Remove P" + sender + 
                                            " from temp_neighbor_list.");
                                    List<Integer> tempNeighbors = 
                                            Node.temp_neighbor_list;
                                    tempNeighbors.remove(Node.temp_neighbor_list.size() - 1);
                                    if (tempNeighbors.isEmpty()) {
                                        System.out.println("I have received all "
                                                + "MAKE_REPLIES. If I am "
                                                + "initiator, then checkpoint "
                                                + "instance is complete, so I "
                                                + "call checkpointComplete "
                                                + "method. If I am not the "
                                                + "initiator, then send "
                                                + "MAKE_REPLY to my parent.");
                                        if (Node.node_id == initiator) {
                                            System.out.println("I am initiator so "
                                                    + "ending checkpoint "
                                                    + "instance");
                                            Node.checkpointComplete();
                                        }
                                        else {
                                            System.out.println("I am not the "
                                                    + "initiator so send "
                                                    + "MAKE_REPLY to parent.");
                                            Messages message = new Messages(
                                                    Types_of_Messages.MAKE_REPLY, 
                                                    Node.node_id, Node.parent_id, 
                                                    initiator, "whoppiiee", 
                                                    Node.parent_id);
                                            Node.control_message.add(message);
                                        }
                                    }
                                    else {
                                        System.out.println("Temp_neighbot_list "
                                                + "not empty so still waiting on "
                                                + "other MAKE_REPLY messages for "
                                                + "last TAKE");
                                    }
                                    break;
                                case REPLY:
                                    // Need to remove cohort from cohort_list to 
                                    // check if I have received all replies
                                    System.out.println("Remove P" + sender + 
                                            " from cohort_list.");
                                    List<Integer> cohortList = Node.cohort_list;
                                    cohortList.remove(Node.cohort_list.size() - 1);
                                    if (cohortList.isEmpty()) {
                                        System.out.println("Cohort list empty so I "
                                                + "have received all replies to "
                                                + "TAKE message, now if I am "
                                                + "initiator, I start phase 2 "
                                                + "where I reset my arrays, allow "
                                                + "application messages for this "
                                                + "process by calling "
                                                + "updateJustFlags, and send MAKE "
                                                + "message to ALL neighbors, not "
                                                + "just cohorts. If I am not "
                                                + "initiator, then send REPLY to "
                                                + "my parent.");
                                        if (Node.node_id == initiator) {
                                            System.out.println("I am initiator so "
                                                    + "sending MAKE to all "
                                                    + "neighbors and make my "
                                                    + "checkpoint permanent.");
                                            List<Integer> neighbors = 
                                                    Node.neighbours;
                                            for (int neighbor: neighbors) {
                                                Node.temp_neighbor_list.add(neighbor);
                                                Messages message = new Messages(
                                                        Types_of_Messages.MAKE, 
                                                        Node.node_id, neighbor, 
                                                        initiator, "whoppiiee", 
                                                        -2); // No parent
                                                Node.control_message.add(message);
                                            }
                                            System.out.println("Making my "
                                                    + "checkpoint permanent.");
                                            Node.makePermanent();
                                            Node.updateFlags(initiator, false, true);
                                        }
                                        else {
                                            System.out.println("I am not initiator "
                                                    + "so then send reply to "
                                                    + "parent");
                                            Messages message = new Messages(
                                                    Types_of_Messages.REPLY, 
                                                    Node.node_id, Node.parent_id, 
                                                    initiator, "whoppiiee", 
                                                    Node.parent_id);
                                            Node.control_message.add(message);
                                        }
                                    }
                                    else {
                                        System.out.println("Cohort list not empty "
                                                + "so still waiting on other REPLY"
                                                + " messages for last TAKE");
                                    }
                                    break;
                                case ROLLBACK:
                                    // I know I am not the initiator so I get LLS 
                                    // from sender and determine if I also need to 
                                    // take a roll back
                                    LLR = Node.local_LLR[sender];
                                    int LLS = msg.getLLS();
                                    System.out.println("LLR=" + LLR + ", LLS=" + 
                                            LLS + ".");
                                    // Agree to rollback if LLR > LLS and I 
                                    // have not already agreed to rollback
                                    if (LLR > LLS && !Node.already_taken) {
                                        System.out.println("I agree to rollback.");
                                        
                                        // For ROLLBACK messages, I need to store who the 
                                        // sender is as my parent
                                        Node.parent_id = sender;
                                        
                                        // Process stops execution
                                        System.out.println("Updating Node flags to "
                                                + "prevent from sending "
                                                + "application messages.");
                                        Node.updateFlags(-1, true, false);
                                        Node.rollback_mode = true;

                                        // Check if I have neighbors other than sender
                                        List<Integer> neighbors = Node.neighbours;
                                        int neighborSize = neighbors.size() - 1;
                                        if (neighbors.contains(initiator) && initiator != sender) {
                                           neighborSize--;
                                        }
                                        System.out.println("Neighbor set size minus 1 "
                                                + "(for sender): " + neighborSize + ".");
                                        if (neighborSize > 0) {
                                            // Send ROLLBACK message to all my neighbors 
                                            // EXCEPT to the sending process.
                                            System.out.println("Sending ROLLBACK "
                                                    + "to all neighbors EXCEPT "
                                                    + "to the sending process.");
                                            for (int neighbor: neighbors) {
                                                if (neighbor != sender && neighbor != initiator) {
                                                    Node.temp_neighbor_list.add(neighbor);
                                                    System.out.println(
                                                            "Sending ROLLBACK to P" 
                                                            + neighbor + ".");
                                                    Messages message = 
                                                            new Messages(
                                                            Types_of_Messages.
                                                            ROLLBACK,
                                                            Node.node_id, 
                                                            neighbor, 
                                                            initiator, 
                                                            "whoppiiee", 
                                                            sender);
                                                    message.setLLS(
                                                            Node.permanent_checkPoint.LLS[neighbor]);
                                                    Node.control_message.
                                                            add(message);
                                                }
                                                else {
                                                    System.out.println("Not "
                                                            + "sending to P" + 
                                                            neighbor + " since "
                                                            + "it is the "
                                                            + "sender (" + 
                                                            sender + ").");
                                                }
                                            }
                                        }
                                        else {
                                            System.out.println("No other "
                                                    + "rollbacks needed, "
                                                    + "sending ROLLBACK_REPLY to P" + sender 
                                                    + ".");
                                            Messages message = new Messages(
                                                    Types_of_Messages.ROLLBACK_REPLY, 
                                                    Node.node_id, sender, 
                                                    initiator, "whoppiiee", 
                                                    sender);
                                            Node.control_message.add(message);
                                        }
                                    }
                                    else {
                                        System.out.println("Will NOT need to "
                                                + "rollback, sending "
                                                + "ROLLBACK_REPLY to P" + sender 
                                                + ".");
                                        Messages message = new Messages(
                                                Types_of_Messages.ROLLBACK_REPLY, 
                                                Node.node_id, sender, initiator, 
                                                "whoppiiee", sender);
                                        Node.control_message.add(message);
                                    }
                                    break;
                                case ROLLBACK_REPLY:
                                    // Need to remove neighbor from temp_neighbor_list to 
                                    // check if I have received all replies
                                    System.out.println("Remove P" + sender + 
                                            " from temp_neighbor_list.");
                                    List<Integer> neighborList = Node.temp_neighbor_list;
                                    neighborList.remove(Node.temp_neighbor_list.size() - 1);
                                    if (neighborList.isEmpty()) {
                                        System.out.println("Neighbor list empty so I "
                                                + "have received all replies to "
                                                + "ROLLBACK message, now if I am "
                                                + "initiator, I start phase 2 "
                                                + "where I reset my arrays, allow "
                                                + "application messages for this "
                                                + "process by calling "
                                                + "updateJustFlags, and send DONE_ROLLBACK "
                                                + "message to ALL neighbors. If I am not "
                                                + "initiator, then send ROLLBACK_REPLY to "
                                                + "my parent.");
                                        if (Node.node_id == initiator) {
                                            System.out.println("I am initiator so "
                                                    + "sending DONE_ROLLBACK to all "
                                                    + "neighbors and allow app processing.");
                                            List<Integer> neighbors = 
                                                    Node.neighbours;
                                            for (int neighbor: neighbors) {
                                                Node.temp_neighbor_list.add(neighbor);
                                                Messages message = new Messages(
                                                        Types_of_Messages.DONE_ROLLBACK, 
                                                        Node.node_id, neighbor, 
                                                        initiator, "whoppiiee", 
                                                        -2); // No parent
                                                Node.control_message.add(message);
                                            }
                                            System.out.println("Allowing app messages");
                                            // Now allow application messages
                                            Node.updateFlags(initiator, false, true);
                                            Node.rollback_mode = false;
                                        }
                                        else {
                                            System.out.println("I am not initiator "
                                                    + "so then send reply to "
                                                    + "parent");
                                            Messages message = new Messages(
                                                    Types_of_Messages.ROLLBACK_REPLY, 
                                                    Node.node_id, Node.parent_id, 
                                                    initiator, "whoppiiee", 
                                                    Node.parent_id);
                                            Node.control_message.add(message);
                                        }
                                    }
                                    else {
                                        System.out.println("Neighbor list not empty "
                                                + "so still waiting on other ROLLBACK_REPLY"
                                                + " messages for last ROLLBACK");
                                    }
                                    break;
                                case DONE_ROLLBACK:
                                    // DONE_ROLLBACK messages are recovery protocol phase 
                                    // 2 messages where the initiator is indicating 
                                    // to all neighbors to that they can now rollbac. 
                                    // Will need to add a 
                                    // performRollback method to Node to do this.
                                    // Since the parent is flooding all neighbors, 
                                    // not just cohorts, we need to check if we 
                                    // have agreed to rollback. If not, then 
                                    // we do nothing. Part of the performRollback 
                                    // method should include resetting arrays but don't reset 
                                    // flags until after broadcasting other messages.
                                    // Then I broadcast DONE_ROLLBACK messages to all my 
                                    // neighbors knowing they may receive 
                                    // duplicates (this is to ensure FIFO 
                                    // guarantees all processes receive DONE_ROLLBACK before 
                                    // an app message
                                    if (Node.rollback_mode) {
                                        // Rollback my process then
                                        // broadcast DONE_ROLLBACK to all my neighbors and I 
                                        // will receive reply for these.
                                        System.out.println("Rolling back.");
                                        Node.performRollback();
                                        // Set parent_id to sender
                                        Node.parent_id = sender;
                                        List<Integer> neighbors = Node.neighbours;
                                        System.out.println("Broadcasting DONE_ROLLBACK"
                                                + " message to all neighbors.");
                                        // Subtract 1 from neighbors since I will not send 
                                        // DONE_ROLLBACK back to sender
                                        if (neighbors.size() - 1 > 0) {
                                            for (int neighbor: neighbors) {
                                                if (neighbor != sender) {
                                                    Node.temp_neighbor_list.add(neighbor);
                                                    System.out.println("Sending "
                                                            + "DONE_ROLLBACK to P" + 
                                                            neighbor + ".");
                                                    Messages message = 
                                                            new Messages(
                                                            Types_of_Messages.DONE_ROLLBACK, 
                                                            Node.node_id, neighbor, 
                                                            initiator, "whoppiiee", 
                                                            sender);
                                                    Node.control_message.
                                                            add(message);
                                                }
                                            }
                                        }
                                        else {
                                            System.out.println("I don't have any "
                                                    + "neighbors, so I send "
                                                    + "DONE_ROLLBACK_REPLY right away");
                                            Messages message = new Messages(
                                                Types_of_Messages.DONE_ROLLBACK_REPLY, 
                                                Node.node_id, sender, 
                                                initiator, "whoppiiee", 
                                                sender);
                                            Node.control_message.add(message);
                                        }
                                        
                                        // Now allow application messages
                                        Node.updateFlags(initiator, false, true);
                                        Node.rollback_mode = false;
                                    }
                                    else {
                                        System.out.println("I am not waiting to "
                                                + "rollback because I probably "
                                                + "already have but still need to "
                                                + "send DONE_ROLLBACK_REPLY.");
                                        Messages message = new Messages(
                                                Types_of_Messages.DONE_ROLLBACK_REPLY, 
                                                Node.node_id, sender, 
                                                initiator, "whoppiiee", 
                                                sender);
                                        Node.control_message.add(message);
                                    }
                                    break;
                                case DONE_ROLLBACK_REPLY:
                                    // Need to remove neighbor from 
                                    // temp_neighbor_list to check if I have 
                                    // received all replies
                                    System.out.println("Remove P" + sender + 
                                            " from temp_neighbor_list.");
                                    List<Integer> tempNeighborsList = 
                                            Node.temp_neighbor_list;
                                    tempNeighborsList.remove(Node.temp_neighbor_list.size() - 1);
                                    if (tempNeighborsList.isEmpty()) {
                                        System.out.println("I have received all "
                                                + "DONE_ROLLBACK_REPLIES. If I am "
                                                + "initiator, then recovery "
                                                + "instance is complete, so I "
                                                + "call recoveryComplete "
                                                + "method. If I am not the "
                                                + "initiator, then send "
                                                + "DONE_ROLLBACK_REPLY to my parent.");
                                        if (Node.node_id == initiator) {
                                            System.out.println("I am initiator so "
                                                    + "ending recovery "
                                                    + "instance");
                                            Node.recoveryComplete();
                                        }
                                        else {
                                            System.out.println("I am not the "
                                                    + "initiator so send "
                                                    + "DONE_ROLLBACK_REPLY to parent.");
                                            Messages message = new Messages(
                                                    Types_of_Messages.DONE_ROLLBACK_REPLY, 
                                                    Node.node_id, Node.parent_id, 
                                                    initiator, "whoppiiee", 
                                                    Node.parent_id);
                                            Node.control_message.add(message);
                                        }
                                    }
                                    else {
                                        System.out.println("Temp_neighbot_list "
                                                + "not empty so still waiting on "
                                                + "other DONE_ROLLBACK_REPLY messages for "
                                                + "last DONE_ROLLBACK");
                                    }
                                    break;
                                case APPLICATION:
                                    // If I have agreed to rollback or am in rollback mode,
                                    // then don't do anything, this message will be lost.
                                    if (Node.rollback_mode) {
                                        System.out.println("Process received an application "
                                                + "message while in the process of rolling "
                                                + "back so this message will be lost.");
                                    }
                                    else {
                                        // Application messages just update vector clock and LLR
                                        vectorClock.updateClockRcv(sender, msg.getClock());
                                        Node.local_LLR[sender] = vectorClock.getClockByProcessID(sender);
                                       System.out.print("LLR Values: [");
                                        for (int i = 0; i < Node.local_LLR.length; i++) {
                                                System.out.print(" "+Node.local_LLR[i]);
                                        }
                                        System.out.println("]");
                                    }
                                    break;
                                case START:
                                    System.out.println("Receive START message, starting new Thread_Checkpoint "
                                            + "thread to start next instance");
                                    new Thread(Node.protocol).start();
                                    break;
                                case TERMINATION:
                                    listener = false;
                                    Node.terminate = true;
                                    Node.killApp = true;
                                    break;
                                default:    
                                    System.out.println("Error: Unknown message type received.");
                                    break;
                            }
                    }
                    else {
                        System.err.println("Null received from "
                                + "receiveMessage() in SCTP channel");
                    }
                //}
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } finally {
                if (tcpChannel != null) {
                    try {
                        tcpChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //System.out.println("connectionMap "+ Node.connectionMap.size() );
            //System.out.println("neighbours size" + neighbours.size());
            //System.out.println("i am in listener all connection made");
        }
        System.out.println("Receiver Thread is now closing.");
    }
    
    public Messages receiveMessageSctp(SctpChannel sctpChannel) throws IOException, InterruptedException {
        ByteBuffer buf = ByteBuffer.allocate(60000);
        System.out.println("In ReceiveMessage().");
        sctpChannel.receive(buf,null,null);
        System.out.println("Received data into buffer.");
        buf.flip();
        Messages msg = null;
        if (buf.remaining() > 0) {
            msg = deserialize(buf);
        }
        buf.clear();
        
        return msg;
    }
    
    public Messages receiveMessage(Socket tcpChannel) throws IOException, InterruptedException, ClassNotFoundException {
        ObjectInputStream inputStream = 
            new ObjectInputStream(
            tcpChannel.getInputStream());
        ObjectOutputStream outputStream = 
            new ObjectOutputStream(
            tcpChannel.getOutputStream());
        Object input = inputStream.readObject();
        outputStream.writeObject("reply");
        if (input instanceof Messages) {
            return (Messages)input;
        }
        
        System.out.println("Received message that is not Messages object.");
        return null;
    }




    public Messages deserialize(ByteBuffer byteBuffer){
        ByteArrayInputStream byteArrayInputStream = 
                new ByteArrayInputStream(byteBuffer.array());
        ObjectInputStream objectInputStream;
        Object objectToDeserialize = null;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            objectToDeserialize = objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        Messages convertedObject = null;
        if (objectToDeserialize instanceof Messages) {
            convertedObject = (Messages)objectToDeserialize;
        }
        else {
            System.out.println("Object received in message does not deserialize to Messages object.");
        }
        
        return convertedObject;
    }
}
