package nodes;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.util.ArrayList;

/**
 * Reads and applies view parameters from previously visualized Graph using the
 * ViewVocabulary.
 * 
 * @author kdbanman
 */
public class ViewModelReader {
    
    /**
     * Returns a model containing only the triples used for visual description,
     * including all reifications for Edge identification.  Does not mutate
     * passed Model.
     * 
     * @param toExtract
     * @return 
     */
    public static Model extractViewTriples(Model toExtract) {
        
        Model extracted = ModelFactory.createDefaultModel();
        
        if (containsViewTriples(toExtract)) {
            // iterate through all statements
            StmtIterator it = toExtract.listStatements();
            while (it.hasNext()) {
                Statement stmt = it.next();
                
                // if the subject or the predicate contain the view uri
                // (objects of View triples are always original data elements)
                if (stmt.getPredicate().getNameSpace().equals(ViewVocabulary.getURI()) ||
                    stmt.getSubject().getNameSpace().equals(ViewVocabulary.getURI())) {
                    // then add the statement to the extracted model
                    extracted.add(stmt);
                }
            }
        }
        return extracted;
    }
    
    /**
     * Mutates the passed Graph's Nodes and Edges as per the visual parameters
     * in the passed visual model.
     * 
     * If any Nodes or Edges are described
     * within visual parameters, but are not represented within the graph, they
     * are listed in a MissingTriplesException (accessible through
     * MissingElementsException.listMissingElements())
     * 
     * @param graph Graph whose Nodes and Edges will be mutated.
     * @param toApply 
     */
    public static void applyViewTriples(Graph graph, Model toApply) throws MissingElementsException {
        //TODO
        // iterate through every statment of view
        // if predicate is sharesEdge
            //ignore it
        // if the predicate is Node-specific (position)
            // then get the target name to get the Node
            // and apply the position
        // otherwise
            // define a GraphElement for later assignment
            // if it points to a reification resource of view namespace
                // then get the source and target names to get the edge as the GraphElement
            // otherwise
                // get the resource name to get the node as the GraphElement
            // and apply the view parameter to the GraphElement
    }
    
    /**
     * Tests whether or not a model contains triples using the ViewVocabulary
     * namespace.
     * 
     * @param toTest
     * @return 
     */
    public static boolean containsViewTriples(Model toTest) {
        NsIterator nsIt = toTest.listNameSpaces();
        while (nsIt.hasNext()) {
            if (nsIt.nextNs().equals(ViewVocabulary.getURI())) return true;
        }
        return false;
    }
    
    public class MissingElementsException extends Exception {
        private ArrayList<String> missingNodes;
        private ArrayList<String> missingEdges;
        
        public MissingElementsException() {
            super("WARNING: Visually described GraphElements do not exist within Graph.");
            
            missingNodes = new ArrayList<>();
            missingEdges = new ArrayList<>();
        }
        
        public void addMissingNode(String id) {
            missingNodes.add(id);
        }
        
        public void addMissingEdge(String id) {
            missingEdges.add(id);
        }
        
        public String listMissingElements() {
            String list = "";
            
            for (String node : missingNodes) {
                list += "Misisng Node: " + node + "\n";
            }
            
            for (String edge : missingEdges) {
                list += "Missing Edge: " + edge + "\n";
            }
            
            return list;
        }
    }
}
