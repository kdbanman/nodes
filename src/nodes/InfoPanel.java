/*
 * Information panel for a hovored/selected GraphElement.  Shows instructions if the
 * selection is not a single GraphElement and no GraphElement is being hovered over.
 * otherwise displays:
 * 
 * - namespace prefix legend in displayed information
 * - buttons to query for more triples (if the node is a URI resource)
 * 
 * Displayed information changes with mouse hover until a single element is
 * selected, then the information freezes.  (performance requirements may 
 * necessitate the hover response as an option).
 */
package nodes;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import controlP5.Button;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.Textarea;
import java.util.NoSuchElementException;

import processing.core.PApplet;
import processing.core.PFont;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author kdbanman
 */
public class InfoPanel extends PApplet implements Selection.SelectionListener {
    int w, h;
    
    int padding;
    int elementHeight;
    int buttonWidth;
    int buttonHeight;
    
    PFont infoFont;
    PFont eventFont;
    
    ControlP5 cp5;
    Graph graph;
    
    // update flag raised if the controllers have not responded to a change in
    // selection.  see selectionChanged() and draw().
    AtomicBoolean selectionUpdated;
    
    // default string for infobox when single GraphElement is not selected
    String infoDefault;
    
    // string to add events to
    String eventLogString;
    
    // scrollable text area to render triples readable according to selection
    // or to provide application instructions
    Textarea infoBox;
    
    // scrollable text area to log events/feedback to the user
    Textarea eventLog;
    
    // exploration buttons to query web/SPARQL for information about selection
    Button exploreWeb;
    Button exploreSparql;
    Button exploreSdb;
    
    public InfoPanel(int frameWidth, int frameHeight, Graph parentGraph) {
        w = frameWidth;
        h = frameHeight;
        
        try {
            infoFont = createFont("labelFont.ttf", 12, true);
        } catch (Exception e) {
            System.out.println("ERROR: font not loaded.  ensure labelFont.tiff is in program directory.");
            infoFont = cp5.getFont().getFont();
        }
        
        try {
            eventFont = createFont("labelFont.ttf", 11, false);
        } catch (Exception e) {
            System.out.println("ERROR: font not loaded.  ensure labelFont.tiff is in program directory.");
            eventFont = cp5.getFont().getFont();
        }
        
        padding = 10;
        elementHeight = 20;
        buttonWidth = 200;
        buttonHeight = 30;
        
        // initialize graph
        graph = parentGraph;
        
        selectionUpdated = new AtomicBoolean();
        
        //TODO: change this to a more comprehensive set of instructions
        infoDefault = "Select a single node or edge for information.";
        
        eventLogString = "";
    }
    
    @Override
    public void setup() {
        // subscribe to changes in selection.  see overridden selectionChanged()
        graph.selection.addListener(this);
        
        size(w, h);
        frameRate(25);
        
        cp5 = new ControlP5(this)
                .setMoveable(false);
    
        // scrollable text area to render triples readable
        infoBox = cp5.addTextarea("Data Window")
                .setPosition(padding, padding)
                .setSize(w - 3 * padding - buttonWidth, h - 2 * padding - 50)
                .setText(infoDefault)
                .setFont(infoFont);
        
        eventLog = cp5.addTextarea("Event Log")
                .setPosition(w - padding - buttonWidth, 4 * padding + 3 * buttonHeight)
                .setSize(buttonWidth, h - 5 * padding - 3 * buttonHeight - 25)
                .setText(eventLogString)
                .setFont(eventFont)
                .setColor(0xFFFF5555);
        
        // exploration buttons to query the web or the database
        exploreWeb = cp5.addButton("Explore Web")
                .setPosition(w - padding - buttonWidth, padding)
                .setSize(buttonWidth, buttonHeight)
                .addCallback(new WebExplorationListener());
        exploreSparql = cp5.addButton("Explore SPARQL endpoint")
                .setPosition(w - padding - buttonWidth, 2 * padding + buttonHeight)
                .setSize(buttonWidth, buttonHeight);
        exploreSdb = cp5.addButton("Explore SDB instance")
                .setPosition(w - padding - buttonWidth, 3 * padding + 2 * buttonHeight)
                .setSize(buttonWidth, buttonHeight);
    }
    
    @Override
    public void draw() {
        background(0);
        
        if (selectionUpdated.getAndSet(false)) {
            String toDisplay = infoDefault;
            
            if (graph.selection.nodeCount() + graph.selection.edgeCount() == 1) {
                try {
                    if (graph.selection.nodeCount() == 1) {
                        toDisplay = renderedNodeString(graph.selection.getNodes().iterator().next());
                    } else {
                        toDisplay = renderedEdgeString(graph.selection.getEdges().iterator().next());
                    }
                } catch (NoSuchElementException e) {
                    // when the user is changing the selection rapidly, the iterator may fail here.
                    // this is not an issue, since the next infopanel frame will
                    // react to the change, so this exception is silently swallowed.
                }
            }
            infoBox.setText(toDisplay);
        }
    }
    
