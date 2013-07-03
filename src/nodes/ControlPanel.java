/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import com.hp.hpl.jena.rdf.model.Model;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.ControlFont;
import controlP5.Group;
import controlP5.ListBox;
import controlP5.Tab;
import controlP5.Textfield;

import processing.core.PApplet;

import java.util.ArrayList;

/**
 *
 * @author kdbanman
 */
public class ControlPanel extends PApplet {
    int w, h;
    
    ControlP5 cp5;
    Graph graph;
    
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
    
    ArrayList<Group> importHackTabs;
    Group openImportHackTab;
    
    ArrayList<Group> transformHackTabs;
    Group openTransformHackTab;
    
    Textfield importWebURI;
    
    int colorPickerDefault;
    
    public ControlPanel(int frameWidth, int frameHeight, Graph parentGraph) {
        w = frameWidth;
        h = frameHeight;
        
        graph = parentGraph;
        
        // element size parameters
        padding = 10;
        
        tabHeight = 30;
        
        elementHeight = 20;
        
        labelledElementHeight = 40;
        
        buttonWidth = 100;
        buttonHeight = 40;
        
        modifiersBoxHeight = 200;
        
        // control element miscellany
        importHackTabs = new ArrayList<>();
        transformHackTabs = new ArrayList<>();
        
        colorPickerDefault = 0xFF1A4969;
    }
    
    @Override
    public void setup() {
        size(w, h);
        
        cp5 = new ControlP5(this);
        
        // Main tabs
        
        Tab importTab = cp5.addTab("Get Triples")
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
        
        //====================
        // Import tab elements
        //====================
        
        // import subtabs
        
        int importTabsVert = 2 * tabHeight + padding;
        
        Group webGroup = new HackTab(cp5, "Web")
                .setBarHeight(tabHeight)
                .setPosition(0, importTabsVert)
                .setWidth(w / 4)
                .hideArrow()
                .setOpen(true)
                .moveTo(importTab);
        Group virtuosoGroup = new HackTab(cp5, "Virtuoso")
                .setBarHeight(tabHeight)
                .setPosition(w / 4, importTabsVert)
                .setWidth(w / 4)
                .hideArrow()
                .setOpen(false)
                .moveTo(importTab);
        Group exploreGroup = new HackTab(cp5, "Explore")
                .setBarHeight(tabHeight)
                .setPosition(w / 2, importTabsVert)
                .setWidth(w / 4)
                .hideArrow()
                .setOpen(false)
                .moveTo(importTab);
        
        importHackTabs.add(webGroup);
        importHackTabs.add(virtuosoGroup);
        importHackTabs.add(exploreGroup);
        
        openImportHackTab = webGroup;
        
        // Web import elements
        
        importWebURI = cp5.addTextfield("URI",
                padding,
                padding,
                w - 2 * padding,
                elementHeight)
                .setAutoClear(false)
                .moveTo(webGroup)
                .setText("http://dbpedia.org/resource/Albert_Einstein");
        cp5.addButton("Query Web")
                .setSize(buttonWidth, buttonHeight)
                .setPosition(w - buttonWidth - padding, 
                    labelledElementHeight + padding)
                .moveTo(webGroup)
                .addCallback(new QueryWebListener());
        
        // Virtuoso import elements
        
        cp5.addTextfield("IP:Port", 
                    padding - w / 4, 
                    padding, 
                    w - 2 * padding, 
                    elementHeight)
                .setAutoClear(false)
                .moveTo(virtuosoGroup);
        cp5.addTextfield("Username", 
                    padding - w / 4, 
                    labelledElementHeight + padding, 
                    w - 2 * padding, 
                    elementHeight)
                .setAutoClear(false)
                .moveTo(virtuosoGroup);
        cp5.addTextfield("Password", 
                    padding - w / 4, 
                    2 * labelledElementHeight + padding, 
                    w - 2 * padding, 
                    elementHeight)
                .setAutoClear(false)
                .setPasswordMode(true)
                .moveTo(virtuosoGroup);
        cp5.addTextfield("Query", 
                    padding - w / 4, 
                    3 * labelledElementHeight + padding, 
                    w - 2 * padding, 
                    elementHeight)
                .setAutoClear(false)
                .moveTo(virtuosoGroup);
        
        cp5.addButton("Query Virtuoso")
                .setSize(buttonWidth, buttonHeight)
                .setPosition(w - buttonWidth - padding - w / 4, 
                    4 * labelledElementHeight + padding)
                .moveTo(virtuosoGroup);
        
        // Explore tab elements
        
        cp5.addRadioButton("Source Choice")
                .setPosition(padding - w / 2, padding)
                .setItemHeight(elementHeight)
                .setItemWidth(elementHeight)
                .addItem("Query linked data web", 0)
                .addItem("Query virtuoso server", 1)
                .activate(0)
                .moveTo(exploreGroup);
        
        //=======================
        // Transform tab elements
        //=======================
        
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
        
        // Transform subtabs
        
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
        
        // transformation controllers
        
        cp5.addRadioButton("Layout Choice", padding, padding)
                .setPosition(padding, padding)
                .setItemHeight(elementHeight)
                .setItemWidth(elementHeight)
                .addItem("Drag and Drop", 0)
                .addItem("Expand", 1)
                .addItem("Auto Layout (affects entire graph)", 2)
                .moveTo(positionGroup);
        
        cp5.addColorPicker("Color Choice")
                .setPosition(-(w / 4) + padding, padding)
                .setColorValue(colorPickerDefault)
                .moveTo(colorGroup);
        
        cp5.addRadioButton("Label Visibility")
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
        
        //=====================
        // Options tab elements
        //=====================
        
        //==================
        // Save tab elements
        //==================
    }
    
    @Override
    public void draw() {
        
        for (Group hackTab : transformHackTabs) {
          if (hackTab.isOpen() && hackTab != openTransformHackTab) {
            openTransformHackTab.setOpen(false);
            openTransformHackTab = hackTab;
          }
        }
        
        for (Group hackTab : importHackTabs) {
            if (hackTab.isOpen() && hackTab != openImportHackTab) {
                openImportHackTab.setOpen(false);
                openImportHackTab = hackTab;
            }
        }
        
        background(0);
    }
    
    private class HackTab extends Group {
      
        HackTab(ControlP5 theControlP5, String theName) {
          super(theControlP5, theName);
        }
      
        @Override
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
    
    /*************
     * Callback listeners
     *************/
    
    /*
     * attach to web query button in import tab
     */
    private class QueryWebListener implements CallbackListener {
        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_PRESSED) {
                String uri = importWebURI.getText();
                Model toAdd = Importer.getDescriptionFromWeb(uri);
                graph.addTriples(toAdd);
            }
        }
    }
}
