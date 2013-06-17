/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.ControlP5;
import controlP5.ControlFont;
import controlP5.Group;
import controlP5.ListBox;
import controlP5.Tab;
import processing.core.PApplet;

import java.awt.Frame;
import java.util.ArrayList;

/**
 *
 * @author kdbanman
 */
public class ControlPanel extends PApplet {
    int w, h;
    String name;
    
    ControlP5 cp5;
    
    // TODO:  use these fonts.
    ControlFont tabFont;
    ControlFont button;
    
    int tabHeight;
    int padding;
    int elementHeight;
    int labelledElementHeight;
    int buttonWidth;
    int buttonHeight;
    int modifiersBoxHeight;
    
    
    ArrayList<Group> transformHackTabs;
    Group openTransformHackTab;
    
    int colorPickerDefault;
    
    public ControlPanel() {
        // window parameters
        w = 400;
        h = 500;
        
        name = "Control Panel";
                
        // element size parameters
        padding = 10;
        
        tabHeight = 30;
        
        elementHeight = 20;
        
        labelledElementHeight = 40;
        
        buttonWidth = 100;
        buttonHeight = 40;
        
        modifiersBoxHeight = 200;
        
        // control element miscellany
        transformHackTabs = new ArrayList<>();
        
        colorPickerDefault = 0xFF1A4969;
        
        // window frame initialization.
        // keep this section LAST in the constructor because this.init() depends
        // upon things declared above
        Frame f = new Frame(name);
        f.add(this);
        this.init();        
        f.setTitle(name);
        f.setSize(w, h);
        f.setLocation(0, 0);
        f.setResizable(false);
        f.setVisible(true);
    }
    
