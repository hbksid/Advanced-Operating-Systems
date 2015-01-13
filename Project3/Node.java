//package Koo_Toueg_Protocol;



import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node implements Runnable {

	public static List<Integer> neighbours = new ArrayList<Integer>();
	public static Queue<Messages> application_message = new LinkedBlockingQueue<Messages>(); ;
	public static Queue<Messages> control_message = new LinkedBlockingQueue<Messages>();
	public static List<Integer> cohort_list=new ArrayList<Integer>();
	static volatile ConcurrentHashMap<Integer,Socket> conn_Map = new ConcurrentHashMap<Integer, Socket>();
	static HashMap<Integer, String> node_configuration;
	public volatile static boolean ready_to_take = true;
	public volatile static boolean already_taken = false;
	public volatile static int numberof_permanent_chkpoint_taken=0;
        public volatile static int numberofrollbacks_taken=0;
	public volatile static boolean rollback_mode=false;
        public volatile static boolean terminate = false;
        public volatile static boolean killApp = false;
	public static int[] temp_localLLR;
	public static int[] temp_localLLS;
	public static int[] temp_localFLS;
	public static int[] local_LLR;
	public static int[] local_LLS;
	public static int[] local_FLS;
	static int node_id;
	int timer;
	ServerSocket tcp_server_channel;
	Thread serverThread;
	public static VectorClock vector_clock;
	public static Checkpoint permanent_checkPoint;
        public static Checkpoint tentative_checkPoint;
	public volatile static int initiator_id = -1;
	public volatile static int parent_id = -1;
	public volatile static int rollback_initiator_id=-1;
	public volatile static int rollback_parent_id=-1;
	static File file;
	static File rollback_file;
	static int send_count[];
	static int receiver_count[];
	static int temp_send_count[];
	static int temp_receiver_count[];
	static int retry_count = 0;
	static boolean retry_flag = false;
        // Added by Rusty
        public static List<Integer> temp_neighbor_list = new ArrayList<>();
        private static File protocolSeqFile;
        private static File lastClockFile;
        public static Thread_Checkpoint protocol;
        private static LinkedList<Integer> nextNodeIdSeq;
        
	
	public Node(int nodeId,HashMap<Integer,String> configuration,List<Integer> neighbours,
                int timer,int node_to_fail, Thread_Checkpoint protocol, LinkedList<Integer> nextNodeIdSeq) throws FileNotFoundException {
		this.node_id = nodeId;
                lastClockFile = new File("/home/005/r/rt/rtr100020/CS_6378/Project3/"
                        + "lastClock" + node_id + ".txt");
                protocolSeqFile = new File("/home/005/r/rt/rtr100020/CS_6378/Project3/"
                        + "protocolSeq" + node_id + ".txt");
		this.node_configuration = configuration;
		this.neighbours = neighbours;
		this.timer = timer;
		local_FLS = new int[node_configuration.size()];
		local_LLS = new int[node_configuration.size()];
		local_LLR = new int[node_configuration.size()];
		temp_localFLS = new int[node_configuration.size()];
		temp_localLLS = new int[node_configuration.size()];
		temp_localLLR = new int[node_configuration.size()];
		for (int i = 0; i < node_configuration.size(); i++) {
			local_FLS[i]= -1;
			local_LLS[i]= -1;
			local_LLR[i]= -1;
			temp_localFLS[i]= -1;
			temp_localLLS[i]= -1;
			temp_localLLR[i]= -1;
		}
		file = new File("CheckPoint_"+nodeId+".txt");
		rollback_file = new File("Rollback_"+nodeId+".txt");
		send_count = new int[node_configuration.size()];
		receiver_count = new int[node_configuration.size()];
		temp_send_count = new int[node_configuration.size()];
		temp_receiver_count = new int[node_configuration.size()];
                vector_clock = new VectorClock(node_configuration.size(), 0, 1);
                Node.protocol = protocol;
                this.nextNodeIdSeq = nextNodeIdSeq;
		node_initialize();
	}
	/*
	 * @author Siddharth
	 * Intialization of nodes are done in this method
	 */
	private void node_initialize() {
		// TODO Auto-generated method stub
		String hostAddress =  node_configuration.get(node_id);
		String[] splitAddress  = hostAddress.split("#");
		try{
			tcp_server_channel = new ServerSocket(Integer.parseInt(splitAddress[1]));
			InetSocketAddress server_address = new InetSocketAddress(splitAddress[0],Integer.parseInt(splitAddress[1]));
			//sctp_server_channel.bind(server_address);
			Thread receiver_thread = new Thread(new ReceiverThread(tcp_server_channel,node_configuration,vector_clock,neighbours));
			receiver_thread.start();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void run() {
		//connectAll();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	    
	    while(!Node.terminate){
			Iterator iterator;
			if(Node.application_message.isEmpty() && Node.control_message.isEmpty()){
				//System.out.println("Handling no message");
				continue;
			}
                        if(!Node.control_message.isEmpty()){
				System.out.println("\n\nHandling control messages");
					iterator = Node.control_message.iterator();
					Messages msg = null;
					System.out.println("------------- Control Queue size: "+Node.control_message.size());
					while(iterator.hasNext()){
						msg = (Messages)iterator.next();
						
						int destinationId = msg.getDestinationId();
						System.out.println("Sending message of type: "+msg.getMessageType() 
                                                        + " from node: "+msg.getSenderId()+ " to destination: "+msg.getDestinationId());
                                                try {
                                                        sendMessage(msg);
                                                } catch (CharacterCodingException e1) {
                                                        e1.printStackTrace();
                                                }
						
						Node.control_message.remove(msg);
					}
			}else if(Node.control_message.isEmpty() && !already_taken && !rollback_mode){
				System.out.println("\n\nHandling application messages");
				synchronized (Node.application_message){
					iterator = Node.application_message.iterator();
					Messages msg = null;
					while(iterator.hasNext()){
						msg = (Messages)iterator.next();
						synchronized (vector_clock) {
							msg.setClock(vector_clock.updateClockSnd());	
						}
						int destinationId = msg.getDestinationId();
						switch(msg.getMessageType()){
						case APPLICATION:
								synchronized (local_FLS){
									if(local_FLS[destinationId] == -1){
										local_FLS[destinationId] = vector_clock.getClockByProcessID(node_id);
									}
									
								}
								synchronized (local_LLS){
									local_LLS[destinationId] = vector_clock.getClockByProcessID(node_id);
								}
								System.out.println("Application message sent from "+Node.node_id+" to destination: "+msg.getDestinationId()+"\nAlready Taken flag: "+Node.already_taken);
								send_count[msg.getDestinationId()]++;
								try {
									sendMessage(msg);
								} catch (CharacterCodingException e1) {
									e1.printStackTrace();
								}	
								System.out.print("FLS Values: [");
								for (int i = 0; i < Node.local_FLS.length; i++) {
									System.out.print(" "+Node.local_FLS[i]);
								}
                                                                System.out.println("]");
                                                                System.out.print("LLS Values: [");
								for (int i = 0; i < Node.local_LLS.length; i++) {
									System.out.print(" "+Node.local_LLS[i]);
								}
                                                                System.out.println("]");
							break;
						}
						Node.application_message.remove(msg);
					}
				}
			}
	    }
            System.out.println("Node machine for process is closing.");
//            closeAll();
            
	}
//        private void closeAll() {
//            for (SctpChannel ch: conn_Map.values()) {
//                try {
//                    ch.close();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
	private void connectAll() {
		// TODO Auto-generated method stub
                
		Iterator iterator = node_configuration.keySet().iterator();
		while(iterator.hasNext()) {
                    int n = Integer.parseInt(iterator.next().toString());
                    String address = node_configuration.get(n);
                    String[] splitAddress = address.split("#");
                    InetSocketAddress serverAddr = new InetSocketAddress(splitAddress[0],Integer.parseInt(splitAddress[1].toString()));
                    try {
                            //SctpChannel clientSocket = SctpChannel.open(serverAddr, 0, 0);
                            //clientSocket.configureBlocking(true);
                            Socket clientSocket = new Socket(splitAddress[0], Integer.parseInt(splitAddress[1]));
                            conn_Map.put(n, clientSocket);
                    } catch (UnknownHostException e) {
                            e.printStackTrace();
                    } catch (IOException e) {
                            e.printStackTrace();
                    }	
                }
		
	}
        private static Socket connect(int destId) {
                    String address = node_configuration.get(destId);
                    String[] splitAddress = address.split("#");
                    Socket clientSocket = null;
                    try {
                            //SctpChannel clientSocket = SctpChannel.open(serverAddr, 0, 0);
                            //clientSocket.configureBlocking(true);
                            clientSocket = new Socket(splitAddress[0], Integer.parseInt(splitAddress[1]));
                            //conn_Map.put(n, clientSocket);
                    } catch (UnknownHostException e) {
                            e.printStackTrace();
                    } catch (IOException e) {
                            e.printStackTrace();
                    }	
                    return clientSocket;
		
	}
	public synchronized static void take_A_Checkpoint(int initiator_id){
            if(tentative_checkPoint == null) { 
                System.out.println("Taking a tentative checkpoint. Clock: " + vector_clock.toString());
                    tentative_checkPoint = new Checkpoint(node_configuration.size());
            }
            else {
                System.out.println("Process is already in progress of taking a checkpoint");
                return;
            }
            for(int i = 0;i<node_configuration.size();i++){
                tentative_checkPoint.FLS[i] = -1;
                tentative_checkPoint.LLR[i] = -1;
                tentative_checkPoint.LLS[i] = local_LLS[i];
            }
            tentative_checkPoint.application_message = new LinkedBlockingQueue<Messages>();
            tentative_checkPoint.application_message.addAll(application_message);
            tentative_checkPoint.clock = VectorClock.createCopy(vector_clock);
            tentative_checkPoint.setCheckpointInitiatorId(initiator_id);
	}
        
        public synchronized static void makePermanent() throws FileNotFoundException {
            System.out.println("Making checkpoint permanent. so writing data to file.");
            permanent_checkPoint = tentative_checkPoint;
            tentative_checkPoint = null;
            numberof_permanent_chkpoint_taken++;
            // Write vector clocks to files
            String clockStr = vector_clock.toString();
            PrintWriter clockWriter = new PrintWriter(lastClockFile);
            PrintWriter seqWriter = new PrintWriter(new FileOutputStream(protocolSeqFile, true));
            seqWriter.append("c " + numberof_permanent_chkpoint_taken + " [" + clockStr + "]\n");
            clockWriter.print(clockStr);
            seqWriter.close();
            clockWriter.close();
            // Reset FLS and LLR
            resetLocalMemory();
        }
        
        public synchronized static void performRollback() throws FileNotFoundException {
            // Initialize system with data from last permanent checkpoint
            System.out.println("Performing rollback so setting state data to last permanent checkpoint.");
            resetLocalMemory();
            vector_clock = permanent_checkPoint.clock;
            application_message = permanent_checkPoint.application_message;
            numberofrollbacks_taken++;
            // Write vector clocks to files
            String clockStr = vector_clock.toString();
            PrintWriter clockWriter = new PrintWriter(lastClockFile);
            PrintWriter seqWriter = new PrintWriter(new FileOutputStream(protocolSeqFile, true));
            seqWriter.append("r " + numberofrollbacks_taken + " [" + clockStr + "]\n");
            clockWriter.print(clockStr);
            seqWriter.close();
            clockWriter.close();
        }
        
        public synchronized static void checkpointComplete() {
            try{
			FinalTest.Checking();
			}catch(Exception e)
		{
			System.out.println(e.getMessage());
			
		}
            startNextProtocol();
        }
        
        public synchronized static void recoveryComplete() {
            try{
			FinalTest.Checking();
			}catch(Exception e)
		{
			System.out.println(e.getMessage());
			
		}
            startNextProtocol();
        }
        
        private static void startNextProtocol() {
            Integer nextNodeId = (nextNodeIdSeq.size() == 0 ? null : nextNodeIdSeq.remove());
            if (nextNodeId != null) {
                synchronized (Node.control_message) {
                    Node.control_message.add(new Messages(Types_of_Messages.START, 
                            Node.node_id, nextNodeId, Node.node_id, " ", Node.node_id));
                }
            }
            else {
                startTermination();
            }
        }
        
        public static void startTermination() {
            if (control_message.isEmpty()) {
                System.err.println("P" + Node.node_id +" is starting termination.");
                Node.killApp = true;
                for (int i = 0; i < node_configuration.size(); i++) {
                    if (i != Node.node_id) {
                        Node.control_message.add(new Messages(Types_of_Messages.TERMINATION,
                                Node.node_id, i, Node.node_id, " ", Node.node_id));
                    }
                }
                Node.control_message.add(new Messages(Types_of_Messages.TERMINATION,
                        Node.node_id, Node.node_id, Node.node_id, " ", Node.node_id));
            }
            else {
               System.err.println("P" + Node.node_id + " Error: Termination trying to start when node is "
                       + "still handling control messages. Control_message queueu size: " + control_message.size());
            }
        }
        
        public synchronized static boolean isLastCheckpointTentative() {
            return tentative_checkPoint != null;
        }
	
	
	public synchronized static void resetLocalMemory() {
		for (int i = 0; i < node_configuration.size(); i++) {
			local_FLS[i] = -1;
			local_LLR[i] = -1;
			local_LLS[i] = -1;
			send_count[i] = 0;
			receiver_count[i] = 0;
		}
		/*localMessageReceived.clear();
		localMessageSend.clear();*/
                
                // Added by Rusty. 
                // Permanent checkpoint taken so resetting parentId
                parent_id = -1;
	}
	
	
	/*
	 * @author Siddharth
	 * This method will send messgaes to the given sctp channels
	 */
	public static void sendMessageSctp(SctpChannel client_socket, Messages message) throws CharacterCodingException
    {
        ByteBuffer send_buffer = ByteBuffer.allocate(60000);
        send_buffer.clear();
        send_buffer.put(serializeMessage(message));
        send_buffer.flip();
        try {
            MessageInfo messageInfo = MessageInfo.createOutgoing(null,0);
            client_socket.send(send_buffer, messageInfo);
        } catch (IOException ex) {
          //  logger.log(Level.SEVERE, null, ex);
        }
    }
        
        public static void sendMessage(Messages message) throws CharacterCodingException
    {
        Socket socket = connect(message.getDestinationId());
        try {
            ObjectOutputStream out = 
                    new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(message);
            out.flush();
            Object replyObject = new ObjectInputStream(socket.getInputStream())
                    .readObject();
        } catch (IOException ex) {
          ex.printStackTrace();
        }   catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
    }
        
        
	
	/*
	 * @author Siddharth
	 * This method serializes the messages
	 */
	public static byte[] serializeMessage(Messages msg){
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArrayOutputStream.toByteArray();
	}
	
	
	public synchronized static List<Integer> getCohortMembers() {
		List<Integer> cohort_members = new ArrayList<Integer>();
		for (int i = 0; i < node_configuration.size(); i++) {
			System.out.println("LLR VALUE : "+local_LLR[i]);
			if(local_LLR[i] != -1){
				cohort_members.add(i);
			}
		}
		System.out.println( "Size of Cohort List:  "+cohort_members.size());
		return cohort_members;
	}
	
	
	public synchronized static void undoCheckpoint(int initiatorId) {
		if(initiatorId==Node.initiator_id)
		{
			int[] permanantLLR = permanent_checkPoint.getLLR();
			int[] permanantLLS = permanent_checkPoint.getLLS();
			int[] permanantFLS = permanent_checkPoint.getFLS();
			for (int i = 0; i < node_configuration.size(); i++) {
				if(local_LLR[i] < permanantLLR[i])
					local_LLR[i] = permanantLLR[i];
			}
			for (int i = 0; i < node_configuration.size(); i++) {
				local_LLS[i] = permanantFLS[i];
			}
			for (int i = 0; i < node_configuration.size(); i++) {
				local_LLS[i] = permanantLLS[i];
			}
		
		}
	}
	
	public synchronized static void updateFlags(int initiatorId, boolean alreadyTaken, boolean readytoTake){
		if(Node.initiator_id == -1)
			Node.initiator_id = initiatorId;
		Node.already_taken = alreadyTaken;
		Node.ready_to_take = readytoTake;		
	}
	
	public synchronized static void updateJustFlags(boolean alreadyTaken, boolean readytoTake,int initaitorId){
		Node.already_taken = alreadyTaken;
		Node.ready_to_take = readytoTake;
		Node.initiator_id = initaitorId;
	}
	
	/*
	 * @author Siddharth
	 * This method displays the rolled back values of the node
	 */
	public static void display_rolled_back_values() {
		System.out.println("LLR after Rollback: ");
		for(int i=0;i<node_configuration.size();i++)
		{
			System.out.println(Node.local_LLR[i]+",");
		}
		System.out.println("LLS after Rollback: ");
		for(int i=0;i<node_configuration.size();i++)
		{
			System.out.println(Node.local_LLS[i]+",");
		}
		System.out.println("FLS after Rollback: ");
		for(int i=0;i<node_configuration.size();i++)
		{
			System.out.println(Node.local_FLS[i]+",");
		}
	}

	/*
	 * @author Siddharth
	 * This method rolls back the process to previous temporary checkpoint
	 */
	public synchronized static  void rolling_back_to_temp_previous_chkpoint() {
		// TODO Auto-generated method stub.

		// TODO Auto-generated method stub.
	//loading values from permanent checkpoint to local storage
	BufferedReader br = null;
	try {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(Node.file)));
	} catch (FileNotFoundException e1) {
		e1.printStackTrace();
	}
	String line = null;
	try {
		while ((line = br.readLine()) != null) {
			System.out.println("Read line : "+ line);
			if(line.length() > 0 ){
				String[] splitString = line.split(":");
				String[] splitString1 = splitString[1].split(",");
				if("LLR Value".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.temp_localLLR[i]=Integer.parseInt(splitString1[i].trim());
					}
				}
				if("FLS Value".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.temp_localFLS[i]=Integer.parseInt(splitString1[i].trim());
					}	
				}	
				
				if("LLS Value".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.temp_localLLS[i]=Integer.parseInt(splitString1[i].trim());
					}
				}
				if("Send Count".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.temp_send_count[i]=Integer.parseInt(splitString1[i].trim());
					}
				}
				if("Receive Count".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.temp_receiver_count[i]=Integer.parseInt(splitString1[i].trim());
					}
				}
			}
		}
		} catch (IOException exception) {
		exception.printStackTrace();
	}
	}
	
	public List<Integer> getNeighbours() {
		return neighbours;
	}
	public void setNeighbours(List<Integer> neighbours) {
		this.neighbours = neighbours;
	}
	
	
	public Queue<Messages> getControlMessage() {
		return control_message;
	}
	public void setControlMessage(Queue<Messages> controlMessage) {
		this.control_message = controlMessage;
	}
	public Boolean getWillingToTake() {
		return ready_to_take;
	}
	public void setWillingToTake(Boolean willingToTake) {
		this.ready_to_take = willingToTake;
	}
	public Boolean getAlreadyTaken() {
		return already_taken;
	}
	public void setAlreadyTaken(Boolean alreadyTaken) {
		this.already_taken = alreadyTaken;
	}
