/*
  Coloring triples colors edges.
  Coloring resources colors nodes.
  
  Only nodes may be moved.
  
  Only triples may be hidden/shown.
*/

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
}

void draw() {
  proj.captureViewMatrix((PGraphics3D) this.g);
  
  proj.calculatePickPoints(mouseX, mouseY);
  pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
  
  background(#E07924);
}

