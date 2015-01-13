//package Koo_Toueg_Protocol;

import java.io.Serializable;

public class Messages implements Serializable {
	private Types_of_Messages  messageType;
	private int senderId;
	private int destinationId;
	private int instanceInitiatorId;
	private String content;
	private VectorClock clock;
	private int parentId;
	private int llrValue;
	private int LLS;
	
	/**
	 * Returns the value of the field called 'lLS'.
	 * @return Returns the lLS.
	 */
	public int getLLS() {
		return this.LLS;
	}

	/**
	 * Sets the field called 'lLS' to the given value.
	 * @param lLS The lLS to set.
	 */
	public void setLLS(int lLS) {
		this.LLS = lLS;
	}

	/**
	 * Returns the value of the field called 'llrValue'.
	 * @return Returns the llrValue.
	 */
	public int getLlrValue() {
		return this.llrValue;
	}

	/**
	 * Sets the field called 'llrValue' to the given value.
	 * @param llrValue The llrValue to set.
	 */
	public void setLlrValue(int llrValue) {
		this.llrValue = llrValue;
	}

	/**
	 * Returns the value of the field called 'parentId'.
	 * @return Returns the parentId.
	 */
	public int getParentId() {
		return this.parentId;
	}

	/**
	 * Sets the field called 'parentId' to the given value.
	 * @param parentId The parentId to set.
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public Messages(int senderId,String content){
		this.senderId=senderId;
		this.content=content;
	}
	
	public Messages(Types_of_Messages messageType, int senderId, int destinationId,
			int instanceInitiatorId, String content,int parentId) {
		
		this.messageType = messageType;
		this.senderId = senderId;
		this.destinationId = destinationId;
		this.instanceInitiatorId = instanceInitiatorId;
		this.content = content;
		this.parentId = parentId;
	}
	public Types_of_Messages getMessageType() {
		return messageType;
	}
	public void setMessageType(Types_of_Messages messageType) {
		this.messageType = messageType;
	}
	public int getSenderId() {
		return senderId;
	}
	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}
	public int getDestinationId() {
		return destinationId;
	}
	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}
	public int getInstanceInitiatorId() {
		return instanceInitiatorId;
	}
	public void setInstanceInitiatorId(int instanceInitiatorId) {
		this.instanceInitiatorId = instanceInitiatorId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getLabel() {
		return clock.getClockByProcessID(senderId);
	}
	public void setClock(VectorClock clock) {
		this.clock = clock;
	}
        public VectorClock getClock() {
            return clock;
        }

}
