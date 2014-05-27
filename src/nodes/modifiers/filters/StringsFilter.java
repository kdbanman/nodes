package nodes.modifiers.filters;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.RDFNode;

import nodes.Graph;
import nodes.Modifier;
import nodes.Node;

/**
 *
 * @author Karim
 */
public class StringsFilter extends Modifier {

	public StringsFilter(Graph graph) {
		super(graph);
	}

	@Override
	public boolean isCompatible() {
		return graph.nodeCount() > 0 && selection.nodeCount() > 0;
	}

	@Override
	public String getTitle() {
		return "Filter only strings";
	}

	@Override
	public void modify() {

		// Clear the edges
		selection.clearEdges();

		Iterator<Node> it = selection.getNodes().iterator();
		RDFNode node = null;

		while (it.hasNext()) {
			node = it.next().getRDFNode();

			if (!node.isLiteral()
					|| !(node.asLiteral().getValue() instanceof String))
				it.remove();

		}
	}

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}
}
