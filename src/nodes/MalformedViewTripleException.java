package nodes;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 *
 * @author kdbanman
 */
class MalformedViewTripleException extends Exception {

    public MalformedViewTripleException(Statement viewStmt) {
        super("ERROR: Could not parse value from triple:\n" + viewStmt.toString());
    }
    
}
