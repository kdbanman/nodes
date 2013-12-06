package nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * listenable, iterable, buffered set of GraphElements in which Nodes and Edges
 * are separately accessible.  elements can be added to the selection without
 * buffering them first.
 * 
 * elements in the buffer are treated as "selected" by the contains() method,
 * the nodeCount() and edgeCount() methods, the empty() methods, and the
 * SelectionListeners.  this might seem silly and specific.  it is.  here's why,
 * until the system is refactored: the buffer is a mechanism originally designed
 * to allow arbitrary rectangle group selection without affecting what has
 * already been selected, and it was used carelessly elsewhere.  hence the need
 * for a refactor.
 * 
 * however, the selection buffer is a useful concept when you need to add to the
 * selection based on the selection contents, and thus you shouldn't or can't
 * modify the selection.  if you're comfortable using this Selection's buffer
 * system, go for it.  if not, use a Set as local state and make your own.
 * either way, here's why a buffer is necessary sometimes:
 * 
 * for instance, say you want to add to the selection every node neighboring the
 * current selection.  if you iterate through each node that is currently
 * selected,  and you add each node's neighbors to the selection, one of two
 * things will happen:
 * A. your runtime environment will detect that you are modifying a collection
 *    that you are iterating through, and it will throw a big ol' concurrent
 *    modification exception
 * B. your runtime environment will not detect the concurrent modification, and
 *    you may (or may not, depending upon implementation) iterate through the
 *    neighbor(s) you added to the selection, adding their neighborhoods as well
 * 
 * neither of these are what you wanted.  you should use the buffer instead:
 * 1. clear the buffer
 * 2. iterate through all selected nodes, adding each node's neighbors to the *buffer*
 * 3. commit the buffer
 * 
 * ===============
 * REFACTOR NOTES:
 * 
 * These do consider buffered elements selected:
 * - contains()
 * - nodeCount() and edgeCount()
 * - empty()
 * 
 * - listeners
 * 
 * 
 * These don't consider buffered elements selected:
 * - getNodes() and getEdges()
 * - clearNodes() and clearEdges()
 * 
 * - clear()
 * 
 * - add() and remove()
 * 
 * - iteration
 * 
 * - getColorOfSelection()
 * - getSizeOfSelection()
 * - getLabelSizeOfSelection()
 * 
 * 
 * Undefined behaviour when used with nonempty buffer:
 * 
 * - invertSelectionOfElement()
 * ========================
 * 
 * @author kdbanman
 */
public class Selection implements Iterable<GraphElement> {
    private final Set<Node> nodes;
    private final Set<Edge> edges;
    
    private final Set<Node> nodeBuffer;
    private final Set<Edge> edgeBuffer;
    
    private final ArrayList<SelectionListener> listeners;
    
    Selection() {
        nodes = Collections.synchronizedSet(new HashSet<Node>());
        edges = Collections.synchronizedSet(new HashSet<Edge>());
        
        nodeBuffer = Collections.synchronizedSet(new HashSet<Node>());
        edgeBuffer = Collections.synchronizedSet(new HashSet<Edge>());
        
        listeners = new ArrayList<>();
    }
    
    /**
     * checks if Node has been selected or buffered.
     * 
     * @param n Node whose membership in selection and buffer is to be
     * tested.
     * @return selected or buffered status of Node
     */
    public boolean contains(Node n) {
        return nodes.contains(n) || nodeBuffer.contains(n);
    }
    /**
     * checks if Edge has been selected or buffered.
     * 
     * @param e Edge whose membership in selection and buffer is to be
     * tested.
     * @return selected or buffered status of Edge
     */
    public boolean contains(Edge e) {
        return edges.contains(e) || edgeBuffer.contains(e);
    }
    /**
     * checks if GraphElement has been selected or buffered.
     * 
     * @param e GraphElement whose membership in selection and buffer is to be
     * tested.
     * @return selected or buffered status of GraphElement
     */
    public boolean contains(GraphElement e) {
        boolean ret = false;
        if (e instanceof Node) ret = contains((Node) e);
        else if (e instanceof Edge) ret = contains((Edge) e);
        
        return ret;
    }
    
