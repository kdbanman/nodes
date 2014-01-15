package nodes.modifiers;

import java.util.Collection;
import java.util.Iterator;

import nodes.Graph;
import nodes.Node;
import nodes.Modifier;

/**
 * Modifier class that adds the ability to show common neighbours among a
 * selection of nodes
 *
 * @author Karim
 *
 */
public class CommonNeighboursSelector extends Modifier {

    public CommonNeighboursSelector(Graph graph) {
        super(graph);
    }

    @Override
    public boolean isCompatible() {
        // Option should only be showed when more than one node is selected
        return graph.getSelection().nodeCount() > 1;
    }

    @Override
    public String getTitle() {
        return "Select common neighbour nodes";
    }

    @Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}

    @Override
    public void modify() {
        graph.getSelection().clearBuffer();

        Iterator<Node> it = graph.getSelection().getNodes().iterator();

        Collection<Node> common = graph.getNbrs(it.next());
        Collection<Node> next = null;

        while (it.hasNext()) {
            next = graph.getNbrs(it.next());
            common.retainAll(next); // intersect
        }

        for (Node nbr : common) {
            graph.getSelection().addToBuffer(nbr);
        }

        graph.getSelection().clear();
        graph.getSelection().commitBuffer();
    }
}