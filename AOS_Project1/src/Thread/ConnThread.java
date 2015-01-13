package Thread;
import Server.Process;
import Server.Server;
import Client.Client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class ConnThread extends Thread {

	Process process = null;
	String[] hostDetails;
	
	public ConnThread(Map<Integer, String> pathMap, Integer processNumber,String pathTraversal)
	{
		process = new Process();
		process.setNodeInfoTable(pathMap);
		process.setMessage("Process Number : "+processNumber);
		String PathInfo = pathMap.get(processNumber); //Extracting the details from configuration.txt
		hostDetails = PathInfo.split(":");
		String[] Paths = pathTraversal.split("->"); //Extracting and sorting the arraylist
		process.setPathTraversalList(new ArrayList<String>(Arrays.asList(Paths)));
	}
	
	private void SocketConnection(String[] hostDetails, Process process)
	{
		ObjectOutputStream objectOutputStream = null;
		try
		{
			String hostname = hostDetails[0];
			int hostPort = Integer.parseInt(hostDetails[1].toString());
			Socket client = new Socket(hostname,hostPort);
			objectOutputStream = new ObjectOutputStream(client.getOutputStream()); //sending data to the new machine in the path
			objectOutputStream.writeObject(process);
			System.out.println("sending data to "+hostname+":"+hostPort+" with data"+process.getMessage());
			objectOutputStream.flush();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if(objectOutputStream!=null)
			{
				try {
					objectOutputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			SocketConnection(hostDetails,process);
			
		}

}