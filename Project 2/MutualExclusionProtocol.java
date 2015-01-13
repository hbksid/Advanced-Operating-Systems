package Projects;

/**
 *
 * @author Rusty
 */
public interface MutualExclusionProtocol extends AOSProtocol {
    
    /**
     * Method for entering critical section
     */
    void csEnter();
    
    /**
     * Method for exiting critical section
     */
    void csLeave();
}
