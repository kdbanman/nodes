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
    
    // these are fields so that they may be resized by the componentResized()
    // method in the anonymous class within initialize()
    private JScrollPane scrollPane;
    private InfoPanelControllers infoControllers;
    
    // because swing is very sensitive about how you touch it, these are used
    // to change the text within the information panel
    private HTMLDocument doc;
    private HTMLBuilder htmlBuilder;
    
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
    	JEditorPane htmlInfoPane = new JTextPane();
    	htmlInfoPane.setContentType("text/html");

    	

    	htmlInfoPane.setText("<html bgcolor=\"#000000\">" +
    					 
    					 "</html>");
    	
    	doc = (HTMLDocument) htmlInfoPane.getDocument();
    	// setEditable() affects setText() for no clear reason. fucking swing. try to manipulate the source document instead
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
    
    public void displayInformationText(Iterable<? extends GraphElement> elements) {
        // affects state of doc.  ugly, but it works
        htmlBuilder.renderAsHTML(elements, doc);
    }
    
    public void logEvent(String s) {
        infoControllers.logEvent(s);
    }
}
