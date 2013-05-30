/*
Graph data structure whose nodes and edges are UI controllers in 3D.

The graph is an undirected graph without parallel edges, and it is 
meant to be a view abstraction over the directed multigraph RDF model
within the enclosed jena Model.

Node ids are the URIs or literal values that they represent.
Edge ids are the src and dst Node ids concatenated about "|".
  note: be careful of order when constructing edge ids.

Redundancy in representation:  an adjacency list keeps track of
edges, and there are also Edge objects which know a source and 
destination Node.
*/


import java.util.HashMap;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

class Graph {
  
  UnProjector proj;
  ControlP5 cp5;
  
  Model triples;
  
  int nodeCount;
  int edgeCount;
  
  // adjacent maps node ids (uris and literal values) to lists of node ids
  // NOTE: if things get slow for edge operations, try Set instead of ArrayList 
  HashMap<String, ArrayList<String>> adjacent;
  
  Graph(UnProjector p, ControlP5 c) {
    proj = p;
    cp5 = c;
    
    triples = ModelFactory.createDefaultModel();
    
    nodeCount = 0;
    edgeCount = 0;
    
    adjacent = new HashMap<String, ArrayList<String>>();
  }
  
  /*
  TODO
  * adds triples to model, adding (transformed) Nodes and Edges as necessary
  *
  public void importTriples(Model toAdd) {
    
  }
  */
  
  public ArrayList<String> getNbrs(String id) {
    return adjacent.get(id);
  }
  public ArrayList<String> getNbrs(Node n) {
    return getNbrs(n.getName());
  }
  
  /* TODO
  public Edge addTriple(Statement triple) {
    
  }
  */
  
  /*
  * to be called by addTriple and .
  * affects cp5, nodeCount, and adjacent iff the node is new.
  * a new entry in adjacent will map to an empty ArrayList since no edges may exist yet.
  *
  * returns the new node or the existing node.
  */
  private Node addNode(String id) {
    if (adjacent.containsKey(id)) {
      return (Node) cp5.getController(id);
    } else {
      Node n = new Node(cp5, id, proj)  
                    .setPosition(0,0,0)
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
  private Edge addEdge(String s, String d) {
    if (adjacent.get(s).contains(d)) {
      Edge e = (Edge) cp5.getController(s + "|" + d);
      
      if (e == null) e = (Edge) cp5.getController(d + "|" + s);
      
      if (e == null) print("EDGE CONNECTING \n"+s+"\nAND\n"+d+"\nNOT FOUND");
      return e;
    } else {
      Node src = (Node) cp5.getController(s);
      Node dst = (Node) cp5.getController(d);
      Edge e = new Edge(cp5, s + "|" + d, proj, src, dst).setSize(5);
      
      adjacent.get(s).add(d);
      adjacent.get(d).add(s);
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
    if (!adjacent.containsKey(id)) {
      print("Cannot remove nonexistent node\n" + id);
      return false;
    } else if (adjacent.get(id).isEmpty()) {
      print("Cannot remove still-connected node\n" + id);
      return false;
    } else {
      //node exists and has no neighbors
      cp5.getController(id).remove();
      
      adjacent.remove(id);
      nodeCount -= 1;
      
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
    if ( ! (adjacent.containsKey(s) && adjacent.containsKey(d))) {
      print("Cannot remove edge, one of src or dst doesn't exist:\n"+s+"\n"+d);
      return false;
    } else if (!adjacent.get(s).contains(d)) {
      print("Cannot remove nonexistent edge between:\n"+s+"\n"+d);
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
  private boolean removeEdge(Node s, Node d) {
    return removeEdge(s.getName(), d.getName());
  }
  private boolean removeEdge(Edge e) {
    return removeEdge(e.src, e.dst);
  }
  
}
