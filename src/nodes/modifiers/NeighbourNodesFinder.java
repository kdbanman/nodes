package nodes.modifiers;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Set;

import processing.core.PApplet;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlKey;
import controlP5.ControlP5;
import controlP5.Textfield;
import nodes.Graph;
import nodes.Modifier;
import nodes.Node;
import nodes.Selection;

/**
 * @author Karim
 *
 * Modifier for selecting a list of nodes that share a common number of nodes specified by the user
 * via a popup window
 */
public class NeighbourNodesFinder extends Modifier {

    /**
     * @param graph
     */
    public NeighbourNodesFinder(Graph graph) {
        super(graph);
    }

    @Override
    public boolean isCompatible() {
        return graph.nodeCount() > 0;
    }

    @Override
    public String getTitle() {
        return "Find nodes with common neighbours";
    }

	@Override
	public ModifierType getType() {
		return ModifierType.PANEL;
	}

    @Override
    public void modify() {
        FinderFrame f = new FinderFrame(this);

        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int num = f.getNeighboursSelection();
        f.close();

        if (num <= 0)
            return;

        Selection sele = graph.getSelection();
        sele.clearBuffer();
        sele.clear();

        Set<Node> nodes = graph.getNodes();

        for (Node i : nodes) {
            for (Node j : nodes) {

                if (i == j)
                    continue;

                ArrayList<Node> nbrs = graph.getCommonNbrs(i, j);

                if (nbrs.size() == num) {
                    if (!sele.contains(i))
                        sele.addToBuffer(i);
                    if (!sele.contains(j))
                        sele.addToBuffer(j);
                }
            }
        }
        sele.commitBuffer();
    }

    /*
     * Mostly taken from TripleChoserFrame by kdbanman
     */
    private class FinderFrame extends Frame {

        private static final long serialVersionUID = -5963247227145726367L;

        int neighbours;
        int frameW, frameH;

        Object waiting;

        FinderWindow finder;

        public FinderFrame(Object waitingObj) {
            super("choose a number");

            frameW = 300;
            frameH = 80;

            waiting = waitingObj;

            setLayout(new BorderLayout());

            setSize(frameW, frameH);
            setLocationRelativeTo(null);
            setResizable(false);
            setVisible(true);
            finder = new FinderWindow();

            add(finder, BorderLayout.CENTER);
            validate();
            finder.init();

        }

        public int getNeighboursSelection() {
            return neighbours;
        }

        public void setNeighboursSelection(int neighbours) {
            this.neighbours = neighbours;
        }

        public void close() {
            finder.stop();
            dispose();
        }

        private class FinderWindow extends PApplet {

            private static final long serialVersionUID = 982758246505135143L;

            ControlP5 cp5;
            Textfield nBox;

            public FinderWindow() {}

            @Override
            public void setup() {
                size(frameW, frameH);
                cp5 = new ControlP5(this);

                nBox = cp5.addTextfield("number of neighbours")
                        .setPosition(20, 20).setSize(100, 14)
                        .setInputFilter(ControlP5.INTEGER).setText("1");

                setNeighboursSelection(1);

                cp5.addButton("FIND NODES").setPosition(200, 20)
                        .setSize(70, 14).addCallback(new CallbackListener() {

                            @Override
                            public void controlEvent(CallbackEvent event) {
                                if (event.getAction() == ControlP5.ACTION_RELEASED) {
<<<<<<< HEAD
                                    ParseAndExit();

=======
							        try {
								        setNeighboursSelection(Integer.parseInt(nBox.getText()));
							        } catch (NumberFormatException e) {
								        setNeighboursSelection(0);
							        }

                                    synchronized (waiting) {
                                        waiting.notify();
                                    }
>>>>>>> refs/heads/master
                                }
                            }
                        });

                cp5.mapKeyFor(new ControlKey() {

                    @Override
                    public void keyEvent() {
                        ParseAndExit();
                    }
                }, ENTER);
            }

            @Override
            public void draw() {
                background(0);
            }

            private void ParseAndExit() {
                String text = nBox.getText();

                if (text == null || text.isEmpty())
                    return;

                setNeighboursSelection(Integer.parseInt(text));

                synchronized (waiting) {
                    waiting.notify();
                }
            }
        }
    }
}
