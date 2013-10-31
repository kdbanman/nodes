package nodes;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import controlP5.Button;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.RadioButton;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import processing.core.PApplet;

/**
 * Given an Edge representing multiple triples, this popup window gives a
 * UI for choosing a single one of those triples.
 * 
 * IMPORTANT:  
 * The execution of the Object requiring a single triple choice (i.e. the Object
 * constructing and using a TripleChooserFrame) *needs* to be halted with a
 * synchronized Object.wait() call.  When the OK button of a TripleChooserFrame
 * is clicked, the waiting object will be restarted with an Object.notify() call.
 *
 * @author kdbanman
 */
public class TripleChooserFrame extends Frame {
    int padding;
    int elementSize;
    int buttonWidth;
    int buttonHeight;
    
    int frameW, frameH;
    
    Object waiting;
    
    PrefixMapping pfxMap;
    
    ArrayList<Statement> triples;
    Statement choice;
    
    TripleChooser chooser;
    
    /**
     * Choose a triple from an edge containing >1 triple.  Usage example:
     * 
     *      // get a triple from an edge
     * 
     *      Statement chosenTriple;
     * 
     *      if (edge.getTriples().size() > 1) {
     * 
     *          // create chooser window
     *          TripleChooserFrame chooser = new TripleChooserFrame(this, edge);
     * 
     *          // this thread will be started again upon closure of TripleChooserFrame
     *          try {
     *              synchronized(this) {
     *                  this.wait();
     *              }
     *          } catch (InterruptedException e) {
     *              System.out.println("ExplorationListener was not able to wait for TripleChooserFrame");
     *          }
     *          // chooser will now have restarted this thread, so it is safe
     *          // to get the triple choice.
     *          chosenTriple = chooser.choice();
     * 
     *          // now that a Statement has been chosen, the chooser window can be closed
     *          chooser.close();
     * 
     *      } else {
     * 
     *          chosenTriple = edge.getSingleTriple();
     * 
     *      }
     * 
     * @param waitingObj Object whose execution is waiting on the TripleChooserFrame
     * @param e Edge containing more than one triple
     */
    public TripleChooserFrame(Object waitingObj, Edge e) {
        super("Triple Chooser");
        
        padding = 10;
        elementSize = 20;
        buttonWidth = 100;
        buttonHeight = 30;
            
        frameW = 600;
        frameH = 3 * padding + buttonHeight + e.getTriples().size() * elementSize + 100;
        
        waiting = waitingObj;
        triples = new ArrayList<>(e.getTriples());
        // based on the integrity of the Graph, every Edge will have >= 1 triple
        // but just in case...
        if (!e.getTriples().isEmpty()) {
            pfxMap = e.getSingleTriple().getModel();
        } else {
            System.out.println("ERROR: Edge " + e.getName() + " has no triples");
            System.exit(1);
        }
        
        choice = null;
        
        setLayout(new BorderLayout());

        setSize(frameW, frameH);
        setLocation(230, 700);
        setResizable(true);
        setVisible(true);
        
        chooser = new TripleChooser();
        
        add(chooser, BorderLayout.CENTER);
        validate();
        chooser.init();  
    }
    
    public Statement choice() {
        if (choice == null) {
            System.out.println("ERROR: choice() was called before choice was" + ""
                    + "made.  Perhaps wait() was not called by object that needs the choice?");
        }
        
        return choice;
    }
    
    public void close() {
        chooser.stop();
        this.dispose();
    }
    
    private class TripleChooser extends PApplet {
        ControlP5 cp5;
        
        RadioButton tripleRadio;
        Button ok;
        
        public TripleChooser() {
            
        }
        
        @Override
        public void setup() {
            size(frameW, frameH);
            cp5 = new ControlP5(this);
            
            tripleRadio = cp5.addRadioButton("Choose Triple")
                    .setPosition(padding, padding)
                    .setItemHeight(elementSize)
                    .setItemWidth(elementSize);
            
            int idx = 0;
            for (Statement s : triples) {
                tripleRadio.addItem(pfxMap.shortForm(s.getSubject().toString()) + "  " +
                        pfxMap.shortForm(s.getPredicate().toString()) + "  " +
                        pfxMap.shortForm(s.getObject().toString()), idx);
                idx++;
            }
            tripleRadio.activate(0);
            
            ok = cp5.addButton("OK")
                    .setPosition(width - buttonWidth - padding, height - buttonHeight - padding - 50)
                    .addCallback(new OkButtonListener());
        }
    
        @Override
        public void draw() {
            background(0);
        }
        
        /**
         * code for click action on OK button
         */
        private class OkButtonListener implements CallbackListener {
            @Override
            public void controlEvent(CallbackEvent event) {
                if (event.getAction() == ControlP5.ACTION_RELEASED) {
                    int active = -1;
                    for (int idx = 0 ; idx < triples.size() ; idx++) {
                        if (tripleRadio.getState(idx)) active = idx;
                    }
                    
                    if (active != -1) {
                        choice = triples.get(active);
                        synchronized (TripleChooserFrame.this.waiting) {
                            TripleChooserFrame.this.waiting.notify();
                        }
                    }
                }
            }
        }
    }
}
