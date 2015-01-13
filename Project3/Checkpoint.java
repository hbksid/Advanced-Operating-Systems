//package Koo_Toueg_Protocol;

import java.util.*;

public class Checkpoint {
	/**
	 * @param args
	 */
	public int[] LLR;
	public int[] LLS;
	public int[] FLS;
	private int checkpointInitiatorId ;
	public List<Messages> messageReceived = new ArrayList<Messages>();
	public List<Messages> messageSend = new ArrayList<Messages>();
	public int[] sendCount;
	public int[] receiveCount;
	int numberOfNodes;
        public Queue<Messages> application_message;
        public VectorClock clock;
	
	/**
	 */
	public Checkpoint(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
		LLS = new int[numberOfNodes];
		FLS = new int[numberOfNodes];
		LLR = new int[numberOfNodes];
		sendCount = new int[numberOfNodes];
		receiveCount = new int[numberOfNodes];
	}
	/**
	 * Returns the value of the field called 'sendCount'.
	 * @return Returns the sendCount.
	 */
	public int[] getSendCount() {
		return this.sendCount;
	}
	/**
	 * Sets the field called 'sendCount' to the given value.
	 * @param sendCount The sendCount to set.
	 */
	public void setSendCount(int[] sendCount) {
		this.sendCount = sendCount;
	}
	/**
	 * Returns the value of the field called 'receiveCount'.
	 * @return Returns the receiveCount.
	 */
	public int[] getReceiveCount() {
		return this.receiveCount;
	}
	/**
	 * Sets the field called 'receiveCount' to the given value.
	 * @param receiveCount The receiveCount to set.
	 */
	public void setReceiveCount(int[] receiveCount) {
		this.receiveCount = receiveCount;
	}
	/**
	 * Returns the value of the field called 'lLR'.
	 * @return Returns the lLR.
	 */
	public int[] getLLR() {
		return this.LLR;
	}
	/**
	 * Sets the field called 'lLR' to the given value.
	 * @param lLR The lLR to set.
	 */
	public void setLLR(int[] lLR) {
		this.LLR = lLR;
	}
	/**
	 * Returns the value of the field called 'lLS'.
	 * @return Returns the lLS.
	 */
	public int[] getLLS() {
		return this.LLS;
	}
	/**
	 * Sets the field called 'lLS' to the given value.
	 * @param lLS The lLS to set.
	 */
	public void setLLS(int[] lLS) {
		this.LLS = lLS;
	}
	/**
	 * Returns the value of the field called 'fLS'.
	 * @return Returns the fLS.
	 */
	public int[] getFLS() {
		return this.FLS;
	}
	/**
	 * Sets the field called 'fLS' to the given value.
	 * @param fLS The fLS to set.
	 */
	public void setFLS(int[] fLS) {
		this.FLS = fLS;
	}
	public int getCheckpointInitiatorId() {
		return checkpointInitiatorId;
	}
	public void setCheckpointInitiatorId(int checkpointInitiatorId) {
		this.checkpointInitiatorId = checkpointInitiatorId;
	}
	public List<Messages> getMessageReceived() {
		return messageReceived;
	}
	public void setMessageReceived(List<Messages> messageReceived) {
		this.messageReceived = messageReceived;
	}
	public List<Messages> getMessageSend() {
		return messageSend;
	}
	public void setMessageSend(List<Messages> messageSend) {
		this.messageSend = messageSend;
	}
	
}
