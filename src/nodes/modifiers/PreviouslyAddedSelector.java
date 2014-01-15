package nodes.modifiers;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import nodes.Edge;
import nodes.Graph;
import nodes.Modifier;
import nodes.Node;

/**
 *
 * @author kdbanman
 */
public class PreviouslyAddedSelector extends Modifier {
        
	public PreviouslyAddedSelector(Graph graph) {super(graph);}
        
	@Override
	public boolean isCompatible() {
		return graph.getAllPreviouslyAddedTriples() != null && !graph.getAllPreviouslyAddedTriples().isEmpty();
	}

	@Override
	public String getTitle() {
		return "Select most recently added subgraph";
	}

	@Override
	public ModifierType getType() {
		return ModifierType.PANEL;
	}

	@Override
	public void modify() {
		graph.getSelection().clear();
		StmtIterator it = graph.getAllPreviouslyAddedTriples().listStatements();
		while (it.hasNext()) {
			Statement s = it.next();

			Node src = graph.getNode(s.getSubject().toString());
			Node dst = graph.getNode(s.getObject().toString());
                
			// since the user may have removed graph elements from the most
			// recently added subgraph, make sure we aren't adding the
			// corresponding nulls to selection
			if (src != null) graph.getSelection().add(src);
			if (dst != null) graph.getSelection().add(dst);
			if (src != null && dst != null) {
				Edge edge = graph.getEdge(src, dst);
				if (edge != null) graph.getSelection().add(graph.getEdge(src, dst));
			}
		}
	}
}