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
    public static Model getDescription(String uri) {
        return getDescription(uri, uri);
    }
    
    /**
     * Reads rdf from http or filesystem urls.
     * Attempts to read from documentUri all triples incoming and outgoing
     * from entityUri.  If entityUri is not described by documentUri, then all
     * triples within documentUri are returned.
     * 
     * @param documentUri HTTP or filesystem URL.  Throws RiotException if the valid RDF is not returned.
     * @param entityUri Entity described within document at documentUri
     * @return jena Model of Statements
     */
    public static Model getDescription(String documentUri, String entityUri) {
        // retrieve description as a jena model
        Model retrievedTriples = ModelFactory.createDefaultModel();
        RDFDataMgr.read(retrievedTriples, documentUri);

        // if the queried uri represents a resource, rather than a
        // document that does not describe the retrieval uri, then
        // only include that resource's immediate neighborhood
        RDFNode asResource = retrievedTriples.createResource(entityUri);
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
        String queryString = "CONSTRUCT { <" + resourceURI + "> ?p1 ?o . "
                + "?s ?p2 <" + resourceURI + "> } " + ""
                + "WHERE { <" + resourceURI + "> ?p1 ?o . "
                + "?s ?p2 <" + resourceURI + "> }";

        // construct query
        Query query = QueryFactory.create(queryString);

        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, query);

        // retrieve description as a jena model
        Model toAdd = qexec.execConstruct();

        return toAdd;
    }
}
