/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ContinuousHEXQ;

/**
 *
 * @author bernhardhengst
 */
public class ExitPolicies {
    int pExits = 0;
    Exit exits [] = new Exit[Param.maxExits];

    public ExitPolicies() {
        float [] s = {0f,0f};
        for (int i=0;i<Param.maxExits;i++){
            exits[i] = new Exit(s,-1);
        }        
    }
    
    public void addExit(float [] state, int action){
        System.arraycopy(state, 0, exits[pExits].exitState, 0, Param.dimState);
        exits[pExits].exitAction = action;
        pExits++;
        System.out.println("Number of exits increased to "+pExits);
    }
    
    
    
}
