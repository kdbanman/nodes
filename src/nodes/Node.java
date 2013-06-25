/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.ControllerView;

import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author kdbanman
 */
public class Node extends GraphElement<Node> {
  
    // name of controller is the URI or literal value
    // UnProjector is for 3D extension of inside()
    Node(Graph parentGraph, String name) {
      super(parentGraph, name);

      setView(new ControllerView() {
          @Override
          public void display(PApplet p, Object n) {
            Node node = (Node) n;

            // render in 3D
            p.pushMatrix();
            if (selected()) {
                p.fill(selectCol);
            } else {
                p.fill(currentCol);
            }
                        
            // Translate(x,y,0) called already, but nodes are in 3D
            p.translate(0,0,node.getPosition().z);
            
            if (isInside() || displayLabel) {
                 displayLabel();
            }
            
            p.sphere(node.size); 
            p.popMatrix();
          }
        }
      );
    }

    @Override
    public boolean inside() {

      proj.calculatePickPoints(pApp.mouseX, pApp.mouseY);

      // vector mouse is from cursor inward orthogonally from the screen
      PVector mouse = proj.ptEndPos.get();
      mouse.sub(proj.ptStartPos);

      // vector obj is from the cursor to the position of the node
      PVector obj = getPosition().get();;
      obj.sub(proj.ptStartPos);

      // theta is the angle between the mouse vector and the object vector
      float theta = PVector.angleBetween(mouse, obj);

      // phi is the angular displacement of the radius of the node
      float phi = PApplet.atan(size/obj.mag());

      // the cursor is inside the node if theta is less than phi
      return theta < phi;
    }

    public Node setPosition(final float x, final float y, final float z) {
      return setPosition(new PVector(x, y, z));
    }
}
