/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.ControlP5;
import controlP5.Controller;
import processing.core.PApplet;

/**
 *
 * @author kdbanman
 */
public class GraphElement<T> extends Controller<T> {

    int hoverCol;
    int selectCol;
    
    // defaultCol responds to transformations and selection
    int defaultCol;
    int selectTmp;
    
    // currentCol responds to mouse hover
    int currentCol;
    
    boolean selected;
    
    float size;
    
    UnProjector proj;
    PApplet pApp;

    public GraphElement(ControlP5 cp5, String name, UnProjector unProj, PApplet pApplet) {
        super(cp5, name);
        
        hoverCol = 0xFF5FEA6D;
        selectCol = 0xFFEA5F84;
    
        defaultCol = 0xFF1A4969;
        selectTmp = defaultCol;
    
        currentCol = defaultCol;
    
        selected = false;
        
        size = 10;

        proj = unProj;
        pApp = pApplet;
    }
    
    public void setSelected(boolean s) {
        // selection overrides default/transformed color
        if (s && !selected) {
            selectTmp = defaultCol;
            currentCol = selectCol;
            defaultCol = selectCol;
        } else if (!s && selected) {
            defaultCol = selectTmp;
            currentCol = defaultCol;
        }
        
        selected = s;
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
