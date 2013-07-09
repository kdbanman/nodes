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
public class Modifiers {
    
    private Graph graph;
    private Model model;
    
    private ArrayList<Modifier> modifiers;
    
    private HashMap<Integer, Modifier> runIndex;
    
    private int menuIndex;
    
    public Modifiers(Graph g) {
        graph = g;
        model = graph.triples;
        
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
            if (mod.isCompatible(selection) && !runIndex.containsValue(mod)) {
                
                menu.addItem(mod.getTitle(selection), menuIndex);
                runIndex.put(menuIndex, mod);
                
                menuIndex++;
            // if the modifier is not compatible with the current selection but
            // it is still registered/in the menu, then remove it from the menu/index
            } else if (!mod.isCompatible(selection) && runIndex.containsValue(mod)) {
                
                int toRemove = menu.getItem(mod.getTitle(selection)).getValue();
                
                menu.removeItem(mod.getTitle(selection));
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
        public abstract boolean isCompatible(Selection s);
        public abstract String getTitle(Selection s);
        public abstract void modify();
    }
    
    private class SelectAll extends Modifier {
        
        public String getTitle(Selection s) {
            return "Select all";
        }
        
        public boolean isCompatible(Selection s) {
            return true;
        }
        
        public void modify() {
            for (GraphElement e : graph) {
                graph.selection.add(e);
            }
        }
    }
    
    private class SelectNodes extends Modifier {
        
        public String getTitle(Selection s) {
            return "Filter only nodes";
        }
        
        public boolean isCompatible(Selection s) {
            return s.nodeCount() > 0 && s.edgeCount() > 0;
        }
        
        public void modify() {
            graph.selection.clearEdges();
        }
    }
    
    private class SelectEdges extends Modifier {
        
        public String getTitle(Selection s) {
            return "Filter only edges";
        }
        
        public boolean isCompatible(Selection s) {
            return s.nodeCount() > 0 && s.edgeCount() > 0;
        }
        
        public void modify() {
            graph.selection.clearNodes();
        }
    }
    
    private class SelectNeighbors extends Modifier {
        
        public String getTitle(Selection s) {
            return "Select neighbors";
        }
        
        public boolean isCompatible(Selection s) {
            return s.nodeCount() == 1 && s.edgeCount() == 0;
        }
        
        public void modify() {
            //Node n = graph.selection.getNodes().iterator().next();
            //TODO
        }
    }
}
