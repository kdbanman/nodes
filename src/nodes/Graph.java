/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import java.util.HashMap;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.*;
import controlP5.ControlP5;
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
  // NOTE: if things get slow for edge operations, try Set instead of ArrayList 
  HashMap<String, ArrayList<String>> adjacent;
  
  Graph(UnProjector u, ControlP5 c, PApplet p) {
    proj = u;
    cp5 = c;
    pApp = p;
    
    triples = ModelFactory.createDefaultModel();
    
    nodeCount = 0;
    edgeCount = 0;
    
    adjacent = new HashMap<String, ArrayList<String>>();
  }
  
  public void layout() {
    HashMap<String, PVector> deltas = new HashMap<String, PVector>();
    
    float coulomb = 10000;
    float hooke = .05f;
    
    float saturation = 50;
    
    for (String nodeID : adjacent.keySet()) {
      deltas.put(nodeID, new PVector(0,0,0));
    }
    for (String nodeID : adjacent.keySet()) {
      PVector delta = deltas.get(nodeID);
      Node n = (Node) cp5.getController(nodeID);
      PVector nodePos = n.getPosition();
      
      for (String nbrID : adjacent.get(nodeID)) {
        PVector diff = cp5.getController(nbrID).getPosition().get();
        diff.sub(nodePos);
        float dist = diff.mag();
        diff.normalize();
        diff.mult(hooke * PApplet.sq(dist));
        delta.add(diff);
      }
      
      for (String otherID : adjacent.keySet()) {
        if (!otherID.equals(nodeID)) {
          float degreeScale = (float) (getDegree(otherID) * getDegree(nodeID));
          
          PVector diff = cp5.getController(otherID).getPosition().get();
          diff.sub(nodePos);
          
          float dist = diff.mag();
          
          diff.normalize();
          diff.mult(degreeScale * coulomb / dist);
          delta.sub(diff);
        }
      }
    }
    
    for (String nodeID : adjacent.keySet()) {
      //apply damping and set
      PVector delta = deltas.get(nodeID);
      float mag = delta.mag();
      delta.limit(PApplet.log(mag)/PApplet.log(2));
      
      PVector pos = cp5.getController(nodeID).getPosition();
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
  
  public Edge addTriple(Statement triple) {
    String sub = triple.getSubject().toString();
    String pred = triple.getPredicate().toString();
    String obj = triple.getObject().toString();
    
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
  
  
  public ArrayList<String> getNbrs(String id) {
    return adjacent.get(id);
  }
  public ArrayList<String> getNbrs(Node n) {
    return getNbrs(n.getName());
  }
  
  // return node's degree for view graph, not for the relational graph
  public int getDegree(String id) {
    return getNbrs(id).size();
  }
  public int getDegree(Node n) {
    return getDegree(n.getName());
  }
  
  /*
  * to be called by addTriple and .
  * affects cp5, nodeCount, and adjacent iff the node is new.
  * a new entry in adjacent will map to an empty ArrayList since no edges may exist yet.
  *
  * returns the new node or the existing node.
  */
  public Node addNode(String id) {
    if (adjacent.containsKey(id)) {
      return (Node) cp5.getController(id);
    } else {
      int initBoundary = 500;
      Node n = new Node(cp5, id, proj, pApp)  
                    .setPosition(pApp.random(-initBoundary,initBoundary), 
                                 pApp.random(-initBoundary,initBoundary), 
                                 pApp.random(-initBoundary,initBoundary))
                    .setSize(10);
                    
      adjacent.put(id, new ArrayList<String>());
      
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
  public Edge addEdge(String s, String d) {
    if (adjacent.get(s).contains(d)) {
      return getEdge(s, d);
    } else {
      Node src = (Node) cp5.getController(s);
      Node dst = (Node) cp5.getController(d);
      Edge e = new Edge(cp5, s + "|" + d, proj, pApp, src, dst).setSize(5);
      
      adjacent.get(s).add(d);
      adjacent.get(d).add(s);
      edgeCount += 1;
      
      return e;
    }
    
  }
  public Edge addEdge(Node s, Node d) {
    return addEdge(s.getName(), d.getName());
  }
  
  /*
  * returns true if successful, false otherwise.
  */ 
  public boolean removeNode(String id) {
    if (!adjacent.containsKey(id)) {
      PApplet.println("Cannot remove nonexistent node\n" + id);
      return false;
    } else if (!adjacent.get(id).isEmpty()) {
      PApplet.println("Cannot remove still-connected node\n" + id);
      return false;
    } else {
      //node exists and has no neighbors
      cp5.getController(id).remove();
      
      adjacent.remove(id);
      nodeCount -= 1;
      
      return true;
    }
  }
  public boolean removeNode(Node n) {
    return removeNode(n.getName());
  }
  
  /*
  * returns true if successful, false otherwise.
  */ 
  public boolean removeEdge(String s, String d) {
    if ( ! (adjacent.containsKey(s) && adjacent.containsKey(d))) {
      PApplet.println("Cannot remove edge, one of src or dst doesn't exist:\n"+s+"\n"+d);
      return false;
    } else if (!adjacent.get(s).contains(d)) {
      PApplet.println("Cannot remove nonexistent edge between:\n"+s+"\n"+d);
      return false;
    } else {
      Edge e = (Edge) cp5.getController(s + "|" + d);
      if (e == null) e = (Edge) cp5.getController(d + "|" + s);
      
      e.remove();
      
      adjacent.get(s).remove(d);
      adjacent.get(d).remove(s);
      
      edgeCount -= 1;
      return true;
    }
  }
  public boolean removeEdge(Node s, Node d) {
    return removeEdge(s.getName(), d.getName());
  }
  public boolean removeEdge(Edge e) {
    return removeEdge(e.src, e.dst);
  }
  
  public Edge getEdge(String s, String d) {
    Edge e = (Edge) cp5.getController(s + "|" + d);
      
    if (e == null) e = (Edge) cp5.getController(d + "|" + s);
    
    if (e == null) PApplet.println("Edge connecting\n"+s+"\nand\n"+d+"\nnot found.");
    return e;
  }
}
