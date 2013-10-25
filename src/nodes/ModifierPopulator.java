/*
 * Class containing listeners for selection modification
 */
package nodes;

import controlP5.ListBox;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
        
    	// load and construct all classes in modifier and modifierSet registry files
        modifiers = loadAndConstructModifiers(Modifier.class, 
        										 "resources/modifier_registry", 
        										 graph);
        modifierSets = loadAndConstructModifiers(ModifierSet.class, 
        											"resources/modifierset_registry", 
        											graph);
        
        runIndex = new HashMap<>();
        
        // integer address for accessing menu item methods when clicked
        // (this value is currently never reset, just incremented.  though it
        // should take a safely long time to traverse all positive integers)
        menuIndex = 0;
    }
    
    /**
     * from the specified registry file, construct instances of the Classes named within the 
     * passed package.  this is separate from loadClasses because of the monumental exception
     * handling business, as well as the fact that it is *only* the instantiation of the
     * modifiers that depends upon graph.  the rest is abstract enough to generally load the
     * classes, independent of the implementation of Modifier or ModifierSet.
     * 
     * @param desiredClass class from which the listed classes must inherit.
     * @param registryFilePathpath to file containing list of classes to load
     * @param packageName package to which the classes belong
     * @param constructorArg the graph that the modifier objects should be associated with.
     * @return list of classes named in the registry file
     */
    private <T> ArrayList<T> loadAndConstructModifiers(Class<T> desiredClass, String registryFilePath, Graph constructorArg) {
    	ArrayList<T> returnVal = new ArrayList<>();
    	try {
	        for (Class<T> TClass : loadClasses(desiredClass, registryFilePath)) {
	        	System.out.println("Instantiating " + TClass.getName());
	        	returnVal.add(TClass.getDeclaredConstructor(constructorArg.getClass()).newInstance(constructorArg));
	        }
        } catch (InstantiationException e) {
			System.err.println("ERROR: Class could not be instantiated.  Ensure that the class has a constructor"
					 + " and that it is not abstract or an interface.");
		} catch (IllegalAccessException e) {
			System.err.println("ERROR: Could not access constructor.  Ensure that the class has a public constructor.");
		} catch (IllegalArgumentException e) {
			System.err.println("ERROR: Class did not accept a Graph as a constructor argument.");
		} catch (InvocationTargetException e) {
			System.err.println("ERROR: Class construction throws InvocationTargetException.");
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			System.err.println("ERROR: Class does not have a constructor.");
			e.printStackTrace();
		} catch (SecurityException e) {
			System.err.println("ERROR: Class construction throws SecurityException.");
			e.printStackTrace();
		}
    	return returnVal;
    }
    
    /**
     * from the specified registry file, get the Classes named within the 
     * passed package.
     * 
     * @param desiredClass class from which the listed classes must inherit.
     * @param registryFilePath path to file containing list of classes to load
     * @param packageName package to which the classes belong
     * @return list of classes named in the registry file
     */
    private <T> ArrayList<Class<T>> loadClasses(Class<T> desiredClass, String registryFilePath) {
        ArrayList<Class<T>> returnVal = new ArrayList<>();
        try {
        	// will hold the package name with which the registry file is associated
        	String packageName = null;
        	
            // read the registry file
            for (String line : Files.readAllLines(Paths.get(registryFilePath),
                                                  StandardCharsets.UTF_8)) {
                // if the line in the registry file is not whitespace or a comment,
                // then try to load the class
                if (!line.matches("\\s*(#.*)*")){
                	
                	// first non-comment line of the registry should be the package name with which the registry file is
                	// associated.  (verified as first line by the null check)
                	if (packageName == null) {
                		packageName = line;
                		// the current line is not a Class, so don't continue the loop to try and load it.
                		continue;
                	}
                	
                	// qualify the listed class name (line in file) with the package name
                	String qualifiedClassName = packageName + "." + line;
                    System.out.println("Loading " + qualifiedClassName);
                    // use the classloader to load the class
                    Class<?> loadedClass = this.getClass().getClassLoader().loadClass(qualifiedClassName);
                    
                    // verify that loaded class is of the desired type and add it to the list
                    if (desiredClass.isAssignableFrom(loadedClass)) {
                    	returnVal.add((Class<T>) loadedClass);
                    } else {
                    	System.err.println("ERROR: Did not load " + loadedClass.getName());
                    	System.err.println(loadedClass.getName() + " doesn't inherit from " + desiredClass.getName());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: Could read from file " + registryFilePath);
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: Could not load class.  Ensure that it is compiled to a .class in the correct"
            					+ " package (and corresponding directory).");
        } catch (NoClassDefFoundError e) {
        	System.out.println("ERROR: Could not load class.  Ensure that it is compiled to a .class in the correct"
            					+ " package (and corresponding directory).");
        } catch (ClassCastException e) {
			System.err.println("ERROR: Class is not a subclass of " + desiredClass.getName());
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
