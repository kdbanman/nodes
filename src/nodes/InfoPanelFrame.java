package nodes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import nodes.Selection.SelectionListener;

/**
 *
 * @author kdbanman
 */
public class InfoPanelFrame extends Frame implements SelectionListener{
    int w, h;
    
    Graph graph;
    
    // these are fields so that they may be resized by the componentResized()
    // method in the anonymous class within initialize()
    private JScrollPane scrollPane;
    private InfoPanelControllers infoControllers;
    
    // these are used to change the text within the information panel
    JEditorPane htmlInfoPane;
    private HTMLBuilder htmlBuilder;
    
    // HTML rendering is expensive, so it is repeated at minimum interval
    private long updateInterval;
    private boolean updateScheduled;
    private final ScheduledExecutorService updateExecutor;
    
    /**
     * Must call initialize() after constructor!
     */
    public InfoPanelFrame() {
        super("Information Panel");
        
        // window parameters
        w = 800;
        h = 500;
        
        setLayout(new BorderLayout());

        setSize(w, h);
        setLocation(30, 530);
        setResizable(true);
        setVisible(true);
        
        updateExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    
    /**
     * Must be called after constructor.
     * 
     * @param graph Graph used for prefixing URIs
     * @param maximumElements Maximum number of elements that will be rendered as HTML text.  Adjust for performance.
     */
    public void initialize(Graph graph, int maximumElements, long updateInterval) {
        this.graph = graph;
        
        htmlBuilder = new HTMLBuilder(maximumElements);
        
        this.updateInterval = updateInterval;
        updateScheduled = false;
        
        graph.getSelection().addListener(this);
    	
    	// set up html formatted text pane for readable data rendering
    	htmlInfoPane = new JTextPane();
    	htmlInfoPane.setContentType("text/html");
    	
        htmlInfoPane.setEditorKit(new HTMLEditorKit());
        htmlInfoPane.setText("<html bgcolor=\"#000000\"></html>");
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
    
    public void displayInformationText(final Iterable<? extends GraphElement> elements) {
        // check if minimum interval has elapsed
        if (!checkAndSetScheduled()) {
            updateExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    // try to get swing to change the contents of the HTML pane
                    try {
                        htmlInfoPane.setText(htmlBuilder.renderAsHTML(elements, w - infoControllers.getWidth() - 20));
                    } catch (Exception e) {
                        System.out.println("ERROR: Swing cannot handle this html:\n\n" + htmlBuilder.renderAsHTML(elements, w - infoControllers.getWidth() - 20));
                        e.printStackTrace();
                    }
                    // update swing
                    validate();
                    scrollPane.getVerticalScrollBar().setValue(0);
                    
                    // update completed, set flag accordingly
                    updateScheduled = false;
                }
            }, updateInterval, TimeUnit.MILLISECONDS);
        }
    }
    
    public void logEvent(String s) {
        infoControllers.logEvent(s);
    }

    @Override
    public void selectionChanged() {
        displayInformationText(graph.getSelection());
    }
    
    /**
     * sets the updateScheduled flag to true, returns the value before it was set
     */
    private boolean checkAndSetScheduled() {
        boolean ret = updateScheduled;
        updateScheduled = true;
        return ret;
    }
}
