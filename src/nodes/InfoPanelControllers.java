/*
 * Information panel for a hovored/selected GraphElement.Displayed information changes with mouse hover content.  When the mouse is
 * not hovering over an element, all selected elements will have their information
 * rendered.
 */
package nodes;

import com.hp.hpl.jena.rdf.model.Model;

import controlP5.Button;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.Textarea;

import processing.core.PApplet;
import processing.core.PFont;

import org.openjena.riot.RiotException;

/**
 *
 * @author kdbanman
 */
public class InfoPanelControllers extends PApplet {
    int w, h;
    
    int padding;
    int elementHeight;
    int buttonWidth;
    int buttonHeight;
    
    PFont eventFont;
    
    ControlP5 cp5;
    Graph graph;
    
    // string to add events to
    String eventLogString;
    
    // scrollable text area to log events/feedback to the user
    Textarea eventLog;
    
    // exploration buttons to query web/SPARQL for information about selection
    Button exploreWeb;
    Button exploreSparql;
    
    public InfoPanelControllers(int frameHeight, Graph parentGraph) {
        
        try {
            eventFont = createFont("resources/labelFont.ttf", 11, false);
        } catch (Exception e) {
            System.out.println("ERROR: font not loaded.  ensure labelFont.tiff is in program directory.");
            eventFont = cp5.getFont().getFont();
        }
        
        padding = 10;
        elementHeight = 20;
        buttonWidth = 200;
        buttonHeight = 30;
        
        w = buttonWidth + 2 * padding;
        h = frameHeight;
        
        // initialize graph
        graph = parentGraph;
        
        eventLogString = "";
    }
    
    public int getWidth() {
        return w;
    }
    
    public int getHeight() {
        return h;
    }
    
    public void setHeight(int newHeight) {
        h = newHeight;
        size(w, h);
        eventLog.setSize(buttonWidth, h - 4 * padding - 2 * buttonHeight - 50);
    }
    
    @Override
    public void setup() {
        
        size(w, h);
        frameRate(25);
        
        cp5 = new ControlP5(this)
                .setMoveable(false);
        
        eventLog = cp5.addTextarea("Event Log")
                .setPosition(w - padding - buttonWidth, 3 * padding + 2 * buttonHeight)
                .setSize(buttonWidth, h - 4 * padding - 2 * buttonHeight - 50)
                .setText(eventLogString)
                .setFont(eventFont)
                .setColor(0xFFFF5555);
        
        exploreWeb = cp5.addButton("Explore Web")
                .setPosition(w - padding - buttonWidth, padding)
                .setSize(buttonWidth, buttonHeight)
                .addCallback(new WebExplorationListener());
        exploreSparql = cp5.addButton("Explore SPARQL endpoint")
                .setPosition(w - padding - buttonWidth, 2 * padding + buttonHeight)
                .setSize(buttonWidth, buttonHeight)
                .addCallback(new SparqlExplorationListener());
        
    }
    
    // draw() is called every time a frame is rendered
    @Override
    public void draw() {
        background(0);
    }
    
    // log event to user.  sufficient newlines automatically added.
    public void logEvent(String s) {
        s = s.trim();
        eventLogString = ">>>>>\n\n" + s + "\n\n" + eventLogString;
        eventLog.setText(eventLogString);
    }
    
    /*
     * returns String uri of singly selected node or edge, null if selection
     * is not a single element.
     */
    private String getSelectedURI() {
        String uri;
        if (graph.getSelection().nodeCount() == 1 && graph.getSelection().edgeCount() == 0) {
            uri = graph.getSelection().getNodes().iterator().next().getName();
        } else if (graph.getSelection().edgeCount() == 1 && graph.getSelection().nodeCount() == 0) {
            Edge edge = (Edge) graph.getSelection().getEdges().iterator().next();

            if (edge.getTriples().size() > 1) {
                // user needs to choose which triple to explore
                TripleChooserFrame chooser = new TripleChooserFrame(this, edge);

                // this thread will be started again upon closure of TripleChooserFrame
                try {
                    synchronized(this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("ExplorationListener was not able to wait for TripleChooserFrame");
                }
                // triple chooser's thread will be sleeping, waiting for this
                // to retrieve the chosen triple (Statement)
                uri = chooser.choice().getPredicate().getURI();
                chooser.close();
            } else {
                uri = edge.getSingleTriple().getPredicate().getURI();
            }
        } else {
            // more than one GraphElement is selected
            uri = null;
        }
        
        return uri;
    }
    
    /***************************************
     * exploration button callback listeners
     ***************************************/
    
    /*
     * attach to web query button to enable retrieval of rdf
     * descriptions as published at resources' uris.
     */
    private class WebExplorationListener implements CallbackListener {
        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED) {
                
                // get uri from selected node or edge
                
                String uri = getSelectedURI();
                
                if (uri == null) {
                    logEvent("Select a *single* node or edge to retrieve its data.");
                    return;
                }
                
                // retrieve description as a jena model
                ///////////////////////////////////////
                Model toAdd;
                try {
                    toAdd = IO.getDescription(uri);
                } catch (RiotException e) {
                    logEvent("Valid RDF not hosted at uri \n  " + uri);
                    return;
                }
                // add the retrieved model to the graph (toAdd is empty if 
                // an error was encountered).
                // log results.
                graph.addTriplesLogged(toAdd);
                logEvent("From uri:\n<" + uri + ">\n  ");
            }
        }
    }
    
    /*
     * attach to sparql query button to enable retrieval of rdf
     * descriptions as published at resources' uris.  (coupled to textfield in
     * ControlPanel)
     */
    private class SparqlExplorationListener implements CallbackListener {
        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED) {
                
                // get uri from selected node or edge and form query
                String uri = getSelectedURI();
                
                // ensure uri was retrieved
                if (uri == null) {
                    logEvent("Select a *single* node or edge to retrieve its data.");
                    return;
                }
                // get endpoint uri
                String endpoint = graph.pApp.getSparqlEndpoint();
                
                // retrieve description as a jena model
                Model toAdd;
                try {
                    toAdd = IO.getDescriptionSparql(endpoint, uri);
                } catch (Exception e) {
                    logEvent("Valid RDF not returned from endpoint:\n" + endpoint +
                            "\n\nCheck endpoint address and status.");
                    return;
                }
                // add the retriveed model to the graph (toAdd is empty if 
                // an error was encountered).
                // log results.
                graph.addTriplesLogged(toAdd);
                logEvent("From endpoint:\n" + endpoint + "\n\n" +
                         "about uri: \n" + uri + "\n ");
            }
        }
    }
}
