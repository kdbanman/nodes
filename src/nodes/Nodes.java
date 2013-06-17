/*
 * CONTROLLER INTERACTION
 * ======================
 * 
 * VIEW INTERACTION
 * ================
 * 
 * Click adds to selection.
 * Backspace + Click removes from selection.
 * Click and drag adds to selection.
 * Backspace + Click and drag removes from selection.
 */
package nodes;

import processing.core.*;

import controlP5.ControlP5;
import peasy.PeasyCam;
import processing.opengl.PGraphics3D;


/**
 * Non-controlP5 related mouse and key interaction is here:
 * - mouse selection modification
 * 
 * @author kdbanman
 */
public class Nodes extends PApplet {
    
  /*
   * Main components not written as separate class within package:
   * - Program state tree (may not be necessary if ControlP5 is reasonably queryable
   * - Local Jena model to store all incoming data
   * - Subgraph cache for data mid-manipulation
   */
    PeasyCam cam;
    ControlP5 cp5;
    UnProjector proj;
    Graph graph;
    Selection selection;
    
    // mouse information for click and drag selection
    int lastPressedX;
    int lastPressedY;
    boolean leftDragging;
    
  @Override
  public void setup() {
    int w = 400;
    int h = 300;
    size(w, h, P3D);
    
    ControlPanel panel = new ControlPanel();
    
    cam = new PeasyCam(this, 0, 0, 0, 100);
    cam.setLeftDragHandler(null);
    cam.setRightDragHandler(cam.getRotateDragHandler());
    cam.setWheelHandler(cam.getZoomWheelHandler());
    
    
    cp5 = new ControlP5(this);
    proj = new UnProjector(this);
    graph = new Graph(proj, cp5, this);
    selection = new Selection();
    
    lastPressedX = 0;
    lastPressedY = 0;
    
    // test data
    graph.addTriple("John", "knows", "Bill");
    graph.addTriple("John", "worksAt", "Facecloud");
    graph.addTriple("John", "knows", "Amy");
    graph.addTriple("Amy", "hasPet", "John");
  }

  @Override
  public void draw() {
    background(0xFFE07924);
    
    // necessary for unprojection functionality
    proj.captureViewMatrix((PGraphics3D) this.g);
    
    // pretty light
    proj.calculatePickPoints(mouseX, mouseY);
    pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
    
    if (leftDragging) {
        // draw transparent rectangle over selection area
        int minX = min(mouseX, lastPressedX);
        int minY = min(mouseY, lastPressedY);

        int maxX = max(mouseX, lastPressedX);
        int maxY = max(mouseY, lastPressedY);
         
        cam.beginHUD();
        fill(0x33333333);
        noStroke();
        rect(minX, minY, maxX-minX, maxY-minY);
        cam.endHUD();
    }
  }
  
  @Override
  public void mousePressed() {
      // called only when the mouse button is depressed, NOT while it is held
      lastPressedX = mouseX;
      lastPressedY = mouseY;
  }
  
  @Override
  public void mouseDragged() {
      // called only when the mouse is moved while a button is depressed
      if (mouseButton == LEFT) {
        leftDragging = true;
      
        // determine nodes and edges within the drag box to selection
        int minX = min(mouseX, lastPressedX);
        int minY = min(mouseY, lastPressedY);

        int maxX = max(mouseX, lastPressedX);
        int maxY = max(mouseY, lastPressedY);
        
        // refDir is the direction to the upper left corner of screen
        PVector refDir = proj.getDir(0, 0);
        
        // leftDir is the direction to left boundary of selection horizontal from refDir
        PVector leftDir = proj.getDir(minX, 0);
        float leftAngle = PVector.angleBetween(refDir, leftDir);

        // rightDir is ... right boundary ...
        PVector rightDir = proj.getDir(maxX, 0);
        float rightAngle = PVector.angleBetween(refDir, rightDir);
        
        // topDir is ... top boundary of selection vertically from refDir
        PVector topDir = proj.getDir(0, minY);
        float topAngle = PVector.angleBetween(refDir, topDir);
        
        // bottomDir is ... bottom boundary ...
        PVector bottomDir = proj.getDir(0, maxY);
        float bottomAngle = PVector.angleBetween(refDir, bottomDir);

        // test membership of each graph element
        
        
        // remove from selection if Backspace is held, add otherwise
      }
  }
  
    @Override
  public void mouseReleased() {
      leftDragging = false;
  }
  
 
   /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
      PApplet.main(new String[]{nodes.Nodes.class.getName()});
  }
}
