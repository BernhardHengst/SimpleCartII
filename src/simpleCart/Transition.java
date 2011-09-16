package simpleCart;

/**
 *
 * @author bernhardhengst
 */
public class Transition {
    private int numNextStates = 3;
    public int stateId;
    public int actionId;
    public int [] nextStateId = new int[numNextStates];
    public float [] probability = new float[numNextStates];
    
    Transition() {
    } 
 
    public int getNumNextStates() {
        return numNextStates;
    }    
}
