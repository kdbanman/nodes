import com.hp.hpl.jena.rdf.model.Statement;

import nodes.Edge;
import nodes.Graph;
import nodes.Modifier;
import nodes.Node;
import nodes.Modifier.ModifierType;

/**
 * Filter out selection to have resources selected
 * For references:
 * @see LiteralsFilter
 * @see NumericalLiteralsFilter
 * @author Karim
 *
 */

public class ResourceFilter extends Modifier {

	public ResourceFilter(Graph graph) {
		super(graph);
	}

	@Override
	public boolean isCompatible() {
		return graph.nodeCount() > 0 && selection.nodeCount() > 0;
	}

	@Override
	public String getTitle() {
		return "Filter only resources";
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

			if (st.getObject().isResource()
					&& st.getObject().toString().equals(n.getName()))
				continue;

			else
				selection.remove(n);

		}
	}
}
