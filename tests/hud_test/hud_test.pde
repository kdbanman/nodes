/******************************
this nearly works.  the section in display for HUD elements
    theApplet.pushMatrix();
    theApplet.hint(DISABLE_DEPTH_TEST);
    cam.beginHUD();

    ...

    cam.endHUD();
    theApplet.hint(ENABLE_DEPTH_TEST);
    theApplet.popMatrix();
is the important part, but to get the cursor inside() detection to work properly,
the controller must be extended in the same way as Edge and Node are.  This is
OK for the browser, since HUD controls in the graph window will clutter it up.

Also, it was mentioned on the author's website that implementing ControllerView<T>
was kinda fucked anyways.  This might be why display() isn't translating or filling
properly.

Finally, note that ControlP5 objects are rendered in the order that they are created
when depth test is disabled.  Context menus, tooltips, and labels will have to be
instantiated afterwards, or treated to a .bringToFront().

Picard out.
******************************/

import controlP5.*;
import peasy.*;

PeasyCam cam;
ControlP5 cp5;
UnProjector proj;

void setup() {
  int w = 800;
  int h = 600;
  size(w, h , P3D);
  
  cam = new PeasyCam(this, 0, 0, 0, 300);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(1000);
  
  
  cp5 = new ControlP5(this);
  proj = new UnProjector();

  
  Node n1 = new Node(cp5, "n1", proj)  
                  .setPosition(0,0,0)
                  .setSize(30);
  Node n2 = new Node(cp5, "n2", proj)
                   .setPosition(50,50,0)
                   .setSize(20);
  Node n3 = new Node(cp5, "n3", proj)
                   .setPosition(-40,-120,-50)
                   .setSize(10);
  Node n4 = new Node(cp5, "n4", proj)
                   .setPosition(200,200,200)
                   .setSize(10);
  
  new Edge(cp5, "e13", proj, n1, n3).setSize(5);
  new Edge(cp5, "e23", proj, n2, n3).setSize(5);
  new Edge(cp5, "e34", proj, n3, n4).setSize(5);
    
  cp5.addButton("hello")
     .setPosition(50, 100)
     .setSize(100,100)
     .setView(new CircularButton())
     ;
     
  cp5.addButton("world")
     .setPosition(250, 100)
     .setSize(50,50)
     .setView(new CircularButton())
     ;
}

void draw() {
  proj.captureViewMatrix((PGraphics3D) this.g);
  
  proj.calculatePickPoints(mouseX, mouseY);
  pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
  
  background(#E07924);
}

public void hello(int theValue) {
  println("Hello pressed");
}

public void world(int theValue) {
  println("World pressed");
}

class CircularButton implements ControllerView<Button> {

  public void display(PApplet theApplet, Button theButton) {
    theApplet.pushMatrix();
    theApplet.hint(DISABLE_DEPTH_TEST);
    cam.beginHUD();
    
    if (theButton.isInside()) {
      if (theButton.isPressed()) { // button is pressed
        theApplet.fill(200, 60, 0);
      }  else { // mouse hovers the button
        theApplet.fill(200, 160, 100);
      }
    } else { // the mouse is located outside the button area
      theApplet.fill(0, 160, 100);
    }
    theApplet.translate(theButton.getPosition().x, theButton.getPosition().y);
    theApplet.ellipse(0, 0, theButton.getWidth(), theButton.getHeight());
    
    // center the caption label 
    int x = theButton.getWidth()/2 - theButton.getCaptionLabel().getWidth()/2;
    int y = theButton.getHeight()/2 - theButton.getCaptionLabel().getHeight()/2;
    
    translate(x, y);
    theButton.getCaptionLabel().draw(theApplet);
    
    cam.endHUD();
    theApplet.hint(ENABLE_DEPTH_TEST);
    theApplet.popMatrix();
  }
}
