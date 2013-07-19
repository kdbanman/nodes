/*
 * Class containing listeners for selection modification
 */
package nodes;

import com.hp.hpl.jena.rdf.model.Model;
import controlP5.ListBox;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author kdbanman
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
    }
    
    public void populate(ListBox menu, Selection selection) {
        for (Modifier mod : modifiers) {
            // add the modifier to the menu and register it with runIndex
            // if it isn't already and if it's compatible with the current selection
            if (mod.isCompatible() && !runIndex.containsValue(mod)) {
                
                menu.addItem(mod.getTitle(), menuIndex);
                runIndex.put(menuIndex, mod);
                
                menuIndex++;
            // if the modifier is not compatible with the current selection but
            // it is still registered/in the menu, then remove it from the menu/index
            } else if (!mod.isCompatible() && runIndex.containsValue(mod)) {
                
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
        public abstract boolean isCompatible();
        public abstract String getTitle();
        public abstract void modify();
    }
    
    private class SelectAll extends Modifier {
        
        @Override
        public String getTitle() {
            return "Select all";
        }
        
        @Override
        public boolean isCompatible() {
            return true;
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
        public String getTitle() {
            return "Filter only nodes";
        }
        
        @Override
        public boolean isCompatible() {
            return selection.nodeCount() > 0 && selection.edgeCount() > 0;
        }
        
        @Override
        public void modify() {
            selection.clearEdges();
        }
    }
    
    private class SelectEdges extends Modifier {
        
        @Override
        public String getTitle() {
            return "Filter only edges";
        }
        
        @Override
        public boolean isCompatible() {
            return selection.nodeCount() > 0 && selection.edgeCount() > 0;
        }
        
        @Override
        public void modify() {
            selection.clearNodes();
        }
    }
    
    private class SelectNeighbors extends Modifier {
        
        @Override
        public String getTitle() {
            return "Add neighborhood to selection";
        }
        
        @Override
        public boolean isCompatible() {
            return selection.nodeCount() > 0 || selection.edgeCount() > 0;
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
}
