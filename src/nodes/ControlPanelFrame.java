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

	private static final long serialVersionUID = -7064090646163909907L;

	public ControlPanel controls;
    
    int w;
    int h;
    
    public ControlPanelFrame() {
		super("Control Panel");

		w = 400;
		h = 600;

		setLayout(new BorderLayout());
		//setAlwaysOnTop(true);
		setSize(w, h);
		setLocation(1, 20);
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
