/*
 * Class containing listeners for selection modification
 */
package nodes;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import controlP5.ListBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * dynamically populated menu system whose entries appear and disappear according
 * to the contents of the current selection.  there are Modifiers and ModifierSets.
 * Both are persistent.  Each of the former can have zero or one menu entries linked.
 * Each of the latter can have zero or more Modifiers within, all of which share
 * the same compatibility test (EX: one rdf:type Modifier for each such triple
 * pertaining to a selected Node, all are compatible if the selected node has *any*
 * rdf:type outgoing predicates).
 */
public class ModifierPopulator {
    
    private ArrayList<Modifier> modifiers;
    private ArrayList<ModifierSet> modifierSets;
    
    private HashMap<Integer, Modifier> runIndex;
    
    private int menuIndex;
    
    public ModifierPopulator(Graph graph) {
        
        modifiers = new ArrayList<>();
        modifierSets = new ArrayList<>();
        
        runIndex = new HashMap<>();
        
        // integer address for accessing menu item methods when clicked
        // (this value is currently never reset, just incremented.  though it
        // should take a safely long time to traverse all positive integers)
        menuIndex = 0;
        
        // construct all modifiers and modifier sets, adding them to the
        // corresponding lists
        modifiers.add(new SelectAll(graph));
        modifiers.add(new SelectNodes(graph));
        modifiers.add(new SelectEdges(graph));
        modifiers.add(new SelectNeighbors(graph));
        modifiers.add(new InvertSelection(graph));
        modifiers.add(new SelectLastAdded(graph));
        modifiers.add(new SelectCorrespondingEdges(graph));
        
        modifierSets.add(new SelectCorrespondingNode(graph));
    }
    
    /** 
     * iterate through all persistent Modifiers and ModifierSets.  cleans
     * old menu entries (that are no longer compatible with the selection)
     * and populates with (compatible) new ones
     * @param menu list of clickable menu items to be populated
     */
    public void populate(ListBox menu) {
        
        // add newly compatible modifiers, remove newly incompatible ones
        for (Modifier mod : modifiers) {
            // add the modifier to the menu and register it with runIndex
            // if it isn't already and if it's compatible with the current selection
            if (mod.isCompatible() && !runIndex.containsValue(mod)) {
                // add menu entry for modifier
                addEntry(menu, mod);
            // if the modifier is not compatible with the current selection but
            // it is still registered/in the menu, then remove it from the menu/index
            } else if (!mod.isCompatible() && runIndex.containsValue(mod)) {
                // remove menu entry for modifier
                removeEntry(menu, mod);
            }
        }
        
        // modifier sets 
        for (ModifierSet mSet : modifierSets) {
            // remove old modifiers from last populate() call
            for (Modifier mod : mSet.getModifiers()) {
                if (runIndex.containsValue(mod)) removeEntry(menu, mod);
            }
            // test compatibility with current selection
            if (mSet.isCompatible()) {
                // construct new modifiers (menu entries) based on current selection
                mSet.constructModifiers();
                // add newly constructed modifiers to menu
                for (Modifier mod : mSet.getModifiers()) {
                    addEntry(menu, mod);
                }
            }
        }
    }
    
    /**
     * runs the Modifier selection modification at the index passed.
     * @param modIdx menu index to run (corresponds to runIndex key, internally)
     */
    public void run(int modIdx) {
        try {
            runIndex.get(modIdx).modify();
        } catch (Exception e) {
            System.out.println("Error: Bad menu index passed.  idx=" + modIdx);
        }
    }
    
    /*
     * adds menu entry for Modifier passed
     */
    private void addEntry(ListBox menu, Modifier mod) {
        /*
         * TODO: check for runIndex collision (new mod for same key)
         */
        menu.addItem(mod.getTitle(), menuIndex);
        runIndex.put(menuIndex, mod);

        menuIndex++;
        if (menuIndex == Integer.MAX_VALUE) {
            // this *really* shouldn't happen, but its bugs will be
            // insidious if it does.  tests have verified bad behaviour
            // when menuIndex wraps around to MIN_VALUE
            System.out.println("ERROR: menuIndex has reached max int");
        }
    }
    
    /**
     * removes menu entry for Modifier passed
     */
    private void removeEntry(ListBox menu, Modifier mod) {
        /*
         * TODO: unchecked conditions:
         * - menu doesn't contain modifier passed
         * - runIndex doesn't contain corresponding entry
         */
        
        int toRemove = menu.getItem(mod.getTitle()).getValue();

        menu.removeItem(mod.getTitle());
        runIndex.remove(toRemove);
    }
    
    /**
     * elementary implementable for the reactive selection modification menu.
     * see documentation here as well as existing examples.
     */
    public abstract class Modifier {
        // access model, selection, element counts, and other desired properties
        // with getters.  do not tie them to local state with class fields
        // because they change.
        final Graph graph;
        
