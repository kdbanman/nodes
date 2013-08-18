/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import java.awt.BorderLayout;
import java.awt.Frame;

/**
 *
 * @author kdbanman
 */
public class InfoPanelFrame extends Frame {
    int w, h;
    String name;
    InfoPanel info;
    
    public InfoPanelFrame(Graph graph) {
        super("Information Panel");
        
        // window parameters
        w = 600;
        h = 400;
        
        setLayout(new BorderLayout());

        setSize(w, h);
        setLocation(30, 530);
        setResizable(true);
        setVisible(true);
        
        info = new InfoPanel(w, h, graph);
        
        add(info, BorderLayout.CENTER);
        validate();
        info.init();  
    }
}
