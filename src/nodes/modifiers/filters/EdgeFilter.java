package nodes.modifiers.filters;

import nodes.Graph;
import nodes.Modifier;

/**
 *
 * @author kdbanman
 */
public class EdgeFilter extends Modifier {

    public EdgeFilter(Graph graph) {super(graph);}

    @Override
    public boolean isCompatible() {
        return graph.getSelection().nodeCount() > 0 && graph.getSelection().edgeCount() > 0;
    }

    @Override
    public String getTitle() {
        return "Filter only edges";
    }

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}

    @Override
    public void modify() {
        graph.getSelection().clearNodes();
    }
}