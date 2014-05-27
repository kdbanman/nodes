package nodes.modifiers.selectors;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import nodes.Graph;
import nodes.Modifier;
import nodes.Node;

/**
 *
 * @author Karim
 */
public class TypesSelector extends Modifier {

	public TypesSelector(Graph graph) {
		super(graph);
	}

	@Override
	public boolean isCompatible() {
		return graph.nodeCount() > 0 && selection.nodeCount() > 0;
	}

	@Override
	public String getTitle() {
		return "Select types";
	}

	@Override
	public void modify() {

		selection.clearEdges();
		selection.clearBuffer();

		Iterator<Node> it = selection.getNodes().iterator();
		Node node = null;

		while (it.hasNext()) {
			node = it.next();

			StmtIterator stIt = graph.getRenderedTriples()
								.listStatements(graph.getResource(node.getName()),
																(Property) graph.getResource(Graph.TYPERURI),
																(RDFNode) null);

			selection.remove(node);

			while (stIt.hasNext()) {
				selection.addToBuffer(graph.getNode(stIt.next().getObject()));
				selection.addToBuffer(node);
			}
		}

		selection.clear();
		selection.commitBuffer();
	}

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}
}
