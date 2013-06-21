/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.Controller;
import controlP5.Textarea;
import processing.core.PApplet;

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
    
    Textarea labelBox;
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
        displayLabel();
    }

    @Override
    protected void onLeave() {
        currentCol = defaultCol;
        hideLabel();
    }

    @Override
    protected void mouseReleasedOutside() {
        currentCol = defaultCol;
        hideLabel();
    }
    
    public boolean selected() {
        return selection.contains(this);
    }
    
    public void displayLabel() {
        displayLabel = true;
    }
    
    public void hideLabel() {
        displayLabel = false;
    }
    
    public void setLabelSize(int s) {
        labelSize = s;
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
