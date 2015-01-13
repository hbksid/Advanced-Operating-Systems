//package Koo_Toueg_Protocol;

import java.io.Serializable;

public enum Types_of_Messages implements Serializable {

	TAKE("TAKE_CHECKPOINT"),
	MAKE("MAKE_CHECKPOINT"),
	UNDO("UNDO_CHECKPOINT"),
	APPLICATION("APPLICATION_MESSAGE"),
	REPLY("REPLY_MESSAGE"),
	ROLLBACK("ROLLBACK"),
	ROLLBACK_REPLY("ROLLBACKREPLY"),
	DONE_ROLLBACK("DONE_ROLLBACK"), 
        UNDO_ROLLBACK("UNDO_ROLLBACK"),
        MAKE_REPLY("MAKE_REPLY"),
        DONE_ROLLBACK_REPLY("DONE_ROLLBACK_REPLY"),
        START("START"),
        TERMINATION("TERMINATION");
	
	private final String messageType;
	
	private Types_of_Messages(String messageType) {
		this.messageType = messageType;
	}

	public String getMessageType() {
		return messageType;
	}
}
