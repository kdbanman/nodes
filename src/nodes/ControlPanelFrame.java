/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import java.awt.BorderLayout;
import java.awt.Frame;
import processing.core.PApplet;

/**
 *
 * @author kdbanman
 */
public class ControlPanelFrame extends Frame {
    int w, h;
    String name;
    
<<<<<<< HEAD
    public ControlPanelFrame(Graph graph) {
=======
    public ControlPanelFrame(Graph g) {
>>>>>>> ba501779954799fae4a5b14f8b4537c42cde8bc5
        super("Control Panel");
        
        // window parameters
        w = 400;
        h = 500;
        
        setLayout(new BorderLayout());

        setSize(w, h);
        setLocation(0, 0);
        setResizable(false);
        setVisible(true);
        
<<<<<<< HEAD
        PApplet controlPanel = new ControlPanel(w, h, graph);
=======
        PApplet controlPanel = new ControlPanel(g, w, h);
>>>>>>> ba501779954799fae4a5b14f8b4537c42cde8bc5
        add(controlPanel, BorderLayout.CENTER);
        validate();
        controlPanel.init();  
    }
}
