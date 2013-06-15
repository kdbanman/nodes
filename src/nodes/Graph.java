
package nodes;

import com.hp.hpl.jena.graph.Triple;
import java.util.HashMap;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.*;

import controlP5.ControlP5;
import java.util.HashSet;

import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author kdbanman
 * 
 * 
 */
public class Graph {
  UnProjector proj;
  ControlP5 cp5;
  PApplet pApp;
  
  Model triples;
  
  int nodeCount;
  int edgeCount;
  
  // adjacent maps node ids (uris and literal values) to lists of node ids
  // NOTE: it's formally redundant to include a set of edges along with
  //       an adjacency list, but it's definitely convenient
  
  HashMap<Node, ArrayList<Node>> adjacent;
  HashSet<Edge> edges;
  
  Graph(UnProjector u, ControlP5 c, PApplet p) {
    proj = u;
    cp5 = c;
    pApp = p;
    
    triples = ModelFactory.createDefaultModel();
    
    nodeCount = 0;
    edgeCount = 0;
    
    adjacent = new HashMap<>();
    edges = new HashSet<>();
    
  }
  
  public void layout() {
    HashMap<Node, PVector> deltas = new HashMap<>();
    
    float coulomb = 10000;
    float hooke = .05f;
    
    float saturation = 50;
    
    for (Node node : adjacent.keySet()) {
      deltas.put(node, new PVector(0,0,0));
    }
    for (Node n : adjacent.keySet()) {
      PVector delta = deltas.get(n);
      PVector nodePos = n.getPosition();
      
      for (Node nbr : adjacent.get(n)) {
        PVector diff = nbr.getPosition().get();
        diff.sub(nodePos);
        float dist = diff.mag();
        diff.normalize();
        diff.mult(hooke * PApplet.sq(dist));
        delta.add(diff);
      }
      
      for (Node other : adjacent.keySet()) {
        if (!other.equals(n)) {
          float degreeScale = (float) (getDegree(other) * getDegree(other));
          
          PVector diff = other.getPosition().get();
          diff.sub(nodePos);
          
          float dist = diff.mag();
          
          diff.normalize();
          diff.mult(degreeScale * coulomb / dist);
          delta.sub(diff);
        }
      }
    }
    
    for (Node node : adjacent.keySet()) {
      //apply damping and set
      PVector delta = deltas.get(node);
      float mag = delta.mag();
      delta.limit(PApplet.log(mag)/PApplet.log(2));
      
      PVector pos = node.getPosition();
      pos.add(delta);
      //print(deltas.get(nodeID));
    }
  }
  
  /*
  * adds triples to model, adding (transformed) Nodes and Edges as necessary
  */
  public void addTriples(Model toAdd) {
    StmtIterator it = toAdd.listStatements();
    if (it.hasNext()) {
      while (it.hasNext()) {
        Statement s = it.nextStatement();
        addTriple(s);
      }
    } else {
      PApplet.println("Empty query result.");
    }
    
    triples.add(toAdd);
  }
  
  public Edge addTriple(String sub, String pred, String obj) {
      Edge e;
    
    // add*** just returns the existing *** if a new *** need not be created.  (think sets)
    addNode(sub);
    addNode(obj);
    
    e = addEdge(sub, obj);
    if (e.src.getName().equals(sub)) {
      e.predicates.put(pred, true);
    } else {
      e.predicates.put(pred, false);
    }
    return e;
  }
  public Edge addTriple(Statement triple) {
    String sub = triple.getSubject().toString();
    String pred = triple.getPredicate().toString();
    String obj = triple.getObject().toString();
    
    return addTriple(sub, pred, obj);
  }
  public Edge addTriple(Triple triple) {
    String sub = triple.getSubject().toString();
    String pred = triple.getPredicate().toString();
    String obj = triple.getObject().toString();
    
    return addTriple(sub, pred, obj);
  }
  
  /* TODO:  this cast might fuck stuff.  test after the refactor.
  public HashSet<Node> getNodes() {
      return (HashSet) adjacent.keySet();
  }
  public HashSet<Edge> getEdges() {
      return edges;
  }
  */
  
  /**
   * returns null if node nonexistent
   */
  public ArrayList<Node> getNbrs(String id) {
    return adjacent.get((Node) cp5.getController(id));
  }
   /**
   * returns null if node nonexistent
   */
  public ArrayList<Node> getNbrs(Node n) {
    return adjacent.get(n);
  }
  
