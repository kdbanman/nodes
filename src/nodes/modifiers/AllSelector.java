package nodes.modifiers;

import nodes.Graph;
import nodes.GraphElement;
import nodes.Modifier;

/**
 *
 * @author kdbanman
 */
public class AllSelector extends Modifier {
        
    public AllSelector(Graph graph) {super(graph);}

    @Override
    public boolean isCompatible() {
        return true;
    }

    @Override
    public String getTitle() {
        return "Select all";
    }

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}

    @Override
    public void modify() {
        for (GraphElement<?> e : graph) {
            graph.getSelection().add(e);
        }
    }
}