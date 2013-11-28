package nodes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author kdbanman
 */
public class InfoPanelFrame extends Frame {
    int w, h;
    
    JEditorPane testPane;
    JScrollPane scrollPane;
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
    	
    	testPane = new JTextPane();
    	testPane.setContentType("text/html");
        /* NOTES:
         * - font tags don't persist for long.  each table row seems to need one enclosing the <tr> tag
         * - 
         */
    	testPane.setText("<html bgcolor=\"#000000\"><table border=\"0\">" +
    					 "<font color=\"white\"><tr><th>Month</th><th>Savings</th></tr></font><font color=\"white\"><tr>" +
    					 "<td>Integer metus purus, faucibus sed ornare ac, interdum" +
    					 " sed neque. Vestibulum a urna sit amet nisl tincidunt convallis. Fusce sagittis" +
    					 " lacus sit amet nisi commodo cursus et mattis massa. In tincidunt tellus at" +
    					 " lectus interdum egestas. Aliquam erat volutpat. Pellentesque in congue lorem." +
    					 " Vestibulum molestie massa eget sapien porttitor gravida. Duis tristique ultrices" +
    					 " pulvinar. Pellentesque tempus quam purus, nec tincidunt metus lacinia a. Vestibulum" +
    					 " ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; " +
    					 " condimentum vehicula sem, at condimentum ante placerat sit amet. Fusce mi turpis," +
    					 " fermentum ac nibh id, vehicula pulvinar sapien. Suspendisse odio arcu, sagittis non " +
    					 "sagittis at, ornare sed nibh. Suspendisse potenti.</td><td>$100</td></tr></font>" +
    					 "</table>" +
    					 "<body> <font color=\"white\"> <p>Paragraph 1</p><p>Paragraph 2</p><p>Paragraph 1</p>" +
    					 "<p><b>Paragraph 2</p><p>Paragraph 1</p></b><p>Paragraph 2</p><p>Paragraph 1</p>" +
    					 "<p>Paragraph 2</p><p>Paragraph 1</p><p>Paragraph 2</p><p>Paragraph 1</p>" +
    					 "<p>Paragraph 2</p><p>Integer metus purus, faucibus sed ornare ac, interdum" +
    					 " sed neque. Vestibulum a urna sit amet nisl tincidunt convallis. Fusce sagittis" +
    					 " lacus sit amet nisi commodo cursus et mattis massa. In tincidunt tellus at" +
    					 " lectus interdum egestas. Aliquam erat volutpat. Pellentesque in congue lorem." +
    					 " Vestibulum molestie massa eget sapien porttitor gravida. Duis tristique ultrices" +
    					 " pulvinar. Pellentesque tempus quam purus, nec tincidunt metus lacinia a. Vestibulum" +
    					 " ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; " +
    					 " condimentum vehicula sem, at condimentum ante placerat sit amet. Fusce mi turpis," +
    					 " fermentum ac nibh id, vehicula pulvinar sapien. Suspendisse odio arcu, sagittis non " +
    					 "sagittis at, ornare sed nibh. Suspendisse potenti.</p><p>Paragraph 2</p><p>Paragraph 1</p>" +
    					 "<p>Paragraph 2</p><p>Paragraph 1</p><p>Paragraph 2</p></font></body></html>");
        
        testPane.setCaretPosition(0);
        testPane.setMargin(new Insets(0,0,0,0));
    	testPane.setEditable(false);
    	
        scrollPane = new JScrollPane(testPane);
        scrollPane.setPreferredSize(new Dimension(580, h));
        
    	add(scrollPane, BorderLayout.EAST);
    	
    	doc = (HTMLDocument) testPane.getDocument();
    	
        info = new InfoPanel(h, graph);
        
        add(info, BorderLayout.WEST);
        validate();
        info.init();
        
        addComponentListener(new ComponentListener(){
            @Override
            public void componentResized(ComponentEvent e) {
                w = InfoPanelFrame.this.getWidth();
                h = InfoPanelFrame.this.getHeight();
                
                int calculatedWidth = w - InfoPanelFrame.this.info.getWidth();
                calculatedWidth = calculatedWidth > 50 ? calculatedWidth : 50;
                
                Dimension calculatedSize = new Dimension(calculatedWidth, h);
                
                scrollPane.setPreferredSize(calculatedSize);
                
                InfoPanelFrame.this.info.setHeight(h);
                
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
