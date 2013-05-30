/*
  Test for Graph mutator correctness.  All members of Graph made public for this reason.
*/

import controlP5.*;
import peasy.*;

PeasyCam cam;
ControlP5 cp5;
UnProjector proj;
CallbackListener interactor;

Graph graph;

void setup() {
  int w = 800;
  int h = 600;
  size(w, h , P3D);
  
  cam = new PeasyCam(this, 0, 0, 0, 300);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(1000);
  
  
  cp5 = new ControlP5(this);
  proj = new UnProjector();
    
  graph = new Graph(proj, cp5);
  
  interactor = new CallbackListener() {
    public void controlEvent(CallbackEvent event) {
      switch(event.getAction()) {
        case(ControlP5.ACTION_PRESSED):
        Controller c = event.getController();
        
          if (mouseButton == LEFT) {
            // print name
            println(c.getName());
            // print name of neighbors if node
            if ( c instanceof Node) {
              for (String name : graph.adjacent.get(c.getName())) {
                println("  " + name);
              }
            }
          } else {
            // attempt to remove controller
            if (c instanceof Node) {
              graph.removeNode((Node) c);
            } else {
              graph.removeEdge((Edge) c);
            }
          }
        break;
      }
    }
  };
  cp5.addCallback(interactor);

  
  Node n1 = graph.addNode("n1")  
                  .setPosition(0,0,0);
  Node n2 = graph.addNode("n2")
                   .setPosition(50,50,0);
  Node n3 = graph.addNode("n3")
                   .setPosition(-40,-120,-50);
  Node n4 = graph.addNode("n4")
                   .setPosition(200,200,200);
  
  graph.addEdge(n1, n3);
  graph.addEdge(n2, n3);
  graph.addEdge(n3, n4);
  

}

void draw() {
  proj.captureViewMatrix((PGraphics3D) this.g);
  
  proj.calculatePickPoints(mouseX, mouseY);
  pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
  
  background(#E07924);
}



