package nodes;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.*;

import controlP5.ControlP5;
import controlP5.ControllerGroup;

import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;

import org.reflections.Reflections;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

/**
 *
 * @author kdbanman
 */
public class Graph implements Iterable<GraphElement<?>> {

    UnProjector proj;
    ControlP5 cp5;
    Nodes pApp;
    
    ControllerGroup<?> graphElementGroup;
    
    private static final String FONTRESOURCE = "resources/labelFont.ttf";
    private static final int NODESIZE = 9;
    private static final int EDGESIZE = 3;
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
    private HashMap<Integer, PFont> fonts;
    //shouldn't be more than single instance of Graph but just in-case
    private static final Reflections reflections = new Reflections("");

    private final LinkedList<Modifier> modifiers = new LinkedList<Modifier>();
    private final LinkedList<ModifierSet> modifiersets = new LinkedList<ModifierSet>();;

    Graph(UnProjector u, Nodes p) {
        proj = u;
        pApp = p;
        
        cp5 = new ControlP5(p)
                .setMoveable(false);
        cp5.disableShortcuts();
        cp5.setAutoDraw(false); // leave draw to be called by parent(Nodes) for layering
        
        graphElementGroup = new DepthSortedGroup(cp5, "GraphElementGroup")
                .open();
        
        selection = Selection.getInstance();

        triples = ModelFactory.createDefaultModel();

        nodeCount = 0;
        edgeCount = 0;

        initPositionSparsity = 10e7f;

        adjacent = new HashMap<>();
        edges = new HashSet<>();
        fonts = new HashMap<>();

        loadModifiers();
        loadModifierSets();

        System.out.println("Total Modifiers: " + modifiers.size());
        System.out.println("Total ModifierSets: " + modifiersets.size());
    }

    public PriorityQueue<GraphElement<?>> getDistanceSortedGraphElements() {

        @SuppressWarnings("rawtypes") //cp5 is mistakenly returning a rawtype
		Collection<GraphElement> elements = cp5.getAll(GraphElement.class);

        PriorityQueue<GraphElement<?>> sorted = new PriorityQueue<GraphElement<?>>(100, new Comparator<GraphElement<?>>() {
            PVector referencePoint = pApp.getCamPosition();
            @Override
            public int compare(GraphElement<?> e1, GraphElement<?> e2) {
                if (e1 == e2) return 0;
                float e1Dist = referencePoint.dist(e1.getPosition());
                float e2Dist = referencePoint.dist(e2.getPosition());
                // order is swapped to prioritize furthest elements
                return Float.compare(e2Dist, e1Dist);
            }
        });
        
        for (GraphElement<?> e : elements) {
            sorted.offer(e);
        }
        return sorted;
    }

