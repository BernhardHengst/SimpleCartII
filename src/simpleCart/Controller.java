/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package simpleCart;

/**
 *
 * @author bernhardhengst
 */
public class Controller {
    
    private int numStates = 1000; 
    public State states [] = new State[numStates];
    public int statePt = 0;

    private int numTransitions = 3000; 
    public Transition transitions [] = new Transition[numStates];
    public int transitionPt = 0;
    
    private int lastStateId = -1;
    
    
    Controller() {
    }
    
    public void buildModel(State state, int actionId){
        int dim = state.getDimension();
        
        //find or create id for state
        int stateId = -1; //not specified 
        for(int i=0;i<statePt;i++){
            boolean match = true;
            for(int j=0;j<dim;j++) if(Math.abs(state.value[j]-states[i].value[j])>1.0f) match = false;
            if(match) {stateId = i; break;} 
        }
        if(stateId == -1) { // create new stateId
            stateId = statePt++;
            states[stateId] = new State(state.value);
            System.out.println("stateId = "+stateId
                    +" created. Value = "+state.value[0]+", "+state.value[1]);
        }
        
        //update transition function
        if(lastStateId != -1) {
            // find current transition
            int transId = -1;
            for(int i=0;i<transitionPt;i++){
                if(transitions[i].stateId == lastStateId && 
                   transitions[i].actionId == actionId) {
                    transId = i; 
                    break;
                }
            }
            if(transId == -1){
                transId = transitionPt++;
                transitions[transId] = new Transition();
                transitions[transId].actionId = actionId;
                transitions[transId].stateId = lastStateId;
                transitions[transId].nextStateId[0] = stateId;
                transitions[transId].probability[0] = 1.0f;
            }
            System.out.println("Transition from stateId "+lastStateId
                    +" actionId "+actionId+" to "+stateId);
        }
        lastStateId = stateId;
    }
    
    float nextAction(float speed) {
        if(speed>0.1f) return -1000.0f;
        if(speed<-0.1f) return 1000.0f;
        return 0f;
    }
    
}