    // every time selection is changed, this is called
    @Override
    public void selectionChanged() {
        // queue controller selection update if one is not already queued
        selectionUpdated.compareAndSet(false, true);
    }
    
    // log event to user.  sufficient newlines automatically added.
    public void logEvent(String s) {
        s = s.trim();
        eventLogString = s + "\n\n>>>>>\n\n" + eventLogString;
        eventLog.setText(eventLogString);
    }
    
    /*
     * returns a well formatted description of the entity the passed Node
     * represents based on the existing (imported) data.  do not pass null
     * values.
     */
    public String renderedNodeString(Node n) {
        String rendered = "Data Neigborhood for:  " + graph.prefixed(n.getName()) + "\n\n";
        
        StmtIterator outbound = graph.triples.listStatements(graph.getResource(n.getName()), null, (RDFNode) null);
        while (outbound.hasNext()) {
            Statement s = outbound.next();
            rendered += "  " + graph.prefixed(s.getPredicate().toString()) + "  "
                    + graph.prefixed(s.getObject().toString()) + "\n";
        }
        rendered += "\n";
        
        StmtIterator inbound = graph.triples.listStatements(null, null, (RDFNode) graph.getResource(n.getName()));
        if (!inbound.hasNext()) {
            inbound = graph.triples.listStatements(null, null, n.getName());
        }
        if (!inbound.hasNext()) {
            //TODO: figure out a way to get literal statuments working.  they don't
            // show up in the infopanel as "is <pred> of <uri resource>
        }
        while (inbound.hasNext()) {
            Statement s = inbound.next();
            rendered += "  is  " + graph.prefixed(s.getPredicate().toString()) + "  of  "
                    + graph.prefixed(s.getSubject().toString()) + "\n";
        }
        return rendered;
    }
    
    /*
     * returns a well-formatted description of the predicates that the passed
     * edge represents based on the existing data.
     */
    public String renderedEdgeString(Edge e) {
        String rendered = "";
        for (Statement s : e.triples) {
            rendered += graph.prefixed(s.getSubject().toString()) + "  " +
                    graph.prefixed(s.getPredicate().toString()) + "  " +
                    graph.prefixed(s.getObject().toString());
            Node predicateNode = graph.getNode(s.getPredicate().toString());
            if (predicateNode != null) {
                rendered += renderedNodeString(predicateNode);
            } else {
                rendered += "\n\nNo data yet retrieved about predicate\n  " + s.getPredicate().toString()
                        + "\n\nTry querying the web or a SPARQL instance with the button to the right.";
            }
            rendered += "\n\n\n\n";
        }
        
        return rendered;
    }
    
    /***************************************
     * exploration button callback listeners
     ***************************************/
    
    /*
     * attach to web query button in import tab to enable retrieval of rdf
     * descriptions as published at resources' uris.
     */
    private class WebExplorationListener implements CallbackListener {
        @Override
        public void controlEvent(CallbackEvent event) {
            if (event.getAction() == ControlP5.ACTION_RELEASED) {
                // get uri from selected node 
                String uri;
                // explore button is only unlocked when exactly one element is 
                // selected (see draw())
                if (graph.selection.nodeCount() == 1) {
                    uri = graph.selection.getNodes().iterator().next().getName();
                } else if (graph.selection.edgeCount() == 1) {
                    Edge edge = (Edge) graph.selection.getEdges().iterator().next();
                    
                    if (edge.triples.size() > 1) {
                        // user needs to choose which triple to explore
                        TripleChooserFrame chooser = new TripleChooserFrame(this, edge, graph.triples);

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
                        uri = edge.triples.iterator().next().getPredicate().getURI();
                    }
                } else {
                    logEvent("Select a single node or edge to retrieve its data.");
                    return;
                }
                // retrieve description as a jena model
                Model toAdd = Importer.getDescriptionFromWeb(uri);
                
                // protect from concurrency issues during import
                graph.pApp.waitForNewFrame(this);
                
                int retrievedSize = (int) toAdd.size();
                int addedSize = graph.tripleCount();
                
                // add the retriveed model to the graph (toAdd is empty if 
                // an error was encountered)
                graph.addTriples(toAdd);
                
                addedSize = graph.tripleCount() - addedSize;
                
                // log number of triples added to user
                logEvent("From uri:\n<" + uri + ">\n  " + 
                         retrievedSize + " triples retrieved,\n  " +
                         addedSize + " triples are new");
                
                graph.pApp.restartRendering(this);
                
                // queue controller selection update if one is not already queued
                selectionUpdated.compareAndSet(false, true);
            }
        }
    }
}
