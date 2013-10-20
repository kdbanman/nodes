package nodes.modifiersets;

import com.hp.hpl.jena.rdf.model.Statement;
import java.util.NoSuchElementException;
import nodes.Edge;
import nodes.Graph;
import nodes.Modifier;
import nodes.ModifierSet;
import nodes.Node;

/**
 *
 * @author kdbanman
 */
public class CorrespondingNodeSelector extends ModifierSet {
    Edge edge;

    public CorrespondingNodeSelector(Graph graph) {
        super(graph);
    }

    @Override
    public boolean isCompatible() {
        if (graph.getSelection().edgeCount() == 1 && graph.getSelection().nodeCount() == 0) {
            try {
                edge = graph.getSelection().getEdges().iterator().next();
            } catch (NoSuchElementException e) {
                //user deselected edge before this line.  no problem - this
                //is obviously not compatible. return false
                return false;
            }
            for (Statement s : edge.getTriples()) {
                if (graph.getNode(s.getPredicate().getURI()) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void constructModifiers() {
        modifiers.clear();

        for (Statement s : edge.getTriples()) {
            if (graph.getNode(s.getPredicate().getURI()) != null) {
                modifiers.add(new SelectPredicateNode(s.getPredicate().getURI()));
            }
        }
    }

    private class SelectPredicateNode extends Modifier {
        String pred;

        public SelectPredicateNode(String predicate) {
            super(CorrespondingNodeSelector.this.graph);
            pred = predicate;
        }

        @Override
        public boolean isCompatible() { return true; }

        @Override
        public String getTitle() {
            return "Select node corresponding to " + graph.prefixed(pred);
        }

        @Override
        public void modify() {
            graph.getSelection().clear();
            graph.getSelection().add(graph.getNode(pred));
        }
    }
}