        // always construct with the graph to be observed and operated on.
        public Modifier(Graph graph) {
            this.graph = graph;
        }
                
        /** 
         * using the selection size, element counts, jena model queries, etc test
         * if the *current selection* is compatible with the modifier.
         * do not put expensive computation in this function - it is run often.
         */
        public abstract boolean isCompatible();
        
        /**
         * action phrase stating what this modifier will do to the selection.
         * this will be the menu button label.
         * do not put expensive computation in this function - it is run often.
         */
        public abstract String getTitle();
        
        /**
         * modify the selection using jena model queries and graph properties.
         * this is run once the modifier's menu entry is clicked.
         */
        public abstract void modify();
    }
    
    /**
     * set of Modifiers that share a common compatibility test.
     * see documentation here as well as existing examples.
     */
    public abstract class ModifierSet {
        final Graph graph;
        final ArrayList<Modifier> modifiers;
        
        // always construct with the graph to be observed and operated on.
        public ModifierSet(Graph graph) {
            this.graph = graph;
            modifiers = new ArrayList<>();
        }
        
        /**
         * get the list of modifiers
         */
        public ArrayList<Modifier> getModifiers() {
            return modifiers;
        }
        
        /**
         * test if the set of modifiers to be constructed are compatible.
         * do not put expensive computation in this function - it is run often.
         */
        public abstract boolean isCompatible();
        
        /**
         * construct all the modifiers.  be sure to populate the ArrayList
         * modifiers.  this is only called if isCompatible() passed.
         */
        public abstract void constructModifiers();
    }
    
    private class SelectAll extends Modifier {
        
        public SelectAll(Graph graph) {super(graph);}
        
        @Override
        public boolean isCompatible() {
            return true;
        }
        
        @Override
        public String getTitle() {
            return "Select all";
        }
        
        @Override
        public void modify() {
            for (GraphElement e : graph) {
                graph.getSelection().add(e);
            }
        }
    }
    
    private class SelectNodes extends Modifier {
        
        public SelectNodes(Graph graph) {super(graph);}
        
        @Override
        public boolean isCompatible() {
            return graph.getSelection().nodeCount() > 0 && graph.getSelection().edgeCount() > 0;
        }
        
        @Override
        public String getTitle() {
            return "Filter only nodes";
        }
        
        @Override
        public void modify() {
            graph.getSelection().clearEdges();
        }
    }
    
    private class SelectEdges extends Modifier {
        
        public SelectEdges(Graph graph) {super(graph);}
        
        @Override
        public boolean isCompatible() {
            return graph.getSelection().nodeCount() > 0 && graph.getSelection().edgeCount() > 0;
        }
        
        @Override
        public String getTitle() {
            return "Filter only edges";
        }
        
        @Override
        public void modify() {
            graph.getSelection().clearNodes();
        }
    }
    
    private class SelectNeighbors extends Modifier {
        
        public SelectNeighbors(Graph graph) {super(graph);}
        
        @Override
        public boolean isCompatible() {
            return graph.getSelection().nodeCount() > 0 || graph.getSelection().edgeCount() > 0;
        }
        
        @Override
        public String getTitle() {
            return "Add neighborhood to selection";
        }
        
        @Override
        public void modify() {
            graph.getSelection().clearBuffer();
            for (Edge e : graph.getSelection().getEdges()) {
                graph.getSelection().addToBuffer(e.dst);
                graph.getSelection().addToBuffer(e.src);
            }
            for (Node n : graph.getSelection().getNodes()) {
                for (Node nbr : graph.getNbrs(n)) {
                    graph.getSelection().addToBuffer(nbr);
                    graph.getSelection().addToBuffer(graph.getEdge(n, nbr));
                }
            }
            graph.getSelection().commitBuffer();
        }
    }
    
    private class SelectLastAdded extends Modifier {
        
        public SelectLastAdded(Graph graph) {super(graph);}
        
        @Override
        public boolean isCompatible() {
            return graph.getAllPreviouslyAddedTriples() != null && !graph.getAllPreviouslyAddedTriples().isEmpty();
        }
        
        @Override
        public String getTitle() {
            return "Select most recently added subgraph";
        }
        
        @Override
        public void modify() {
            graph.getSelection().clear();
            StmtIterator it = graph.getAllPreviouslyAddedTriples().listStatements();
            while (it.hasNext()) {
                Statement s = it.next();
                
                Node src = graph.getNode(s.getSubject().toString());
                Node dst = graph.getNode(s.getObject().toString());
                
                // since the user may have removed graph elements from the most
                // recently added subgraph, make sure we aren't adding the
                // corresponding nulls to selection
                if (src != null) graph.getSelection().add(src);
                if (dst != null) graph.getSelection().add(dst);
                if (src != null && dst != null) {
                    Edge edge = graph.getEdge(src, dst);
                    if (edge != null) graph.getSelection().add(graph.getEdge(src, dst));
                }
            }
        }
    }
    
