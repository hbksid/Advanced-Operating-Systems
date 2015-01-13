package Projects;

import java.io.*;

/**
 *
 * @author Rusty
 */
public class MaekawaTimestamp 
        implements Serializable, Comparable<MaekawaTimestamp> {
    private final Integer processID;
    private int clock;
    
    public MaekawaTimestamp(int processID, int clock) {
        this.processID = processID;
        this.clock = clock;
    }
    
    public int getID() {
        return processID;
    }
    
    public int getClock() {
        return clock;
    }
    
    public void setClock(int clock) {
        this.clock = clock;
    }
    
    @Override
    public int compareTo(MaekawaTimestamp timestamp) {
        int clockDelta = clock - timestamp.getClock();
        return clockDelta == 0 ? processID - timestamp.getID() : clockDelta;
    }
    
    public MaekawaTimestamp copy() {
        return new MaekawaTimestamp(processID, clock);
    }
}
