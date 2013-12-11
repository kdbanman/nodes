package nodes;

import com.hp.hpl.jena.rdf.model.Statement;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import sun.font.CoreMetrics;
import sun.font.TextLineComponent;

/**
 * Class for constructing well-formatted HTML based upon jena models.
 * 
 * @author kdbanman
 */
public class HTMLBuilder {
    private String header, footer;
    private int maximum;
    
    /**
     * 
     * @param maxElements maximum number of elements that will be rendered.
     */
    public HTMLBuilder(int maxElements) {
    	header = "<!DOCTYPE html><html bgcolor=\"#000000\"><head><meta charset=\"UTF-8\"></head><body>";
    	footer = "</body></html>";
        maximum = maxElements;
    }
    
    public void setMaximum(int max) {
        maximum = max;
    }
    
    /**
     * render a collection of GraphElements within the root element of the passed HTMLDocument.
     * each node is rendered with a table for outgoing triples and a table for incoming triples.
     * each edge is rendered with a table for each triple contained within it.
     */
    public String renderAsHTML(Iterable<? extends GraphElement> elements, int width) {
    	String bodyHTML = "";
        
        try {
            // build HTML from passed collection
            int i = 0;
            for (GraphElement e : elements) {
                bodyHTML += renderedElement(e, width);
                bodyHTML += "<br><hr align=\"center\" width=\"" + 2 * width / 3 + "\">";
                i++;
                
                if (i >= maximum) {
                    bodyHTML += fontWrap("Up to" + maximum + " elements are displayable here.  Maximum has been reached.");
                    break;
                }
            }
        } catch (java.util.ConcurrentModificationException exc) {
            // if, during iteration through the collection of graph elements,
            // that collection is modified, then this catch block is executed.
            // the string setInfo will be rerendered next frame, so return the
            // error string as a passive indicator of the (transient state)
            bodyHTML = "<font face=\"courier\" color=\"#FFFFFF\">Comodification error in HTMLBuilder.  Try again.</font>";
        }
        
        return header + bodyHTML + footer;
    }
    
    /**
     * render a single GraphElement as a sequence of HTML headings and tables.
     * undefined behavior for null parameter.
     */
    private String renderedElement(GraphElement e, int width) {
        if (e instanceof Node) {
            return renderedNode((Node) e, width);
        } else {
            return renderedEdge((Edge) e, width);
        }
    }
    
    /**
     * render a single Node as a sequence of HTML headings and tables.
     * separate tables are rendered for incoming and outgoing triples.
     */
    private String renderedNode(Node n, int width) {
        Graph graph = n.getGraph();
        
        String rendered = h1Heading("Entity: ");
        rendered += fontWrap(n.getPrefixedName());
        
        rendered += h2Heading("Entity Properties: ");
        rendered += statementTable(n.getOutboundStatements(), graph, width);
        
        rendered += h2Heading("Connections To Entity: ");
        rendered += statementTable(n.getInboundStatements(), graph, width);
        
        return rendered;
    }
    
    /**
     * render a single Edge as an HTML heading and table of its contained triples.
     */
    private String renderedEdge(Edge e, int width) {
        Graph graph = e.getGraph();
        
        String rendered = h2Heading("Statements Within Edge:");
        rendered += statementTable(e.getTriples().iterator(), graph, width);
        
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
    
    private String statementTable(Iterator<Statement> it, Graph graph, int width) {
        String rendered = "";
        if (!it.hasNext()) {
            rendered = fontWrap("  None");
        } else {
            rendered = "<table border=\"1\" cellspacing=\"0\" width=\"" + width + "\">" + tableHead("Node", "Property", "Value");

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
        inside = inside.replaceAll("[\\u0D02-\\u0D6F]+", "!MALAYALAM CHARACTERS NOT SUPPORTED!");
        return "<font face=\"tahoma\" color=\"#FFFFFF\">" + inside + "</font>";
    }
    
    private String tableHead(String first, String second, String third) {
        return fontWrap("<tr><th>" + first + "</th><th>" + second + "</th><th>" + third + "</th></tr>");
    }
    
    private String tableRow(String first, String second, String third) {
        return fontWrap("<tr><td>" + first + "</td><td>" + second + "</td><td>" + third + "</td></tr>");
    }
}
