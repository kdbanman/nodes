/*
 * 
 */
package nodes;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author kdbanman
 */
public class Selection implements Iterable<GraphElement> {
    private final Set<Node> nodes;
    private final Set<Edge> edges;
    
    private final Set<Node> nodeBuffer;
    private final Set<Edge> edgeBuffer;
    
    Selection() {
        nodes = Collections.synchronizedSet(new HashSet<Node>());
        edges = Collections.synchronizedSet(new HashSet<Edge>());
        
        nodeBuffer = Collections.synchronizedSet(new HashSet<Node>());
        edgeBuffer = Collections.synchronizedSet(new HashSet<Edge>());
    }
    
    public int getColor() {
        int color;
        SelectionIterator it = this.iterator();
        
        // get color of first element, return black if empty
        if (it.hasNext()) color = it.next().getCol();
        else return 0;
        
        // verify that all other selected elements are the same color, return
        // black otherwise
        while (it.hasNext()) {
            int nextCol = it.next().getCol();
            if (nextCol != color) return 0;
        }
        
        return color;
    }
    
    public float getSize() {
        // same pattern as getColor, but for size
        float size;
        SelectionIterator it = this.iterator();
        
        if (it.hasNext()) size = it.next().getSize();
        else return 0;
        
        while (it.hasNext()) {
            float nextSize = it.next().getSize();
            if (nextSize != size) return 0;
        }
        
        return size;
    }
    
    public int getLabelSize() {
        // same pattern as getColor, but for size
        int size;
        SelectionIterator it = this.iterator();
        
        if (it.hasNext()) size = it.next().getLabelSize();
        else return 0;
        
        while (it.hasNext()) {
            int nextSize = it.next().getLabelSize();
            if (nextSize != size) return 0;
        }
        
        return size; 
    }
    
    public Set<Node> getNodes() {
        return nodes;
    }
    
    public void clearNodes() {
        nodes.clear();
    }
    
    public Set<Edge> getEdges() {
        return edges;
    }
    
    public void clearEdges() {
        edges.clear();
    }
    
    public int nodeCount() {
        return nodes.size() + nodeBuffer.size();
    }
    
    public int edgeCount() {
        return edges.size() + edgeBuffer.size();
    }
    
    public boolean contains(Node n) {
        return nodes.contains(n) || nodeBuffer.contains(n);
    }
    public boolean contains(Edge e) {
        return edges.contains(e) || edgeBuffer.contains(e);
    }
    public boolean contains(GraphElement e) {
        boolean ret = false;
        if (e instanceof Node) ret = contains((Node) e);
        else if (e instanceof Edge) ret = contains((Edge) e);
        
        return ret;
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
    }
    
    public void remove(Node n) {
        nodes.remove(n);
    }
    public void remove(Edge e) {
        edges.remove(e);
    }
    public void remove(GraphElement e) {
        if (e instanceof Edge) remove((Edge) e);
        else if (e instanceof Node) remove((Node) e);
    }
    
    public void invert(GraphElement e) {
        if (contains(e)) {
            remove(e);
        } else {
            add(e);
        }
    }
    
    /**
     * clears selection of nodes and edges.  does not affect buffer.
     */
    public void clear() {
        synchronized (nodes) {
            nodes.clear();
        }
        synchronized (edges) {
            edges.clear();
        }
    }
    
    /*
     * Buffer operations
     */
    
    public void addToBuffer(Node n) {
        if (!nodes.contains(n)) {
            nodeBuffer.add(n);
        }
    }
    public void addToBuffer(Edge e) {
        if (!edges.contains(e)) {
            edgeBuffer.add(e);
        }
    }
    public void addToBuffer(GraphElement e) {
        if (e instanceof Edge) addToBuffer((Edge ) e);
        else if (e instanceof Node) addToBuffer((Node) e);
    }
    
    public void removeFromBuffer(Node n) {
        nodeBuffer.remove(n);
    }
    public void removeFromBuffer(Edge e) {
        edgeBuffer.remove(e);
    }
    public void removeFromBuffer(GraphElement e) {
        if (e instanceof Edge) removeFromBuffer((Edge) e);
        else if (e instanceof Node) removeFromBuffer((Node) e);
    }
    
    public void commitBuffer() {
        synchronized(nodes) {
            nodes.addAll(nodeBuffer);
        }
        synchronized(edges) {
            edges.addAll(edgeBuffer);
        }
        clearBuffer();
    }
    
    public void clearBuffer() {
        synchronized (nodeBuffer) {
            nodeBuffer.clear();
        }
        synchronized (edgeBuffer) {
            edgeBuffer.clear();
        }
    }
    
    @Override
    public SelectionIterator iterator() {
        return new SelectionIterator();
    }
    
    /**
     * iterates through all nodes then all edges
     */
    public class SelectionIterator implements Iterator<GraphElement> {
        Iterator itNodes;
        Iterator itEdges;
        
        private boolean iteratingThroughNodes;
        
        public SelectionIterator() {
            itNodes = getNodes().iterator();
            itEdges = getEdges().iterator();
            
            iteratingThroughNodes = true;
        }
        
        @Override
        public boolean hasNext() {
            return itNodes.hasNext() || itEdges.hasNext();
        }
        
        @Override
        public GraphElement next() {
            GraphElement ret = null;
            if (itNodes.hasNext())  {
                ret = (GraphElement) itNodes.next();
            } else if (itEdges.hasNext()) {
                ret = (GraphElement) itEdges.next();
                iteratingThroughNodes = false;
            } else {
                throw new NoSuchElementException();
            }
            
            return ret;
        }

        @Override
        public void remove() {
            if (iteratingThroughNodes) {
                itNodes.remove();
            } else {
                itEdges.remove();
            }
        }
    }
}
