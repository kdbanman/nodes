package nodes.modifiers;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.Set;

import processing.core.PApplet;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;
import controlP5.Textfield;
import nodes.Edge;
import nodes.Graph;
import nodes.Modifier;
import nodes.Node;
import nodes.Selection;

/**
 * @author karim
 *
 * Search modifier, adds the ability to search for nodes containing or matching a string specified by the user
 * via a popup window
 */
public class TripleSearch extends Modifier {

    public TripleSearch(Graph graph) {
        super(graph);
    }

    @Override
    public boolean isCompatible() {
        return graph.tripleCount() > 0;
    }

    @Override
    public String getTitle() {
        return "Search";
    }

    @Override
    public void modify() {
        SearchFrame s = new SearchFrame(this);

        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String searchTerm = s.getSearchTerm();
        s.close();

        if (searchTerm == null || searchTerm.isEmpty())
            return;

        Selection sele = graph.getSelection();
        sele.clearBuffer();
        sele.clear();

        Set<Edge> edges = graph.getEdges();
        Node src, dest;

        for (Edge e : edges) {
            src = e.getSourceNode();
            dest = e.getDestinationNode();

            if(e.getName().contains(searchTerm) || graph.prefixed(e.getName()).contains(searchTerm))
                sele.addToBuffer(e);

            if (src != null && (src.getName().contains(searchTerm) || graph.prefixed(src.getName()).contains(searchTerm)))
                sele.addToBuffer(src);

            if (dest != null && (dest.getName().contains(searchTerm) || graph.prefixed(dest.getName()).contains(searchTerm)))
                sele.addToBuffer(dest);

            src = null;
            dest = null;
        }
        sele.commitBuffer();
    }

    /*
     * Mostly taken from TripleChoserFrame by kdbanman
     */
    private class SearchFrame extends Frame {

        private static final long serialVersionUID = 1150469663366192294L;

        private String searchTerm;
        int frameW, frameH;

        Object waiting;

        SearchWindow search;

        public SearchFrame(Object waitingObj) {
            super("Enter search text");

            frameW = 300;
            frameH = 100;

            waiting = waitingObj;

            setLayout(new BorderLayout());

            setSize(frameW, frameH);
            setLocationRelativeTo(null);
            setResizable(false);
            setVisible(true);
            search = new SearchWindow();

            add(search, BorderLayout.CENTER);
            validate();
            search.init();
        }

        public void close() {
            search.stop();
            dispose();
        }

        public String getSearchTerm() {
            return searchTerm;
        }

        public void setSearchTerm(String searchTerm) {
            this.searchTerm = searchTerm;
        }

        private class SearchWindow extends PApplet {

            private static final long serialVersionUID = 982758246505135143L;

            ControlP5 cp5;
            Textfield sBox;

            public SearchWindow() {}

            @Override
            public void setup() {
                size(frameW, frameH);
                cp5 = new ControlP5(this);

                sBox = cp5.addTextfield("search text").setPosition(20, 20)
                        .setSize(150, 20).setText("").setFocus(true);

                setSearchTerm("");

                cp5.addButton("FIND TRIPLES").setPosition(200, 20)
                        .setSize(70, 20).addCallback(new CallbackListener() {

                            @Override
                            public void controlEvent(CallbackEvent event) {
                                if (event.getAction() == ControlP5.ACTION_RELEASED) {

                                    setSearchTerm(sBox.getText());

                                    synchronized (waiting) {
                                        waiting.notify();
                                    }
                                }
                            }
                        });
            }

            @Override
            public void draw() {
                background(0);
            }
        }
    }
}
