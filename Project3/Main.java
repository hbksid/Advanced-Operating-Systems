//package Koo_Toueg_Protocol;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Main {
	public volatile static int node_to_fail;
	public volatile static int time_to_fail;
	private static HashMap<Integer, String> configuration = new HashMap<Integer, String>();
	private static HashMap<Integer, String> command_configuration = new HashMap<Integer, String>();
        private static final LinkedList<String> protocolSeq = new LinkedList<>();
        private static final LinkedList<Integer> nextNodeIdSeq = new LinkedList<>();
	private static int node_id;
	private static List<Integer> neighbour_node_ids = new ArrayList<Integer>();
	private static int timer;
	public static Queue<String> myQueue = new LinkedList<String>();
        public static boolean firstProtocol;

	public static void main(String[] args) throws FileNotFoundException {
		if (args.length > 0) {
                        node_id = Integer.parseInt(args[2]);
			loadConfiguration(args[0], args[1]); // loads the configuration file
                        Thread_Checkpoint protocol = new Thread_Checkpoint(configuration, timer, protocolSeq);
			Node node = new Node(node_id, configuration, neighbour_node_ids, timer, node_to_fail, protocol, nextNodeIdSeq); // Creates
																									// a
			// All process take a checkpoint at the beginning
                        Node.take_A_Checkpoint(node_id);
                        Node.makePermanent();
                        
                        // System.out.println("----------- NodeId : "+nodeId);
			Thread Thread_send = new Thread(node); // Creates client thread
													// which will send the
													// message
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Thread_send.start();
			String cont = "coming from";
                        
                        if (firstProtocol) {
                            System.out.println("I am the first node to start a protocol");
                            new Thread(protocol).start();
                        }
																						// for
																						// checkpointing
			// Thread chkpoint_thread = new Thread(checkpoint);
			// chkpoint_thread.start();
                        // This is the application
                        int count = 0;
			while (!Node.terminate && !Node.killApp) {
                                count++;
				int destination_id = (int) Math.floor(Math.random() * Node.neighbours.size());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				if (node_to_fail == 1 && Node.numberof_permanent_chkpoint_taken == time_to_fail && !Node.already_taken) {
//					// failing here
//					System.out.println("Application msg generation Failed");
//					Node.numberof_permanent_chkpoint_taken = 0; // to generate
//																// Node failing
//																// again
//					Node.rollback_mode = true; // rollback mode is set
//					Node.already_taken = true;
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					// rolling back to last permanent checkpoint
//					// adding control message rollback and send to all neighbors
//					Node.rolling_back_to_previous_chkpoint();
//					Node.display_rolled_back_values();
//					for (int i = 0; i < Node.neighbours.size(); i++) {
//						Messages message = new Messages(Types_of_Messages.ROLLBACK, node_id, Node.neighbours.get(i),
//								node_id, "Rollingback", node_id);
//						node.rollback_initiator_id = node_id;
//						message.setLLS(Node.local_LLS[i]);
//						synchronized (Node.control_message) {
//							Node.control_message.add(message);
//						}
//					}
//				} else if (Node.retry_flag) {
//					try {
//						Thread.sleep(2000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					System.out.println("In retry : " + Node.retry_flag);
//					Node.numberof_permanent_chkpoint_taken = 0; // to generate
//																// Node failing
//																// again
//					Node.rollback_mode = true;
//					Node.already_taken = true;
//					// rolling back to last permanent checkpoint
//					// adding control message rollback and send to all neighbors
//					Node.rolling_back_to_previous_chkpoint();
//					Node.display_rolled_back_values();
//					for (int i = 0; i < Node.neighbours.size(); i++) {
//						Messages message = new Messages(Types_of_Messages.ROLLBACK, node_id, Node.neighbours.get(i),
//								node_id, "Rollingback", node_id);
//						node.rollback_initiator_id = node_id;
//						message.setLLS(Node.local_LLS[i]);
//						synchronized (Node.control_message) {
//							Node.control_message.add(message);
//						}
//					}
//				} else 
                                if (!Node.already_taken) {
                                        System.out.println("Application adding to app message queue.");
					Messages message = new Messages(Types_of_Messages.APPLICATION, node_id,
							Node.neighbours.get(destination_id), node_id, cont, node_id);
					synchronized (Node.application_message) {
						Node.application_message.add(message);
					}

				}

			}
// 
                        System.err.println("P" + Node.node_id + " program is complete.");
                        System.out.println("Application thread is now closing.");
		}
	}

	private static void loadConfiguration(String config_file, String config_file1) {
		// TODO Auto-generated method stub
		String local_host;
		BufferedReader buff_reader = null;
		try {
			buff_reader = new BufferedReader(new InputStreamReader(new FileInputStream(config_file)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader buff_reader1 = null;
		try {
			buff_reader1 = new BufferedReader(new InputStreamReader(new FileInputStream(config_file1)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			local_host = java.net.InetAddress.getLocalHost().getHostAddress();
			String new_line = null;
			time_to_fail = Integer.parseInt(buff_reader.readLine());

                        int config_nodeId = 0;
			while ((new_line = buff_reader.readLine()) != null) {
				if (new_line.length() > 0) {
					String[] splitString = new_line.split(" ");
                                        
					String host_name = splitString[0];
					int port_number = Integer.parseInt(splitString[1]);

					configuration.put(config_nodeId, host_name + "#" + port_number);
                                        System.out.println("Host: " + configuration.get(config_nodeId));
					if (node_id == config_nodeId) { // id for
																	// current
										
                                               							// node.
						System.out.println("NodeId: " + config_nodeId);
						String neighbourList = splitString[2];
						String[] neighbours = neighbourList.split(",");
                                                System.out.print("Neighbors: ");
						for (int i = 0; i < neighbours.length; i++) {
							neighbour_node_ids.add(Integer.parseInt(neighbours[i]));
                                                        System.out.print(neighbour_node_ids.get(i) + " ");
						}
						timer = Integer.parseInt(splitString[3]);
                                                System.out.println("\nTimer: " + timer);
						// node_to_fail=Integer.parseInt(splitString[5]);

					}
                                        config_nodeId++;;
				}
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String line = null;
                        boolean getNextId = false;
                        int count = 0;
			while ((line = buff_reader1.readLine()) != null) {
				if (line.length() > 0) {
					String[] splitStr = line.split(" ");
                                        if (getNextId) {
                                           nextNodeIdSeq.add(Integer.parseInt(splitStr[0]));
                                           getNextId = false;
                                        }
                                        if (Integer.parseInt(splitStr[0]) == node_id) {
                                            if (count == 0) {
                                                firstProtocol = true;
                                            }
                                           protocolSeq.add(splitStr[1]);
                                           getNextId = true;
                                        }
                                    count++;    
				}
			}

			for (int i = 0; i < protocolSeq.size(); i++) {
				System.out.println("Protocol: " + protocolSeq.get(i) + " on node " + (nextNodeIdSeq.size() == 0 ? null : nextNodeIdSeq.get(i)));
			}

			}

		 catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
}

	private static void displayInput() {
		System.out.println("Node : " + node_id);
		System.out.println("Timer : " + timer);
		System.out.println("Address : " + configuration.get(node_id));
		System.out.println("Neighours : ");
		Iterator iteration = neighbour_node_ids.iterator();
		while (iteration.hasNext()) {
			System.out.println(" " + iteration.next() + " ");
		}
	}

}
