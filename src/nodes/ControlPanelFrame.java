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
    
    public ControlPanelFrame() {
        super("Control Panel");
        
        // window parameters
        w = 400;
        h = 500;
           

        
        setLayout(null);
        setSize(w, h);
        setLocation(0, 0);
        setResizable(false);
        setVisible(true);
        
        PApplet controlPanel = new ControlPanel(w, h - 50);
        add(controlPanel, BorderLayout.CENTER);
        validate();
        controlPanel.init();  
    }
}
