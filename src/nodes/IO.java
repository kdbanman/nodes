package nodes;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import org.apache.jena.riot.RDFDataMgr;
import org.openjena.riot.RiotException;

/**
 *
 * @author kdbanman
 */
public class IO {
    
    //ensure non-instantiability
    private IO() {
        
    }
    
    /**
     * Reads rdf from http or filesystem urls.
     * If the supplied uri is an http url identifying a resource, then the
     * returned results will be pruned to contain only triples incoming and
     * outgoing from that resource.
     * If not, all result statements will be returned.
     * 
     * @param uri HTTP or filesystem URL.  Throws RiotException if the valid RDF is not returned.
     * @return jena Model of Statements.
     */
    public static Model getDescription(String uri) throws RiotException {
        // retrieve description as a jena model
        Model retrievedTriples = ModelFactory.createDefaultModel();
        RDFDataMgr.read(retrievedTriples, uri);

        // if the queried uri represents a resource, rather than a
        // document that does not describe the retrieval uri, then
        // only include that resource's immediate neighborhood
        RDFNode asResource = retrievedTriples.createResource(uri);
        Model toAdd = retrievedTriples;
        if (retrievedTriples.containsResource(asResource)) {
            toAdd = retrievedTriples.query(new SimpleSelector((Resource) asResource, null, (Object) null));
            toAdd.add(retrievedTriples.query(new SimpleSelector(null, null, asResource)));
            toAdd.setNsPrefixes(retrievedTriples);
        }
        
        return toAdd;
    }
    
    /**
     * From the specified SPARQL endpoint URL, retrieves all incoming and outgoing
     * triples from the specified resource and returns them as a single jena Model.
     * 
     * @return jena Model of Statements
     */
    public static Model getDescriptionSparql(String endpointURL, String resourceURI) {
        String resoruceURI = "";
                String queryString = "CONSTRUCT { <" + resoruceURI + "> ?p1 ?o . "
                        + "?s ?p2 <" + resoruceURI + "> } " + ""
                        + "WHERE { <" + resoruceURI + "> ?p1 ?o . "
                        + "?s ?p2 <" + resoruceURI + "> }";
                
                // construct query
                Query query = QueryFactory.create(queryString);
                
                QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, query);
                
                // retrieve description as a jena model
                Model toAdd = qexec.execConstruct();
                
                return toAdd;
    }
}
