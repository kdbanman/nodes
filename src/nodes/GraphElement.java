/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.Controller;
import controlP5.Pointer;
import java.util.ArrayList;

import processing.core.PFont;
import processing.core.PMatrix3D;
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
    
    float labelSize;
    PFont labelFont;
    ArrayList<String> labelText;
    String constructedLabel;
    
    boolean displayLabel;
    int labelW;
    int labelH;
    int charW;
    int charH;
    
    Graph graph;
    UnProjector proj;
    Nodes pApp;
    Selection selection;

    //public GraphElement(ControlP5 cp5, String name, UnProjector unProj, Nodes pApplet) {
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
        
        labelText = new ArrayList<>();
        constructedLabel = "";
        
        labelSize = 12;
        try {
            labelFont = pApp.createFont("labelFont.ttf", labelSize);
            charW = labelFont.getGlyph('A').width;
            charH = labelFont.getGlyph('A').height;
        } catch (Exception e) {
            System.out.println("ERROR: font not loaded.  ensure labelFont.tiff is in program directory.");
            labelFont = graph.cp5.getFont().getFont();
        }
        
        labelW = charW;
        labelH = charH;
        
        displayLabel = false;
    }
    
    public void setColor(int col) {
      defaultCol = col;
      currentCol = col;
    }

    public T setSize(final int s) {
        size = s;
        return setSize(s, s);
    }
    
    public float getSize() {
        return size;
    }

    @Override
    protected void onEnter() {
        //DEBUG
        pApp.drag = Nodes.DragBehaviour.DRAG;
        currentCol = hoverCol;
    }
    
    @Override
    protected void onDrag() {
        // get distance between pixels on near frustum
        proj.calculatePickPoints(0, 0);
        PVector origin = proj.ptStartPos.get();
        proj.calculatePickPoints(0, 1);
        float pixelDist = PVector.dist(origin, proj.ptStartPos);
        
        // get distance traversed over drag
        Pointer pointer = getPointer();
        float rawDistH = pixelDist * ((float) (pointer.x() - pointer.px()));
        float rawDistV = pixelDist * ((float) (pointer.y() - pointer.py()));
        
        // get near frustum unit vectors
        PVector horiz = proj.getScreenHoriz();
        PVector vert = proj.getScreenVert();
        
        // calculate scale to tranlate from cursor movement to element movement
        //      (camera to element distance)/(camera to cursor distance)
        proj.calculatePickPoints(pApp.mouseX, pApp.mouseY);
        PVector camPos = pApp.getCamPosition();
        float cursorDist = PVector.dist(proj.ptStartPos, camPos);
        float elementDist = PVector.dist(getPosition(), camPos);
        float scale = elementDist / cursorDist;
        
        // calculate drag vectors
        horiz.mult(scale * rawDistH);
        vert.mult(scale * rawDistV);
        
        // because shift may or may not be held at this point, this element may
        // or may not be in the selection, so it should be moved separately from
        // the selection if it is not selected.  single element movement is 
        // semantically different between nodes and edges (edge position is
        // derived), so the unselectedMove() is overriden in Edge
        unselectedMove(horiz, vert);
        
        // now that element has been moved if unselected, move the selection
        for (Node n : selection.getNodes()) {
            n.getPosition().add(horiz);
            n.getPosition().add(vert);
        }
    }

    @Override
    protected void onLeave() {
        pApp.drag = Nodes.DragBehaviour.SELECT;
        currentCol = defaultCol;
    }
    
    @Override
    protected void onRelease() {
        if (!pApp.leftDragging) selection.invert(this);
    }

    @Override
    protected void mouseReleasedOutside() {
        currentCol = defaultCol;
    }
    
    public boolean selected() {
        return selection.contains(this);
    }
    
    /**
     * for drag and drop movement that is agnostic to selection status. called
     * within onDrag, and is semantically different for Edges (hence the override)
     */
    protected void unselectedMove(PVector horiz, PVector vert) {
        if (!selected()) {
            getPosition().add(horiz);
            getPosition().add(vert);
        }
    }
    
    public void setDisplayLabel(boolean setVal) {
        displayLabel = setVal;
    }
    
    public void setLabelSize(int s) {
        labelSize = s;
        labelFont = pApp.createFont("cour.ttf", labelSize);
        charW = labelFont.getGlyph('A').width;
        charH = labelFont.getGlyph('A').height;
        
        calculateLabelDim();
    }
    
    public void calculateLabelDim() {
        labelH = charH + (labelText.size() - 1) * charH * 5 / 3;
        labelW = 0;
        for (String line : labelText) {
            labelW = Nodes.max(labelW, line.length() * charW);
        }
    }
    
    /**
     * converts labelText list of lines into a single label.
     * AFFECTS STATE: constructedLabel field
     */
    public void constructLabel() {
        constructedLabel = "";
        for (String line : labelText) {
            constructedLabel += line + "\n";
        }
    }
    
    /**
     * attempts to convert each line in labelText to a prefixed uri.
     * AFFECTS STATE: strings within Set labelText
     */
    public void prefixLabel() {
        for (String line : labelText) {
            line = graph.prefixed(line);
        }
    }
    
    /**
     * recalculates label dimensions and reconstructs the raw string for rendering.
     * attempts to prefix each line in the label.
     * 
     */
    public void updateLabel() {
        // order of the next three calls is critical
        prefixLabel();
        constructLabel();
        calculateLabelDim();
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
        
        pApp.fill(0xFF333333);
        
        pApp.pushMatrix();
        pApp.translate(0,0,size);
        pApp.rect(size - 3, -3, labelW + 6, labelH + 6, 2);
        pApp.popMatrix();

        //DEBUG
        //pApp.textSize(labelSize);
        pApp.fill(0xFF999999);
        pApp.textFont(labelFont);
        
        // translate() already called within display() function, so
        // text position spaced relative to GraphElement for separation/alignment
        pApp.text(constructedLabel, size, charH, size);

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
