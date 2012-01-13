/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package ContinuousHEXQ;

import java.util.Arrays;


/**
 *
 * @author bernhardhengst
 * 
 * Accepts state transitions and rewards and builds a model of 
 * the transition function and the reward function
 * using radial basis-like function approximation
 */
public class Model{
    
    int nCases = 10000; //allocate maximum number of cases in memory
    int pCases = 0;     //pointer to latest case
    public Tset [] cases = new Tset[nCases];
    public ModelGraph graph;
    int numExits = 0;
    //public ExitPolicies exitPolicies = new ExitPolicies();

    
    public Model(){
        for (int i=0; i<nCases; i++) {
            cases[i] = new Tset();
        }
        graph = new ModelGraph(this);
        WindowUtilities.openInJFrame(graph, 800, 800);
    }
    
    public void update(Transition c, boolean isExit){
        //add case?
        int a = c.action;
        boolean add = true;
        for (int i=0; i<pCases; i++) {
            boolean neighbour = true; 
            for (int d=0; d<Param.dimState; d++) {
                float v1 = c.state[d];
                float v2 = cases[i].state[d];
                float delta = cases[i].delta[d];
                if(Math.abs(v1-v2)>0.9*delta) neighbour = false;
            }  
            if(neighbour) {
                add = false;
                if(cases[i].action[a]==-1){
                    for (int d=0; d<Param.dimState; d++) {
                        float s1 = cases[i].state[d];
                        float s  = c.state[d];
                        float sd = c.nextState[d];
                        cases[i].nextStates[a][d] = sd + (s1 - s);
                    }
                    cases[i].action[a] = a;
                    cases[i].reward[a] = c.reward;
                    cases[i].updateDelta();
                    //graph.go(cases[i],a);
                }
                continue;
            }             
        } 
        if(add || isExit){
            System.arraycopy(c.state, 0, cases[pCases].state, 0, Param.dimState);
            cases[pCases].action[a] = a;
            cases[pCases].reward[a] = c.reward;
            System.arraycopy(c.nextState, 0, cases[pCases].nextStates[a], 0, Param.dimState);
            cases[pCases].updateDelta();
            cases[pCases].isExit = isExit;
            //graph.go(cases[pCases],a);
            //c.print();
            pCases++;
            //System.out.println("pCases = "+pCases);
            if(isExit) System.out.println("exit "+numExits++);
            return;
        }
        
        // update neighbour transition functions
        for (int i=0; i<pCases; i++) { 
            // update s' & r for each neigbour using c
            boolean neighbour = true; 
            for (int d=0; d<Param.dimState; d++) {
                float v1 = c.state[d];
                float v2 = cases[i].state[d];
                float delta = cases[i].delta[d];
                if(Math.abs(v1-v2)>2.0f*delta) neighbour = false;
            }  
            if(neighbour && cases[i].action[a]!=-1) {            
                float aveBeta = 0f; //rbfDist(cases[i].state,c.state)*alpha;
                for (int d=0; d<Param.dimState; d++) {
                    float s1 = cases[i].state[d];
                    float s1d= cases[i].nextStates[a][d];
                    float s  = c.state[d];
                    float sd = c.nextState[d];
                    float os1d = sd + (s1 - s);
                    float spread = cases[i].delta[d];
                    float dist = Math.abs(s1-s)/spread;
                    float beta = Param.alphaTransition*(float)Math.exp(-dist*dist);
                    cases[i].nextStates[a][d] += beta*(os1d-s1d);
                    aveBeta += beta;
                }
                float r1= cases[i].reward[a];
                float or1 = c.reward;
                cases[i].reward[a] += aveBeta/(float)Param.dimState*(or1-r1);
                //System.out.println("aveBeta = "+aveBeta);
                //graph.go(cases[i],a);
                cases[i].updateDelta();
            }
        }
        //graph.transition(c);
    }
    
