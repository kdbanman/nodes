/*
 * Class containing listeners for selection modification
 */
package nodes;

import controlP5.ListBox;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;

import nodes.modifiers.*;
import nodes.modifiersets.*;

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
        
        /* TODO: loadClasses is failing.  the classloader mechanism is silly.
        ArrayList<Class> modifierClasses = loadClasses(Modifier.class,
                                                       "resources/modifier_registry",
                                                       "modifiers/");
        ArrayList<Class> modifiersetClasses = loadClasses(Modifier.class,
                                                       "resources/modifierset_registry",
                                                       "modifiersets/");
        */
        
        // construct all modifiers and modifier sets, adding them to the
        // corresponding ArrayLists
        modifiers.add(new AllSelector(graph));
        modifiers.add(new NodeFilter(graph));
        modifiers.add(new EdgeFilter(graph));
        modifiers.add(new NeighborhoodSelector(graph));
        modifiers.add(new SelectionInverter(graph));
        modifiers.add(new PreviouslyAddedSelector(graph));
        modifiers.add(new CorrespondingEdgesSelector(graph));
        
        modifierSets.add(new CorrespondingNodeSelector(graph));
    }
    
    /**
     * from the specified registry file, get the specified Classes from the 
     * specified package-relative path.
     * @param desiredClass
     * @param registryFilePath
     * @param classPath
     * @return 
     */
    private ArrayList<Class> loadClasses(Class desiredClass, String registryFilePath, String classPath) {
        ArrayList<Class> returnVal = new ArrayList<>();
        try {
            
            // get URL from passed classpath
            URL location = this.getClass().getClassLoader().getResource("nodes/" + classPath);
            // instantiate classloader for passed classpath
            URLClassLoader loader = new URLClassLoader(new URL[]{location});
            
            // read the registry file
            for (String line : Files.readAllLines(Paths.get(registryFilePath),
                                                  StandardCharsets.UTF_8)) {
                // if the line in the registry file is not whitespace or a comment,
                // then try to load the class
                if (!line.matches("\\s*(#.*)*")){
                    System.out.println("Attempting to load " + location + line + ".class");
                    returnVal.add(this.getClass().getClassLoader().loadClass(line + ".class"));
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("ERROR: URL invalid");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("ERROR: Could read from file " + registryFilePath);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: Could not load class");
            e.printStackTrace();
        }
        
        return returnVal;
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
