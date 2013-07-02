package nodes;

import com.hp.hpl.jena.graph.Triple;
import java.util.HashMap;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.*;

import controlP5.ControlP5;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import processing.core.PApplet;
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
    PApplet pApp;
    
    Selection selection;
    
    Model triples;
    
    int nodeCount;
    int edgeCount;
    
    float initPositionSparsity;
    
    // adjacent maps node ids (uris and literal values) to lists of node ids
    // NOTE: it's formally redundant to include a set of edges along with
    //       an adjacency list, but it's definitely convenient
    
    HashMap<Node, ArrayList<Node>> adjacent;
    HashSet<Edge> edges;

    Graph(UnProjector u, ControlP5 c, PApplet p) {
        proj = u;
        cp5 = c;
        pApp = p;
        
        selection = new Selection();
        
        cp5.addCallback(new SingleSelector(selection));

        triples = ModelFactory.createDefaultModel();

        nodeCount = 0;
        edgeCount = 0;

        initPositionSparsity = 10e7f;

        adjacent = new HashMap<>();
        edges = new HashSet<>();

    }

    public void layout() {
        HashMap<Node, PVector> deltas = new HashMap<>();

        float coulomb = 10000;
        float hooke = .05f;

        for (Node node : adjacent.keySet()) {
            deltas.put(node, new PVector(0, 0, 0));
        }
        for (Node n : adjacent.keySet()) {
            PVector delta = deltas.get(n);
            PVector nodePos = n.getPosition();

            for (Node nbr : adjacent.get(n)) {
                PVector diff = nbr.getPosition().get();
                diff.sub(nodePos);
                float dist = diff.mag();
                diff.normalize();
                diff.mult(hooke * PApplet.sq(dist));
                delta.add(diff);
            }

            for (Node other : adjacent.keySet()) {
                if (!other.equals(n)) {
                    float degreeScale = (float) (getDegree(other) * getDegree(other));

                    PVector diff = other.getPosition().get();
                    diff.sub(nodePos);

                    float dist = diff.mag();

                    diff.normalize();
                    diff.mult(degreeScale * coulomb / dist);
                    delta.sub(diff);
                }
            }
        }

        for (Node node : adjacent.keySet()) {
            //apply saturation damping to delta
            PVector delta = deltas.get(node);
            float mag = delta.mag();
            delta.limit(PApplet.log(mag) / PApplet.log(2));

            // apply delta to position
            PVector pos = node.getPosition();
            pos.add(delta);
            //print(deltas.get(nodeID));
        }
    }

    /*
     * adds triples to model, adding Nodes and Edges as necessary
     */
    public void addTriples(Model toAdd) {
        StmtIterator it = toAdd.listStatements();
        if (it.hasNext()) {
            while (it.hasNext()) {
                Statement s = it.nextStatement();
                addTriple(s);
            }
        } else {
            PApplet.println("Empty query result.");
        }

        triples.add(toAdd);
    }

    public Edge addTriple(String sub, String pred, String obj) {
        Edge e;

        // add*** just returns the existing *** if a new *** need not be created.  (think sets)
        addNode(sub);
        addNode(obj);

        e = addEdge(sub, obj);
        if (e.src.getName().equals(sub)) {
            e.predicates.put(pred, true);
        } else {
            e.predicates.put(pred, false);
        }
        return e;
    }

    public Edge addTriple(Statement triple) {
        String sub = triple.getSubject().toString();
        String pred = triple.getPredicate().toString();
        String obj = triple.getObject().toString();

        return addTriple(sub, pred, obj);
    }

    public Edge addTriple(Triple triple) {
        String sub = triple.getSubject().toString();
        String pred = triple.getPredicate().toString();
        String obj = triple.getObject().toString();

        return addTriple(sub, pred, obj);
    }

    public Set<Node> getNodes() {
        return adjacent.keySet();
    }

    public Set<Edge> getEdges() {
        return edges;
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

    /**
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
            float initBoundary = PApplet.pow((float) nodeCount, 0.333f)
                    * initPositionSparsity;
            initBoundary = PApplet.min(initBoundary, 100);

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
     * returns true if successful, false otherwise. succeeds iff node exists and
     * is not connected
     */
    private boolean removeNode(String id) {

        Node n = (Node) cp5.getController(id);

        if (n == null) {
            PApplet.println("ERROR: Cannot remove nonexistent node\n" + id);
            return false;
        } else if (!adjacent.get(n).isEmpty()) {
            //PApplet.println("ERROR: Cannot remove still-connected node\n" + id);
            return false;
        } else {
            //node exists and has no neighbors

            adjacent.remove(n);
            nodeCount -= 1;

            n.remove();

            return true;
        }
        
    }

    private boolean removeNode(Node n) {
        return removeNode(n.getName());
    }

    /**
     * returns true if successful, false otherwise. succeeds iff given src and
     * dst nodes exist and edge exists between them.
     */
    private boolean removeEdge(String s, String d) {

        Node src = (Node) cp5.getController(s);
        Node dst = (Node) cp5.getController(d);

        if (src == null || dst == null) {
            printNullEdgeTargets(s, d, src, dst);
            return false;
        } else if (!adjacent.get(src).contains(dst)) {
            PApplet.println("ERROR: Cannot remove nonexistent edge between:\n" + s + "\n" + d);
            return false;
        } else {
            Edge e = getEdge(s, d);
            // TODO: remove triples from model
            
            
            // remove controller
            e.remove();

            // adjust adjacency list and size
            adjacent.get(src).remove(dst);
            adjacent.get(dst).remove(src);

            edgeCount -= 1;
            
            return true;
        }
    }

    private boolean removeEdge(Node s, Node d) {
        return removeEdge(s.getName(), d.getName());
    }

    private boolean removeEdge(Edge e) {
        return removeEdge(e.src, e.dst);
    }
    
    public Node getNode(String n) {
        Node ret = (Node) cp5.getController(n);
        
        if (ret == null) {
            PApplet.println("Node " + n + " not found.");
        }
        
        return ret;
    }

    /**
     *
     * @param s id of source node
     * @param d id of destination node
     * @return Edge between nodes, or null if nonexistent
     *
     * returns the existing edge between s and d. order of s and d are actually
     * irrelevant, the correct edge will be retrieved.
     */
    public Edge getEdge(String s, String d) {
        Edge e = (Edge) cp5.getController(s + "|" + d);

        if (e == null) {
            e = (Edge) cp5.getController(d + "|" + s);
        }

        if (e == null) {
            PApplet.println("Edge connecting\n" + s + "\nand\n" + d + "\nnot found.");
        }
        return e;
    }

    /**
     * prints error message for edge creation between nonexistent nodes
     */
    private void printNullEdgeTargets(String s, String d, Node src, Node dst) {
        PApplet.println("ERROR: Edge cannot be created between /n"
                + s + " and /n" + d);
        String problem = (src == null) ? s : d;
        PApplet.println("   " + problem + "/n  doesn't exist as Node.");
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
