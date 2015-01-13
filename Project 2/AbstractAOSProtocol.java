package Projects;

/**
 * 
 * @author Rusty
 */
public abstract class AbstractAOSProtocol implements AOSProtocol {
    // Communication Engine object linked to all AOSProtocols
    protected CommunicationEngine commEngine;
    
    protected AbstractAOSProtocol() {
        // Start CommunicationEngine for communication with processes and pass 
        // this instance of the protocol so it can call the receive method 
        // to indicate messages were received.
        commEngine = new CommunicationEngine(this);
    }
    
    /**
     * Send method that merely calls client send for commEngine. Requires 
     * 2-dimensional String array holding host and port names
     * @param nodeTable
     * @param processIndex
     * @param message
     * @param noReply
     * @return 
     */
    protected Object send(String[][] nodeTable, int processIndex, 
            Object message, boolean noReply) {
        // Define the network domain name to append to the host name to get the 
        // full host name
        String networkDomainName = ".utdallas.edu";
        
        return commEngine.clientSend(nodeTable[processIndex - 1][0] + 
                networkDomainName, Integer.parseInt(
                nodeTable[processIndex - 1][1]), message, noReply);
    }
}
