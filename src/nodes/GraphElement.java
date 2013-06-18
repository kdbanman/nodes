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

    int defaultCol = 0xFF1A4969;
    int hoverCol = 0xFF5FEA6D;
    int selectCol = 0xFFEA5F84;
    int currentCol = defaultCol;
    float size;
    UnProjector proj;
    PApplet pApp;

    public GraphElement(ControlP5 cp5, String name, UnProjector unProj, PApplet pApplet) {
        super(cp5, name);

        proj = unProj;
        pApp = pApplet;
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
