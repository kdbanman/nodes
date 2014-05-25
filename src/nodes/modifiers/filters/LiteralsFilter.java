package nodes.modifiers.filters;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.RDFNode;

import nodes.Node;
import nodes.Graph;
import nodes.Modifier;


/**
 * Filter selection to include literals only
 * @author Karim
 */
public class LiteralsFilter extends Modifier {

	public LiteralsFilter(Graph graph) {
		super(graph);
	}

	@Override
	public boolean isCompatible() {
		return graph.nodeCount() > 0 && selection.nodeCount() > 0;
	}

	@Override
	public String getTitle() {
		return "Filter only literals";
	}

	@Override
	public void modify() {

		// Clear the edges
		selection.clearEdges();

		Iterator<Node> it = selection.getNodes().iterator();
		RDFNode node = null;

		while (it.hasNext()) {
			node = it.next().getRDFNode();

			if (!node.isLiteral())
				it.remove();
		}
	}

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}
}
