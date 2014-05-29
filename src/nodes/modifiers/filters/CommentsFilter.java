package nodes.modifiers.filters;

import java.util.Iterator;

import nodes.Edge;
import nodes.Graph;
import nodes.Modifier;

/**
 * Comments selection filter
 *
 * @author Karim
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

		selection.clearNodes();

		Iterator<Edge> it = selection.getEdges().iterator();
		Edge edge = null;

		while (it.hasNext()) {
			edge = it.next();

			if (!edge.getSingleTriple()
					.getPredicate()
					.getURI()
					.equals(Graph.COMMENTURI))
				it.remove();

		}
	}

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}
}