//	public ConcurrentHashMap<Integer, SctpChannel> getConnectionMap() {
//		return conn_Map;
//	}
	public void setConnectionMap(
			ConcurrentHashMap<Integer, SctpChannel> connectionMap) {
		connectionMap = connectionMap;
	}

	
	public static void rolling_back_to_previous_chkpoint() {
		// TODO Auto-generated method stub.

		// TODO Auto-generated method stub.
	//loading values from permanent checkpoint to local storage
	BufferedReader br = null;
	try {
		br = new BufferedReader(new InputStreamReader(new FileInputStream(Node.file)));
	} catch (FileNotFoundException e1) {
		e1.printStackTrace();
	}
	String line = null;
	try {
		while ((line = br.readLine()) != null) {
			if(line.length() > 0 ){
				String[] splitString = line.split(":");
				String[] splitString1 = splitString[1].split(",");
				if("LLR Value".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.local_LLR[i]=Integer.parseInt(splitString1[i].trim());
					}
				}
				if("FLS Value".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.local_FLS[i]=Integer.parseInt(splitString1[i].trim());
					}	
				}	
				
				if("LLS Value".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.local_LLS[i]=Integer.parseInt(splitString1[i].trim());
					}
				}
				if("Send Count".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.send_count[i]=Integer.parseInt(splitString1[i].trim());
					}
				}
				if("Receive Count".equals(splitString[0].trim()))
				{
					for(int i=0;i<Node.node_configuration.size();i++)
					{
						Node.receiver_count[i]=Integer.parseInt(splitString1[i].trim());
					}
				}
			}
		}
		} catch (IOException exception) {
		exception.printStackTrace();
	}
	}
}

