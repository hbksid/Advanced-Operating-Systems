package Projects;

import java.util.*;
import java.io.*;

/**
 *
 * @author Rusty
 */
public class MaekawaMessage implements Serializable {
    private final String type;
    private final MaekawaTimestamp timestamp;
    
    public MaekawaMessage(int processID, String type, int clock) {
        this(new MaekawaTimestamp(processID, clock), type);
    }
    
    public MaekawaMessage(MaekawaTimestamp timestamp, String type) {
        this.timestamp = timestamp;
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public MaekawaTimestamp getTimestamp() {
        return timestamp;
    }
}
