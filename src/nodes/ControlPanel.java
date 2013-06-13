/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.ControlP5;
import controlP5.ListBox;
import controlP5.RadioButton;
import controlP5.Tab;
import processing.core.PApplet;

import java.awt.Frame;

/**
 *
 * @author kdbanman
 */
public class ControlPanel extends PApplet {
    int w, h;
    String name;
    
    ControlP5 cp5;
    
    public ControlPanel() {
        w = 400;
        h = 500;
        
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
        
        Tab importTab = cp5.addTab("Import")
                .setSize(w / 4, 30)
                .setActive(true);
        Tab transformTab = cp5.addTab("Transform")
                .setSize(w / 4, 30);
        Tab optionTab = cp5.addTab("Options")
                .setSize(w / 4, 30);
        Tab saveTab = cp5.addTab("Save")
                .setSize(w / 4, 30);
        cp5.getDefaultTab().remove();
        
        // Import tab elements
        cp5.addTextfield("IP:Port", 10, 50, w - 60, 20)
                .setAutoClear(false)
                .moveTo(importTab);
        cp5.addTextfield("Username", 10, 100, w - 60, 20)
                .setAutoClear(false)
                .moveTo(importTab);
        cp5.addTextfield("Password", 10, 150, w - 60, 20)
                .setAutoClear(false)
                .setPasswordMode(true)
                .moveTo(importTab);
        cp5.addTextfield("Query", 10, 200, w - 60, 20)
                .setAutoClear(false)
                .moveTo(importTab);
        
        cp5.addButton("Add to Graph")
           .setPosition(280, 430)
           .moveTo(importTab);
        
        // Transform tab elements
        ListBox modifiers = cp5.addListBox("Selection Modifiers", 10, 50, w - 60, 200)
                .setBarHeight(25)
                .moveTo(transformTab);
        modifiers.addItem("Select all neighbors", 0);
        modifiers.addItem("Select all nodes of same rdf:Type", 1);
        modifiers.addItem("Select shortest path between nodes", 2);
        modifiers.addItem("Select subgraph of the same namespace", 3);
        modifiers.addItem("Select similar resources", 4);
        modifiers.addItem("Select nodes sharing this predicate", 5);
        modifiers.addItem("Select nodes sharing this predicate and object", 6);
        
        // nested tabs are sketchy.  use Groups activated by toggles, even
        // thought that's stupid.
        Tab positionTab = cp5.addTab("Position")
                .setPosition(0, 300)
                .setSize(w / 4, 30)
                .moveTo(transformTab);
        Tab colorTab = cp5.addTab("Color")
                .setPosition(w / 4, 300)
                .setSize(w / 4, 30)
                .moveTo(transformTab);
        Tab labelTab = cp5.addTab("Label")
                .setPosition(w / 2, 300)
                .setSize(w / 4, 300)
                .moveTo(transformTab);
        Tab hideTab = cp5.addTab("Hide")
                .setPosition(3 * (w / 4), 300)
                .setSize(w / 4, 300)
                .moveTo(transformTab);
        
        RadioButton positionRadio = cp5.addRadioButton("Layout Choice", 10, 350)
                .moveTo(positionTab);
        
        positionRadio.addItem("Drag and Drop", 0);
        positionRadio.addItem("Auto Layout", 1);
        
        
        // Options tab elements
        
        // Save tab elements
    }
    
    public void draw() {
        
        background(0);
    }
}
