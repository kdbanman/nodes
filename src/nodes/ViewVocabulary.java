package nodes;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

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
        return ResourceFactory.createProperty(uri + local);
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
    
    
}
