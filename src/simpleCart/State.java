package simpleCart;

/**
 *
 * @author bernhardhengst
 */
public class State {
    private int dimension = 2;
    public float value [] = new float[dimension];
    
    State() {
        for (int i = 0; i<dimension; i++){
            value[i] = Float.NaN;
        }
    }
    
    State(float value[]) {
        this.value = (float[])value.clone();
    }
    
    public int getDimension() {
        return dimension;
    }   
}
