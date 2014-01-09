package nodes.modifiers;

import nodes.Graph;
import nodes.GraphElement;
import nodes.Modifier;

/**
 *
 * @author kdbanman
 */
public class SelectionInverter extends Modifier {
        
	public SelectionInverter(Graph graph) {super(graph);}
        
	@Override
	public boolean isCompatible() {
		return graph.getSelection().nodeCount() > 0 || graph.getSelection().edgeCount() > 0;
	}
        
	@Override
	public String getTitle() {
		return "Invert selection";
	}

	@Override
	public ModifierType getType() {
		return ModifierType.PANEL;
	}

	@Override
	public void modify() {
		graph.getSelection().clearBuffer();
		for (GraphElement<?> e : graph) {
			if (!graph.getSelection().contains(e)) graph.getSelection().addToBuffer(e);
		}
		graph.getSelection().clear();
		graph.getSelection().commitBuffer();
	}
}