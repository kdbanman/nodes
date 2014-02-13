package nodes.modifiers;

import nodes.Graph;
import nodes.GraphElement;
import nodes.Modifier;

/**
 * Clears All labels
 * @author Karim Baabas
 *
 */
public class ClearLabels extends Modifier {

	public ClearLabels(Graph graph) {
		super(graph);
	}

	@Override
	public boolean isCompatible() {
		return graph.nodeCount() > 0 || graph.edgeCount() > 0;
	}

	@Override
	public String getTitle() {
		return "Clear Labels";
	}

	@Override
	public void modify() {
        for (GraphElement<?> e : graph) {
            e.setDisplayLabel(false);
        }
	}

	@Override
	public ModifierType getType() {
		return ModifierType.PANEL;
	}

}
