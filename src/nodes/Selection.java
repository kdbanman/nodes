/*
 * Selection is currently stupid, and I'm not sure how to rectify that.  Notes:
 *   - the GraphElements have to know when they're selected for rendering purposes (color)
 *   - the selection modifier menu needs quick access to the number of selected nodes and edges, separately
 *   - the selection menue should also needs to know the selected resources themselves for rendering and for query generation
 * 
 */
package nodes;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import processing.core.PApplet;

/**
 *
 * @author kdbanman
 */
public class Selection {
    private final Set<Node> nodes;
    private final Set<Edge> edges;
    
    Selection() {
        nodes = Collections.synchronizedSet(new HashSet<Node>());
        edges = Collections.synchronizedSet(new HashSet<Edge>());
    }
    
    public int nodeCount() {
        return nodes.size();
    }
    
    public int edgeCount() {
        return edges.size();
    }
    
    public boolean contains(Node n) {
        return nodes.contains(n);
    }
    public boolean contains(Edge e) {
        return edges.contains(e);
    }
    
    public void add(Node n) {
        nodes.add(n);
        n.setSelected(true);
    }
    public void add(Edge e) {
        edges.add(e);
        e.setSelected(true);
    }
    public void add(GraphElement e) {
        if (e instanceof Edge) add((Edge ) e);
        else if (e instanceof Node) add((Node) e);
        else PApplet.println("ERROR: only Node or Edge may be added to Selection");
    }
    
    public void remove(Node n) {
        nodes.remove(n);
        n.setSelected(false);
    }
    public void remove(Edge e) {
        edges.remove(e);
        e.setSelected(false);
    }
    public void remove(GraphElement e) {
        if (e instanceof Edge) remove((Edge ) e);
        else if (e instanceof Node) remove((Node) e);
        else PApplet.println("ERROR: only Node or Edge may be removed from Selection");
    }
    
    public void clear() {
        for (Node n : nodes) {
          n.setSelected(false);
        }
        nodes.clear();
        
        for (Edge e : edges) {
          e.setSelected(false);
        }
        edges.clear();
    }
}
