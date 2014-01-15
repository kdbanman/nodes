package nodes.modifiers;

import nodes.Graph;
import nodes.Modifier;

/**
 *
 * @author kdbanman
 */
public class NodeFilter extends Modifier {
        
    public NodeFilter(Graph graph) {super(graph);}

    @Override
    public boolean isCompatible() {
        return graph.getSelection().nodeCount() > 0 && graph.getSelection().edgeCount() > 0;
    }

    @Override
    public String getTitle() {
        return "Filter only nodes";
    }

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}

    @Override
    public void modify() {
        graph.getSelection().clearEdges();
    }
}