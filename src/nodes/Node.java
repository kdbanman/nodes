package nodes;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import controlP5.ControllerView;

import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author kdbanman
 */
public class Node extends GraphElement<Node> {
    
    private RDFNode rdfNode;
  
    // name of controller is the URI or literal value
    // UnProjector is for 3D extension of inside()
	Node(Graph parentGraph, RDFNode rdfNode) {
		super(parentGraph, rdfNode.toString());
        
        this.rdfNode = rdfNode;

		labelText.add(rdfNode.toString());
		updateLabel();

		ControllerView<Node> view = new ControllerView<Node>() {

			@Override
			public void display(PApplet p, Node n) {
				Node node = (Node) n;
				// render in 3D
				p.pushMatrix();

				if (selected() && !inside()) {
					p.fill(pApp.selectColor);
				} else {
					p.fill(currentCol);
				}

				// Translate(x,y,0) called already, but nodes are in 3D
				p.translate(0, 0, node.getPosition().z);
				p.sphere(node.size);
				if (displayLabel) {
					displayLabel();
				}

				p.popMatrix();
			}
		};

		setView(view);
	}
    
    @Override
    public boolean inside() {
        try {
            proj.calculatePickPoints(pApp.mouseX, pApp.mouseY);
        } catch (NullPointerException e) {
            System.err.println("DEBUG: null caught in Node");
            return false;
        }

        // vector mouse is from cursor inward orthogonally from the screen
        PVector mouse = proj.ptEndPos.get();
        mouse.sub(proj.ptStartPos);

        // vector obj is from the cursor to the position of the node
        PVector obj = getPosition().get();
        obj.sub(proj.ptStartPos);

        // theta is the angle between the mouse vector and the object vector
        float theta = PVector.angleBetween(mouse, obj);

        // phi is the angular displacement of the radius of the node
        float phi = Nodes.atan(size/obj.mag());

        // the cursor is inside the node if theta is less than phi
        return theta < phi;
    }

    public Node setPosition(final float x, final float y, final float z) {
      return setPosition(new PVector(x, y, z));
    }
    
    public void setPositionX(double x) {
        this.getPosition().x = (float) x;
    }
    public void setPositionY(double y) {
        this.getPosition().y = (float) y;
    }
    public void setPositionZ(double z) {
        this.getPosition().z = (float) z;
    }
    
    public String getPrefixedName() {
        return graph.prefixed(getName());
    }
    
    public StmtIterator getOutboundStatements() {
        return graph.getRenderedTriples().listStatements(graph.getResource(getName()), null, (RDFNode) null);
    }
    
    public StmtIterator getInboundStatements() {
        StmtIterator inbound = graph.getRenderedTriples().listStatements(null, null, (RDFNode) graph.getResource(getName()));
        if (!inbound.hasNext()) {
            inbound = graph.getRenderedTriples().listStatements(null, null, getName());
        }
        return inbound;
    }
    
    public RDFNode getRDFNode() {
        return rdfNode;
    }
}
