package ContinuousHEXQ;

/**
 *
 * @author bernhardhengst
 * 
 * of the for s,a --> r,s'
 */
public class Transition {
    float [] state = {0f,0f};
    int action;
    float reward;
    float [] nextState = {0f,0f};
    
    public Transition(float [] s, int a, float r, float [] t){
        System.arraycopy(s, 0, this.state, 0, Param.dimState);
        this.action = a;
        this.reward = r;
        System.arraycopy(t, 0, this.nextState, 0, Param.dimState);
    }
    
    public void equals(Transition c) {
        System.arraycopy(c.state, 0, this.state, 0, Param.dimState);
        this.action = c.action;
        this.reward = c.reward;
        System.arraycopy(c.nextState, 0, this.nextState, 0, Param.dimState);
    }
    
    public void print() {
        System.out.println(""+state[0]+", "+state[1]);
//        System.out.println("state     = "+state.value[0]+", "+state.value[1]);
//        System.out.println("action    = "+action);
//        System.out.println("reward    = "+reward);
//        System.out.println("nextState = "+nextState.value[0]+", "+nextState.value[1]);
//        System.out.println(" ");
    }
}