    /**
     * invoke single iteration of iterative force-directed layout algorithm.
     */
    public void autoLayoutIteration() {
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

    /**
     * adds triples to model, adding Nodes and Edges as necessary.
     */
    public void addTriples(Model toAdd) {
        // protect from concurrency issues during import
        pApp.waitForNewFrame(this);
        // triples to be added are now the most recently added triples

        allPreviouslyAddedTriples = toAdd;
        
        // add yet undiscovered namespace prefixes to the model
        triples.withDefaultMappings(toAdd);
        
        // add each triple in the model to the graph
        StmtIterator it = toAdd.listStatements();
        if (it.hasNext()) {
            
            while (it.hasNext()) {
                Statement s = it.nextStatement();
                addTriple(s);
            }
        } else {
            Nodes.println("Empty query result - no triples to add.");
        }
        
        // concurrency danger over
        pApp.restartRendering(this);
    }
    
    /**
     * adds triples to model, adding Nodes and Edges as necessary.
     * outputs feedback to event log.
     */
    public void addTriplesLogged(Model toAdd) {
        int retrievedSize = (int) toAdd.size();
                int beforeSize = tripleCount();
                
                // add the retriveed model to the graph (toAdd is empty if 
                // an error was encountered)
                addTriples(toAdd);
                
                int addedSize = tripleCount() - beforeSize;
                
                // log number of triples added to user
                pApp.logEvent(retrievedSize + " triples retrieved\n  " +
                         addedSize + " triples are new");
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
    
    /**
     * returns namespace prefixed version (short form) of uri.
     * Example: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" --> "rdf:type"
     * 
     * @return namespace prefixed version of uri. 
     */
    public String prefixed(String fullUri) {
        return triples.shortForm(fullUri);
    }
    /**
     * returns the fully qualified uri from a prefixed version.
     * Example: "rdf:type" --> "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
     * 
     * @param prefixed
     * @return expanded form.
     */
    public String expanded(String prefixedUri) {
        return triples.expandPrefix(prefixedUri);
    }
    /**
     * returns the prefix corresponding to a uri.
     * Example: "http://www.w3.org/1999/02/22-rdf-syntax-ns" --> "rdf"
     */
    public String prefix(String uri) {
        return triples.getNsURIPrefix(uri);
    }
    /**
     * returns the uri corresponding to a prefix.
     * Example: "rdf" --> "http://www.w3.org/1999/02/22-rdf-syntax-ns"
     */
    public String prefixURI(String prefix) {
        return triples.getNsPrefixURI(prefix);
    }

    /**
     * Creates a copy of the passed Statement with the Graph's model "triples",
     * adds the copy to "triples", adds the copy to the correct Edge (either by
     * adding it to an existing edge or creating a new one), and returns the copy.
     * 
     * @param triple Statement to add to the Graph
     * @return Statement originating from the Graph's model
     */ 
    public Statement addTriple(Statement triple) {
        String sub = triple.getSubject().toString();
        String obj = triple.getObject().toString();

        Edge e;
        
        // addNode just returns the existing Node if a new one need not be created
        addNode(sub);
        addNode(obj);

        // addEdge returns the existing edge if one already exists between the two nodes
        //      note:  node order does not matter.
        e = addEdge(sub, obj);
        
        // create the triple using the Graph's model so that Statement.getModel()
        // works as expected
        Statement tripleToAdd = triples.createStatement(triple.getSubject(),
                                                        triple.getPredicate(),
                                                        triple.getObject());
        // add the created triple to the model
        triples.add(tripleToAdd);
        // associate the triple with the edge
        e.addTriple(tripleToAdd);
        return tripleToAdd;
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
    @SuppressWarnings("unchecked")
    public ArrayList<Node> getNbrs(Node n) {
        return (ArrayList<Node>) adjacent.get(n).clone();
    }

    /**
     * Given two nodes this will return the common neighbours that they share
     * @param a node
     * @param b node
     * @return list of common neighbours
     */
    public ArrayList<Node> getCommonNbrs(Node a, Node b) {

        if(a == null || b == null)
            return (ArrayList<Node>) Collections.<Node>emptyList();

        ArrayList<Node> rtn = getNbrs(a);

        rtn.retainAll(getNbrs(b));

        return rtn;
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
                    .setSize(NODESIZE)
                    .setGroup(graphElementGroup);

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

            Edge e = new Edge(this, s + "|" + d, src, dst)
                    .setSize(EDGESIZE)
                    .setGroup(graphElementGroup);

            adjacent.get(src).add(dst);
            adjacent.get(dst).add(src);
            
            edges.add(e);
            
            edgeCount += 1;

            return e;
        }

    }

    @SuppressWarnings("unused")
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
            for (Statement stmt : e.getTriples()) {
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
        return removeEdge(e.getSourceNode(), e.getDestinationNode());
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
	 * Retrieves a list of the loaded modifiers
	 * List is ready-only, any write operations will throw UnsupportedOperationException.
	 * @return Collection of modifiers
	 * @throws Exception on failure to initiate a modifier
	 */
	public Collection<Modifier> getModifiersList() throws Exception {
		return Collections.unmodifiableList(modifiers);
	}

	/**
	 * Retrieves a list of the loaded modifiersets
	 * List is ready-only, any write operations will throw UnsupportedOperationException.
	 * @return Collection of modifiersset
	 * @throws Exception on failure to initiate a modifierset
	 */
	public Collection<ModifierSet> getModifierSetsList() throws Exception {
		return Collections.unmodifiableList(modifiersets);
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
     * To avoid duplication of font object creations this method will return a font object for a specific
     * size or create a new font element if it doesn't exists
     * @param size size of font > 0
     * @return font element NULL if invalid size
     */
    public PFont getFontBySize(int size) {

        if(size <= 0)
            return null;

        if(fonts.containsKey(size))
           return fonts.get(size);

        PFont font = cp5.getFont().getFont();

        try {
            font = pApp.createFont(FONTRESOURCE, size);
        } catch(Exception e) {
            System.out.println("ERROR: font not loaded.  ensure " + FONTRESOURCE + " is in program directory.");
        }

        return font;
    }

    /**
     * iterates through all nodes then all edges
     */
    public class GraphIterator implements Iterator<GraphElement<?>> {
        Iterator<Node> itNodes;
        Iterator<Edge> itEdges;
        
        public GraphIterator() {
            itNodes = getNodes().iterator();
            itEdges = getEdges().iterator();
        }
        
        @Override
        public boolean hasNext() {
            return itNodes.hasNext() || itEdges.hasNext();
        }
        
        @Override
        public GraphElement<?> next() {
            GraphElement<?> ret = null;
            if (itNodes.hasNext()) ret = (GraphElement<?>) itNodes.next();
            else if (itEdges.hasNext()) ret = (GraphElement<?>) itEdges.next();
            else throw new NoSuchElementException();
            
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

	public void draw() {
		cp5.draw();
	}

    /**
     * DepthSortedTab is meant to hold GraphElements only. The point of it is to
     * override the drawControllers() method so that the order in which the
     * elements are rendered can be controlled.  It renders whether or not it
     * is open (see ControllerGroup.drawControllers() for context).
     */
    private final class DepthSortedGroup extends ControllerGroup<DepthSortedGroup> {
        public DepthSortedGroup(ControlP5 theControlP5, String theName) {
            super(theControlP5, theName);
        }
        
        @Override
        public void drawControllers(PApplet theApplet) {
            // draw graph in order of depth
            PriorityQueue<GraphElement<?>> elementQueue = getDistanceSortedGraphElements();
            while (!elementQueue.isEmpty()) {
                GraphElement<?> currentElement = elementQueue.poll();
                currentElement.updateInternalEvents(pApp);
                currentElement.draw(pApp);
            }
        }
    }

	/**
     * Loads and creates instances of all modifier classes that have been loaded in the current JVM using reflections
     */
	private void loadModifiers() {
		//reflections magic
		Set<Class<? extends Modifier>> subTypes = reflections.getSubTypesOf(Modifier.class);

		for (Class<? extends Modifier> TClass : subTypes) {
			//ignore "private" modifiers such as those sub-classed in a ModifierSet that get loaded separately 
			if (java.lang.reflect.Modifier.isPrivate(TClass.getModifiers()))
				continue;

			try {
				modifiers.add(TClass.getDeclaredConstructor(this.getClass()).newInstance(this));
				System.out.println("Initiated: " + TClass.getName());
			} catch (InstantiationException | IllegalAccessException
			        | IllegalArgumentException | InvocationTargetException
			        | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				System.err.println("ERROR: unable to construct " + TClass.getName() + " modifier");
			}
		}

		// let's sort them
		Collections.sort(modifiers, new Comparator<Modifier>() {
			@Override
			public int compare(Modifier m1, Modifier m2) {
				return Integer.compare(m1.getTitle().length(), m2.getTitle().length());
			}
		});
	}

	/**
     *  Loads and creates instances of all modifiersets classes that have been loaded in the current JVM using reflections
     */
	private void loadModifierSets() {
		Set<Class<? extends ModifierSet>> subTypes = reflections.getSubTypesOf(ModifierSet.class);

		for (Class<? extends ModifierSet> TClass : subTypes) {
			//Private ModifierSet?
			if (java.lang.reflect.Modifier.isPrivate(TClass.getModifiers()))
				continue;

			try {
				modifiersets.add(TClass.getDeclaredConstructor(this.getClass()).newInstance(this));
				System.out.println("Initiated: " + TClass.getName());
			} catch (InstantiationException | IllegalAccessException
			        | IllegalArgumentException | InvocationTargetException
			        | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				System.err.println("ERROR: unable to construct " + TClass.getName() + " modifierset");
			}
		}
	}
}