   public void solveQ(int exit){
        // backup Tsets by iterating through nextStates without priority
        float totChange = 0f;
        int iteration = 0;
        float reward = 0;
        float maxQ = 0;
        do{
            totChange = 0f;
            for(int i=0;i<pCases;i++){
                Tset ti = cases[i];
                for(int a=0; a<Param.nActions;a++){
                    if(ti.action[a]==-1) continue;
                    float estQneighbour [] = new float[Param.nActions];
                    float totWeight = 0f;
                    int numNeighbours = 0;
                    for(int n=0;n<pCases;n++){ //neighbours
                        Tset tn = cases[n];
                        boolean neighbour = true;
                        for (int d=0; d<Param.dimState; d++) {
                            float v1 = tn.state[d];
                            float v2 = ti.nextStates[a][d];
                            float delta = tn.delta[d];
                            if(Math.abs(v1-v2)>2.0*delta) neighbour = false;
                        }
                        if(neighbour){
                            numNeighbours++;
                            float dist = distance(tn.state, ti.nextStates[a]);
                            float weight = (float)Math.exp(-dist*dist);
                            totWeight += weight;
                            for(int ad=0;ad<Param.nActions;ad++){
                                estQneighbour[ad] += weight*tn.q[ad];
                            }           
                        }
                    }
                    float change = 0f;
                    if(ti.isExit){
                       change =  Math.abs(ti.q[a]-1000f);
                       ti.q[a] = 1000f; 
                       //System.out.println("exit found during solvQ");
                    }
                    else if(numNeighbours<1){
                        //continue;
                        change =  Math.abs(ti.q[a]+1000000f);
                        ti.q[a] = -1000000f;
                    }
                    else{
                        for(int ad=0;ad<Param.nActions;ad++) estQneighbour[ad] /= totWeight;
                        maxQ = Float.NEGATIVE_INFINITY;
                        for(int ad=0;ad<Param.nActions;ad++){
                            if(estQneighbour[ad]>maxQ) maxQ = estQneighbour[ad];
                        }
                        reward = ti.reward[a];
                        //backup value
                        change = reward + Param.gammaQ*maxQ - ti.q[a];
                        ti.q[a] += Param.alphaQ*change;
                    }
                    totChange += Math.abs(change);
                }
            }
            if(iteration%1==0) System.out.println("iteration: "+iteration+" totChange = "+totChange);
            iteration++;
        } while (totChange > 1f); 
        //print q values
        for(int i=0;i<pCases;i++){
            Tset ti = cases[i];
            for(int a=0; a<Param.nActions;a++){
                System.out.println("Q["+ti.state[0]+","+ti.state[1]+"]["+a+"] = "+ti.q[a]);
            }
        }
    }
    
    public int greedyAction(float [] s) {
        float estQneighbour [] = new float[Param.nActions];
        float totWeight = 0f;
        int numNeighbours = 0;
        for(int n=0;n<pCases;n++){ //neighbours
            Tset tn = cases[n];
            boolean neighbour = true;
            for (int d=0; d<Param.dimState; d++) {
                float v1 = tn.state[d];
                float v2 = s[d];
                float delta = tn.delta[d];
                if(Math.abs(v1-v2)>=1.99f*delta) neighbour = false;
            }
            if(neighbour){
                numNeighbours++;
                float dist = distance(tn.state, s);
                float weight = (float)Math.exp(-dist*dist);
                totWeight += weight;
                for(int ad=0;ad<Param.nActions;ad++){
                    estQneighbour[ad] += weight*tn.q[ad];
                }           
            }
        }
        if(numNeighbours<2) return -2;
        for(int ad=0;ad<Param.nActions;ad++) estQneighbour[ad] /= totWeight;
        float maxQ = Float.NEGATIVE_INFINITY;
        int action = -1;
        for(int ad=0;ad<Param.nActions;ad++){
            if(estQneighbour[ad]>maxQ) {
                maxQ = estQneighbour[ad];
                action = ad;
            }
        }
        return action;
    }
    
    
    public float distance(float [] s, float [] t) {
        float sum = 0f;
        for (int i=0;i<Param.dimState;i++) {
            float v = s[i]-t[i];
            sum+= v*v;
        }
        return (float)Math.sqrt((double)sum);
    }
    
    private void makeEqual(float [] a, float [] b){
        System.arraycopy(b, 0, a, 0, Param.dimState);
    }
    
}

//old code fragments

//    public int neighbourUnexploredAction(State s){
//        for (int i=0; i<pCases; i++) { 
//            boolean neighbour = true;
//            for (int d=0; d<s.dimension; d++) {
//                float v1 = s.value[d];
//                float v2 = cases[i].state.value[d];
//                float delta = cases[i].delta[d];
//                if(Math.abs(v1-v2)>=0.9999*delta) neighbour = false;
//            }  
//            if(neighbour) {
//                for(int a =0;a<cases[i].action.length;a++){
//                    if(cases[i].action[a]==-1){
//                        return a;
//                    }                    
//                }
//            }
//        }
//        return -1;
//    }

