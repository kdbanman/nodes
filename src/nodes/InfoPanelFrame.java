package nodes;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JEditorPane;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author kdbanman
 */
public class InfoPanelFrame extends Frame {
    int w, h;
    
    JEditorPane testPane;
    InfoPanel info;
    
    HTMLDocument doc;
    
    public InfoPanelFrame() {
        super("Information Panel");
        
        // window parameters
        w = 800;
        h = 400;
        
        setLayout(new BorderLayout());

        setSize(w, h);
        setLocation(30, 530);
        setResizable(true);
        setVisible(true);
    }
    
    public void initialize(Graph graph) {
    	
    	testPane = new JEditorPane();
    	testPane.setContentType("text/html");
    	testPane.setText("<html><head><title>An example HTMLDocument</title><style type=\"text/css\">" +
    					 "div { background-color: silver; } ul { color: red; }</style></head>" +
    					 "<body> <div id=\"BOX\"><p>Paragraph 1</p><p>Paragraph 2</p></div></body></html>");
    	testPane.setSize(580, h);
    	testPane.setEditable(false);
    	testPane.setUI(new BasicTextFieldUI());
    	
    	add(testPane, BorderLayout.WEST);
    	
    	doc = (HTMLDocument) testPane.getDocument();
    	
    	
        info = new InfoPanel(220, h, graph);
        
        add(info, BorderLayout.WEST);
        validate();
        info.init();  
    }
    
    //TODO: pass resize(w, h) to infopanel so it can setSize and setPosition (clamped)
}
