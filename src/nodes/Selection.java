
package nodes;

import java.util.HashSet;
import processing.core.PApplet;

/**
 *
 * @author kdbanman
 */
public class Selection {
    HashSet<Node> nodes;
    HashSet<Edge> edges;
    
    Selection() {
        nodes = new HashSet<>();
        edges = new HashSet<>();
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
    }
    public void add(Edge e) {
        edges.add(e);
    }
    public void add(GraphElement e) {
        if (e instanceof Edge) add((Edge ) e);
        else if (e instanceof Node) add((Node) e);
        else PApplet.println("ERROR: only Node or Edge may be added to Selection");
    }
    
    public void remove(Node n) {
        nodes.remove(n);
    }
    public void remove(Edge e) {
        edges.remove(e);
    }
    public void remove(GraphElement e) {
        if (e instanceof Edge) remove((Edge ) e);
        else if (e instanceof Node) remove((Node) e);
        else PApplet.println("ERROR: only Node or Edge may be removed from Selection");
    }
    
    public void clear() {
        nodes.clear();
        edges.clear();
    }
}
