package ContinuousHEXQ;

/**
 *
 * @author bernhardhengst
 * 
 * Set of transitions from a state for all actions
 */
public class Tset{
    public float [] state = {0f,0f};
    public float delta []; //coarsity of representation
    public float resolution [] = {0.9f, 0.9f}; // minimum coarsity 
    public int action [];
    public float reward [];
    public float [][] nextStates;
    public float q [];
    public boolean isExit;
    
    Tset(){ 
        action = new int[Param.nActions];
        reward = new float[Param.nActions];
        nextStates = new float[Param.nActions][Param.dimState];
        delta = new float[Param.dimState]; //coarsity of representation
        q = new float[Param.nActions];       
        for(int i=0;i<Param.nActions;i++){
            action[i] = -1;
            reward[i] = 0f;
            System.arraycopy(state, 0, nextStates[i], 0, Param.dimState);
            q [i] = 0f;
        }
        for(int i=0;i<Param.dimState;i++) delta[i] = Float.MAX_VALUE;
        isExit = false;
    } 
    
    public void updateDelta(){ // revise local coarsity
        for(int d=0;d<Param.dimState;d++){
            float del = Float.MAX_VALUE;
            for(int a=0;a<Param.nActions;a++){
                if(action[a]==-1) continue;
                float deltaS = Math.abs(state[d]-nextStates[a][d]);
                if(deltaS<del) del = deltaS;
            }
            if(del<resolution[d])  delta[d] = resolution[d];
            else delta[d] = del;
            //System.out.println("dim "+d+" delta = "+delta[d]);
        }
    }
}