    /**
     * @return number of currently selected nodes, including buffered Nodes.
     */
    public int nodeCount() {
        return nodes.size() + nodeBuffer.size();
    }
    
    /**
     * @return number of currently selected edges, including buffered Edges.
     */
    public int edgeCount() {
        return edges.size() + edgeBuffer.size();
    }
    
    /**
     * @return true if no edges or nodes are selected or buffered.
     */
    public boolean empty() {
        return nodeCount() == 0 && edgeCount() == 0;
    }
    
    /**
     * @return set of currently selected nodes. (does not include buffered nodes)
     */
    public Set<Node> getNodes() {
        return nodes;
    }
    
    /**
     * @return set of currently selected edges. (does not include buffered edges)
     */
    public Set<Edge> getEdges() {
        return edges;
    }
    
    /**
     * empties current selection of nodes.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * does not affect buffer.
     */
    public void clearNodes() {
        synchronized (nodes) {
            nodes.clear();
        }
        broadcastChange();
    }
    
    /**
     * empties current selection of edges.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * does not affect buffer.
     * 
     */
    public void clearEdges() {
        synchronized (edges) {
            edges.clear();
        }
        broadcastChange();
    }
    
    /**
     * Add Node to selection.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * does not affect buffer.
     * 
     * @param n Node to add to selection.
     */
    public void add(Node n) {
        nodes.add(n);
        broadcastChange();
    }
    /**
     * Add Edge to selection.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * does not affect buffer.
     * 
     * @param e Edge to add to selection.
     */
    public void add(Edge e) {
        edges.add(e);
        broadcastChange();
    }
    /**
     * Add GraphElement to selection.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * does not affect buffer.
     * 
     * @param e GraphElement to add to selection.
     */
    public void add(GraphElement e) {
        if (e instanceof Edge) add((Edge ) e);
        else if (e instanceof Node) add((Node) e);
    }
    
    /**
     * Remove Node from selection.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * does not affect buffer.
     * 
     * @param n Node to remove from selection.
     */
    public void remove(Node n) {
        nodes.remove(n);
        broadcastChange();
    }
    /**
     * Remove Edge from selection.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * does not affect buffer.
     * 
     * @param e Edge to remove from selection.
     */
    public void remove(Edge e) {
        edges.remove(e);
        broadcastChange();
    }
    /**
     * Remove GraphElement from selection.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * does not affect buffer.
     * 
     * @param e GraphElement to remove from selection.
     */
    public void remove(GraphElement e) {
        if (e instanceof Edge) remove((Edge) e);
        else if (e instanceof Node) remove((Node) e);
    }
    
    /**
     * if passed GraphElement is selected, deselect it.  otherwise, select it.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * behavior undefined when used with buffer.
     * 
     * @param e GraphElement to invert selection status of
     */
    public void invertSelectionOfElement(GraphElement e) {
        if (contains(e)) {
            remove(e);
        } else {
            add(e);
        }
        broadcastChange();
    }
    
    /**
     * clears selection of nodes and edges.
     * 
     * broadcasts change to SelectionListeners.
     * 
     * does not affect buffer.
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
    
    /**
     * Add node to selection buffer.
     * 
     * broadcasts change to SelectionListeners.
     * @param n Node to buffer for selection.
     */
    public void addToBuffer(Node n) {
        if (!nodes.contains(n)) {
            nodeBuffer.add(n);
        }
        broadcastChange();
    }
    /**
     * Add edge to selection buffer.
     * 
     * broadcasts change to SelectionListeners.
     * @param e Edge to buffer for selection.
     */
    public void addToBuffer(Edge e) {
        if (!edges.contains(e)) {
            edgeBuffer.add(e);
        }
        broadcastChange();
    }
    /**
     * Add GraphElement to selection buffer
     * 
     * broadcasts change to SelectionListeners.
     * @param e GraphElement to buffer for selection.
     */
    public void addToBuffer(GraphElement e) {
        if (e instanceof Edge) addToBuffer((Edge ) e);
        else if (e instanceof Node) addToBuffer((Node) e);
    }
    
