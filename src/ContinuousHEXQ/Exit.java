package ContinuousHEXQ;

/**
 *
 * @author bernhardhengst
 */
public class Exit{
    float [] exitState = {0f,0f};
    int exitAction;
    
    public Exit(float [] s, int action){
        System.arraycopy(s, 0, this.exitState, 0, Param.dimState);
        this.exitAction = action;
    }
    
}
