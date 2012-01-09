/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;
import java.util.Random;

/**
 *
 * @author Timothy Wiley <timothyw@cse.unsw.edu.au>
 */
public class Cart1D implements Model {
    
    Random rnd = new Random();

    // Common elements
    AssetManager assetManager;
    Node rootNode;
    BulletAppState bulletAppState;
    
    // Starting distance of cart from origin
    public static final float DEFAULT_START_DIST = 30;
    private float startDistance;
    
    // Acceleration
    public static final float ACCELERATION_RATE = 2;
    private float acceleration;
    
    // Floor geometry & physics
    private Material solidFloorMat;
    private Material floorMat;
    private Material markerMat;
    private Material cartBodyMat;
    private Material cartWheelMat;
    
    // Cart
    private VehicleControl cartVehicle;
    private Node cartNode;
    
    // Starting point marker
    Geometry startMarkerGeom;
    BitmapText startText;
    
    // Lights
    private AmbientLight ambientLight;

    public Cart1D(AssetManager assetManager, Node rootNode,
            BulletAppState bulletAppState, float startDist) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.bulletAppState = bulletAppState;
        this.startDistance = startDist;
    }
    
    public Cart1D(AssetManager assetManager, Node rootNode,
            BulletAppState bulletAppState) {
        this(assetManager, rootNode, bulletAppState, DEFAULT_START_DIST);
    }
    
    public void setStartDistance(float startDistance) {
        this.startDistance = startDistance;
    }
    
    public float getStartDistance() {
        return startDistance;
    }
    
    public VehicleControl getVehicleControl() {
        return cartVehicle;
    }

    public void setupModel() {
        // Init
        initMaterials();
        initLights();

        // Setup Floor
        buildFloor();
        
        // Setup cart
        buildCart();
        
        // Set starting position of the cart
        //      Position above floor to they don't start inside each other
        //      And position along x-axis
        //      Starting speed
        reset();
    }
    
    // Allows for start marker to move in scene
    public void simpleUpdate() {
        // Conifgure start point marker
        //startMarkerGeom.move(startMarkerGeom.getLocalTranslation().mult(-1));
        //startMarkerGeom.move(0, 0, -startDistance);
        startMarkerGeom.setLocalTranslation(0, 0, -startDistance);
        startText.setLocalTranslation(-10f, 2f, startText.getLineWidth()/2 - startDistance);
    }
    
    public void reset() {
        // Configure cart position
        //Vector3f initCartPos = new Vector3f(0f, 0.4f, -startDistance);
        Vector3f initCartPos = new Vector3f(0f, 0.4f, 15f-30f*rnd.nextFloat());
        cartVehicle.setPhysicsLocation(initCartPos);
        acceleration = 0;
        Vector3f initCartVel = new Vector3f(0f,0f,15f-30f*rnd.nextFloat());
        //cartVehicle.setLinearVelocity(Vector3f.ZERO);
        cartVehicle.setLinearVelocity(initCartVel);
        cartVehicle.resetSuspension();
    }
    
    public void setupKeys(InputManager inputManager) {
        inputManager.addMapping("AccelCart", new KeyTrigger(KeyInput.KEY_EQUALS));
        inputManager.addMapping("AccelCart", new KeyTrigger(KeyInput.KEY_ADD));
        inputManager.addMapping("DecelCart", new KeyTrigger(KeyInput.KEY_MINUS));
        inputManager.addMapping("DecelCart", new KeyTrigger(KeyInput.KEY_UNDERLINE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addListener(actionListener, "AccelCart");
        inputManager.addListener(actionListener, "DecelCart");
        inputManager.addListener(actionListener, "Reset");
    }

    private void initMaterials() {
        solidFloorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        solidFloorMat.setColor("Color", new ColorRGBA(0, 0, 0, 0));
        solidFloorMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        floorMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        TextureKey roadKey = new TextureKey("Textures/Terrain/splat/road.jpg");
        roadKey.setGenerateMips(true);
        Texture roadTexture = assetManager.loadTexture(roadKey);
        roadTexture.setWrap(Texture.WrapMode.Repeat);
        floorMat.setTexture("DiffuseMap", roadTexture);
        
        markerMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        markerMat.setBoolean("UseMaterialColors", true);
        markerMat.setColor("Ambient", ColorRGBA.Black);
        markerMat.setColor("Diffuse", ColorRGBA.Black);
        
        cartBodyMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        cartBodyMat.setBoolean("UseMaterialColors", true);
        cartBodyMat.setColor("Ambient", ColorRGBA.Blue);
        cartBodyMat.setColor("Diffuse", ColorRGBA.Blue);
        
        cartWheelMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        cartWheelMat.setBoolean("UseMaterialColors", true);
        cartWheelMat.setColor("Ambient", ColorRGBA.Red);
        cartWheelMat.setColor("Diffuse", ColorRGBA.Red);
    }

    private void initLights() {
        ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.2f));
        rootNode.addLight(ambientLight);
        
        PointLight lamp_light = new PointLight();
        lamp_light.setColor(ColorRGBA.White);
        lamp_light.setRadius(100f);
        lamp_light.setPosition(new Vector3f(10f, 20f, -startDistance - 10));
        rootNode.addLight(lamp_light);
        
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(0, -1, 0).normalizeLocal());
        rootNode.addLight(sun);
    }
    
    private void buildFloor() {
        float floorWidth = 8.0f;
        
        // Solid floor
        Box solidFloorShape = new Box(160f, 0.1f, 160f);
        Geometry solidFloorGeom = new Geometry("floor", solidFloorShape);
        solidFloorGeom.setMaterial(solidFloorMat);
        solidFloorGeom.setQueueBucket(Bucket.Transparent);
        solidFloorGeom.addControl(new RigidBodyControl(0));
        rootNode.attachChild(solidFloorGeom);
        bulletAppState.getPhysicsSpace().add(solidFloorGeom);
        
        // Visible Floor
        Box floorShape = new Box(floorWidth, 0.1f, floorWidth*20);
        floorShape.scaleTextureCoordinates(new Vector2f(20f, 1f));
        Geometry floorGeom = new Geometry("floor", floorShape);
        floorGeom.setMaterial(floorMat);
        rootNode.attachChild(floorGeom);
        
        // Floor markers
        Box markerShape = new Box(Vector3f.ZERO, floorWidth, 0.2f, 0.2f);
        Geometry goalMarkerGeom = new Geometry("marker1", markerShape);
        goalMarkerGeom.setMaterial(markerMat);
        startMarkerGeom = new Geometry("marker1", markerShape);
        startMarkerGeom.setMaterial(markerMat);
        startMarkerGeom.setLocalTranslation(0, 0, -startDistance);
        rootNode.attachChild(goalMarkerGeom);
        rootNode.attachChild(startMarkerGeom);
        
        // Setup written text
        Quaternion zRotation = new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_Z);
        Quaternion yRotation = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        Quaternion textRotation = Quaternion.IDENTITY.mult(zRotation).mult(yRotation);
        BitmapText goalText = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"), false);
        goalText.setColor(ColorRGBA.Black);
        goalText.setSize(2f);
        goalText.setText("Goal");
        goalText.setLocalRotation(textRotation);
        goalText.setLocalTranslation(-10f, 2f, goalText.getLineWidth()/2);
        startText = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"), false);
        startText.setColor(ColorRGBA.Black);
        startText.setSize(2f);
        startText.setText("Start");
        startText.setLocalRotation(textRotation);
        startText.setLocalTranslation(-10f, 2f, startText.getLineWidth()/2 - startDistance);
        rootNode.attachChild(goalText);
        rootNode.attachChild(startText);
    }
    
    private void buildCart() {
        // Basic geometric properties of cart
        Vector3f cartSize = new Vector3f(1.2f, 0.5f, 2.4f); // was 1.2, 0.5, 2.4
        Vector3f cartPos = new Vector3f(0,1.2f,0); // was 0, 1, 0
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0

        // Create cart collision body and spatial.
        // Shape is set at (0,1,0) from the centre of the vehicle,
        //      also shifting the centre of mass to (0,-1,0)
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(cartSize);
        compoundShape.addChildShape(box, cartPos);
        Box cartShape = new Box(cartPos, cartSize.x, cartSize.y, cartSize.z);
        Geometry cartGeometry = new Geometry("cartBody", cartShape);
        cartGeometry.setMaterial(cartBodyMat);
        
        // Create Node for the cart vehicle object
        cartNode = new Node("vehicleNode");
        
        // Create Control from compound shape
        cartVehicle = new VehicleControl(compoundShape, 400);
        
        // Add controls and spatials to vehicle
        cartNode.addControl(cartVehicle);
        cartNode.attachChild(cartGeometry);

        // Set suspension values for the wheels, this can be a bit tricky
        //  see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 60.0f;//200=f1 car    // was 60.0
        float compValue = 0.3f; //(should be lower than damp)  was 0.3
        float dampValue = 0.4f;  // was 0.4
        cartVehicle.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        cartVehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        cartVehicle.setSuspensionStiffness(stiffness);
        cartVehicle.setMaxSuspensionForce(10000.0f);

        //Create four wheels and add them at their locations
        float radius = 0.5f;    // was 0.5
        float restLength = 0.3f;    // was 0.3
        float yOff = 0.5f;      // was 0.5
        float xOff = 1f;        // was 1
        float zOff = 2f;        // was 2

        Cylinder wheelMesh = new Cylinder(16, 16, radius, radius * 0.6f, true);

        Node node1 = new Node("wheel 1 node");
        Geometry wheels1 = new Geometry("wheel 1", wheelMesh);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(cartWheelMat);
        cartVehicle.addWheel(node1, new Vector3f(-xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2 = new Geometry("wheel 2", wheelMesh);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(cartWheelMat);
        cartVehicle.addWheel(node2, new Vector3f(xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3 = new Geometry("wheel 3", wheelMesh);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(cartWheelMat);
        cartVehicle.addWheel(node3, new Vector3f(-xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node4 = new Node("wheel 4 node");
        Geometry wheels4 = new Geometry("wheel 4", wheelMesh);
        node4.attachChild(wheels4);
        wheels4.rotate(0, FastMath.HALF_PI, 0);
        wheels4.setMaterial(cartWheelMat);
        cartVehicle.addWheel(node4, new Vector3f(xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        cartNode.attachChild(node1);
        cartNode.attachChild(node2);
        cartNode.attachChild(node3);
        cartNode.attachChild(node4);
        
        // Add vehicle to Root Node
        rootNode.attachChild(cartNode);

        // Add vehicle to Physics Space
        bulletAppState.getPhysicsSpace().add(cartVehicle);    
    }
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String binding, boolean isPressed, float tpf) {
            if (binding.equals("AccelCart") && !isPressed) {
                acceleration += ACCELERATION_RATE;
                cartVehicle.accelerate(acceleration);
                //cartVehicle.setLinearVelocity(new Vector3f(0, 0, acceleration));
            } else if (binding.equals("DecelCart") && !isPressed) {
                acceleration -= ACCELERATION_RATE;
                cartVehicle.accelerate(-acceleration);
                //cartVehicle.setLinearVelocity(new Vector3f(0, 0, acceleration));
            } else if (binding.equals("Reset") && !isPressed) {
                reset();
            }
        }
    };
}
