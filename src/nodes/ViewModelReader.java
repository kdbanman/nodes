package nodes;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
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
     * including all reifications for Edge identification.
     * 
     * @param toExtract
     * @return 
     */
    public static Model extractViewTriples(Model toExtract) {
        
        Model extracted = ModelFactory.createDefaultModel();
        
        if (containsViewTriples(toExtract)) {
            //TODO
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
