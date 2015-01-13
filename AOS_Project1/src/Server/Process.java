package Server;

import Thread.ConnThread;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class Process implements Serializable{

	private int label = 0;
	private String message = "";
	private Map<Integer,String> InfoNode = new HashMap<Integer,String>();
	private ArrayList<String> PathList = new ArrayList<String>();
	//private static final long serialVersionUID = 5950169519310163575L;
	
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public void setNodeInfoTable(Map<Integer, String> infoNode)
	{
		InfoNode = infoNode;
	}
	public Map<Integer, String> getInfoNode()
	{
		return InfoNode;
	}
	public void setLabel(Integer label) 
	{
		this.label = label;
	}
	public int getLabel()
	{
		return label;
	}
	public void setPathTraversalList(ArrayList<String> PathList)
	{
		this.PathList = PathList;
	}
	public ArrayList<String> getPathList()
	{
		return PathList;
	}
}
