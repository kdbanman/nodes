package nodes;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Class for constructing well-formatted HTML based upon jena models.
 * 
 * @author kdbanman
 */
public class HTMLBuilder {
    private String header, footer;
    
    public HTMLBuilder() {
    	header = "<html bgcolor=\"#000000\"><body>";
    	footer = "</body></html>";
    }
    
    public String renderAsHTML(Model toRender) {
    	//TODO
    	String body =    "<h1><font color=\"#FFFFFF\">title weeeeeeeee</font></h1>" +
		    			 "<table border=\"1\">" +
		    			 "<font color=\"#FFFFFF\"><tr><th>Month</th><th>Savings</th></tr></font>" +
		    			 "<font color=\"#FFFFFF\"><tr><td>butt</td><td>$100</td></tr></font>" +
		    			 "<font color=\"#FFFFFF\"><tr><td>boo</td><td>$1030</td></tr></font>" +
		    			 "</table>";
    	
    	return header + body + footer;
    }
    
    private String fontWrap(String inside) {
    	return "<font color=\"#FFFFFF\">" + inside + "<\font>";
    }
}
