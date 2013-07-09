/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import com.hp.hpl.jena.rdf.model.Model;

import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ColorPicker;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.ControlFont;
import controlP5.ControlKey;
import controlP5.Group;
import controlP5.ListBox;
import controlP5.Slider;
import controlP5.Tab;
import controlP5.Textfield;
import controlP5.Toggle;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


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
    
    // Control elements that need to be accessed outside of setup
    
    Tab transformTab;
    Group positionGroup;
    
    ArrayList<Group> importHackTabs;
    Group openImportHackTab;
    
    ArrayList<Group> transformHackTabs;
    Group openTransformHackTab;
    
    Textfield importWebURI;
    
    Toggle autoLayout;
    
    ColorPicker colorPicker;
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
        buttonHeight = 30;
        
        modifiersBoxHeight = 200;
        
        // control element miscellany
        
        importHackTabs = new ArrayList<>();
        transformHackTabs = new ArrayList<>();
        
        colorPickerDefault = 0xFF1A4969;
    }
    
    @Override
    public void setup() {
        size(w, h);
        
        cp5 = new ControlP5(this)
                .mapKeyFor(new PasteListener(), CONTROL, 'v')
                .mapKeyFor(new CopyListener(), CONTROL, 'c');
        
        // Main tabs
        
        Tab importTab = cp5.addTab("Get Triples")
                .setWidth(w / 4)
                .setHeight(tabHeight)
                .setActive(true);
        transformTab = cp5.addTab("Transform")
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
                .setText("http://www.w3.org/1999/02/22-rdf-syntax-ns");
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
        
        positionGroup = new HackTab(cp5, "Position")
                .setBarHeight(tabHeight)
                .setPosition(0, transformTabsVert)
                .setWidth(w / 4)
                .hideArrow()
                .setOpen(true)
                .moveTo(transformTab);
        Group colorSizeGroup = new HackTab(cp5, "Color and Size")
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
        transformHackTabs.add(colorSizeGroup);
        transformHackTabs.add(labelGroup);
        transformHackTabs.add(hideGroup);
        
        openTransformHackTab = positionGroup;
        
        /*******************
        * Layout controllers
        ********************/
        
        cp5.addButton("Expand")
                .setPosition(padding, padding)
                .setHeight(buttonHeight)
                .setWidth(buttonWidth)
                .moveTo(positionGroup)
                .addCallback(new ExpandLayoutListener());
        
        cp5.addButton("Contract")
                .setPosition(padding, 2 * padding + buttonHeight)
                .setHeight(buttonHeight)
                .setWidth(buttonWidth)
                .moveTo(positionGroup)
                .addCallback(new ContractLayoutListener());
        
        cp5.addButton("Radial Sort")
                .setPosition(padding, 3 * padding + 2 * buttonHeight)
                .setHeight(buttonHeight)
                .setWidth(buttonWidth)
                .moveTo(positionGroup);
        
        autoLayout = cp5.addToggle("Auto-Layout")
                .setPosition(padding, 4 * padding + 3 * buttonHeight)
                .setHeight(elementHeight)
                .setWidth(buttonWidth)
                .moveTo(positionGroup);
        
        // color and size controllers
        
        //NOTE:  ColorPicker is a ControlGroup, not a Controller, so I can't 
        //       attach a callback to it.  It's functionality is in the 
        //       controlEvent() function of the ControlPanel PApplet
        colorPicker = cp5.addColorPicker("Color")
                .setPosition(-(w / 4) + padding, padding)
                .setColorValue(colorPickerDefault)
                .moveTo(colorSizeGroup);
        
        cp5.addSlider("Size")
                .setPosition(-(w / 4) + padding, 2 * padding + 80)
                .setHeight(elementHeight)
                .setWidth(w - 80)
                .setRange(5, 100)
                .setValue(10)
                .moveTo(colorSizeGroup)
                .addCallback(new ElementSizeListener());
        
        // label controllers
        
        cp5.addButton("Show Labels")
                .setPosition(padding - w / 2, padding)
                .setHeight(buttonHeight)
                .setWidth(buttonWidth)
                .moveTo(labelGroup)
                .addCallback(new ShowLabelListener());
        
        cp5.addButton("Hide Labels")
                .setPosition(padding - w / 2, buttonHeight + 2 * padding)
                .setHeight(buttonHeight)
                .setWidth(buttonWidth)
                .moveTo(labelGroup)
                .addCallback(new HideLabelListener());
        
        cp5.addSlider("Label Size")
                .setPosition(padding - w / 2, padding * 3 + 2 * buttonHeight)
                .setWidth(w - 80)
                .setHeight(elementHeight)
                .setRange(5, 100)
                .setValue(10)
                .moveTo(labelGroup)
                .addCallback(new LabelSizeListener());
        
        // visibility controllers
        
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
        
        // make hackTabs perform as tabs instead of Groups
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
        
        // stop autoLayout if any other tab is selected
        if (autoLayout.getState() && (!transformTab.isOpen() || !positionGroup.isOpen())) {
            autoLayout.setState(false);
        }
        
        background(0);
    }
    
    public void controlEvent(ControlEvent event) {
        // adjust color of selected elements
        if (event.isFrom(colorPicker)) {
            int newColor = colorPicker.getColorValue();
            for (GraphElement e : graph.selection) {
                e.setColor(newColor);
            }
        }
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
     * Import listeners
     *************/
    
    /*
     * attach to main cp5 for paste functionality
     */
    private class PasteListener implements ControlKey {
        Clipboard cb;
        
        @Override
        public void keyEvent() {
            for (Textfield c : cp5.getAll(Textfield.class)) {
                if (cb == null) cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                
                if (c.isActive()) {
                    int idx = c.getIndex();
                    String before = c.getText().substring(0, idx);
                    String after = "";
                    if (c.getIndex() != c.getText().length()) {
                        after = c.getText().substring(idx, c.getText().length());
                    }
                    
                    Transferable clipData = cb.getContents(this);
                    String s = "";
                    try {
                      s = (String) (clipData.getTransferData(DataFlavor.stringFlavor));
                    } catch (UnsupportedFlavorException | IOException ee) {
                        System.out.println("Cannot paste clipboard contents.");
                    }
                    c.setText(before + s + after);
                }
            }
        }
    }
    
    /*
     * attach to main cp5 for copy functionality
     */
    private class CopyListener implements ControlKey {
        Clipboard cb;
        
        @Override
        public void keyEvent() {
            for (Textfield c : cp5.getAll(Textfield.class)) {
                if (cb == null) cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                
                if (c.isActive()) {
                    StringSelection data = new StringSelection(c.getText());
                    cb.setContents(data, data);
                }
            }
        }
    }
    
    /*
     * attach to web query button in import tab
     */
    private class QueryWebListener implements CallbackListener {
        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED) {
                String uri = importWebURI.getText();
                Model toAdd = Importer.getDescriptionFromWeb(uri);
                graph.addTriples(toAdd);
            }
        }
    }
    
    /*************
     * Transformation listeners
     *************/
    
    /*
     * attach to layout expansion button
     */
    private class ExpandLayoutListener implements CallbackListener {

        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED) {
                // calculate center of current selection of nodes
                PVector center = new PVector();
                for (Node n : graph.selection.getNodes()) {
                    center.add(n.getPosition());
                }
                center.x =  center.x / graph.selection.nodeCount();
                center.y =  center.y / graph.selection.nodeCount();
                center.z =  center.z / graph.selection.nodeCount();
                
                // extrapolate all node positions 20% outward from center
                for (Node n : graph.selection.getNodes()) {
                    n.getPosition().lerp(center, -0.2f);
                }
            }
        }
    }
    
    /*
     * attach to layout contraction button
     */
    private class ContractLayoutListener implements CallbackListener {

        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED) {
                // calculate center of current selection of nodes
                PVector center = new PVector();
                for (Node n : graph.selection.getNodes()) {
                    center.add(n.getPosition());
                }
                center.x =  center.x / graph.selection.nodeCount();
                center.y =  center.y / graph.selection.nodeCount();
                center.z =  center.z / graph.selection.nodeCount();
                
                // extrapolate all node positions 20% outward from center
                for (Node n : graph.selection.getNodes()) {
                    n.getPosition().lerp(center, 0.2f);
                }
            }
        }
    }
    
    /*
     * attach to element size slider
     */
    private class ElementSizeListener implements CallbackListener {

        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED
                    || event.getAction() == ControlP5.ACTION_RELEASEDOUTSIDE) {
                int newSize = 10;
                try {
                    newSize = (int) ((Slider) event.getController()).getValue();
                } catch (Exception e) {
                    System.out.println("ERROR:  ElementSizeListener not hooked up to a Slider.");
                }
                for (GraphElement e : graph.selection) {
                    e.setSize(newSize);
                }
            }
        }
    }
    
    /*
     * attach to hide label button
     */
    private class HideLabelListener implements CallbackListener {

        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED) {
                for (GraphElement e : graph.selection) {
                    e.setDisplayLabel(false);
                }
            }
        }
    }
    
    /*
     * attach to show label button
     */
    private class ShowLabelListener implements CallbackListener {

        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED) {
                for (GraphElement e : graph.selection) {
                    e.setDisplayLabel(true);
                }
            }
        }
    }
    
    /*
     * attach to label size slider
     */
    private class LabelSizeListener implements CallbackListener {

        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED
                    || event.getAction() == ControlP5.ACTION_RELEASEDOUTSIDE) {
                int newSize = 10;
                try {
                    newSize = (int) ((Slider) event.getController()).getValue();
                } catch (Exception e) {
                    System.out.println("ERROR:  LabelSizeListener not hooked up to a Slider.");
                }
                for (GraphElement e : graph.selection) {
                    e.setLabelSize(newSize);
                }
            }
        }
    }
}
