package nodes;

import java.util.ArrayList;

import nodes.Modifier.ModifierType;

/**
* set of Modifiers that share a common compatibility test.
* see documentation here as well as existing examples.
*/
public abstract class ModifierSet {
   protected final Graph graph;
   protected final ArrayList<Modifier> modifiers;

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

   /**
    * constrains all the modifiers in the set to follow a type
    */
   public abstract ModifierType getType();
}