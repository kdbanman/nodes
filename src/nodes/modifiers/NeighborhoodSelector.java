package nodes.modifiers;

import nodes.Edge;
import nodes.Graph;
import nodes.Modifier;
import nodes.Node;

/**
 *
 * @author kdbanman
 */
public class NeighborhoodSelector extends Modifier {
        
    public NeighborhoodSelector(Graph graph) {super(graph);}

    @Override
    public boolean isCompatible() {
        return graph.getSelection().nodeCount() > 0 || graph.getSelection().edgeCount() > 0;
    }

    @Override
    public String getTitle() {
        return "Add neighborhood to selection";
    }

	@Override
	public ModifierType getType() {
		return ModifierType.ALL;
	}

    @Override
    public void modify() {
        graph.getSelection().clearBuffer();
        for (Edge e : graph.getSelection().getEdges()) {
            graph.getSelection().addToBuffer(e.getDestinationNode());
            graph.getSelection().addToBuffer(e.getSourceNode());
        }
        for (Node n : graph.getSelection().getNodes()) {
            for (Node nbr : graph.getNbrs(n)) {
                graph.getSelection().addToBuffer(nbr);
                graph.getSelection().addToBuffer(graph.getEdge(n, nbr));
            }
        }
        graph.getSelection().commitBuffer();
    }
}