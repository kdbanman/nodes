/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import com.hp.hpl.jena.rdf.model.*;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;


/**
 *
 * @author kdbanman
 */
public class Importer {
    
    
    private Importer() {
        //ensure non-instantiability
    }
    
    /**
     * 
     * @param uri non-namespaced http uri to query with
     * @return 
     */
    public static Model getNeigborhoodFromWeb(String uri) {
        Model m = ModelFactory.createDefaultModel();
        
        try {
            RDFDataMgr.read(m, uri);
        } catch (RiotException e) {
            System.out.println("ERROR: RDF not retrieved from uri \n  " + uri);
        }
        
        StmtIterator it = m.listStatements();
        
        while (it.hasNext()) {
            Statement stmt = it.nextStatement();
            String s = stmt.getSubject().getURI();
            if (!s.equals(uri)) {
                it.remove();
            }
        }
        
        return m;
    }
}
