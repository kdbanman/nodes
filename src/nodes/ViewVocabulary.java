package nodes;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.util.Set;

/**
 * A vocabulary for describing the visual properties of an RDF model that is
 * rendered by nodes.
 * 
 * Intended for use by save/load feature.
 * 
 * @author kdbanman
 */
public class ViewVocabulary {
    
    private ViewVocabulary() {
        // Ensure non-instantiability with private constructor
    }
    
    // Define base uri
    private static final String uri = "http://semanticweb.ca/nodesView#";
    
    public static String getURI() {
        return uri;
    }
    
    /*
     * Generate namespaced Resource
     */
    private static Resource resource( String local ) {
        return ResourceFactory.createResource( uri + local );
    }

    /*
     * Generate namespaced Property
     */
    private static Property property( String local ) {
        return ResourceFactory.createProperty( uri, local );
    }
    
    /*
     * Below are the public functions for generating Resources for the 
     * view parameters.  They are used in this way:
     * - positionX -------> double
     * - positionY -------> double
     * - positionZ -------> double
     * - color -----------> int
     * - size ------------> int
     * - labelVisibility -> boolean
     * - labelSize -------> int
     */
    
    public static Resource createResource(double val) {
        
        return resource(Double.toString(val));
    }
    
    public static Resource createResource(int val) {
        
        return resource(Integer.toString(val));
    }
    
    public static Resource createResource(boolean val) {
        
        return resource(Boolean.toString(val));
    }
    
    /*
     * Below are the public properties that are intended to point from generated
     * view parameter Resources to Model Resources/ReifiedStatements
     */
    public static Property positionX = property("positionX"); //Node only
    public static Property positionY = property("positionY"); //Node only
    public static Property positionZ = property("positionZ"); //Node only
    public static Property color = property("color");
    public static Property size = property("size");
    public static Property labelVisibility = property("labelVisibility");
    public static Property labelSize = property("labelSize");
    
    // Property intended to show that a (reified) statement shares an edge with
    // another that is already visually described.
    public static Property sharesEdge = property("sharesEdge");
    
    public static Model augmentedModel(Graph graph) {
        // initialize graph to return
        Model augmented = ModelFactory.createDefaultModel();
        
        // Iterate through all edges of the graph.
        // This will allow access to every triple with easy access to each
        // triple's visual elements (Nodes and Edges)
        for (Edge e : graph.getEdges()) {
            
            // add the edge's triples to the augmented model
            Statement[] statementArr = new Statement[e.getTriples().size()];
            augmented.add(e.getTriples().toArray(statementArr));
            
            // add (to the augmented model) View triples to Edge's source RDFNode
            //TODO
            
            // add (to the augmented model) View triples to Edge's target RDFNode
            //TODO
        }
        
        // return augmented model
        return augmented;
    }
}
