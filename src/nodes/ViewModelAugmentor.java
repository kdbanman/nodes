package nodes;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Uses ViewVocabulary to encode the visual properties of a Graph's Nodes and
 * Edges into a jena model.
 * 
 * @author kdbanman
 */
public class ViewModelAugmentor {
    
    // ensure non-instantiability
    private ViewModelAugmentor() {}
    
    /*
     * Describe a Node using its jena RDFNode as the object for a series of
     * triples for View parameters.
     */
    private static Model nodeDescription(Node node) {
        // create jena model to contain description triples
        Model description = ModelFactory.createDefaultModel();
        
        // use Node's RDFNode as the object of each description triple
        RDFNode obj = node.getRDFNode();
        
        // create subject resources for each description triple
        
        Resource posX = ViewVocabulary.createResource(node.getPosition().x);
        Resource posY = ViewVocabulary.createResource(node.getPosition().y);
        Resource posZ = ViewVocabulary.createResource(node.getPosition().z);
        
        Resource col = ViewVocabulary.createResource(node.getCol());
        Resource size = ViewVocabulary.createResource(node.getSize());
        
        Resource labelVis = ViewVocabulary.createResource(node.getDisplayLabel());
        Resource labelSize = ViewVocabulary.createResource(node.getLabelSize());
        
        // create and add Statements to description model using created
        // resources for each View property.
        
        description.add(description.createStatement(posX,
                                                    ViewVocabulary.positionX,
                                                    obj));
        description.add(description.createStatement(posY,
                                                    ViewVocabulary.positionY,
                                                    obj));
        description.add(description.createStatement(posZ,
                                                    ViewVocabulary.positionZ,
                                                    obj));
        
        description.add(description.createStatement(col,
                                                    ViewVocabulary.color,
                                                    obj));
        description.add(description.createStatement(size,
                                                    ViewVocabulary.size,
                                                    obj));
        
        description.add(description.createStatement(labelVis,
                                                    ViewVocabulary.labelVisibility,
                                                    obj));
        description.add(description.createStatement(labelSize,
                                                    ViewVocabulary.labelSize,
                                                    obj));
        
        return description;
    }
    
    private static Model edgeDescription(Edge edge, ReifiedStatement object) {
        // create jena model to contain description triples
        Model description = ModelFactory.createDefaultModel();
        
        // create subject resources for each description triple
        
        Resource col = ViewVocabulary.createResource(edge.getCol());
        Resource size = ViewVocabulary.createResource(edge.getSize());
        
        Resource labelVis = ViewVocabulary.createResource(edge.getDisplayLabel());
        Resource labelSize = ViewVocabulary.createResource(edge.getLabelSize());
        
        // create and add Statements to description model using created
        // resources for each View property.
        
        description.add(description.createStatement(col,
                                                    ViewVocabulary.color,
                                                    object));
        description.add(description.createStatement(size,
                                                    ViewVocabulary.size,
                                                    object));
        description.add(description.createStatement(labelVis,
                                                    ViewVocabulary.labelVisibility,
                                                    object));
        description.add(description.createStatement(labelSize,
                                                    ViewVocabulary.labelSize,
                                                    object));
        
        return description;
    }
    
    private static Model sharedEdgeDescription(ArrayList<ReifiedStatement> slaves, ReifiedStatement master) {
        Model description = ModelFactory.createDefaultModel();
        
        for (ReifiedStatement slave : slaves) {
            description.add(description.createStatement(slave,
                                                        ViewVocabulary.sharesEdge,
                                                        master));
        }
        
        return description;
    }
    
    /*
     * MUTATES MODEL PARAMETER
     */
    private static ReifiedStatement reifyStatement(Model toAugment, Statement toReify) {
        return toAugment.createReifiedStatement(ViewVocabulary.getURI() + "Statement_" + UUID.randomUUID().toString(), toReify);
    }
    
    public static Model augmentedModel(Graph graph) {
        // initialize graph to return
        Model augmented = ModelFactory.createDefaultModel();
        
        // Iterate through all edges of the graph.
        // This will allow access to every triple with easy access to each
        // triple's visual elements (Nodes and Edges)
        for (Edge e : graph.getEdges()) {
            
            // add the edge's triples to the augmented model using the bulk
            // (Array) Statement addition method of Model
            Statement[] statementArr = new Statement[e.getTriples().size()];
            e.getTriples().toArray(statementArr);
            augmented.add(statementArr);
            
            // add (to the augmented model) View triples to Edge's source node
            augmented.add(nodeDescription(e.getSourceNode()));
            
            // add (to the augmented model) View triples to Edge's target node
            augmented.add(nodeDescription(e.getDestinationNode()));
            
            // reify the first triple associated with the Edge and add (to the
            // augmented model) View triples associated with the nodes.Edge/jena.Statement
            ReifiedStatement firstTriple = reifyStatement(augmented, statementArr[0]);
            augmented.add(edgeDescription(e, firstTriple));
            
            // reify the remaining triples and associate them with the first
            // one using the sharesEdge property
            ArrayList<ReifiedStatement> remainingTriples = new ArrayList<>();
            for (int i = 1; i < statementArr.length; i++) {
                remainingTriples.add(reifyStatement(augmented, statementArr[i]));
            }
            augmented.add(sharedEdgeDescription(remainingTriples, firstTriple));
        }
        
        // return augmented model
        return augmented;
    }
}
