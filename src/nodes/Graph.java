package nodes;

import java.util.HashMap;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.*;

import controlP5.ControlP5;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import processing.core.PVector;

/**
 *
 * @author kdbanman
 *
 * NOTE: consider making (most) of the private methods public, making sure that
 * consistency is maintained within them.
 */
public class Graph implements Iterable<GraphElement> {

    UnProjector proj;
    ControlP5 cp5;
    Nodes pApp;
    
    private Selection selection;
    
    private Model triples;
    private Model allPreviouslyAddedTriples;
    
    private int nodeCount;
    private int edgeCount;
    
    private float initPositionSparsity;
    
    // adjacent maps node ids (uris and literal values) to lists of node ids
    // NOTE: it's formally redundant to include a set of edges along with
    //       an adjacency list, but it's definitely convenient
    
    private HashMap<Node, ArrayList<Node>> adjacent;
    private HashSet<Edge> edges;

    Graph(UnProjector u, ControlP5 c, Nodes p) {
        proj = u;
        cp5 = c;
        pApp = p;
        
        selection = new Selection();

        triples = ModelFactory.createDefaultModel();

        nodeCount = 0;
        edgeCount = 0;

        initPositionSparsity = 10e7f;

        adjacent = new HashMap<>();
        edges = new HashSet<>();
    }

    /**
     * iterative force-directed layout algorithm.  one layout() call is one iteration.
     */
    public void layout() {
        // initialize map of changes in node positions
        HashMap<Node, PVector> deltas = new HashMap<>();
        for (Node node : adjacent.keySet()) {
            deltas.put(node, new PVector(0, 0, 0));
        }

        // repulsive force between nodes
        float coulomb = 15000;
        // attractive force between connected nodes
        float hooke = .1f;
        // gravitational force to camera center
        float gravity = .5f;
        // base of damping logarithm.  higher => less jitter, slower stabilization
        float saturationLogBase = 10f;
        
        // calculate position delta for each node
        for (Node n : adjacent.keySet()) {
            PVector delta = deltas.get(n);
            PVector nodePos = n.getPosition();

            // add attractive force movement from neighbors
            for (Node nbr : adjacent.get(n)) {
                PVector diff = nbr.getPosition().get();
                diff.sub(nodePos);
                float dist = diff.mag();
                diff.normalize();
                diff.mult(hooke * Nodes.sq(dist));
                delta.add(diff);
            }

            // add repulsive force movement from all other nodes
            for (Node other : adjacent.keySet()) {
                if (!other.equals(n)) {
                    float degreeScale = (float) (getDegree(other) * getDegree(other));

                    PVector diff = other.getPosition().get();
                    diff.sub(nodePos);

                    float dist = diff.mag();

                    diff.normalize();
                    diff.mult(degreeScale * coulomb / (dist * dist));
                    delta.sub(diff);
                }
            }
            
            // add gravitational force from camera centre
            PVector diff = new PVector(0, 0, 0);
            diff.sub(nodePos);
            float dist = diff.mag();
            diff.normalize();
            diff.mult(gravity * dist);
            delta.add(diff);
        }

        // apply damping and set new positions
        for (Node node : adjacent.keySet()) {
            //apply saturation damping to delta
            PVector delta = deltas.get(node);
            float mag = delta.mag();
            delta.limit(Nodes.log(mag) / Nodes.log(saturationLogBase));

            // apply delta to position
            PVector pos = node.getPosition();
            pos.add(delta);
        }
    }

    /*
     * adds triples to model, adding Nodes and Edges as necessary
     */
    public void addTriples(Model toAdd) {
        // add to model first so that prefixes are used in the GraphElements
        allPreviouslyAddedTriples = toAdd;
        triples.add(toAdd);
        
        int prevMapSize = triples.getNsPrefixMap().size();
        int toAddMapSize = toAdd.getNsPrefixMap().size();
        
        triples.withDefaultMappings(toAdd);
        int newMapSize = triples.getNsPrefixMap().size();
        
        // if the prefix map for the Model triples changes or defines a
        // prefix for uris that already exist in the model (with the old or no
        // prefix), then uniqueness is broken. the assertion of sizes here
        // protects from changes, but not from definitions.
        if (newMapSize != prevMapSize + toAddMapSize) {
            // TODO: this "error" is thrown too often to be real.  debug this
            //System.out.println("ERROR:  prefix overwritten");
        }
        
        StmtIterator it = toAdd.listStatements();
        if (it.hasNext()) {
            // stop rendering because of transient ugliness and for possible concurrency issues
            cp5.setAutoDraw(false);
            
            while (it.hasNext()) {
                Statement s = it.nextStatement();
                addTriple(s);
            }
            // begin rendering again
            cp5.setAutoDraw(true);
        } else {
            Nodes.println("Empty query result - no triples to add.");
        }
    }
    
