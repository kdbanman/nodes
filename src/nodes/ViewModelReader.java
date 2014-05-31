package nodes;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RSIterator;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Reads and applies view parameters from previously visualized Graph using the
 * ViewVocabulary.
 * 
 * @author kdbanman
 */
public class ViewModelReader {
    
    public static double extractDouble(Resource doubleRes) {
        return Double.parseDouble(getLocalValue(doubleRes));
    }
    
    public static int extractInt(Resource intRes) {
        return Integer.parseInt(getLocalValue(intRes));
    }
    
    public static boolean extractBool(Resource boolRes) {
        return Boolean.parseBoolean(getLocalValue(boolRes));
    }
    
    /**
     * Gets local values ONLY from the ViewVocabulary namespace.
     * @param res
     * @return 
     */
    private static String getLocalValue(Resource res) {
        // assert that there is the viewvocabulary namespace with a local value.
        // throw the right exception if not.
        String[] both = res.toString().split("#");
        if (both.length == 2 && ViewVocabulary.getURI().equals(both[0] + "#")) {
            return both[1];
        } else {
            throw new IllegalArgumentException();
        }
    }
    
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
     * *MUTATES* the passed Graph's Nodes and Edges as per the visual parameters
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
    public static void applyViewTriples(Graph graph, Model toApply) throws MissingElementsException, MalformedViewTripleException {
        
        // prepare MissingElementsException in case it needs to be thrown at the end
        MissingElementsException missingElements = new MissingElementsException();
        
        // iterate through every statment of view to mutate the Nodes of the graph
        StmtIterator viewIt = toApply.listStatements();
        while (viewIt.hasNext()) {
            Statement viewStmt = viewIt.next();
            try {
                // if the subject/object are part of a reified statement,
                // then ignore this statement because it identifies an Edge
                if (!(isReifiedViewStatement(viewStmt.getObject()) ||
                      isReifiedViewStatement(viewStmt.getSubject()))) {
                    // get the target name to get the Node
                    Node toMutate = graph.getNode(viewStmt.getObject());
                    // this should be a node based on the if(!isReified...
                    // filter above, so classify the object as missing if it
                    // wasn't found
                    if (toMutate != null) {
                        mutateNode(toMutate, viewStmt);
                    } else {
                         missingElements.addMissingNode(viewStmt.getObject().toString());
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new MalformedViewTripleException(viewStmt);
            }
        }
        
        // iterate through all reified statements to find the Edges of the graph
        RSIterator reifiedIt = toApply.listReifiedStatements();
        while (reifiedIt.hasNext()) {
            ReifiedStatement viewReifiedStmt = reifiedIt.next();
            
            // if the statement is a reification resource of the view namespace
            if (isReifiedViewStatement(viewReifiedStmt)) {
                // get the source and target RDFNodes to get the Edge
                RDFNode source = viewReifiedStmt.getStatement().getSubject();
                RDFNode target = viewReifiedStmt.getStatement().getObject();
                
                // get the Edge from the. source and target resource names
                Edge toMutate = graph.getEdge(source.toString(), target.toString());
                if (toMutate != null) {
                    
                    // iterate through all statements with the reified view statement
                    // as the object.  (those should all be ViewVocabulary Property triples)
                    StmtIterator edgeViewIt = toApply.listStatements(null, null, viewReifiedStmt);
                    while (edgeViewIt.hasNext()) {
                        Statement edgeViewStmt = edgeViewIt.next();
                        
                        // mutate the edge based on the View triple
                        mutateGraphElement(toMutate, edgeViewStmt);
                    }
                } else {
                    missingElements.addMissingEdge(source.toString() + " | " + target.toString());
                }
            }
        }
        if (!missingElements.isEmpty()) {
            throw missingElements;
        }
    }
    
    private static void mutateNode(Node toMutate, Statement viewStmt) {
        if (viewStmt.getPredicate().equals(ViewVocabulary.positionX)) {

            // apply the position
            toMutate.setPositionX(extractDouble(viewStmt.getSubject()));

        } else if (viewStmt.getPredicate().equals(ViewVocabulary.positionY)) {

            // apply the position
            toMutate.setPositionY(extractDouble(viewStmt.getSubject()));

        } else if (viewStmt.getPredicate().equals(ViewVocabulary.positionZ)) {

            // apply the position
            toMutate.setPositionZ(extractDouble(viewStmt.getSubject()));

        }
        mutateGraphElement(toMutate, viewStmt);
    }
    
    private static void mutateGraphElement(GraphElement toMutate, Statement viewStmt) {
         if (viewStmt.getPredicate().equals(ViewVocabulary.color)) {

            // apply the position
            toMutate.setColor(extractInt(viewStmt.getSubject()));
            
        } else if (viewStmt.getPredicate().equals(ViewVocabulary.size)) {

            // apply the position
            toMutate.setSize(extractInt(viewStmt.getSubject()));

        } else if (viewStmt.getPredicate().equals(ViewVocabulary.labelVisibility)) {

            // apply the position
            toMutate.setLabelVisible(extractBool(viewStmt.getSubject()));

        } else if (viewStmt.getPredicate().equals(ViewVocabulary.labelSize)) {

            // apply the position
            toMutate.setLabelSize(extractInt(viewStmt.getSubject()));

        }
    }
    
    private static boolean isPosition(Property p) {
        return p.toString().matches(".*position[XYZ]");
    }
    
    private static boolean isReifiedViewStatement(RDFNode res) {
        return res.isResource() && isReifiedViewStatement(res.asResource());
    }
    
    private static boolean isReifiedViewStatement(Resource res) {
        String[] both = res.getURI().toString().split("#");
        if (both.length == 2 && ViewVocabulary.getURI().equals(both[0] + "#")) {
            return both[1].matches("Statement_.*");
        } else {
            return false;
        }
    }
    
    private static boolean isViewVocabNamespaced(RDFNode res) {
        String[] both = res.toString().split("#");
        return ViewVocabulary.getURI().equals(both[0] + "#");
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
}
