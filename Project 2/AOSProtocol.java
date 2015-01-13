package Projects;

import java.net.*;

/**
 * Abstract AOS Protocol object
 * @author Rusty
 */
public interface AOSProtocol {
    // Contains run method to start the protocol
    void run();
    
    // Receive method for receiving new messages from a ServerSocket
    Object serverReceive(InetAddress inetAddress, Object input);
    
    // Method to terminate protocol which may cause it to end prematurely
    void terminate();
}
