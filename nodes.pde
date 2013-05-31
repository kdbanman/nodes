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
ControlP5 cp5;
UnProjector proj;

Graph graph;

// Virtuoso test environment data
String URL = "jdbc:virtuoso://129.128.212.41:1111";
String USER = "dba";
String PASS = "marek";
String GRAPH = "Arts";
String START_URI = "http://dbpedia.org/resource/Edvard_Munch";

void setup() {
  int w = 800;
  int h = 600;
  size(w, h , P3D);
  
  cam = new PeasyCam(this, 0, 0, 0, 300);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(10000);
  
  
  cp5 = new ControlP5(this);
  proj = new UnProjector();
  
  graph = new Graph(proj, cp5);
  
  VirtGraph g = new VirtGraph(GRAPH, URL, USER, PASS);
  Query sparql = QueryFactory.create(getStarQuery(START_URI));
  VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, g);
  Model toAdd = ModelFactory.createDefaultModel();
  vqe.execConstruct(toAdd);
  
  graph.addTriples(toAdd);

}

void draw() {
  if (keyPressed && key == ' ') graph.layout();
  
  proj.captureViewMatrix((PGraphics3D) this.g);
  
  proj.calculatePickPoints(mouseX, mouseY);
  pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
  
  background(#E07924);
}

String getStarQuery(String URI) {
  return "CONSTRUCT { <" + URI + "> ?p ?o } WHERE { GRAPH <" + GRAPH + "> { <" + URI + "> ?p ?o } }";
}