//  public void update(Transition c){
//        //add case?
//        int stateDim = c.state.value.length;
//        int neighbour1 = -1;
//        int neighbour2 = -1;
//        boolean add = true;
//        for (int i=0; i<pCases; i++) {    
//                if(c.action!=cases[i].action) continue;
//                boolean neighbour = true; 
//                for (int d=0; d<dim; d++) {
//                    float v1 = c.state.value[d];
//                    float v2 = cases[i].state.value[d];
//                    if(Math.abs(v1-v2)>delta) neighbour = false;
//                }
//                if (neighbour) {
//                    neighbour1 = i;
//                    add = false;
//                    break;
//                }             
//        } 
//        if(add || pCases==0){
//            cases[pCases].equals(c);
//            //System.out.println("case = "+pCases);
//            pCases++;
//            //c.print();
//            return;
//        }
//        
//        // found 1 neighbour, now find closest next one that meets criteria
//        float bestDist = Float.MAX_VALUE;
//        for (int i=0; i<pCases; i++) {
//            if(c.action!=cases[i].action) continue;
//            boolean neighbour = true;    
//            for (int d=0; d<dim; d++) {
//                float v1n1 = cases[neighbour1].state.value[d];
//                float v1 = c.state.value[d];
//                float v2 = cases[i].state.value[d];
//                if(Math.abs(v1-v2)> 2.0f*delta) neighbour = false;
//                if(Math.signum(v1n1-v1) != Math.signum(v1-v2)) neighbour = false;
//            }
//            if(neighbour){
//                float dist = distance(cases[i].state, c.state);
//                if(dist<bestDist) {
//                    bestDist = dist;
//                    neighbour2 = i;
//                }
//            }
//        }
//        
//        // update two neighbour transition functions
//        // o = observation, d = dash, s = a state dimension (d), r = reward
//        if(neighbour1!=-1 && neighbour2!=-1) {
//            float dist1 = Float.MAX_VALUE;
//            float dist2 = Float.MAX_VALUE; 
//            float alpha = 0.1f; //learning rate
//            // update s' for each neigbour using c
//            for (int d=0; d<dim; d++) {
//                float s1 = cases[neighbour1].state.value[d];
//                float s1d= cases[neighbour1].nextState.value[d];
//                float s2 = cases[neighbour2].state.value[d];
//                float s2d= cases[neighbour2].nextState.value[d];
//                float s  = c.state.value[d];
//                float sd = c.nextState.value[d];
//                float os1d = sd; // if s1 and s2 coincide with s
//                float os2d = sd; // if s1 and s2 coincide with s
//                if(s-s2 == 0f) os1d = sd + (s1 - s);
//                if(s-s1 == 0f) os2d = sd + (s2 - s);
//                if((s-s2)!=0f && (s-s1)!=0f) {
//                    os1d = sd + (s1-s)*(sd-s2d)/(s-s2);
//                    os2d = sd + (s2-s)*(sd-s1d)/(s-s1);  
//                }
//                cases[neighbour1].nextState.value[d] += alpha*(os1d-s1d);
//                cases[neighbour2].nextState.value[d] += alpha*(os2d-s2d);
//                dist1 += (sd-s1d)*(sd-s1d);
//                dist2 += (sd-s2d)*(sd-s2d);
//            }
//            dist1 = (float)Math.sqrt((double)dist1);
//            dist2 = (float)Math.sqrt((double)dist2);
//            // update r for each neighbour
//            float r = c.reward;
//            float r1= cases[neighbour1].reward;
//            float r2= cases[neighbour2].reward;
//            float or1 = r;
//            float or2 = r;
//            if(dist1!=0f && dist2!=0f) {
//                or1 = r + (r-r2)*dist1/dist2;
//                or2 = r + (r-r1)*dist2/dist1;
//            }
//            cases[neighbour1].reward += alpha*(or1-r1);
//            cases[neighbour2].reward += alpha*(or2-r2);
//        }
//        
//        // update one neighbour transition functions
//        if(neighbour1!=-1 && neighbour2==-1) {
//            float alpha = 0.1f; //learning rate
//            // update s' for each neigbour using c
//            for (int d=0; d<dim; d++) {
//                float s1 = cases[neighbour1].state.value[d];
//                float s1d= cases[neighbour1].nextState.value[d];
//                float s  = c.state.value[d];
//                float sd = c.nextState.value[d];
//                float os1d = sd;
//                if(s1!=s) os1d = sd + (s1 - s);
//                cases[neighbour1].nextState.value[d] += alpha*(os1d-s1d);
//            }
//            // update r for each neighbour
//            float r = c.reward;
//            float r1= cases[neighbour1].reward;
//            float or1 = r;
//            cases[neighbour1].reward += alpha*(or1-r1);
//        }
//        
//        // debug
//        int neighbours = 0;
//        if(neighbour1!=-1) neighbours++;
//        if(neighbour2!=-1) neighbours++;
//        if(neighbour1==3 || neighbour2==3) {
//            System.out.println("Found "+neighbours+" neighbours, one of which has case id 3");
//            System.out.println("observation");
//            c.print();
//            System.out.println("neighbour1");
//            cases[neighbour1].print();
//            if(neighbours>1){
//                System.out.println("neighbour2");
//                cases[neighbour2].print();
//            }
//        }
//        
//    }