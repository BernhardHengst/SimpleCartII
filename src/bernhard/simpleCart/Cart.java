package bernhard.simpleCart;

import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import java.util.Random;
import models.Cart1D;
import ContinuousHEXQ.*;

// testing git version control - CartII


public class Cart extends SimpleApplication implements PhysicsTickListener {

    // Cart scene/model
    Cart1D cartModel;
    private VehicleControl cartVehicle;
    
    // Physics
    private BulletAppState bulletAppState;
    
    // Learning controls
    private static final float ACCELERATION_FORCE = 1000.0f;
    private static final float BRAKE_FORCE = 100.0f;
    private static final Vector3f JUMP_FORCE = new Vector3f(0, 3000, 0);  
    
    // Dynaic cart properties
    private float steeringValue = 0;
    private float accelerationValue = 0;
    
    // Learning parameters
    private int ticks = 0; // physics ticks at default 60Hz
    private int timeTick = 10; // periodicity at which actions can be changed ie 6Hz
    private int steps = 0; // total timeTick steps
    private float [] state = {0f,0f};
    private float reward = 0f;
    private float [] nextState = {0f,0f};
    private int act, lastAct = -1;
    Random rnd = new Random();
    Model model;
    boolean terminated = false;
    float exploration = 0.5f;
    
    // GUI Text
    private BitmapText accelText;
    private BitmapText speedText;
    private BitmapText posText;
    private BitmapText explorationText;
    
    // GUI Controls
    private boolean keyboardInput = false;
    private boolean physicsBackground = true;
    private boolean learnParameter = false;
    
    // Goal
    float goal [] = {10f, 10f};
    
    
    public static void main(String[] args) {
        Cart app = new Cart();
        app.start();
    }
    
