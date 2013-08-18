/*
 * Information panel for a (single) selected node.  Shows nothing if the
 * selection is not a single node, otherwise displays:
 * 
 * - namespace prefixes in immediate neighborhood
 * - alphabetically sorted (by predicate) list of triples leading from the node
 * - alphabetically sorted (by predicate) list of triples leading to the node
 * - a button to query for more triples (if the node is a URI resource)
 */
package nodes;

import controlP5.ControlP5;
import java.util.concurrent.atomic.AtomicBoolean;
import processing.core.PApplet;

/**
 *
 * @author kdbanman
 */
public class InfoPanel extends PApplet implements Selection.SelectionListener {
    int w, h;
    
    ControlP5 cp5;
    Graph graph;
    
    // update flag raised if the controllers have not responded to a change in
    // selection.  see selectionChanged() and draw().
    AtomicBoolean selectionUpdated;
    
    public InfoPanel(int frameWidth, int frameHeight, Graph parentGraph) {
        w = frameWidth;
        h = frameHeight;
        
        // initialize graph
        graph = parentGraph;
        
        selectionUpdated = new AtomicBoolean();
        
        // scrollable text field to render triples readable
        
        // exploration button to query the web or the database
    }
    
    @Override
    public void setup() {
        // subscribe to changes in selection.  see overridden selectionChanged()
        graph.selection.addListener(this);
        
        size(w, h);
        
        cp5 = new ControlP5(this)
                .setMoveable(false);
    }
    
    @Override
    public void draw() {
        if (selectionUpdated.getAndSet(false)) {
            
        }
    }
    
    // every time selection is changed, this is called
    @Override
    public void selectionChanged() {
        // queue controller selection update if one is not already queued
        selectionUpdated.compareAndSet(false, true);
    }
}
