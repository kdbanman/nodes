/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.Controller;
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
        updateColor();
    }

    @Override
    protected void mouseReleasedOutside() {
        updateColor();
    }
    
    public void updateColor() {
        if (selection.contains(this)) {
            currentCol = selectCol;
        } else {
             currentCol = defaultCol;
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
