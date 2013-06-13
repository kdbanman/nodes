/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import processing.core.*;

import controlP5.ControlP5;
import controlP5.CallbackEvent;
import controlP5.Controller;


/**
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
    
  @Override
  public void setup() {
    int w = 400;
    int h = 300;
    size(w,h);
    
    ControlWindow cw = new ControlWindow();
  }

  @Override
  public void draw() {
    
    background(0xFFE07924);

  }

   /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
      PApplet.main(new String[]{nodes.Nodes.class.getName()});
  }
}
