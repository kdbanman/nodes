/*
 * 
 */
package nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * listenable, iterable, buffered set of GraphElements in which Nodes and Edges
 * are separately manipulable.  elements can be added to the selection without
 * buffering them first, and elements in the buffer are treated as "selected."
 * the buffer is a mechanism to allow arbitrary rectangle group selection
 * without affecting what has already been selected.
 * @author kdbanman
 */
public class Selection implements Iterable<GraphElement> {
    private final Set<Node> nodes;
    private final Set<Edge> edges;
    
    private final Set<Node> nodeBuffer;
    private final Set<Edge> edgeBuffer;
    
    ArrayList<SelectionListener> listeners;
    
    Selection() {
        nodes = Collections.synchronizedSet(new HashSet<Node>());
        edges = Collections.synchronizedSet(new HashSet<Edge>());
        
        nodeBuffer = Collections.synchronizedSet(new HashSet<Node>());
        edgeBuffer = Collections.synchronizedSet(new HashSet<Edge>());
        
        listeners = new ArrayList<>();
    }
    
    /**
     * @return integer color of selected element(s). returns 0 (black) if selection is
     * empty or heterogeneous in color
     */
    public int getColor() {
        int color;
        int black = 0xFF000000;
        
        SelectionIterator it = this.iterator();
        
        // get color of first element, return black if empty
        if (it.hasNext()) color = it.next().getCol();
        else return black;
        
        // verify that all other selected elements are the same color, return
        // black otherwise
        while (it.hasNext()) {
            int nextCol = it.next().getCol();
            if (nextCol != color) return black;
        }
        
        return color;
    }
    
    /**
     * @return float size of selected element(s). returns 0 if selection is
     * empty or heterogeneous in size
     */
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
    
    /**
     * @return integer label size of selected element(s). returns 0 if selection is
     * empty or heterogeneous in label size
     */
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
    
    /**
     * @return selected status of Node
     */
    public boolean contains(Node n) {
        return nodes.contains(n) || nodeBuffer.contains(n);
    }
    /**
     * @return selected status of Edge
     */
    public boolean contains(Edge e) {
        return edges.contains(e) || edgeBuffer.contains(e);
    }
    /**
     * @return selected status of GraphElement
     */
    public boolean contains(GraphElement e) {
        boolean ret = false;
        if (e instanceof Node) ret = contains((Node) e);
        else if (e instanceof Edge) ret = contains((Edge) e);
        
        return ret;
    }
    
    /**
     * @return integer number of currently selected nodes
     */
    public int nodeCount() {
        return nodes.size() + nodeBuffer.size();
    }
    
    /**
     * @return integer number of currently selected edges
     */
    public int edgeCount() {
        return edges.size() + edgeBuffer.size();
    }
    
    /**
     * @return boolean true if no edges or nodes selected
     */
    public boolean empty() {
        return nodeCount() == 0 && edgeCount() == 0;
    }
    
    /**
     * @return set of currently selected nodes
     */
    public Set<Node> getNodes() {
        return nodes;
    }
    
    /**
     * @return set of currently selected edges
     */
    public Set<Edge> getEdges() {
        return edges;
    }
    
    /**
     * empties current selection of nodes
     */
    public void clearNodes() {
        synchronized (nodes) {
            nodes.clear();
        }
        broadcastChange();
    }
    
    /**
     * empties current selection of edges
     */
    public void clearEdges() {
        synchronized (edges) {
            edges.clear();
        }
        broadcastChange();
    }
    
    public void add(Node n) {
        nodes.add(n);
        broadcastChange();
    }
    public void add(Edge e) {
        edges.add(e);
        broadcastChange();
    }
    public void add(GraphElement e) {
        if (e instanceof Edge) add((Edge ) e);
        else if (e instanceof Node) add((Node) e);
    }
    
    public void remove(Node n) {
        nodes.remove(n);
        broadcastChange();
    }
    public void remove(Edge e) {
        edges.remove(e);
        broadcastChange();
    }
    public void remove(GraphElement e) {
        if (e instanceof Edge) remove((Edge) e);
        else if (e instanceof Node) remove((Node) e);
    }
    
    /**
     * if passed GraphElement is selected, deselect it.  otherwise, select it.
     */
    public void invert(GraphElement e) {
        if (contains(e)) {
            remove(e);
        } else {
            add(e);
        }
        broadcastChange();
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
        broadcastChange();
    }
    
    /*
     * Buffer operations
     */
    
    public void addToBuffer(Node n) {
        if (!nodes.contains(n)) {
            nodeBuffer.add(n);
        }
        broadcastChange();
    }
    public void addToBuffer(Edge e) {
        if (!edges.contains(e)) {
            edgeBuffer.add(e);
        }
        broadcastChange();
    }
    public void addToBuffer(GraphElement e) {
        if (e instanceof Edge) addToBuffer((Edge ) e);
        else if (e instanceof Node) addToBuffer((Node) e);
    }
    
    public void removeFromBuffer(Node n) {
        nodeBuffer.remove(n);
        broadcastChange();
    }
    public void removeFromBuffer(Edge e) {
        edgeBuffer.remove(e);
        broadcastChange();
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
    
    /*
     * listener operations and interface
     */
    public void addListener(SelectionListener l) {
        listeners.add(l);
    }
    
    public void removeListener(SelectionListener l) {
        try {
            listeners.remove(l);
        } catch (Exception e) {
            System.err.println("Cannot remove unregistered SelectionListener:");
            System.err.println(l);
            e.printStackTrace();
        }
    }
    
    private void broadcastChange() {
        for (SelectionListener l : listeners) {
            l.selectionChanged();
        }
    }
    
    public interface SelectionListener {
        public void selectionChanged();
    }
    
    /*
     * iterator operations and class
     */
    
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
            broadcastChange();
        }
    }
}
