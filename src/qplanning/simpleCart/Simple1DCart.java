/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package qplanning.simpleCart;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import models.Cart1D;

/**
 *
 * @author Tim
 */
public class Simple1DCart extends SimpleApplication {

    // Cart scene/model
    Cart1D cartModel;
    public static final float START_DISTANCE = 30;
    
    // Physics
    private BulletAppState bulletAppState;
    
    // GUI Text
    private BitmapText speedText;

    public static void main(String[] args) {
        Simple1DCart app = new Simple1DCart();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Init
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        
        // Register Cart scene
        cartModel = new Cart1D(assetManager, rootNode, bulletAppState, START_DISTANCE);
        cartModel.setupModel();
        
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
    }
    
    private void resetCamera(int position) {
        if (position == 1) {
            cam.setLocation(new Vector3f(40f, 20f, -cartModel.getStartDistance()/2));
            cam.lookAt(new Vector3f(0, 0, -cartModel.getStartDistance()/2), Vector3f.UNIT_Y);
        } else if (position == 2) {
            cam.setLocation(new Vector3f(0f, cartModel.getStartDistance()+10, -cartModel.getStartDistance()/2));
            cam.lookAt(new Vector3f(0, 0, -cartModel.getStartDistance()/2), Vector3f.UNIT_X.mult(-1));
        } else if (position == 3) {
            cam.setLocation(new Vector3f(0, 1f, 10f));
            cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
        }
    }
    
    private void setupGUI() {
        speedText = new BitmapText(guiFont, false);
        //speedText.setSize(2f);
        speedText.setColor(ColorRGBA.Black);
        speedText.setText("Cart Speed: 0");
        speedText.setLocalTranslation(50, speedText.getLineHeight() + 10, 0);
        
        guiNode.detachAllChildren();
        guiNode.attachChild(speedText);
    }
    
    private void setupKeys() {
        inputManager.addMapping("CamPos1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("CamPos2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("CamPos3", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping("Quit", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(actionListener, "CamPos1");
        inputManager.addListener(actionListener, "CamPos2");
        inputManager.addListener(actionListener, "CamPos3");
        inputManager.addListener(actionListener, "Quit");
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        speedText.setText("Cart Speed: " + cartModel.getVehicleControl().getLinearVelocity().z);
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String binding, boolean isPressed, float tpf) {
            if (binding.equals("CamPos1") && !isPressed) {
                resetCamera(1);
            } else if (binding.equals("CamPos2") && !isPressed) {
                resetCamera(2);
            } else if (binding.equals("CamPos3") && !isPressed) {
                resetCamera(3);
            } else if (binding.equals("Quit") && !isPressed) {
                stop();
            }
        }
    };
}