    @Override
    public void simpleInitApp() {
        // Init
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().addTickListener(this);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        
        // Register Cart scene
        cartModel = new Cart1D(assetManager, rootNode, bulletAppState, 15f);
        cartModel.setupModel();
        cartVehicle = cartModel.getVehicleControl();
        
        // Define background colour
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

        // Setup GUI
        setupGUI();
        
        // Setup camera
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(10f);
        resetCamera(1);
        
        // Setup keys
        setupKeys();
        cartModel.setupKeys(inputManager);
        
        // use physics background knowledge
        model = new Model();
        if(physicsBackground) {
            exploration = 0f;
            buildModel();
            System.out.println("Model built in simpleInitApp()");
            model.graph.drawTransitions();
            model.solveQ(0);
            //System.out.println("Policy found in simpleInitApp()");
            model.graph.drawPolicy();
            try{Thread.sleep(10000);}catch(InterruptedException e){}
            model.graph.drawClear();
        }
    }

    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }

    private void setupGUI() {
        posText = new BitmapText(guiFont, false);
        posText.setColor(ColorRGBA.Black);
        posText.setText("Cart Position: ");
        posText.setLocalTranslation(50, posText.getLineHeight() + 10, 0);
        
        speedText = new BitmapText(guiFont, false);
        speedText.setColor(ColorRGBA.Black);
        speedText.setText("Cart Speed: ");
        speedText.setLocalTranslation(posText.getLocalTranslation().
                add(new Vector3f(0,speedText.getLineHeight(),0)));
        
        accelText = new BitmapText(guiFont, false);
        accelText.setColor(ColorRGBA.Black);
        accelText.setText("Cart Acceleration: ");
        accelText.setLocalTranslation(speedText.getLocalTranslation().
                add(new Vector3f(0,accelText.getLineHeight(),0)));
        
        explorationText = new BitmapText(guiFont, false);
        explorationText.setColor(ColorRGBA.Black);
        explorationText.setText("Exploration Ratio: ");
        explorationText.setLocalTranslation(
                new Vector3f(100 + accelText.getLineWidth(),
                        explorationText.getLineHeight()+10,
                        0));
        
        guiNode.detachAllChildren();
        guiNode.attachChild(accelText);
        guiNode.attachChild(speedText);
        guiNode.attachChild(posText);
        guiNode.attachChild(explorationText);
    }
    
    private void setupKeys() {
        inputManager.addMapping("CamPos1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("CamPos2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("CamPos3", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping("Quit", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(actionListener, "CamPos1");
        inputManager.addListener(actionListener, "CamPos2");
        inputManager.addListener(actionListener, "CamPos3");
        inputManager.addListener(actionListener, "Quit");
        inputManager.addListener(actionListener, "Lefts");
        inputManager.addListener(actionListener, "Rights");
        inputManager.addListener(actionListener, "Ups");
        inputManager.addListener(actionListener, "Downs");
        inputManager.addListener(actionListener, "Space");
        inputManager.addListener(actionListener, "Reset");
    }

    private void resetCamera(int position) {
        if (position == 1) {
            cam.setLocation(new Vector3f(45f, 20f, 0f));
            cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
        } else if (position == 2) {
            cam.setLocation(new Vector3f(0f, 45f, 0f));
            cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_X.mult(-1));
        } else if (position == 3) {
            cam.setLocation(new Vector3f(0, 1f, 10f));
            cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
        }
    }
    
    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        if(ticks%timeTick==0) { // can only change action at timeTick periods
            System.arraycopy(nextState, 0, state, 0, state.length); //state = nextState;
            //sense new next state
            float obsNextState [] = {cartVehicle.getPhysicsLocation().z, cartVehicle.getLinearVelocity().z};
            System.arraycopy(obsNextState, 0, nextState, 0, obsNextState.length);
            lastAct = act; 
            //test for goal
            reward = -1f; 
            if(goalReached(nextState)) reward = 1000f;
            //update model
            if(ticks!=0){
                Transition transition = new Transition(state, lastAct, reward, nextState);
                model.update(transition, false);
                model.graph.drawTransition(transition);
                if(!physicsBackground) model.solveQ(0);
                if(learnParameter){
                    float vt = nextState[1];
                    float v0 = state[1];
                    float t  = timeTick/60f; //seconds
                    float acceleration = (vt-v0)/t; 
                    System.out.println("Parameter = "+acceleration+" lastAct = "+lastAct);
                }
            }
            // limit region that cart can operate in and reset on reaching goal
            if(Math.abs(nextState[0])>20f || Math.abs(nextState[1])>20f){
                System.out.println("Cart out of bounds, ticks = "+ticks);
                terminated = true;
            }
            // test termination
            if(goalReached(nextState)) {
                System.out.println("Reached goal, ticks = "+ticks);
                terminated = true;
            }
            // action policy
            act = model.greedyAction(nextState);
            if(act < 0) act = rnd.nextInt(3); //if greedy action is not well founded
            if(rnd.nextDouble()<exploration) act = rnd.nextInt(3); //exploration
            //if(ticks==0) act = 1; // give cart time to settle down
        }
        ticks++;
        if(terminated) reset(); 
    }
        
    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
         if(act == 0) accelerationValue = -1000f; 
         if(act == 1) accelerationValue = 0f;
         if(act == 2) accelerationValue = 1000f;
         cartVehicle.accelerate(accelerationValue);
         steps++;
    }

    @Override
    public void simpleUpdate(float tpf) { 
        //cam.lookAt(vehicle.getPhysicsLocation(), Vector3f.UNIT_Y);
        //cam.lookAt(new Vector3f(0,-4,0), Vector3f.UNIT_Y);
        accelText.setText("Cart Acceleration: " + Math.round(accelerationValue));
        speedText.setText("Cart Speed: " + Math.round(cartVehicle.getLinearVelocity().z));
        posText.setText("Cart Position: " + Math.round(cartVehicle.getPhysicsLocation().z));
        explorationText.setText("Exploration Ratio: "+ exploration);
        cartModel.simpleUpdate();
    }

    private void reset(){
        ticks = 0;
        terminated = false;
        //float startDistance = rnd.nextFloat()*34f-17f;
        //cartModel.setStartDistance(startDistance);
        cartModel.reset();
        //model.graph.drawClear();

        
        //vehicle.setPhysicsLocation(Vector3f.ZERO);
        /*Vector3f init = new Vector3f(0f,-4.5f,rnd.nextFloat()*34f-17f);
        //Vector3f init = new Vector3f(0f,-4.5f,15f);
        cartVehicle.setPhysicsLocation(init);
        cartVehicle.setPhysicsRotation(new Matrix3f());
        cartVehicle.setLinearVelocity(Vector3f.ZERO);
        //init = new Vector3f(0f,0f,rnd.nextFloat()*10f-5f);     
        //vehicle.setLinearVelocity(init);
        //vehicle.setAngularVelocity(Vector3f.ZERO);
        cartVehicle.resetSuspension();
        model.graph.drawModel(model);*/
    }
    
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String binding, boolean isPressed, float tpf) {
            if(isPressed) keyboardInput = true; else keyboardInput = false;
            if (binding.equals("CamPos1") && !isPressed) {
                resetCamera(1);
            } else if (binding.equals("CamPos2") && !isPressed) {
                resetCamera(2);
            } else if (binding.equals("CamPos3") && !isPressed) {
                resetCamera(3);
            } else if (binding.equals("Quit") && !isPressed) {
                stop();
            } else if (binding.equals("Lefts")) {
                if (isPressed) {
                    steeringValue += .5f;
                } else {
                    steeringValue += -.5f;
                }
                cartVehicle.steer(steeringValue);
            } else if (binding.equals("Rights")) {
                if (isPressed) {
                    steeringValue += -.5f;
                } else {
                    steeringValue += .5f;
                }
                cartVehicle.steer(steeringValue);
            } else if (binding.equals("Ups")) {
                if (isPressed) {
                    accelerationValue += ACCELERATION_FORCE;
                } else {
                    accelerationValue -= ACCELERATION_FORCE;
                }
                cartVehicle.accelerate(accelerationValue);
            } else if (binding.equals("Downs")) {
                if (isPressed) {
                    accelerationValue -= ACCELERATION_FORCE;
                } else {
                    accelerationValue += ACCELERATION_FORCE;
                }
                cartVehicle.accelerate(accelerationValue);
                if (isPressed) {
                    cartVehicle.brake(BRAKE_FORCE);
                } else {
                    cartVehicle.brake(0f);
                }
            } else if (binding.equals("Space")) {
                if (isPressed) {
                    cartVehicle.applyImpulse(JUMP_FORCE, Vector3f.ZERO);
                }
            } else if (binding.equals("Reset")) {
                if (isPressed) {
                    reset(); 
                    exploration += 0.2f;
                    if(exploration>1.0f) exploration = 0f;
                    System.out.println("Exporation factor = "+exploration);
                } else {
                }        
            }
        }
    };
    
    private void buildModel(){
        boolean findParameter=true;
        boolean goalExitFound = false;
        for(float x = -25f; x<25f;x+=1.0f) for(float y = -25f; y<25f;y+=1.0f) for(int a=0;a<3;a++){
            float [] s = {x,y};
            //if(goalReached(s)) continue; 
            float dt = timeTick/60f;
            float parameter = 9.6f;
            float [] tempNextState = {x + y*dt+0.5f*parameter*(a-1)*dt*dt, y + parameter*(a-1)*dt};
            reward = -1f; 
            boolean isExit = false;
            if(!goalExitFound && Math.abs(goal[0]-tempNextState[0])<0.5f && Math.abs(goal[1]-tempNextState[1])<0.5f){
                reward = 0f;
                isExit = true;
                goalExitFound = true;
            }
            Transition transition = new Transition(s, a, reward, tempNextState);
            model.update(transition, isExit);
            //model.graph.transition(transition);
        }
    }
       
    
    private boolean goalReached(float state []){
        if(Math.abs(goal[0]-state[0])<2f && Math.abs(goal[1]-state[1])<2f) return true;
        return false;
    }
}

