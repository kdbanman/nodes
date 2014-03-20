package nodes.modifiers.filters;

import com.hp.hpl.jena.rdf.model.Statement;

import nodes.Node;
import nodes.Edge;
import nodes.Graph;
import nodes.Modifier;


/**
 *
 * @author Karim
 *
 */
public class LiteralsFilter extends Modifier {

	public LiteralsFilter(Graph graph) {
		super(graph);
	}

	@Override
	public boolean isCompatible() {
		return selection.nodeCount() > 0 && selection.nodeCount() > 0;
	}

	@Override
	public String getTitle() {
		return "Filter only literals";
	}

	@Override
	public void modify() {

		// Clear the edges
		selection.clearEdges();

		// Can only check if a node is a literal from the "Statement" that
		// contains the node
		// so we have iterate over every edge
		for (Edge e : graph.getEdges()) {
			Node src = e.getSourceNode();
			Node dst = e.getDestinationNode();

			if (selection.contains(src)) checkAndAdd(e, src);

			if (selection.contains(dst)) checkAndAdd(e, dst);
		}
	}

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}

	private void checkAndAdd(Edge e, Node n) {

		for (Statement st : e.getTriples()) {
			// match? leave it
			if (st.getObject().isLiteral() && st.getObject().toString().equals(n.getName()))
				continue;
			else // else remove it
				selection.remove(n);
		}
	}
}
