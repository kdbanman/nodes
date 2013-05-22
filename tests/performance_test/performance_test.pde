import controlP5.*;
import peasy.*;
import java.util.ArrayList;

PeasyCam cam;
ControlP5 cp5;
UnProjector proj;

void setup() {
  int w = 800;
  int h = 600;
  size(w, h , P3D);
  
  cam = new PeasyCam(this, 0, 0, 0, 6000);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(10000);
  
  perspective(PI/3.0, w/h, 10.0, 20000.0);
  
  
  cp5 = new ControlP5(this);
  proj = new UnProjector();
  
  // performance test:
  //   a fully rendered 500 nodes with an edge density of 3 is in the
  //   order of magnitude of the reasonable maximum on the Xeon machine
  randomSeed(0);
  int numNodes = 500;
  int edgeDensity = 3;
  
  ArrayList<Node> nodes = new ArrayList();
  for (int i = 0; i < numNodes; i++) {
    Node n = new Node(cp5, "n" + Integer.toString(i), proj)
                     .setPosition(rr(-5000,5000),
                                  rr(-5000,5000),
                                  rr(-5000,5000))
                     .setSize(rr(10, 50));
    nodes.add(n);
  }

  for (int i = 0; i < numNodes; i++) {
    for (int j = 0; j < numNodes; j++) {
      if (rr(0, 100) < edgeDensity && i != j) {
        new Edge(cp5,
                 "e" + Integer.toString(i) + "," + Integer.toString(j),
                 proj,
                 nodes.get(i),
                 nodes.get(j)).setSize(rr(3,15));
      }
    }
  }
}

void draw() {
  proj.captureViewMatrix((PGraphics3D) this.g);
  
  proj.calculatePickPoints(mouseX, mouseY);
  pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
  
  background(#E07924);
}

int rr(int l, int h) {
  return int(random(l,h));
}

