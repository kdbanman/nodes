/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.Controller;
import processing.core.PApplet;
import processing.core.PMatrix3D;
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
        // set transform matrix for spherical billboard
        float[] tmp = new float[16];
        pApp.getMatrix().get(tmp);
        
        for (int i=0; i < 3; i++) {
            for (int j=0; j < 3; j++) {
                tmp[i*4 + j] = i==j ? 1 : 0;
            }
        }
        PMatrix3D billboarded = new PMatrix3D();
        billboarded.set(tmp);
        
        pApp.pushMatrix();
        pApp.setMatrix(billboarded);

        pApp.textSize(labelSize);

        // translate() already called within display() function, so
        // text @ (0,0,0) + size offset for separation
        pApp.text(labelText, size,size,size);

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
