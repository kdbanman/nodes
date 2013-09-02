/*
 * Class containing listeners for selection modification
 */
package nodes;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;
import controlP5.ListBox;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *TODO::
 *  This abstraction isn't complete.  The first 5 modifiers need to be part of
 *  a modifier set.  All rdf:type selectors need to be part of a modifier set.
 *  All ?s ?p ?o edge pattern matcher selectors need to be part of a modifier set.  
 *  node ?p ?o and ?s ?p node are part of a set.  Select edge predicates as nodes
 *  is part of some set too.  Sets should probably have their colors known to them.
 * 
 */
public class ModifierPopulator {
    
    private Graph graph;
    private Model model;
    private Selection selection;
    
    private ArrayList<Modifier> modifiers;
    
    private HashMap<Integer, Modifier> runIndex;
    
    private int menuIndex;
    
    public ModifierPopulator(Graph g) {
        graph = g;
        model = graph.triples;
        selection = graph.selection;
        
        modifiers = new ArrayList<>();
        
        runIndex = new HashMap<>();
        
        menuIndex = 0;
        
        // TODO: refactor this to a classloader thing so plugins work
        modifiers.add(new SelectAll());
        modifiers.add(new SelectNodes());
        modifiers.add(new SelectEdges());
        modifiers.add(new SelectNeighbors());
        modifiers.add(new InvertSelection());
    }
    
    public void populate(ListBox menu, Selection selection) {
        for (Modifier mod : modifiers) {
            // add the modifier to the menu and register it with runIndex
            // if it isn't already and if it's compatible with the current selection
            if (mod.checkCompatibleAndConstruct() && !runIndex.containsValue(mod)) {
                
                menu.addItem(mod.getTitle(), menuIndex);
                runIndex.put(menuIndex, mod);
                
                menuIndex++;
            // if the modifier is not compatible with the current selection but
            // it is still registered/in the menu, then remove it from the menu/index
            } else if (!mod.checkCompatibleAndConstruct() && runIndex.containsValue(mod)) {
                
                int toRemove = menu.getItem(mod.getTitle()).getValue();
                
                menu.removeItem(mod.getTitle());
                runIndex.remove(toRemove);
            }
        }
    }
    
    public void run(int modIdx) {
        try {
            runIndex.get(modIdx).modify();
        } catch (Exception e) {
            System.out.println("Error: Bad menu index passed");
        }
    }
    
    private abstract class Modifier {
        public abstract boolean checkCompatibleAndConstruct();
        public abstract String getTitle();
        public abstract void modify();
    }
    
    private class SelectAll extends Modifier {
        
        @Override
        public boolean checkCompatibleAndConstruct() {
            return true;
        }
        
        @Override
        public String getTitle() {
            return "Select all";
        }
        
        @Override
        public void modify() {
            for (GraphElement e : graph) {
                selection.add(e);
            }
        }
    }
    
    private class SelectNodes extends Modifier {
        
        @Override
        public boolean checkCompatibleAndConstruct() {
            return selection.nodeCount() > 0 && selection.edgeCount() > 0;
        }
        
        @Override
        public String getTitle() {
            return "Filter only nodes";
        }
        
        @Override
        public void modify() {
            selection.clearEdges();
        }
    }
    
    private class SelectEdges extends Modifier {
        
        @Override
        public boolean checkCompatibleAndConstruct() {
            return selection.nodeCount() > 0 && selection.edgeCount() > 0;
        }
        
        @Override
        public String getTitle() {
            return "Filter only edges";
        }
        
        @Override
        public void modify() {
            selection.clearNodes();
        }
    }
    
    private class SelectNeighbors extends Modifier {
        
        @Override
        public boolean checkCompatibleAndConstruct() {
            return selection.nodeCount() > 0 || selection.edgeCount() > 0;
        }
        
        @Override
        public String getTitle() {
            return "Add neighborhood to selection";
        }
        
        @Override
        public void modify() {
            selection.clearBuffer();
            for (Edge e : selection.getEdges()) {
                selection.addToBuffer(e.dst);
                selection.addToBuffer(e.src);
            }
            for (Node n : selection.getNodes()) {
                for (Node nbr : graph.getNbrs(n)) {
                    selection.addToBuffer(nbr);
                    selection.addToBuffer(graph.getEdge(n, nbr));
                }
            }
            selection.commitBuffer();
        }
    }
    
    private class InvertSelection extends Modifier {
        
        @Override
        public boolean checkCompatibleAndConstruct() {
            return selection.nodeCount() > 0 || selection.edgeCount() > 0;
        }
        
        @Override
        public String getTitle() {
            return "Invert selection";
        }
        
        @Override
        public void modify() {
            selection.clearBuffer();
            for (GraphElement e : graph) {
                if (!selection.contains(e)) selection.addToBuffer(e);
            }
            selection.clear();
            selection.commitBuffer();
        }
    }
    
    /*
    private class SelectSameType extends Modifier {
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
