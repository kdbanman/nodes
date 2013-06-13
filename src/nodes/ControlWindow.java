/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.ControlP5;
import controlP5.ListBox;
import controlP5.Tab;
import processing.core.PApplet;

import java.awt.Frame;

/**
 *
 * @author kdbanman
 */
public class ControlWindow extends PApplet {
    int w, h;
    String name;
    
    ControlP5 cp5;
    
    public ControlWindow() {
        w = 400;
        h = 600;
        
        name = "Control Panel";
        
        Frame f = new Frame(name);
        f.add(this);
        this.init();
        f.setTitle(name);
        f.setSize(w, h);
        f.setLocation(0, 0);
        f.setResizable(false);
        f.setVisible(true);
    }
    
    public void setup() {
        cp5 = new ControlP5(this);
        
        Tab importTab = cp5.addTab("Import");
        Tab transformTab = cp5.addTab("Transform");
        Tab saveTab = cp5.addTab("Save");
        cp5.getDefaultTab().remove();
        
        // Import tab elements
        cp5.addTextfield("IP:Port", 10, 50, w - 60, 20)
           .moveTo(importTab);
        cp5.addTextfield("Username", 10, 100, w - 60, 20)
           .moveTo(importTab);
        cp5.addTextfield("Password", 10, 150, w - 60, 20)
           .moveTo(importTab);
        cp5.addTextfield("Query", 10, 200, w - 60, 200)
           .moveTo(importTab);
        
        cp5.addButton("Add to Graph")
           .setPosition(10, 500)
           .moveTo(importTab);
        
        // Transform tab elements
        ListBox modifiers = cp5.addListBox("Selection Modifiers", 10, 50, w - 60, 200)
           .moveTo(transformTab);
        modifiers.addItem("Select all neighbors", 20);
        modifiers.addItem("Select all nodes of same rdf:Type", 20);
        modifiers.addItem("Select shortest path between nodes", 20);
        modifiers.addItem("Select subgraph of the same namespace", 20);
        modifiers.addItem("Select similar resources", 20);
        modifiers.addItem("Select nodes sharing this predicate", 20);
        modifiers.addItem("Select nodes sharing this predicate and object", 20);
        
        
    }
    
    public void draw() {
        
        background(0);
    }
}
