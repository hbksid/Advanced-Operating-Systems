//package Koo_Toueg_Protocol;


import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

	/**
	 * TODO Put here a description of what this class does.
	 *
	 * @author Siddharth
	 * 
	 */
public class Thread_Checkpoint implements Runnable{
		Map<Integer,String> nodeConfiguration;
		int timer;
                LinkedList<String> protocolSeq;
		
		public Thread_Checkpoint(Map<Integer,String> nodeConfiguration,int timer, 
                        LinkedList<String> protocolSeq) {
			this.nodeConfiguration = nodeConfiguration;
			this.timer = timer;
                        this.protocolSeq = protocolSeq;
		}
		
		//@Override
		public void run() {
			/*while(true){*/
			
				try {
					Thread.sleep(timer);
				} catch (InterruptedException exception) {
					// TODO Auto-generated catch-block stub.
					exception.printStackTrace();
				}
				System.out.println("Wake  from sleep for node "+Node.node_id);
				System.out.println("------------- Already Taken :"+Node.already_taken);
                                String protocolType = protocolSeq.remove();
                                System.out.println("Next protocol sequence: " + protocolType);
                                if(!Node.already_taken){
                                    if ("c".equals(protocolType)) {
                                        System.out.println("Node " + Node.node_id + " initiating a checkpoint instance.");
					//synchronized (Node.controlMessage){
					
				//	Node.initiatorId = Node.nodeId;
					Node.updateFlags(Node.node_id,true,false);
					System.out.println("Initiator ID : for Node 0 instance "+Node.initiator_id);
					Node.take_A_Checkpoint(Node.node_id);
					
					List<Integer> cohortList = Node.getCohortMembers();
					System.out.println("in main Cohort list Node 0-------- :::::: " + cohortList.size());
					Iterator itr = cohortList.iterator();
					while(itr.hasNext()){
						System.out.println(itr.next()+" ");
					}
					if(cohortList.size() > 0) {
                                                    for (int j = 0; j < cohortList.size(); j++) {
                                                            Node.cohort_list.add(cohortList.get(j));
                                                            System.out.println("Sending INITIAL TAKE message to " + cohortList.get(j) + ".");
                                                            Messages message = new Messages(Types_of_Messages.TAKE,Node.node_id,cohortList.get(j),Node.node_id,"  ",Node.node_id);
                                                            message.setLlrValue(Node.local_LLR[cohortList.get(j)]);
                                                            Node.control_message.add(message);	
                                                    }
                                        }
                                        else {
                                            System.out.println("I don't have any cohorts so make last checkpoint permanennt and resume application");
                                            try {
                                                Node.makePermanent();
                                            } catch (FileNotFoundException ex) {
                                                ex.printStackTrace();
                                            }
                                            Node.updateFlags(Node.node_id, false, true);
                                            Node.checkpointComplete();
                                        }
                                    }
                                    else if ("r".equals(protocolType)) {
                                        System.out.println("Node " + Node.node_id + " initiating a recovery instance.");
                                        System.out.println("Updating Node flags to "
                                                + "prevent from sending "
                                                + "application messages.");
                                        Node.updateFlags(-1, true, false);
                                        Node.rollback_mode = true;
                                        System.out.println("Initiator rolling back.");
                                        try {
                                            Node.performRollback();
                                        } catch (FileNotFoundException ex) {
                                            ex.printStackTrace();
                                        }
                                        
                                        // Check if I have neighbors other than sender
                                        List<Integer> neighbors = Node.neighbours;
                                        int neighborSize = neighbors.size();
                                        System.out.println("Neighbor set size: " + neighborSize + ".");
                                        if (neighborSize > 0) {
                                            // Send ROLLBACK message to all my neighbors 
                                            System.out.println("Sending ROLLBACK "
                                                    + "to all neighbors.");
                                            for (int neighbor: neighbors) {
                                                    Node.temp_neighbor_list.add(neighbor);
                                                    System.out.println(
                                                            "Sending ROLLBACK to P" 
                                                            + neighbor + ".");
                                                    synchronized (Node.control_message) {
                                                        Messages message = 
                                                                new Messages(
                                                                Types_of_Messages.
                                                                ROLLBACK,
                                                                Node.node_id, 
                                                                neighbor, 
                                                                Node.node_id, 
                                                                "whoppiiee", 
                                                                Node.node_id);
                                                        message.setLLS(
                                                                Node.permanent_checkPoint.LLS[neighbor]);
                                                        Node.control_message.
                                                                add(message);
                                                    }
                                            }
                                        }
                                        else {
                                            System.out.println("I don't have any neighbpors sogo ahead and perform rollback and resume application");
                                            try {
                                                Node.performRollback();
                                            } catch (FileNotFoundException ex) {
                                                ex.printStackTrace();
                                            }
                                            Node.updateFlags(Node.node_id, false, true);
                                            Node.rollback_mode = false;
                                            Node.recoveryComplete();
                                        }
                                    }
                                    else {
                                        System.err.println("Received unknown protocol type (should be r or c): " + protocolType);
                                    }
				}
				                System.out.println("Protocol initiation complete.");
			}

			}

