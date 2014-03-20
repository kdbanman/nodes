package nodes.modifiers.filters;

import com.hp.hpl.jena.rdf.model.Statement;

import nodes.Edge;
import nodes.Graph;
import nodes.Modifier;
import nodes.Node;

/**
 * Comments selection filter
 * @author Karim
 *
 */
public class CommentsFilter extends Modifier {

	public CommentsFilter(Graph graph) {
		super(graph);
	}

	@Override
	public boolean isCompatible() {
		return graph.nodeCount() > 0 && selection.nodeCount() > 0;
	}

	@Override
	public String getTitle() {
		return "Filter only comments";
	}

	@Override
	public void modify() {

		selection.clearEdges();

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

			if (st.getPredicate().getLocalName().equalsIgnoreCase("comment")
					&& st.getObject().toString().equals(n.getName()))
				continue;
			else
				selection.remove(n);

		}
	}
}