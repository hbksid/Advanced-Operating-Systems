package Server;
import Thread.ConnThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
public class Server {

	public static void main (String[] args) throws ClassNotFoundException{
		Connection();
	}

	private static void Connection() throws ClassNotFoundException {
		// TODO Auto-generated method stub
		ObjectInputStream objectInputStream = null;
		String message = "";
		try
		{
			ServerSocket serverSocket = new ServerSocket(5000);
			while(true)
			{
				Socket sock = serverSocket.accept();
				objectInputStream = new ObjectInputStream(sock.getInputStream());
				Process process = (Process) objectInputStream.readObject();
				System.out.println("Server:" +process.getInfoNode()+" "+process.getMessage()+" "+process.getPathList());
			}
				//System.out.println("Server:" +process.getInfoNode()+" "+process.getMessage()+" "+process.getPathList());
				
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
