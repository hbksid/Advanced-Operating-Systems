package Client;
import Thread.ConnThread;
import Server.Server;
import Server.Process;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



public class Client {

	public static void main(String[] args){
		ReadConfigFile();
	}

	private static void ReadConfigFile() {
		// TODO Auto-generated method stub
		Properties Configuration = new Properties();
		InputStream inputStreamReader = null;
		int Process = 0;
		Map<Integer,String> pathMap = new HashMap<Integer,String>();
		try {
			inputStreamReader = new FileInputStream("configuration.txt");
	 		// load a properties file
			Configuration.load(inputStreamReader);
	 		Process = Integer.parseInt(Configuration.getProperty("Processes"));
			Thread[] connectionThread = new Thread[Process];
			//Store paths in hashmaps
			for(int i=1; i<=Process; i++)
			{
				pathMap.put(i, Configuration.getProperty("ProcessLocation"+i));
				
			}
			for(int i=0; i<Process; i++)
			{
				String pathTraversal = Configuration.getProperty("ProcessPath"+(i+1));
				connectionThread[i] = new ConnThread(pathMap,i+1,pathTraversal);				
			}
			for(int i=0; i<Process; i++)	
			{
				connectionThread[i].start();				
			}
		
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		
	}
}

