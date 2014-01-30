package nodes.modifiers;

import nodes.Graph;
import nodes.GraphElement;
import nodes.Modifier;


/**
 * @author Karim Baaba
 *
 * Modifier to toggle element labels
 */
public class LabelToggle extends Modifier {

	public LabelToggle(Graph graph) {
		super(graph);
	}

	@Override
	public boolean isCompatible() {
		return graph.getSelection().edgeCount() > 0	|| graph.getSelection().nodeCount() > 0;
	}

	@Override
	public String getTitle() {
		return "Toggle Label";
	}

	@Override
	public void modify() {
		for (GraphElement<?> e : graph.getSelection())
            e.setDisplayLabel(!e.getDisplayLabel());
	}

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}

}
