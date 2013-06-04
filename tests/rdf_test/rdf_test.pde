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

import java.awt.Frame;
import java.awt.BorderLayout;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

PeasyCam cam;
int initZoom;
int backG;

ControlP5 cp5;
UnProjector proj;

Graph graph;

TestFrame tf;
List<String> dispHolder;
String defaultDisp;

// Virtuoso test environment data
String URL = "jdbc:virtuoso://129.128.212.41:1111";
String USER = "dba";
String PASS = "marek";
String GRAPH = "Arts";
String PICASSO_URI = "http://dbpedia.org/resource/Pablo_Picasso";
String EDVARD_URI = "http://dbpedia.org/resource/Edvard_Munch";
String CLIFFORD_URI = "http://dbpedia.org/resource/Clifton_Pugh";

void setup() {
  int w = 1024;
  int h = 768;
  size(w, h , P3D);
  
  initZoom = 1500;
  dispHolder = Collections.synchronizedList(new ArrayList<String>());
  defaultDisp = "Hover over graph elements for information:\n\n";
  dispHolder.add(defaultDisp);
  
  cam = new PeasyCam(this, 0, 0, 0, initZoom);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(10000);
  
  
  cp5 = new ControlP5(this);
  proj = new UnProjector();
  
  graph = new Graph(proj, cp5);
  
  virtAddNeighbors(PICASSO_URI);
  virtAddNeighbors(EDVARD_URI);
  virtAddNeighbors(CLIFFORD_URI);
  
  tf = addTestFrame("Test Controls", cam);
  
  CallbackListener hoverer = new CallbackListener() {
    public void controlEvent(CallbackEvent event) {
      if (event.getAction() == ControlP5.ACTION_ENTER) {
        if (event.getController() instanceof Node) {
          Node n = (Node) event.getController();
          dispHolder.add(getNodeString(n));
        } else {
          Edge e = (Edge) event.getController();
          dispHolder.add(getEdgeString(e));
        }
      } else if (event.getAction() == ControlP5.ACTION_LEAVE) {
        if (event.getController() instanceof Node) {
          Node n = (Node) event.getController();
          dispHolder.remove(getNodeString(n));
        } else {
          Edge e = (Edge) event.getController();
          dispHolder.remove(getEdgeString(e));
        }
      }
    }
  };
  cp5.addCallback(hoverer);

}

void draw() {
  if (tf.layout.getItem(0).getState()) {
    graph.layout();
  }
  
  proj.captureViewMatrix((PGraphics3D) this.g);
  
  proj.calculatePickPoints(mouseX, mouseY);
  pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
  
  background(0xFFE07924);
}

String getStarQuery(String URI) {
  return "CONSTRUCT { <" + URI + "> ?p ?o } WHERE { GRAPH <" + GRAPH + "> { <" + URI + "> ?p ?o } }";
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

/*
*  TEST CONTROL WINDOW
*/

TestFrame addTestFrame(String theName, PeasyCam c) {
  Frame f = new Frame(theName);
  TestFrame p = new TestFrame(this, c);
  f.add(p);
  p.init();
  f.setTitle(theName);
  f.setSize(p.w, p.h);
  f.setLocation(0, 0);
  f.setResizable(true);
  f.setVisible(true);
  return p;
}

public class TestFrame extends PApplet {
  
  ControlP5 _cp5;
  Object _parent;
  
  PeasyCam viewCam;
  
  int w, h;
  
  public Slider zoom;
  public RadioButton layout;
  public Textlabel info;
  
  public String def;
  
  public void setup() {
    size(w, h);
    frameRate(25);
    _cp5 = new ControlP5(this);
    
    zoom = _cp5.addSlider("Zoom")
               .setPosition(10, 10)
               .setSize(200, 20)
               .setRange(100, 3000)
               .setValue(initZoom);
               
    zoom.addCallback(new CallbackListener() {
      public void controlEvent(CallbackEvent event) {
        if (event.getAction() == ControlP5.ACTION_BROADCAST) {
          viewCam.setDistance(zoom.getValue());
        }
      }
    }
    );
               
    layout = _cp5.addRadioButton("layout")
                 .setPosition(10, 50)
                 .setSize(40,20)
                 .setColorForeground(color(120))
                 .setColorActive(color(255))
                 .setColorLabel(color(255))
                 .setItemsPerRow(1)
                 .setSpacingColumn(50)
                 .addItem("LAYOUT",0);
                 
    Toggle t = layout.getItem(0);
    t.captionLabel().setColorBackground(color(255,80));
    t.captionLabel().style().moveMargin(-7,0,0,-3);
    t.captionLabel().style().movePadding(7,0,0,3);
    t.captionLabel().style().backgroundWidth = 45;
    t.captionLabel().style().backgroundHeight = 13;
    
               
    info = _cp5.addTextlabel("Info")
               .setText(defaultDisp)
               .setPosition(10, 100)
               .setColorValue(0xffffff00)
               .setFont(createFont("Georgia",16));
  }
  
  public void draw() {
    String toDisp = "";
    for (String s : dispHolder) {
      toDisp += s;
    }
    info.setText(toDisp);
    background(0);
  }
  
  private TestFrame() {
    
  }
  
  public TestFrame(Object P, PeasyCam c) {
    _parent = P;
    viewCam = c;
    
    w = 600;
    h = 600;
  }
  
  public ControlP5 control() {
    return _cp5;
  }
}

String getNodeString(Node n) {
  return "Node:\n  " + n.getName() + "\n\n";
}

String getEdgeString(Edge e) {
  String toAdd = "Edge between:\n  "
                 + e.src.getName() + "\nand\n  " + e.dst.getName()
                 + "\nwith predicates\n  ";
  
  for (String p : e.predicates) {
    toAdd += p + "\n  ";
  }
  return toAdd + "\n\n";
}
