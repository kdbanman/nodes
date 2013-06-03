/*
  Coloring triples colors edges.
  Coloring resources colors nodes.
  
  Only nodes may be moved.
  
  Only triples may be hidden/shown.
*/

import controlP5.*;
import peasy.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import virtuoso.jena.driver.*;

PeasyCam cam;
boolean zooming;
boolean laying;
int backG;

ControlP5 cp5;
UnProjector proj;

Graph graph;

CallbackListener selector;

// Virtuoso test environment data
String URL = "jdbc:virtuoso://129.128.212.41:1111";
String USER = "dba";
String PASS = "marek";
String GRAPH = "Arts";
String PICASSO_URI = "http://dbpedia.org/resource/Pablo_Picasso";
String EDVARD_URI = "http://dbpedia.org/resource/Edvard_Munch";
String CLIFFORD_URI = "http://dbpedia.org/resource/Clifton_Pugh";

void setup() {
  int w = 800;
  int h = 600;
  size(w, h , P3D);
  
  cam = new PeasyCam(this, 0, 0, 0, 1500);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(10000);
  zooming = false;
  backG = 0xFFE07924;
  
  
  cp5 = new ControlP5(this);
  proj = new UnProjector();
  
  graph = new Graph(proj, cp5);
  
  virtAddNeighbors(PICASSO_URI);
  virtAddNeighbors(EDVARD_URI);
  virtAddNeighbors(CLIFFORD_URI);
  
  selector = new CallbackListener() {
    public void controlEvent(CallbackEvent event) {
      if (event.getAction() == ControlP5.ACTION_PRESSED
          && event.getController() instanceof Node) {
            Node n = (Node) event.getController();
        print(n.getName());
      }
    }
  };
  cp5.addCallback(selector);

}

void draw() {
  if (laying) {
    graph.layout();
  }
  
  proj.captureViewMatrix((PGraphics3D) this.g);
  
  proj.calculatePickPoints(mouseX, mouseY);
  pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
  
  background(backG);
}

String getStarQuery(String URI) {
  return "CONSTRUCT { <" + URI + "> ?p ?o } WHERE { GRAPH <" + GRAPH + "> { <" + URI + "> ?p ?o } }";
}

void keyPressed() {
  if (key == ' ') {
    if (!zooming) {
      cam.setLeftDragHandler(cam.getZoomDragHandler());
      backG = 0xFF96D876;
    } else {
      cam.setLeftDragHandler(cam.getRotateDragHandler());
      backG = 0xFFE07924;
    }
    zooming = !zooming;
  } else if (key == 'l') {
    laying = !laying;
  }
}

void virtAddNeighbors(String uri) {
  VirtGraph g = new VirtGraph(GRAPH, URL, USER, PASS);
  Query sparql = QueryFactory.create(getStarQuery(uri));
  VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, g);
  Model toAdd = ModelFactory.createDefaultModel();
  vqe.execConstruct(toAdd);
  
  graph.addTriples(toAdd);
  
  g.close();
  vqe.close();
  toAdd.close();
}
