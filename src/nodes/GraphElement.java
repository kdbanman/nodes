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
        // normalized camera direction vector 
        PVector screenIn = proj.getDir(pApp.width / 2, pApp.height / 2);
        screenIn.normalize();
        
        PVector screenCenter = proj.ptStartPos.get();
        
        // project reference horizontal (1,0,0) onto screen plane (near frustum)
            //The projection of a point q = (x, y, z) onto a plane given by a point p = (a, b, c) and a normal n = (d, e, f) is
            //q_proj = q - dot(q - p, n) * n
            //This calculation assumes that n is a unit vector.
        PVector horiz = new PVector(100,0,0);
        float projectionCoeff = PVector.sub(horiz, screenCenter).dot(screenIn);
        PVector projectedHoriz = PVector.sub(horiz, PVector.mult(screenIn, projectionCoeff));
        
        PVector origin = new PVector(0,0,0);
        projectionCoeff = PVector.sub(origin, screenCenter).dot(screenIn);
        PVector projectedOrigin = PVector.sub(origin, PVector.mult(screenIn, projectionCoeff));
        
        projectedHoriz.sub(screenCenter);
        projectedOrigin.sub(screenCenter);
        projectedHoriz.sub(projectedOrigin);
        
        //DEBUG
        pApp.stroke(0);
        pApp.line(0,0,0, projectedHoriz.x, projectedHoriz.y, projectedHoriz.z);
        pApp.line(0,0,0, projectedOrigin.x, projectedOrigin.y, projectedOrigin.z);
        pApp.noStroke();
        
        // get angle of rotation to keep text vertically aligned
        proj.calculatePickPoints(pApp.width, pApp.height / 2);
        PVector screenHoriz = proj.ptStartPos.get();
        screenHoriz.sub(screenCenter);
        float vertAngle = PVector.angleBetween(projectedHoriz, screenHoriz);
        //DEBUG
        pApp.println(vertAngle);
        
        // get axis and angle of rotation to align orthogonally to text rendering plane
        PVector in = new PVector(0,0,-1);
        PVector inAxis = screenIn.cross(in);
        float inAngle = PVector.angleBetween(screenIn, in);

        pApp.pushMatrix();
        // rotate to align orthogonally to text rendering plane
        pApp.rotate(-inAngle, inAxis.x, inAxis.y, inAxis.z);
        // rotate to align with text rendering plane's horizontal and vertical axes
        pApp.rotateZ(vertAngle);

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
