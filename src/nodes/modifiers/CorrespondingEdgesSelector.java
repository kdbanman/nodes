package nodes.modifiers;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.util.NoSuchElementException;
import nodes.Graph;
import nodes.Modifier;

/**
 *
 * @author kdbanman
 */
public class CorrespondingEdgesSelector extends Modifier {
        String uri;
        
        public CorrespondingEdgesSelector(Graph graph) {super(graph);}
        
        @Override
        public boolean isCompatible() {
            if (graph.getSelection().nodeCount() == 1 && graph.getSelection().edgeCount() == 0) {
                try {
                    uri = graph.getSelection().getNodes().iterator().next().getName();
                } catch (NoSuchElementException e) {
                    //user deselected node before this line.  no problem - this
                    //is obviously not compatible. return false
                    return false;
                }
                // determine if model contains statements with node's name as property
                return graph.getRenderedTriples().contains(null, graph.getRenderedTriples().createProperty(uri));
            }
            return false;
        }
        
        @Override
        public String getTitle() {
            return "Select edges with to property " + graph.prefixed(uri);
        }
        
        @Override
        public void modify() {
            // select all edges with statements with node's name as property
            //////////////////
            graph.getSelection().clear();
            // query model
            StmtIterator it = graph.getRenderedTriples().listStatements(null, graph.getRenderedTriples().createProperty(uri), (RDFNode) null);
            
            // for each statement, add edge between subject and object to selection
            while (it.hasNext()) {
                Statement s = it.next();
                String sub = s.getSubject().toString();
                String obj = s.getObject().toString();
                
                graph.getSelection().add(graph.getEdge(sub, obj));
            }
        }
    }