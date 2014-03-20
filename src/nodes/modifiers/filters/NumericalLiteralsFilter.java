package nodes.modifiers.filters;

import com.hp.hpl.jena.rdf.model.Statement;

import nodes.Edge;
import nodes.Graph;
import nodes.Modifier;
import nodes.Node;

/**
 *
 * @author Karim
 *
 */
public class NumericalLiteralsFilter extends Modifier {

	public NumericalLiteralsFilter(Graph graph) {
		super(graph);
	}

	@Override
	public boolean isCompatible() {
		return graph.nodeCount() > 0 && selection.nodeCount() > 0;
	}

	@Override
	public String getTitle() {
		return "Filter only numerical literals";
	}

	@Override
	public void modify() {
		//it would be nice if we were able to invoke "LiteralsFilter" from here
		//which would give us a selection of already filter literals
		//the cost of independence is a little redundancy...
		//straight from LiteralsFilter.java
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
		String lexform;

		for (Statement st : e.getTriples()) {
			if (st.getObject().isLiteral() && st.getObject().toString().equals(n.getName())) {
				if ((lexform = st.getLiteral().getLexicalForm()) == null) {
					selection.remove(n);
				} else {
					//This is a very expensive method. A lot of exceptions will be
					//thrown but there doesn't seem to be another way to check
					//note: getLiteral().getDataType*URI* is not consistent
					try {
						Integer.parseInt(lexform);
					}
					catch (NumberFormatException ex) {
						selection.remove(n);
					}
				}
			} else {// else remove it
				selection.remove(n);
			}
		}
	}
}