    private class InvertSelection extends Modifier {
        
        public InvertSelection(Graph graph) {super(graph);}
        
        @Override
        public boolean isCompatible() {
            return graph.getSelection().nodeCount() > 0 || graph.getSelection().edgeCount() > 0;
        }
        
        @Override
        public String getTitle() {
            return "Invert selection";
        }
        
        @Override
        public void modify() {
            graph.getSelection().clearBuffer();
            for (GraphElement e : graph) {
                if (!graph.getSelection().contains(e)) graph.getSelection().addToBuffer(e);
            }
            graph.getSelection().clear();
            graph.getSelection().commitBuffer();
        }
    }
    
    private class SelectCorrespondingEdges extends Modifier {
        String uri;
        
        public SelectCorrespondingEdges(Graph graph) {super(graph);}
        
        @Override
        public boolean isCompatible() {
            if (graph.getSelection().nodeCount() == 1 && graph.getSelection().edgeCount() == 0) {
                try {
                    uri = graph.getSelection().getNodes().iterator().next().getName();
                } catch (NoSuchElementException e) {
                    //user deselected node before this line.  no problem - this
                    //is obviously not compatible. return false
                    return false;
                }
                // determine if model contains statements with node's name as property
                return graph.getRenderedTriples().contains(null, graph.getRenderedTriples().createProperty(uri));
            }
            return false;
        }
        
        @Override
        public String getTitle() {
            return "Select edges with to property " + graph.prefixed(uri);
        }
        
        @Override
        public void modify() {
            // select all edges with statements with node's name as property
            //////////////////
            graph.getSelection().clear();
            // query model
            StmtIterator it = graph.getRenderedTriples().listStatements(null, graph.getRenderedTriples().createProperty(uri), (RDFNode) null);
            
            // for each statement, add edge between subject and object to selection
            while (it.hasNext()) {
                Statement s = it.next();
                String sub = s.getSubject().toString();
                String obj = s.getObject().toString();
                
                graph.getSelection().add(graph.getEdge(sub, obj));
            }
        }
    }
    
    private class SelectCorrespondingNode extends ModifierSet {
        Edge edge;
        
        public SelectCorrespondingNode(Graph graph) {
            super(graph);
        }
        
        @Override
        public boolean isCompatible() {
            if (graph.getSelection().edgeCount() == 1 && graph.getSelection().nodeCount() == 0) {
                try {
                    edge = graph.getSelection().getEdges().iterator().next();
                } catch (NoSuchElementException e) {
                    //user deselected edge before this line.  no problem - this
                    //is obviously not compatible. return false
                    return false;
                }
                for (Statement s : edge.triples) {
                    if (graph.cp5.get(s.getPredicate().getURI()) != null) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        @Override
        public void constructModifiers() {
            modifiers.clear();
            
            for (Statement s : edge.triples) {
                if (graph.cp5.get(s.getPredicate().getURI()) != null) {
                    modifiers.add(new SelectPredicateNode(s.getPredicate().getURI()));
                }
            }
        }
        
        private class SelectPredicateNode extends Modifier {
            String pred;
            
            public SelectPredicateNode(String predicate) {
                super(SelectCorrespondingNode.this.graph);
                pred = predicate;
            }
            
            @Override
            public boolean isCompatible() { return true; }
            
            @Override
            public String getTitle() {
                return "Select node corresponding to " + graph.prefixed(pred);
            }
            
            @Override
            public void modify() {
                graph.getSelection().clear();
                graph.getSelection().add((Node) graph.cp5.get(pred));
            }
        }
    }
    
    
    
    /*TODO: modifier set for nodes of same type (multiple types per entity)
    private class SelectSameType implements Modifier {
        ArrayList<RDFNode> types;
        
        Query qry;
        QueryExecution qe;
        ResultSet rs;
        
        public SelectSameType() {
            types = new ArrayList<>();
        }
        
        @Override
        public boolean checkCompatibleAndConstruct() {
            boolean singletNode =  selection.nodeCount() == 1 && selection.edgeCount() == 0;
            boolean isCompatible = false;
            
            if (singletNode) {
                String sparql = "select ?o where { <" + 
                        selection.iterator().next().getName() + "> <" +
                        RDF.type.getURI() + "> ?o }";
                
                qry = QueryFactory.create(sparql);
                qe = QueryExecutionFactory.create(qry, model);
                rs = qe.execSelect();
                
                while (rs.hasNext()) {
                    types.add(rs.nextSolution().get("o"));
                    isCompatible = true;
                }
            }
        }
        
        @Override
        public String getTitle() {
            return "Select nodes of type";
        }
    }
        * */
}