  // return node's degree for view graph, not for the relational graph
  public int getDegree(String id) {
    return getNbrs(id).size();
  }
  public int getDegree(Node n) {
    return getNbrs(n).size();
  }
  
  /*
  * to be called by addTriple and .
  * affects cp5, nodeCount, and adjacent iff the node is new.
  * a new entry in adjacent will map to an empty ArrayList since no edges may exist yet.
  *
  * returns the new node or the existing node.
  */
  private Node addNode(String id) {
      
    // ControlP5's source has been checked, this should be reliable and fast
    Node n = (Node) cp5.getController(id);
    if (n != null) {
      return n;
    } else {
      int initBoundary = 500;
      n = new Node(cp5, id, proj, pApp)  
                    .setPosition(pApp.random(-initBoundary,initBoundary), 
                                 pApp.random(-initBoundary,initBoundary), 
                                 pApp.random(-initBoundary,initBoundary))
                    .setSize(10);
                    
      adjacent.put(n, new ArrayList<Node>());
      
      nodeCount += 1;
      
      return n;
    }
  }
  
  /*
  * to be called by addTriple, both nodes must exist.
  * affects cp5, edgeCount, and adjacent iff the edge is new.
  * 
  * returns the new edge or the existing edge.
  */
  private Edge addEdge(String s, String d) {
    
    Node src = (Node) cp5.getController(s);
    Node dst = (Node) cp5.getController(d);
    
    // make sure the nodes exist
    if (src == null || dst == null) {
        printNullEdgeTargets(s, d, src, dst);
    }
    
    if (adjacent.get(src).contains(dst)) {
      return getEdge(s, d);
    } else {
      
      Edge e = new Edge(cp5, s + "|" + d, proj, pApp, src, dst).setSize(5);
      
      adjacent.get(src).add(dst);
      adjacent.get(dst).add(src);
      edgeCount += 1;
      
      return e;
    }
    
  }
  private Edge addEdge(Node s, Node d) {
    return addEdge(s.getName(), d.getName());
  }
  
  /*
  * returns true if successful, false otherwise.
  */ 
  private boolean removeNode(String id) {
    
    Node n = (Node) cp5.getController(id);
    
    if (n == null) {
      PApplet.println("ERROR: Cannot remove nonexistent node\n" + id);
      return false;
    } else if (!adjacent.get(n).isEmpty()) {
      PApplet.println("ERROR: Cannot remove still-connected node\n" + id);
      return false;
    } else {
      //node exists and has no neighbors
      
      adjacent.remove(n);
      nodeCount -= 1;
      
      n.remove();

      return true;
    }
  }
  private boolean removeNode(Node n) {
    return removeNode(n.getName());
  }
  
  /*
  * returns true if successful, false otherwise.
  */ 
  private boolean removeEdge(String s, String d) {
    
    Node src = (Node) cp5.getController(s);
    Node dst = (Node) cp5.getController(d);
      
    if (src == null || dst == null) {
      printNullEdgeTargets(s, d, src, dst);
      return false;
    } else if (!adjacent.get(src).contains(dst)) {
      PApplet.println("Cannot remove nonexistent edge between:\n"+s+"\n"+d);
      return false;
    } else {
      Edge e = getEdge(s, d);
      
      e.remove();
      
      adjacent.get(src).remove(dst);
      adjacent.get(dst).remove(src);
      
      edgeCount -= 1;
      return true;
    }
  }
  private boolean removeEdge(Node s, Node d) {
    return removeEdge(s.getName(), d.getName());
  }
  private boolean removeEdge(Edge e) {
    return removeEdge(e.src, e.dst);
  }
  
  public Edge getEdge(String s, String d) {
    Edge e = (Edge) cp5.getController(s + "|" + d);
      
    if (e == null) e = (Edge) cp5.getController(d + "|" + s);
    
    if (e == null) PApplet.println("Edge connecting\n"+s+"\nand\n"+d+"\nnot found.");
    return e;
  }
  
  private void printNullEdgeTargets(String s, String d, Node src, Node dst) {
        PApplet.println("ERROR: Edge cannot be created between /n" 
                + s + " and /n" + d);
        String problem = (src == null) ? s : d;
        PApplet.println("   " + problem + "/n  doesn't exist as Node.");
  }
}
