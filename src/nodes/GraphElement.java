/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.Controller;
import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author kdbanman
 */
public class GraphElement<T> extends Controller<T> {

    int hoverCol;
    int selectCol;
    int defaultCol;
    
    // currentCol responds to mouse hover
    int currentCol;
    
    float size;
    
    String labelText;
    boolean displayLabel;
    float labelSize;
    
    Graph graph;
    UnProjector proj;
    PApplet pApp;
    Selection selection;

    //public GraphElement(ControlP5 cp5, String name, UnProjector unProj, PApplet pApplet) {
    public GraphElement(Graph parentGraph, String name) {
        super(parentGraph.cp5, name);
        
        graph = parentGraph;
        proj = parentGraph.proj;
        pApp = parentGraph.pApp;
        selection = parentGraph.selection;
        
        hoverCol = 0xFF5FEA6D;
        selectCol = 0xFFEA5F84;
    
        defaultCol = 0xFF1A4969;
    
        currentCol = defaultCol;
        
        size = 10;
        
        labelText = "GraphElement default";
        displayLabel = false;
        labelSize = 10;
    }
    
    public void setColor(int col) {
      defaultCol = col;
    }

    public T setSize(final int s) {
        size = s;
        return setSize(s, s);
    }

    @Override
    protected void onEnter() {
        currentCol = hoverCol;
    }

    @Override
    protected void onLeave() {
        currentCol = defaultCol;
    }

    @Override
    protected void mouseReleasedOutside() {
        currentCol = defaultCol;
    }
    
    public boolean selected() {
        return selection.contains(this);
    }
    
    public void setDisplayLabel(boolean setVal) {
        displayLabel = setVal;
    }
    
    public void setLabelSize(int s) {
        labelSize = s;
    }
    
    public void displayLabel() {
            // NOTE: the rotate calls are differing for each element
            // get screen axes
        /*
            proj.calculatePickPoints(pApp.width / 2, 0);
            PVector screenVert = proj.ptStartPos.get();
            
            proj.calculatePickPoints(pApp.width, pApp.height / 2);
            PVector screenHoriz = proj.ptStartPos.get();
            
            proj.calculatePickPoints(pApp.width / 2, pApp.height / 2);
            PVector screenIn = proj.ptEndPos.get();
            
            screenVert.sub(proj.ptStartPos);
            screenHoriz.sub(proj.ptStartPos);
            screenIn.sub(proj.ptStartPos);
            
            // get angles from default to axes
            PVector defVert = new PVector(0,100,0);
            PVector defHoriz = new PVector(100,0,0);
            PVector defIn = new PVector(0,0,100);
            
            //DEBUG
            // red is horiz
            pApp.stroke(0xFFFF0000);
            pApp.line(0,0,0, 100,0,0);
            // vert is blue
            pApp.stroke(0xFF0000FF);
            pApp.line(0,0,0, 0,100,0);
            // in is white
            pApp.stroke(0xFFFFFFFF);
            pApp.line(0,0,0, 0,0,100);
            pApp.noStroke();
            
            float toScreenVert = PVector.angleBetween(screenVert, new PVector(0,100,0));
            float toScreenHoriz = PVector.angleBetween(screenHoriz, new PVector(100, 0, 0));
            float toScreenIn = PVector.angleBetween(screenIn, new PVector(0, 0, 100));
            
            // rotate to align with screen orthogonally
            pApp.pushMatrix();
            pApp.rotateY(toScreenVert);
            pApp.rotateX(toScreenHoriz);
            pApp.rotateZ(toScreenIn);
        
            pApp.textSize(labelSize);
            
            // translate() already called within display() function
            pApp.text(labelText, 0,0,0);
            
            pApp.popMatrix();
            * */
        proj.calculatePickPoints(pApp.width / 2, pApp.height / 2);
        PVector screenIn = proj.ptEndPos.get();
        screenIn.sub(proj.ptStartPos);

        PVector up = new PVector(0,1,0);

        PVector axis = screenIn.cross(up);

        float angle = PVector.angleBetween(screenIn, up);

        pApp.pushMatrix();
        pApp.rotate(-1*angle, axis.x, axis.y, axis.z);

        pApp.textSize(labelSize);

        // translate() already called within display() function
        pApp.text(labelText, 0,0,0);

        pApp.popMatrix();
    }

    @Override
    public boolean equals(Object e) {
        if (!(e instanceof GraphElement)) {
            return false;
        }

        return getName().equals(((GraphElement) e).getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
