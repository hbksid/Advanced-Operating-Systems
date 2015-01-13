package Projects;

import java.io.File;

/**
 * CS6378 Advanced Operating Systems Project2
 * @author Rusty
 */
public class Project2 {
    
    public static void main(String[] args) {
        // Store my process index
        int processIndex = Integer.parseInt(args[0]);
        
        // Create the protocol object by passing the config file
        MutualExclusionProtocol protocol = new MaekawaProtocol(processIndex, 
                new File("/home/005/r/rt/rtr100020/CS_6378/Project2/"
                        + "config.txt"));
        
        // Start the protocol
        protocol.run();
        
        // ****Application code here*****
    }
    
}
