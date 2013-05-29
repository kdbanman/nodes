// PROBLEM: the triples may form antiparallel edges, so the view graph cannot be directed.
//          think more then make the appropriate changes.  maybe there is a more clever
//          representation with the HashMaps.  or would a matrix be better?  is there a
//          representation that doesn't risk having a u,v edge without a v,u edge?
//          rephrased, is there an unordered pair thing? maybe i should make that thing.\
//          or maybe i should maintain the nodes and transpose and don't be an idiot.

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
  
  // nodes maps node ids (uris and literal values) to lists of node ids
  HashMap<String, ArrayList<String>> nodes;
  HashMap<String, ArrayList<String>> transpose;
  
  Graph(UnProjector p, ControlP5 c) {
    proj = p;
    cp5 = c;
    
    triples = ModelFactory.createDefaultModel();
    
    nodeCount = 0;
    edgeCount = 0;
    
    nodes = new HashMap<String, ArrayList<String>>();
  }
  
  /*
  TODO
  * adds triples to model, adding (transformed) Nodes and Edges as necessary
  *
  public void importTriples(Model toAdd) {
    
  }
  */
  
  public ArrayList<String> getNbrs(String id) {
    return nodes.get(id);
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
  * affects cp5, nodeCount, and nodes/transpose iff the node is new.
  * a new entry in nodes will map to an empty ArrayList since no edges may exist yet.
  *
  * returns the new node or the existing node.
  */
  private Node addNode(String id) {
    if (nodes.containsKey(id)) {
      return (Node) cp5.getController(id);
    } else {
      Node n = new Node(cp5, id, proj)  
                    .setPosition(0,0,0)
                    .setSize(10);
                    
      nodes.put(id, new ArrayList<String>());
      transpose.put(id, new ArrayList<String>());
      
      nodeCount += 1;
      
      return n;
    }
  }
  
  /*
  * to be called by addTriple, both nodes must exist.
  * affects cp5, edgeCount, and nodes/transpose iff the edge is new.
  * 
  * returns the new edge or the existing edge.
  */
  private Edge addEdge(String s, String d) {
    if (nodes.get(s).contains(d) || transpose.get(d).contains(s)) {
      return (Edge) cp5.getController(s + "|" + d);
    } else {
      Node src = (Node) cp5.getController(s);
      Node dst = (Node) cp5.getController(d);
      Edge e = new Edge(cp5, s + "|" + d, proj, src, dst).setSize(5);
      
      nodes.get(s).add(d);
      transpose.get(d).add(s);
      edgeCount += 1;
      
      return e;
    }
    
  }
  private Edge addEdge(Node s, Node d) {
    return addEdge(s.getName(), d.getName());
  }
  
  /* TODO
  private boolean removeNode(String id) {
    
  }
  private boolean removeNode(Node n) {
    
  }
  
  private boolean removeEdge(String s, String d) {
    
  }
  private boolean removeEdge(Node s, Node d) {
    
  }
  private boolean removeEdge(Edge e) {
    
  }
  */
}
