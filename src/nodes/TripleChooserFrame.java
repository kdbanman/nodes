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
    
    public TripleChooserFrame(Object waitingObj, Edge e, PrefixMapping pfx) {
        super("Triple Chooser");
        
        padding = 10;
        elementSize = 20;
        buttonWidth = 100;
        buttonHeight = 30;
            
        frameW = 600;
        frameH = 3 * padding + buttonHeight + e.getTriples().size() * elementSize + 100;
        
        waiting = waitingObj;
        triples = new ArrayList<>(e.getTriples());
        pfxMap = pfx;
        
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
        
        /********************
         * chooser listeners
         *******************/
        
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
