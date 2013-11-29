package nodes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author kdbanman
 */
public class InfoPanelFrame extends Frame {
    int w, h;
    
    JEditorPane htmlInfoPane;
    JScrollPane scrollPane;
    InfoPanelControllers infoControllers;
    
    HTMLDocument doc;
    HTMLBuilder htmlBuilder;
    
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
 
        htmlBuilder = new HTMLBuilder();
    }
    
    public void initialize(Graph graph) {
    	
    	// set up html formatted text pane for readable data rendering
    	htmlInfoPane = new JTextPane();
    	htmlInfoPane.setContentType("text/html");

    	

    	htmlInfoPane.setText("<html bgcolor=\"#000000\">" +
    					 "<body>" +
    					 "<h1><font color=\"#FFFFFF\">title weeeeeeeee</font></h1>" +
    					 "<table border=\"1\">" +
    					 "<font color=\"#FFFFFF\"><tr><th>Month</th><th>Savings</th></tr></font>" +
    					 "<font color=\"#FFFFFF\"><tr><td>butt</td><td>$100</td></tr></font>" +
    					 "<font color=\"#FFFFFF\"><tr><td>boo</td><td>$1030</td></tr></font>" +
    					 "</table>" +
    					 "</body>" +
    					 "</html>");
    	
    	doc = (HTMLDocument) htmlInfoPane.getDocument();
    	// setEditable() affects setText() for no clear reasons. fucking swing. try to manipulate the source document instead
    	htmlInfoPane.setEditable(false);
        htmlInfoPane.setMargin(new Insets(0,0,0,0));
    	
    	// make text pane scrollable
        scrollPane = new JScrollPane(htmlInfoPane);
        scrollPane.setPreferredSize(new Dimension(580, h));
        
        // add pane to frame
    	add(scrollPane, BorderLayout.EAST);
    	
    	//set up and add exploration buttons and event log
        infoControllers = new InfoPanelControllers(h, graph);
        add(infoControllers, BorderLayout.WEST);
        
        // validate the frame (for layout, magic, etc.)
        validate();
        infoControllers.init();
        
        // make sure components are resized appropriately
        addComponentListener(new ComponentListener(){
            @Override
            public void componentResized(ComponentEvent e) {
                w = InfoPanelFrame.this.getWidth();
                h = InfoPanelFrame.this.getHeight();
                
                int calculatedWidth = w - InfoPanelFrame.this.infoControllers.getWidth();
                calculatedWidth = calculatedWidth > 50 ? calculatedWidth : 50;
                
                Dimension calculatedSize = new Dimension(calculatedWidth, h);
                
                scrollPane.setPreferredSize(calculatedSize);
                
                InfoPanelFrame.this.infoControllers.setHeight(h);
                
                InfoPanelFrame.this.validate();
            }
            @Override
            public void componentMoved(ComponentEvent e) {}
            @Override
            public void componentShown(ComponentEvent e) {}
            @Override
            public void componentHidden(ComponentEvent e) {}
            
        });
    }
}