    public Model getRenderedTriples() {
        return triples;
    }
    
    public Model getAllPreviouslyAddedTriples() {
        return allPreviouslyAddedTriples;
    }
    
    public Selection getSelection() {
        return selection;
    }
    
    public Resource getResource(String uri) {
        return triples.getResource(uri);
    }
    
    public String prefixed(String uri) {
        return triples.shortForm(uri);
    }
    public String expanded(String prefixed) {
        return triples.expandPrefix(prefixed);
    }
    public String prefix(String uri) {
        return triples.getNsURIPrefix(uri);
    }
    public String prefixURI(String uri) {
        return triples.getNsPrefixURI(uri);
    }

    public Edge addTriple(Statement triple) {
        String sub = triple.getSubject().toString();
        String obj = triple.getObject().toString();

        Edge e;
        
        // addNode just returns the existing Node if a new one need not be created
        addNode(sub);
        addNode(obj);

        // addEdge returns the existing edge if one already exists between the two nodes
        //      note:  node order does not matter.
        e = addEdge(sub, obj);
        e.addTriple(triple);
        return e;
    }

    public Set<Node> getNodes() {
        return adjacent.keySet();
    }

    public Set<Edge> getEdges() {
        return edges;
    }
    
    public int nodeCount() {
        return nodeCount;
    }
    
    public int edgeCount() {
        return edgeCount;
    }
    
    public int tripleCount() {
        long size = triples.size();
        
        //DEBUG:  jena models return their size as doubles...  never forget. ugh
        //System.out.println(size + "\n" + (int) size);
        
        return (int) size;
    }
    
    @Override
    public GraphIterator iterator() {
        return new GraphIterator();
    }
    
    /**
     * returns null if node nonexistent
     */
    public ArrayList<Node> getNbrs(String id) {
        return adjacent.get((Node) cp5.getController(id));
    }

    /**
     * returns null if node nonexistent
     */
    public ArrayList<Node> getNbrs(Node n) {
        return adjacent.get(n);
    }

    /**
     * return node's degree for view graph, not for the relational graph
     */
    public int getDegree(String id) {
        return getNbrs(id).size();
    }

    public int getDegree(Node n) {
        return getNbrs(n).size();
    }

    /*
     * to be called by addTriple. affects cp5, nodeCount, and adjacent iff the
     * node is new. a new entry in adjacent will map to an empty ArrayList since
     * no edges may exist yet.
     *
     * returns the new node or the existing node.
     */
    private Node addNode(String id) {

        // ControlP5's source has been checked, this should be reliable and fast
        Node n = (Node) cp5.getController(id);
        if (n != null) {
            return n;
        } else {

            // set random initial position within reasonable boundary.
            // (cube root for volume)
            float initBoundary = Nodes.pow((float) nodeCount, 0.333f)
                    * initPositionSparsity;
            initBoundary = Nodes.min(initBoundary, 300);

            n = new Node(this, id)
                    .setPosition(pApp.random(-initBoundary, initBoundary),
                    pApp.random(-initBoundary, initBoundary),
                    pApp.random(-initBoundary, initBoundary))
                    .setSize(10);

            adjacent.put(n, new ArrayList<Node>());

            nodeCount += 1;

            return n;
        }
    }

    /**
     * to be called by addTriple, both nodes must exist. affects cp5, edgeCount,
     * and adjacent iff the edge is new.
     *
     * returns the new edge or the existing edge.
     */
    private Edge addEdge(String s, String d) {

        Node src = (Node) cp5.getController(s);
        Node dst = (Node) cp5.getController(d);

        // make sure the nodes exist
        if (src == null || dst == null) {
            printNullEdgeTargets(s, d, src, dst);
        }

        if (adjacent.get(src).contains(dst)) {
            return getEdge(s, d);
        } else {

            Edge e = new Edge(this, s + "|" + d, src, dst).setSize(5);

            adjacent.get(src).add(dst);
            adjacent.get(dst).add(src);
            
            edges.add(e);
            
            edgeCount += 1;

            return e;
        }

    }

    private Edge addEdge(Node s, Node d) {
        return addEdge(s.getName(), d.getName());
    }

