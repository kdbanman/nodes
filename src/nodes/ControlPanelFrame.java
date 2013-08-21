/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import java.awt.BorderLayout;
import java.awt.Frame;

/**
 * call the constructor in main() of Nodes so that input focus may be directed
 * to the control panel on linux machines.  This necessitates that a
 * ControlPanelFrame is a static field within Nodes, which is why initialize()
 * is separate from the constructor.  If it weren't separate, all fields that it
 * affects would have to be static as well.  One horrible hack is enough...
 * 
 * @author kdbanman
 */
public class ControlPanelFrame extends Frame {
    public ControlPanel controls;
    
    int w;
    int h;
    
    public ControlPanelFrame() {
        super("Control Panel");
        
        w = 400;
        h = 500;
        
        setLayout(new BorderLayout());

        setSize(w, h);
        setLocation(30, 30);
        setResizable(false);
        setVisible(true);
    }
    
    public void initialize(Graph graph) {
        controls = new ControlPanel(w, h, graph);
        
        add(controls, BorderLayout.CENTER);
        validate();
        controls.init();  
    }
}
