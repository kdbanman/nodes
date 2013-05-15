import controlP5.*;
import peasy.*;

PeasyCam cam;
ControlP5 cp5;
UnProjector proj;

void setup() {
  size(800, 600, P3D);
  
  cam = new PeasyCam(this, 0, 0, 0, 500);
  
  
  cp5 = new ControlP5(this);
  proj = new UnProjector();
  
  new Node(cp5, "n1", proj).setPosition(0,0,0).setSize(30);
  new Node(cp5, "n2", proj).setPosition(50,50,0).setSize(20);
}

void draw() {
  proj.captureViewMatrix((PGraphics3D) this.g);
  
  background(0);

  
  
}

