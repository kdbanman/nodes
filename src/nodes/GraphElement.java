/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.Controller;
import controlP5.Textarea;
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
        //TODO:  this is fucking weird.  resetMatrix() is unpredictable so I might need
        // to depend upon the translate() calls within the display() method.  the rotation is stupid.
        if (isInside() || displayLabel) {
            pApp.pushMatrix();
            pApp.resetMatrix();
            
            pApp.stroke(0); 
            
            proj.calculatePickPoints(0, pApp.height);
            PVector screenVert = proj.ptStartPos.get();
            proj.calculatePickPoints(0, 0);
            screenVert.sub(proj.ptStartPos);
            
            float toScreenVert = PVector.angleBetween(screenVert, new PVector(0,100,0));
            
            pApp.rotate(toScreenVert, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
        
            pApp.textSize(labelSize);
            pApp.text(labelText, getPosition().x, getPosition().y, getPosition().z);
            //pApp.text(labelText, 0,0,0);
            
            pApp.noStroke();
            pApp.popMatrix();
        }
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
