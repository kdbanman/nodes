package nodes;

/**
* elementary implementable for the reactive selection modification menu.
* see documentation here as well as existing examples.
*/
public abstract class Modifier {
   // access model, selection, element counts, and other desired properties
   // with getters.  do not tie them to local state with class fields
   // because they change.
   protected final Graph graph;

   // always construct with the graph to be observed and operated on.
   public Modifier(Graph graph) {
       this.graph = graph;
   }
   
   public Graph getGraph() {
       return graph;
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