    /**
     * Remove Node from selection buffer.
     * 
     * broadcasts change to SelectionListeners.
     * @param n Node to remove from selection buffer.
     */
    public void removeFromBuffer(Node n) {
        nodeBuffer.remove(n);
        broadcastChange();
    }
    /**
     * Remove Edge from selection buffer.
     * 
     * broadcasts change to SelectionListeners.
     * @param e Edge to remove from selection buffer.
     */
    public void removeFromBuffer(Edge e) {
        edgeBuffer.remove(e);
        broadcastChange();
    }
    /**
     * Remove GraphElement from selection buffer.
     * 
     * broadcasts change to SelectionListeners.
     * @param e GraphElement to remove from selection buffer.
     */
    public void removeFromBuffer(GraphElement e) {
        if (e instanceof Edge) removeFromBuffer((Edge) e);
        else if (e instanceof Node) removeFromBuffer((Node) e);
    }
    
    /**
     *  Move all Nodes and Edges currently in the selection buffer to the actual
     * selection.  Clears the buffer.
     * 
     * broadcasts change to SelectionListeners.
     */
    public void commitBuffer() {
        synchronized(nodes) {
            nodes.addAll(nodeBuffer);
        }
        synchronized(edges) {
            edges.addAll(edgeBuffer);
        }
        broadcastChange();
        clearBuffer();
    }
    
    /** 
     * Clear all Nodes and Edges from the selection buffer.
     * 
     * broadcasts change to SelectionListeners.
     */
    public void clearBuffer() {
        synchronized (nodeBuffer) {
            nodeBuffer.clear();
        }
        synchronized (edgeBuffer) {
            edgeBuffer.clear();
        }
        broadcastChange();
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
    
    /**
     * Iterator for iterating through all selected Nodes then all selected Edges.
     * (Does *not* iterate through buffer).
     * @return 
     */
    @Override
    public Iterator<GraphElement> iterator() {
        return new SelectionIterator();
    }
    
    /**
     * iterates through all nodes then all edges
     */
    public class SelectionIterator implements Iterator<GraphElement> {
        private Iterator itNodes;
        private Iterator itEdges;
        
        private boolean iteratingThroughNodes;
        
        public SelectionIterator() {
            itNodes = getNodes().iterator();
            itEdges = getEdges().iterator();
            
            iteratingThroughNodes = true;
        }
        
        /**
         * returns true if there is a selected Node or Edge left to iterate
         * through.  (does *not* look at selection buffer)
         * @return 
         */
        @Override
        public boolean hasNext() {
            return itNodes.hasNext() || itEdges.hasNext();
        }
        
        /**
         * iterates through all selected nodes then all selected edges.  (does
         * *not* iterate through selection buffer.)
         * @return 
         */
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

        /**
         * safely remove GraphElement pointed to by iterator from selection  
         * (does *not* remove from selection buffer).
         * 
         * broadcasts change to SelectionListeners.
         */
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
    
    /////////////////////
    //color, size, and label size aggregators for ControlPanel
    /////////////////////
    
    /**
     * @return integer color of selected element(s). returns 0 (black) if selection is
     * empty or heterogeneous in color
     */
    public int getColorOfSelection() {
        int color;
        int black = 0xFF000000;
        
        Iterator<GraphElement> it = this.iterator();
        
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
    public float getSizeOfSelection() {
        // same pattern as getColor, but for size
        float size;
        Iterator<GraphElement> it = this.iterator();
        
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
    public int getLabelSizeOfSelection() {
        // same pattern as getColor, but for size
        int size;
        Iterator<GraphElement> it = this.iterator();
        
        if (it.hasNext()) size = it.next().getLabelSize();
        else return 0;
        
        while (it.hasNext()) {
            int nextSize = it.next().getLabelSize();
            if (nextSize != size) return 0;
        }
        
        return size; 
    }
}