    @Override
    public void setup() {
        cp5 = new ControlP5(this);
        
        Tab importTab = cp5.addTab("Import")
                .setWidth(w / 4)
                .setHeight(tabHeight)
                .setActive(true);
        Tab transformTab = cp5.addTab("Transform")
                .setWidth(w / 4)
                .setHeight(tabHeight);
        Tab optionTab = cp5.addTab("Options")
                .setWidth(w / 4)
                .setHeight(tabHeight);
        Tab saveTab = cp5.addTab("Save")
                .setWidth(w / 4)
                .setHeight(tabHeight);
        cp5.getDefaultTab().remove();
        
        // Import tab elements
        cp5.addTextfield("IP:Port", 
                    padding, 
                    tabHeight + padding, 
                    w - 2 * padding, 
                    elementHeight)
                .setAutoClear(false)
                .moveTo(importTab);
        cp5.addTextfield("Username", 
                    padding, 
                    tabHeight + labelledElementHeight + padding, 
                    w - 2 * padding, 
                    elementHeight)
                .setAutoClear(false)
                .moveTo(importTab);
        cp5.addTextfield("Password", 
                    padding, 
                    tabHeight + 2 * labelledElementHeight + padding, 
                    w - 2 * padding, 
                    elementHeight)
                .setAutoClear(false)
                .setPasswordMode(true)
                .moveTo(importTab);
        cp5.addTextfield("Query", 
                    padding, 
                    tabHeight + 3 * labelledElementHeight + padding, 
                    w - 2 * padding, 
                    elementHeight)
                .setAutoClear(false)
                .moveTo(importTab);
        
        cp5.addButton("Add to Graph")
                .setSize(buttonWidth, buttonHeight)
                .setPosition(w - buttonWidth - padding, 
                    tabHeight + 4 * labelledElementHeight + padding)
                .moveTo(importTab);
        
        // Transform tab elements
        ListBox modifiers = cp5.addListBox("Selection Modifiers", 
                    padding, 
                    2 * tabHeight + padding, 
                    w - 2 * padding, 
                    modifiersBoxHeight)
                .setBarHeight(tabHeight)
                .setItemHeight(elementHeight)
                .setScrollbarWidth(elementHeight)
                .moveTo(transformTab);
        modifiers.addItem("Clear selection", 7);
        modifiers.addItem("Remove edges from selection", 8);
        modifiers.addItem("Remove nodes from selection", 9);
        modifiers.addItem("Select all nodes", 10);
        modifiers.addItem("Select all edges", 11);
        modifiers.addItem("Select entire graph", 12);
        modifiers.addItem("Select all neighbors", 0);
        modifiers.addItem("Select all nodes of same rdf:Type", 1);
        modifiers.addItem("Select shortest path between nodes", 2);
        modifiers.addItem("Select subgraph of the same namespace", 3);
        modifiers.addItem("Select similar resources", 4);
        modifiers.addItem("Select nodes sharing this predicate", 5);
        modifiers.addItem("Select nodes sharing this predicate and object", 6);
        
        int transformTabsVert = modifiersBoxHeight + 3 * tabHeight + padding;
        
        Group positionGroup = new HackTab(cp5, "Position")
                .setBarHeight(tabHeight)
                .setPosition(0, transformTabsVert)
                .setWidth(w / 4)
                .hideArrow()
                .setOpen(true)
                .moveTo(transformTab);
        Group colorGroup = new HackTab(cp5, "Color")
                .setBarHeight(tabHeight)
                .setPosition(w / 4, transformTabsVert)
                .setWidth(w / 4)
                .hideArrow()
                .setOpen(false)
                .moveTo(transformTab);
        Group labelGroup = new HackTab(cp5, "Label")
                .setBarHeight(tabHeight)
                .setPosition(w / 2, transformTabsVert)
                .setWidth(w / 4)
                .hideArrow()
                .setOpen(false)
                .moveTo(transformTab);
        Group hideGroup = new HackTab(cp5, "Hide")
                .setBarHeight(tabHeight)
                .setPosition(3 * (w / 4), transformTabsVert)
                .setWidth(w / 4)
                .hideArrow()
                .setOpen(false)
                .moveTo(transformTab);
        
        transformHackTabs.add(positionGroup);
        transformHackTabs.add(colorGroup);
        transformHackTabs.add(labelGroup);
        transformHackTabs.add(hideGroup);
        
        openTransformHackTab = positionGroup;
        
        cp5.addRadioButton("Layout Choice", padding, padding)
                .setPosition(padding, padding)
                .setItemHeight(elementHeight)
                .setItemWidth(elementHeight)
                .addItem("Drag and Drop", 0)
                .addItem("Auto Layout", 1)
                .activate(0)
                .moveTo(positionGroup);
        
        cp5.addColorPicker("Color Choice")
                .setPosition(-(w / 4) + padding, padding)
                .setColorValue(colorPickerDefault)
                .moveTo(colorGroup);
        
        cp5.addRadioButton("Label Visibility", padding, padding)
                .setPosition(padding - w / 2, padding)
                .setItemHeight(elementHeight)
                .setItemWidth(elementHeight)
                .addItem("Show Labels", 0)
                .addItem("Hide Labels", 1)
                .activate(0)
                .moveTo(labelGroup);
        
        cp5.addButton("Hide Nodes")
                .setPosition(padding - 3 * (w / 4), padding)
                .setSize(buttonWidth, buttonHeight)
                .moveTo(hideGroup);
        
        // Options tab elements
        
        // Save tab elements
    }
    
    public void draw() {
        
        for (Group g : transformHackTabs) {
          if (g.isOpen() && g != openTransformHackTab) {
            openTransformHackTab.setOpen(false);
            openTransformHackTab = g;
          }
        }
        
        background(0);
    }
    
    private class HackTab extends Group {
      
        HackTab(ControlP5 theControlP5, String theName) {
          super(theControlP5, theName);
        }
      
        protected void postDraw(PApplet theApplet) {
            if (isBarVisible) {
                theApplet.fill(isOpen ? color.getActive() : 
                        (isInside ? color.getForeground() : color.getBackground()));
                theApplet.rect(0, -1, _myWidth - 1, -_myHeight);
                _myLabel.draw(theApplet, 0, -_myHeight-1, this);
                if (isCollapse && isArrowVisible) {
                    theApplet.fill(_myLabel.getColor());
                    theApplet.pushMatrix();

                    if (isOpen) {
                            theApplet.triangle(_myWidth - 10, -_myHeight / 2 - 3, _myWidth - 4, -_myHeight / 2 - 3, _myWidth - 7, -_myHeight / 2);
                    } else {
                            theApplet.triangle(_myWidth - 10, -_myHeight / 2, _myWidth - 4, -_myHeight / 2, _myWidth - 7, -_myHeight / 2 - 3);
                    }
                    theApplet.popMatrix();
                }
            }
        }
    }
}