    /**
     * returns true if successful, false otherwise. removes all connected edges.
     */
    public boolean removeNode(String id) {

        Node n = (Node) cp5.getController(id);

        if (n == null) {
            System.out.println("ERROR: Cannot remove nonexistent node\n" + id);
            return false;
        }
        
        // removing all connected edges will leave this node as a singleton,
        // so removeEdge will remove the node on its last call.
        // a copy of the adjacency list is iterated through so that the original
        // may be modified during iteration (within removeEdge() call).
        // NOTE:  the nbr Nodes are copied, then the copies are used to call the 
        //        removal method.  this only works because the removeEdge(Node, Node)
        //        method is just a wrapper for removeEdge(String, String), which
        //        uses String names, not object references.
        boolean success = false;
        ArrayList<Node> adjCopy = new ArrayList<>(getNbrs(n));
        for (Node nbr : adjCopy) {
            success = removeEdge(n, nbr);
        }
        return success;
    }

    public boolean removeNode(Node n) {
        return removeNode(n.getName());
    }

    /**
     * returns true if successful, false otherwise. succeeds iff given src and
     * dst nodes exist and edge exists between them.
     */
    public boolean removeEdge(String s, String d) {

        Node src = (Node) cp5.getController(s);
        Node dst = (Node) cp5.getController(d);

        if (src == null || dst == null) {
            printNullEdgeTargets(s, d, src, dst);
            return false;
        } else if (!adjacent.get(src).contains(dst)) {
            Nodes.println("ERROR: Cannot remove nonexistent edge between:\n" + s + "\n" + d);
            return false;
        } else {
            Edge e = getEdge(s, d);
            
            // remove edge from selection
            selection.remove(e);
            // remove triples from the model
            for (Statement stmt : e.triples) {
                triples.remove(stmt);
            }

            // remove controller
            edges.remove(e);
            e.remove();
            
            // adjust adjacency list and size
            adjacent.get(src).remove(dst);
            adjacent.get(dst).remove(src);

            edgeCount -= 1;
            
            // test if src or dst are singleton, remove if so
            if (adjacent.get(src).isEmpty()) {
                adjacent.remove(src);
                selection.remove(src);
                selection.removeFromBuffer(src);
                src.remove();
            }
            if (adjacent.get(dst).isEmpty()) {
                adjacent.remove(dst);
                selection.remove(dst);
                selection.removeFromBuffer(dst);
                dst.remove();
            }
            
            return true;
        }
    }

    public boolean removeEdge(Node s, Node d) {
        return removeEdge(s.getName(), d.getName());
    }

    public boolean removeEdge(Edge e) {
        return removeEdge(e.src, e.dst);
    }
    
    /**
     * 
     * @param n string id of node to be retrieved
     * @return Node referred to by string parameter.  Returns null if the passed
     * uri identifies either an Edge or nothing at all.
     */
    public Node getNode(String n) {
        Node ret;
        try {
            ret = (Node) cp5.getController(n);
        } catch (ClassCastException e) {
            ret = null;
        }
        return ret;
    }

    /**
     * returns the existing edge between s and d. order of s and d are actually
     * irrelevant, the correct edge will be retrieved.  returns null if there is
     * no edge between the passed node ids, or if one of the passed node ids 
     * doesn't actually identify a node.
     * @param s id of source node
     * @param d id of destination node
     * @return Edge between nodes, or null if nonexistent
     */
    public Edge getEdge(String s, String d) {
        Edge e = (Edge) cp5.getController(s + "|" + d);

        if (e == null) {
            e = (Edge) cp5.getController(d + "|" + s);
        }

        if (e == null) {
            Nodes.println("Edge connecting\n" + s + "\nand\n" + d + "\nnot found.");
        }
        return e;
    }
    public Edge getEdge(Node s, Node d) {
        return getEdge(s.getName(), d.getName());
    }

    /**
     * prints error message for edge creation between nonexistent nodes.
     */
    private void printNullEdgeTargets(String s, String d, Node src, Node dst) {
        Nodes.println("ERROR: Edge cannot be retrieved between \n\t"
                + s + "\nand \nt" + d);
        String problem = (src == null) ? s : d;
        Nodes.println("\n" + problem + "\n  doesn't exist as Node.");
    }
    
    /**
     * iterates through all nodes then all edges
     */
    public class GraphIterator implements Iterator<GraphElement> {
        Iterator itNodes;
        Iterator itEdges;
        
        public GraphIterator() {
            itNodes = getNodes().iterator();
            itEdges = getEdges().iterator();
        }
        
        @Override
        public boolean hasNext() {
            return itNodes.hasNext() || itEdges.hasNext();
        }
        
        @Override
        public GraphElement next() {
            GraphElement ret = null;
            if (itNodes.hasNext()) ret = (GraphElement) itNodes.next();
            else if (itEdges.hasNext()) ret = (GraphElement) itEdges.next();
            else throw new NoSuchElementException();
            
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
