/*
 * CONTROLLER INTERACTION
 * ======================
 * 
 * VIEW INTERACTION
 * ================
 * 
 * Click (and drag) starts selection.
 * Shift + Click (and drag) adds to selection.
 * 
 * Right Click and drag rotates the camera
 */
package nodes;

import processing.core.*;

import controlP5.ControlP5;
import peasy.PeasyCam;
import processing.opengl.PGraphics3D;

/**
 * Non-controlP5 related mouse and key interaction is in this class:
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
    ControlPanelFrame panel;
    PeasyCam cam;
    ControlP5 cp5;
    UnProjector proj;
    Graph graph;
    
    // mouse information for click and drag selection
    int lastPressedX;
    int lastPressedY;
    boolean leftDragging;

    @Override
    public void setup() {
        int w = 400;
        int h = 300;
        size(w, h, P3D);

        panel = new ControlPanelFrame();

        cam = new PeasyCam(this, 0, 0, 0, 200);
        cam.setLeftDragHandler(null);
        cam.setRightDragHandler(cam.getRotateDragHandler());
        cam.setWheelHandler(cam.getZoomWheelHandler());


        cp5 = new ControlP5(this);
        proj = new UnProjector(this);
        graph = new Graph(proj, cp5, this);
        
        
        //TODO:  add event listener to invert selection on click.
        
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
            rect(minX, minY, maxX - minX, maxY - minY);
            cam.endHUD();
        }
    }

    @Override
    public void mousePressed() {
        if (mouseButton == LEFT) {
            // called only when the mouse button is depressed, NOT while it is held
            lastPressedX = mouseX;
            lastPressedY = mouseY;

            // if shift is pressed, user is selectively adding graph elements (with the buffer
            if (!(shiftPressed())) {
                graph.selection.clear();
            }
        }
    }

    @Override
    public void mouseDragged() {
        // called only when the modispHolderuse is moved while a button is depressed
        if (mouseButton == LEFT) {
            leftDragging = true;

            // determine nodes and edges within the drag box to selection
            int minX = min(mouseX, lastPressedX);
            int minY = min(mouseY, lastPressedY);

            int maxX = max(mouseX, lastPressedX);
            int maxY = max(mouseY, lastPressedY);

            graph.selection.clearBuffer();
            for (GraphElement n : graph) {
                PVector nPos = n.getPosition();
                float nX = screenX(nPos.x, nPos.y, nPos.z);
                float nY = screenY(nPos.x, nPos.y, nPos.z);

                // test membership of graph element
                if (nX <= maxX && nX >= minX && nY <= maxY && nY >= minY) {
                    graph.selection.addToBuffer(n);
                } else {
                    graph.selection.removeFromBuffer(n);
                }
            }
        }
    }

    @Override
    public void mouseReleased() {
        leftDragging = false;
        graph.selection.commitBuffer();
    }

    public boolean shiftPressed() {
        return keyPressed && key == CODED && keyCode == SHIFT;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        PApplet.main(new String[]{nodes.Nodes.class.getName()});
    }
}
