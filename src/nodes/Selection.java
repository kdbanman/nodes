
package nodes;

import java.util.HashSet;

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
}
