package nodes.modifiers.filters;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.RDFNode;

import nodes.Graph;
import nodes.Modifier;
import nodes.Node;

/**
 * Filter selection to include resources only
 * @author Karim
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

		// Clear the edges
		selection.clearEdges();

		Iterator<Node> it = selection.getNodes().iterator();
		RDFNode node = null;

		while (it.hasNext()) {
			node = it.next().getRDFNode();

			if (!node.isResource())
				it.remove();
		}
	}

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}
}
