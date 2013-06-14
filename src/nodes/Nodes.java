/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import com.hp.hpl.jena.graph.Triple;


import processing.core.*;

import controlP5.ControlP5;
import controlP5.CallbackEvent;
import peasy.PeasyCam;


/**
 * Non-controlP5 related mouse and key interaction is here:
 * - mouse selection modification
 * 
 * @author kdbanman
 */
public class Nodes extends PApplet {
    
  /*
   * Main components not written as separate class within package:
   * - Program state tree (may not be necessary if ControlP5 is reasonably queryable
   * - Local Jena model to store all incoming data
   * - Subgraph cache for data mid-manipulation
   */
    PeasyCam cam;
    ControlP5 cp5;
    UnProjector proj;
    Graph graph;
    Selection selection;
    
    int lastPressedX;
    int lastPressedY;
    
  @Override
  public void setup() {
    int w = 400;
    int h = 300;
    size(w, h, P3D);
    
    ControlPanel panel = new ControlPanel();
    
    cam = new PeasyCam(this, 0, 0, 0, 50);
    
    
    cp5 = new ControlP5(this);
    proj = new UnProjector(this);
    graph = new Graph(proj, cp5, this);
    selection = new Selection();
    
    lastPressedX = 0;
    lastPressedY = 0;
    
    /* test code for next commit - nonfuctional for now 
    com.hp.hpl.jena.graph.Node sub = new com.hp.hpl.jena.graph.Node("the");
    com.hp.hpl.jena.graph.Node pred = new com.hp.hpl.jena.graph.Node("butt");
    com.hp.hpl.jena.graph.Node ob = new com.hp.hpl.jena.graph.Node("donkey");
    graph.addTriple(new Triple(sub, pred, ob));
    */
  }

  @Override
  public void draw() {
    
    background(0xFFE07924);

  }
  
  @Override
  public void mousePressed() {
      lastPressedX = mouseX;
      lastPressedY = mouseY;
  }
  
  @Override
  public void mouseDragged() {
      int minX = min(mouseX, lastPressedX);
      int minY = min(mouseY, lastPressedY);
      
      // min gives direction between near and far frustum planes @ drag box min
      proj.calculatePickPoints(minX, minY);
      PVector min = proj.ptEndPos.get();
      min.sub(proj.ptStartPos);
      
      int maxX = max(mouseX, lastPressedX);
      int maxY = max(mouseY, lastPressedY);
      
      proj.calculatePickPoints(maxX, maxY);
      PVector max = proj.ptEndPos.get();
      max.sub(proj.ptStartPos);
      
      print(min);
      print("  ");
      println(max);
      
      // angular sweep of drag from camera in X direction
      
      // angular sweep of drag from camera in Y direction
      
      
  }

   /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
      PApplet.main(new String[]{nodes.Nodes.class.getName()});
  }
}
