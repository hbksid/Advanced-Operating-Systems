package Projects;

import java.io.File;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 *
 * @author Rusty
 */
public class MaekawaProtocol extends AbstractAOSProtocol 
        implements MutualExclusionProtocol {
    // Store my current timestamp
    private final MaekawaTimestamp currentTimestamp; 
    // Store timestamp of my last request and timestamp of process locking me
    private MaekawaTimestamp requestTimestamp = null, lockingTimestamp = null;
    // Store process ID's of quorum members obtained from config file and store 
    // inquires that may need yields from me later
    private final Set<Integer> quorum = new HashSet<>(), 
            inquireSet = new HashSet<>();
    // Store host and process for all processes in the system. Typically 
    // obtained from config file
    protected final String[][] nodeTable = null;
    // Store pending requests - can be sorted converting to PriorityQueue
    private final Set<MaekawaTimestamp> requestSet = new HashSet<>();
    // Store the number of grants that have been received
    private int grantCount = 0;
    // Lock used to ensure received messages are handled one at a time
    private final Lock lock = new ReentrantLock();
    // Condition used to notify a cs-enter call that all grants have been 
    // received
    private final Condition grantCS = lock.newCondition();
    // Remember if I have sent at least 1 inquire message after a new grant 
    // has been sent. Also remember if I have received at least one fail 
    // message since my last request
    boolean inquireSent = false, failReceived = false;
    
   public MaekawaProtocol(int processID, File configFile) {
        currentTimestamp = new MaekawaTimestamp(processID, 0);
        readConfig(configFile);
    }
   
   private void readConfig(File configFile) {
       
   }
   
   @Override
   public final void run() {
        try {
            commEngine.openServer(Integer.parseInt(
                    nodeTable[currentTimestamp.getID() - 1][1]));
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            System.err.println("Run" + ex);
            System.exit(0);
        }
   }

    @Override
    public Object serverReceive(InetAddress inetAddress, 
            Object input) {
        // Acquire the lock
        lock.lock();
        
        try {
            // Ensure it is a MaekawaMessage
            if (input instanceof MaekawaMessage) {
                // Get the timestamp and type from the message and 
                // get the process ID and clock from timestamp in message
                MaekawaMessage message = (MaekawaMessage)input;
                String messageType = message.getType();
                MaekawaTimestamp messageTimestamp = message.getTimestamp();

                // Update clock based on timestamp in the message received
                updateClockRcv(messageTimestamp.getClock());
                
                // Actions taken based on message type
                switch (messageType) {
                    case "request":
                        // If it is a request, then first check if I am locked 
                        // to another process (sent GRANT message to it)
                        if (lockingTimestamp == null) {
                            // I am not locked, so I can send a GRANT message
                            sendGrant(messageTimestamp);
                        }
                        else {
                            // I am locked to another process so I know I will 
                            // not be able to send a grant, so I add the 
                            // requesting process to my requestSet
                            requestSet.add(messageTimestamp);
                            
                            
                            if (messageTimestamp.compareTo(lockingTimestamp) 
                                    < 0) {
                                // The requesting process has a lower timestamp 
                                // so we send an inquire message to the locking 
                                // process
                                sendInquire();
                            }
                            else {
                                // The requesting process has a higher 
                                // timestamp so we send a fail message
                                sendFail(messageTimestamp);
                            }
                        }
                            
                        break;
                    case "grant":
                        // I have been granted by a quorum member
                        // Increment grantCount
                        grantCount++;
                        
                        // If I have received all of my grants, clear the 
                        // inquireSet and notify the csEnter method by using 
                        // lock condition signal
                        if (grantCount == quorum.size()) {
                            inquireSet.clear();
                            grantCS.signalAll();
                        }
                        
                        break;
                    case "release":
                        // Release lock on current process and grant next 
                        // process in line
                        releaseAndGrant();
                        
                        break;
                    case "fail":
                        // Set flag that I have received at least one fail 
                        // message since my last request
                        failReceived = true;
                        
                        // Check inquireSet and broadcast yield messages if 
                        // non-empty
                        if (!inquireSet.isEmpty()) {
                            sendYields();
                        }
                        
                        break;
                    case "inquire":
                        // Send yield if I am not in critical section (I know 
                        // that is true if grantCount is less than quorum size) 
                        // and I have received a fail message since my last 
                        // request
                        if (grantCount < quorum.size()) {
                            // Not in critical section so I will likely be 
                            // sending a yield message either now or later so 
                            // I add to inquireSet
                            inquireSet.add(messageTimestamp.getID());
                            
                            // If fail already received, send yield now
                            if (failReceived) {
                                sendYields();
                            }
                        }
                        
                        break;
                    case "yield":
                        // Yield is almost identical to a release, except that 
                        // we also store the yielding process timestamp in 
                        // the requestQueue since it is also requesting CS. 
                        // However, we don't need to add it until after we 
                        // sort the set and get the next requesting process 
                        // since the yielding process will not take priority 
                        // over the process that forced the yield.
                        releaseAndGrant();
                        requestSet.add(messageTimestamp);
                        
                        break;
                }
            }
            else if (input instanceof TerminationMessage) {
                /* Input termination protocol here */
            }
        }
        finally {
            // Release the lock
            lock.unlock();
        }
        
        return null;
    }
    
    /** 
     * Generic method for sending a request messages
     */
    private void sendRequests() {
        // About to send a new request broadcast so reset failReceived flag 
        failReceived = false;

        // Broadcast request message to all quorum members
        broadcast(quorum, "request", currentTimestamp);

        // Store the timestamp used in the request
        requestTimestamp = currentTimestamp.copy();
    }
    
    /**
     * Generic method for sending grant message given the timestamp of the 
     * requesting process
     */
    private void sendGrant(MaekawaTimestamp timestamp) {
        // Send grant message
        sendMaekawa(timestamp.getID(), "grant", currentTimestamp, true, true);

        // Update the lockingTimestamp to the timestamp of the request message
        // from the requesting process
        lockingTimestamp = timestamp;
    }
    
    /**
     * Generic method for sending release messages
     */
    private void sendReleases() {
        // Reset grantCount
        grantCount = 0;

        // Broadcast release message to all quorum members
        broadcast(quorum, "release", currentTimestamp);
    }
    
    /**
     * Generic method for sending fail message to requesting process
     */
    private void sendFail(MaekawaTimestamp timestamp) {
        // Send fail message
        sendMaekawa(timestamp.getID(), "fail", currentTimestamp, true, true);
    }
    
    /**
     * Generic method for sending inquire message
     */
    private void sendInquire() {
        // Only send the inquire message if it has not already been sent (reset 
        // when received yield or release)
        if (!inquireSent) {
            inquireSent = true;
            sendMaekawa(lockingTimestamp.getID(), "inquire", currentTimestamp, 
                    true, true);
        }
    }
    
    /**
     * Generic method for sending yield messages to processIDs in the 
     * inquireSet and sending with a distinct timestamp
     */
    private void sendYields() {
        // Decrease my grantCount by the number of yield messages that will be 
        // sent (based on size of inquireSet)
        grantCount -= inquireSet.size();
        
        // Broadcast yield messages
        broadcast(inquireSet, "yield", requestTimestamp);
        
        // Empty inquireSet
        inquireSet.clear();
    }
    
    /**
     * Release lock I have as a quorum member on another process and send grant 
     * to next process in line if there is one
     */
    private void releaseAndGrant() {
        // Release the timestamp of the locking process (and 
        // reset my inquireSent boolean)
        lockingTimestamp = null;
        inquireSent = false;

        // Sort the requestQueue and retrieve the next 
        // requesting process timestamp
        MaekawaTimestamp nextTimestamp = sortSet(requestSet).poll();

        // If there is a next process, send grant message to it
        if (nextTimestamp != null) {
            sendGrant(nextTimestamp);
        }
    }
    
    /**
     * Update clock based on given clock by taking maximum of current clock and 
     * given clock and adding 1.
     */
    private void updateClockRcv(int clock) {
        currentTimestamp.setClock(Math.max(currentTimestamp.getClock(), clock) 
                + 1);
    }
    
    /**
     * Update clock for send messages by just incrementing by one.
     */
    private void updateClockSend() {
        currentTimestamp.setClock(currentTimestamp.getClock() + 1);
    }
    
    /**
     * Send method that creates MaekawaMessage of given type, updates clock 
     * if indicated, and sends to given process
     */
    private Object sendMaekawa(int processIndex, String type, 
            MaekawaTimestamp timestamp, boolean updateClock, boolean noReply) {
        if (updateClock) {
            updateClockSend();
        }
        return send(nodeTable, processIndex, 
                new MaekawaMessage(timestamp.copy(), type), noReply);
    }
    
    /**
     * Method for requesting critical section
     */
    @Override
    public void csEnter() {
        // Acquire the lock to ensure new messages can't be processed while 
        // we are sending out request messages. This is to preserve clock for 
        // broadcasting and allow lock condition to get notified when all 
        // grant messages have been received
        lock.lock();
        
        try {
            // Send request messages
            sendRequests();
            
            // Now wait until all grant messages have been received
            grantCS.await();
        }
        catch (InterruptedException ex) {
        }        
        finally {
            // Release the lock
            lock.unlock();
        }
    }
    
    /**
     * Method for leaving critical section
     */
    @Override
    public void csLeave() {
        // Acquire the lock to ensure new messages can't be processed while 
        // we are sending out release messages. This is to preserve clock for 
        // broadcasting.
        lock.lock();
        
        try {
            sendReleases();
        }  
        finally {
            // Release the lock
            lock.unlock();
        }
    }
    
    /**
     * Method for broadcasting messages. Clock is updated and saved then 
     * given message type is sent to all given processes
     */
    private void broadcast(Set<Integer> processes, String messageType, 
            MaekawaTimestamp timestamp) {
        // Increment the clock and use the new value for all messages
        updateClockSend();

        // Loop through and send messages to all processes in the set
        for (Integer processID: processes) {
            sendMaekawa(processID, messageType, timestamp, false, true);
        }
    }
    
    /**
     * Sort the set of timestamps in the requestQueue by creating a 
     * PriorityQueue based on the set and return it.
     */
    private PriorityQueue<MaekawaTimestamp> sortSet(Set<MaekawaTimestamp> set) 
    {
        return new PriorityQueue<>(set);
    }
    
    /**
     * Override terminate method to by initiating stop of protocol and 
     * broadcasting termination messages
     */
    @Override
    public void terminate() {
        // Broadcast termination messages
    }
}
