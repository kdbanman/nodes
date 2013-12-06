package nodes;

import com.hp.hpl.jena.rdf.model.Statement;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

/**
 * Class for constructing well-formatted HTML based upon jena models.
 * 
 * @author kdbanman
 */
public class HTMLBuilder {
    private String header, footer;
    
    public HTMLBuilder() {
    	header = "<body>";
    	footer = "</body>";
    }
    
    /**
     * render a collection of GraphElements within the root element of the passed HTMLDocument.
     * each node is rendered with a table for outgoing triples and a table for incoming triples.
     * each edge is rendered with a table for each triple contained within it.
     */
    public void renderAsHTML(Iterable<? extends GraphElement> elements, HTMLDocument doc) {
    	String bodyHTML = "";
        
        try {
            for (GraphElement e : elements) {
                bodyHTML += renderedElement(e);
            }
        } catch (java.util.ConcurrentModificationException exc) {
            // if, during iteration through the collection of graph elements,
            // that collection is modified, then this catch block is executed.
            // the string setInfo will be rerendered next frame, so return the
            // error string as a passive indicator of the (transient state)
            bodyHTML = "<font color=\"#FFFFFF\">comodification error in HTMLBuilder.renderAsHTML()</font>";
        }
        
    	try {
            doc.setInnerHTML(doc.getDefaultRootElement(), header + bodyHTML + footer);
        } catch (BadLocationException ex) {
            System.out.println("bad location");
        } catch (IOException ex) {
            System.out.println("io exception");
        }
    }
    
    /**
     * render a single GraphElement as a sequence of HTML headings and tables.
     * undefined behavior for null parameter.
     */
    private String renderedElement(GraphElement e) {
        if (e instanceof Node) {
            return renderedNode((Node) e);
        } else {
            return renderedEdge((Edge) e);
        }
    }
    
    /**
     * render a single Node as a sequence of HTML headings and tables.
     * separate tables are rendered for incoming and outgoing triples.
     */
    private String renderedNode(Node n) {
        Graph graph = n.getGraph();
        
        String rendered = h1Heading("Entity: ");
        rendered += fontWrap(n.getPrefixedName());
        
        rendered += h2Heading("Entity Properties: ");
        rendered += statementTable(n.getOutboundStatements(), graph);
        
        rendered += h2Heading("Connections To Entity: ");
        rendered += statementTable(n.getInboundStatements(), graph);
        
        return rendered;
    }
    
    /**
     * render a single Edge as an HTML heading and table of its contained triples.
     */
    private String renderedEdge(Edge e) {
        Graph graph = e.getGraph();
        
        String rendered = h2Heading("Statements Within Edge:");
        rendered += statementTable(e.getTriples().iterator(), graph);
        
        return rendered;
    }
    
    private String h1Heading(String headingText) {
        return "<h1>" + fontWrap(headingText) + "</h1>";
    }
    
    private String h2Heading(String headingText) {
        return "<h2>" + fontWrap(headingText) + "</h2>";
    }
    
    private String h3Heading(String headingText) {
        return "<h3>" + fontWrap(headingText) + "</h3>";
    }
    
    private String statementTable(Iterator<Statement> it, Graph graph) {
        String rendered = "";
        if (!it.hasNext()) {
            rendered = fontWrap("  None");
        } else {
            rendered = "<table border=\"1\">" + tableHead("Node", "Property", "Value");

            while (it.hasNext()) {
                Statement s = it.next();
                rendered += tableRow(graph.prefixed(s.getSubject().toString()),
                                     graph.prefixed(s.getPredicate().toString()),
                                     graph.prefixed(s.getObject().toString()));
            }
            rendered += "</table>";
        }
        return rendered;
    }
    
    private String fontWrap(String inside) {
    	return "<font color=\"#FFFFFF\">" + inside + "</font>";
    }
    
    private String tableHead(String first, String second, String third) {
        return fontWrap("<tr><th>" + first + "</th><th>" + second + "</th><th>" + third + "</th></tr>");
    }
    
    private String tableRow(String first, String second, String third) {
        return fontWrap("<tr><td>" + first + "</td><td>" + second + "</td><td>" + third + "</td></tr>");
    }
}
