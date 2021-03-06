/*
 * main application logic for Nodes.
 */
package nodes;

import processing.core.*;
import processing.event.KeyEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import nodes.Modifier.ModifierType;
import nodes.controllers.RightClickList;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Tab;
//  this is the PeasyCam from https://github.com/jeffg2k/peasycam
import peasy.PeasyCam;
import processing.opengl.PGraphics3D;

/**
 * 
 * @author kdbanman
 */
public class Nodes extends PApplet implements Selection.SelectionListener {

	private static final long serialVersionUID = 8527157297916319L;
	// 3D graph-viewing camera
    PeasyCam cam;
    //ControlP5
    ControlP5 cp5;
    // matrix and vector module for interaction in 3D
    UnProjector proj;
    // graph module for RDF visualization
    Graph graph;

    // update flag raised if the controllers have not responded to a change in
    // selection.  see selectionChanged() and draw().
    AtomicBoolean selectionUpdated;

    // control panel window (contains its own controlP5 instance)
    // static so that it can be initialized in main() as a workaround for the
    // bug that doesn't allow focus to change to panelFrame in linux
    static ControlPanelFrame controlPanelFrame;
    static InfoPanelFrame infoPanelFrame;
    
    // mouse information for click and drag selection
    int lastPressedX;
    int lastPressedY;
    boolean leftButtonDragging;
    
    // current color for selected nodes and edges (for color pulsation)
    int selectColor;
    // current direction of selection color change (for color pulsation)
    boolean selectColorRising;

    // enum for tracking what the mouse is currently doing.  for management of
    // drag movement and selection, which all use left mouse button
    DragBehaviour drag;

    // Right Click list controller
    RightClickList rightClickList;
    // To keep track of modifiers execution on a mouse "click"
    // since the modifier code gets executed prior to the event getting fired
    boolean modifierFired = false;
    // Lists of modifiers and modifiersets
    private Collection<Modifier> modifiers = Collections.emptyList();
    private Collection<ModifierSet> modifiersets = Collections.emptyList();
    // Map of list indexes to each modifier
    private final ConcurrentHashMap<Integer, Modifier> uiModifiers = new ConcurrentHashMap<Integer, Modifier>();

    // list for tracking which GraphElements have been hovered over.  this is 
    // necessary because:
    //   ControlP5's native onLeave() calls are based on the assumption that only
    //   one controller will be hovered over at any given time.
    // "hovered" is populated by onEnter() calls within GraphElement.
    ArrayList<GraphElement<?>> hovered;
    
    // object container for GraphElement concurrency between rendering and 
    // external modification
    Object waitingOnNewFrame;

    @Override
    public void setup() {
		// configure parent PApplet
		int w = 1024;
		int h = 768;

		size(w, h, P3D);
		frameRate(30);
		noStroke();

        // initialize camera
        cam = new PeasyCam(this, 0, 0, 0, 600);

        cp5 = new ControlP5(this);
        cp5.setAutoDraw(false);
        // configure camera controls
        cam.setLeftDragHandler(null);
        cam.setRightDragHandler(cam.getRotateDragHandler());
        cam.setCenterDragHandler(cam.getZoomDragHandler());
        cam.setWheelHandler(cam.getZoomWheelHandler());
        cam.setResetOnDoubleClick(false);
        
        // set camera movement behaviour
        cam.setSpeedLock(false);
        cam.setDamping(.4, .4, .4);

        proj = new UnProjector(this);
        graph = new Graph(proj, this);

        // horrible hack means that static panelFrame has already been constructed
        // within main()
        controlPanelFrame.initialize(graph);
        // initialize info panel so that it renders up to 50 graph elements simultaneously
        // at a maximum rate of once every .75 seconds
        infoPanelFrame.initialize(graph, 50, 750);
        
        // set program startup default values (see comments in field declaration
        // for more information)
        lastPressedX = 0;
        lastPressedY = 0;
        
        selectColor = 0xFF5C5C5C;
        selectColorRising = true;
        
        drag = DragBehaviour.SELECT;
        
        hovered = new ArrayList<>();
        
        waitingOnNewFrame = null;

        selectionUpdated = new AtomicBoolean(true);

        graph.getSelection().addListener(this);

        // Right click list controller
		rightClickList = new RightClickList(cp5, (Tab) cp5.controlWindow.getTabs().get(1), "rightClickList", 0, 0, 200, 20);
		//equivalent to cp5.MultiList
		cp5.register(null, "", rightClickList);
		rightClickList.registerProperty("value")
						.setVisible(false)
						.hide();
		//get a list of modifiers and modifiersets
        try {
	        modifiers = graph.getModifiersList();
	        modifiersets = graph.getModifierSetsList();
        } catch (Exception e) {
	        e.printStackTrace();
	        System.err.println("ERROR: getting list of Modifiers/ModifierSets");
        }
    }

    @Override
    public void draw() {

        if (waitingOnNewFrame != null) {
            synchronized (waitingOnNewFrame) {
                waitingOnNewFrame.notify();
            }
            try {
                synchronized(this) {
                    wait();
                }
            } catch (InterruptedException e) {
                System.out.println("ERROR: view window wait() interrupted");
            }
        }
        // light orange pastel background color
        background(0xFFFFDCBF);

        // light the scene from the cursor
        proj.captureViewMatrix((PGraphics3D) this.g);
        proj.calculatePickPoints(mouseX, mouseY);
        pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);

        // draw the graph
		graph.draw();

		// on re-draw don't erase the right click menu if it's visible
		if (rightClickList.isVisible()) {
			// For 2D rect
			hint(DISABLE_DEPTH_TEST);
			cam.beginHUD();
			noLights();
			cp5.draw();
			cam.endHUD();
			hint(ENABLE_DEPTH_TEST);
		}

        // determine if the user is currently selecting graph elements
        if (leftButtonDragging && drag == DragBehaviour.SELECT) {
            // draw transparent rectangle over selection area
            int minX = min(mouseX, lastPressedX);
            int minY = min(mouseY, lastPressedY);

            int maxX = max(mouseX, lastPressedX);
            int maxY = max(mouseY, lastPressedY);

            // HUD calls allow drawing on screen instead of 3D space
            cam.beginHUD();
            fill(0x33333333);
            noStroke();
            rect(minX, minY, maxX - minX, maxY - minY);
            cam.endHUD();
        }

        // perform a step of the force-directed layout if the corresponding control
        // is selected
        if (controlPanelFrame.controls.autoLayoutSelected()) {
            graph.autoLayoutIteration();
        }
        
        // ensure no graph elements are mistakenly mouse-hovered
        cleanHovered();
        
        // iterate selection color pulsation
        updateSelectColor();
	}

    @Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case PConstants.UP: 	cam.getPanDragHandler().handleDrag(0, 60); break;
			case PConstants.DOWN: 	cam.getPanDragHandler().handleDrag(0, -60); break;
			case PConstants.LEFT: 	cam.getPanDragHandler().handleDrag(60, 0); break;
			case PConstants.RIGHT: 	cam.getPanDragHandler().handleDrag(-60, 0); break;
		}
    }

    // every time selection is changed, this is called
    @Override
    public void selectionChanged() {
        // queue controller selection update if one is not already queued
        selectionUpdated.compareAndSet(false, true);
    }

    public void controlEvent(ControlEvent event) {
		if (event.isFrom(rightClickList)) {
			try {
				uiModifiers.get((int) event.getValue()).modify();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				modifierFired = true;
			}
		}
    }

    /*
     * synchronize another process between rendering frames of Nodes
     * to begin waiting for new frame:
     *   graph.pApp.waitForNewFrame(this);
     * be sure to restart nodes after the process is done messing with Nodes' state:
     *   graph.pApp.restartRendering(tihs);
     */
    public void waitForNewFrame(Object o) {
        try {
            waitingOnNewFrame = o;
            synchronized(o) {
                o.wait();
            }
        } catch (InterruptedException e) {
            System.out.println("ERROR: object " + o.toString() + " wait() was interrupted.");
            waitingOnNewFrame = null;
        }
    }

    public void restartRendering(Object o) {
        if (waitingOnNewFrame != o) {
            System.out.println("ERROR: " + waitingOnNewFrame.toString() 
                    + " currently processing, but " + o.toString() + 
                    "attempted to restart thread.");
        } else {
            synchronized (this) {
                this.notify();
            }
            waitingOnNewFrame = null;
        }
    }
    
    // called only when the mouse button is initially depressed, NOT while it is held
    @Override
    public void mousePressed() {
		rightClickList.hide();

        // store initial mouse click location for rectangular selection box
        if (mouseButton == LEFT) {
            lastPressedX = mouseX;
            lastPressedY = mouseY;
        }
		if (mouseButton == CENTER) {
			GraphElement<?> hovered = getNearestHovered();

			if (hovered != null)
				graph.getSelection().add(hovered);

			if (selectionUpdated.getAndSet(false))
				refreshRightClickList();

			rightClickList.updateLocation(mouseX, mouseY);
			rightClickList.show();
		}
    }

    // called only when the mouse is moved while a button is depressed
    @Override
    public void mouseDragged() {
        if (mouseButton == LEFT) {
            // if this is the first mouse drag since the left button has been
            // depressed, determine if the user is selecting elements or
            // dragging them around to move them
            if (!leftButtonDragging) {
                if (hovered.isEmpty()) {
                    // if the user is not dragging a graph element, enable selection
                    drag = DragBehaviour.SELECT;
                    // add to selection if shift button is held
                    if (!shiftPressed()) graph.getSelection().clear();
                } else {
                    // if the user is dragging a graph element, enable movement
                    drag = DragBehaviour.DRAG;
                }
            }
            leftButtonDragging = true;

            if (drag == DragBehaviour.SELECT) {
                // BOX SELEECT
                //   elements within the rectangle of selection are added to the
                //   selection buffer, which is committed to the selection upon
                //   mouse release.
                //////////////
                
                // determine nodes and edges within the drag box to selection
                int minX = min(mouseX, lastPressedX);
                int minY = min(mouseY, lastPressedY);

                int maxX = max(mouseX, lastPressedX);
                int maxY = max(mouseY, lastPressedY);

                // every time the selection box is changed, clear the selection
                // and recompute the box membership of each graph element
                graph.getSelection().clearBuffer();
                for (GraphElement<?> n : graph) {
                    // get screen coordinates of graph element
                    PVector nPos = n.getPosition();
                    float nX = screenX(nPos.x, nPos.y, nPos.z);
                    float nY = screenY(nPos.x, nPos.y, nPos.z);

                    // test membership graph element
                    if (nX <= maxX && nX >= minX && nY <= maxY && nY >= minY) {
                        graph.getSelection().addToBuffer(n);
                    }
                }
            } else {
                // DRAG MOVE
                //    element being dragged, and any other elements currently
                //    selected, are dragged orthogonally to the srceen frustum
                //    plane.  the element being dragged is used as the reference,
                //    meaning that it stays underneath the mouse cursor.
                ////////////
                
                // element to be moved
				GraphElement<?> toBeMoved = getNearestHovered();
                
                // get distance between pixels on near frustum
                proj.calculatePickPoints(0, 0);
                PVector origin = proj.ptStartPos.get();
                proj.calculatePickPoints(0, 1);
                float pixelDist = PVector.dist(origin, proj.ptStartPos);

                // get distance traversed over drag
                float rawDistH = pixelDist * ((float) (mouseX - pmouseX));
                float rawDistV = pixelDist * ((float) (mouseY - pmouseY));

                // get near frustum unit vectors
                PVector horiz = proj.getScreenHoriz();
                PVector vert = proj.getScreenVert();

                // calculate scale to tranlate from cursor movement to element movement
                //      (camera to element distance)/(camera to cursor distance)
                proj.calculatePickPoints(mouseX, mouseY);
                PVector camPos = getCamPosition();
                float cursorDist = PVector.dist(proj.ptStartPos, camPos);
                float elementDist = PVector.dist(toBeMoved.getPosition(), camPos);
                float scale = elementDist / cursorDist;

                // calculate drag vectors
                horiz.mult(scale * rawDistH);
                vert.mult(scale * rawDistV);

                // because shift may or may not be held at this point, mouse content may
                // or may not be in the selection, so it should be moved separately from
                // the selection if it is not selected.  single element movement is 
                // semantically different between nodes and edges (edge position is
                // derived), so the moveIfNotSelected() is overriden in Edge
                toBeMoved.moveIfNotSelected(horiz, vert);

                // now that element has been moved if unselected, move the selection
                for (Node n : graph.getSelection().getNodes()) {
                    n.getPosition().add(horiz);
                    n.getPosition().add(vert);
                }
            }
        }
    }

    // called whenever the mouse button is released
    @Override
    public void mouseReleased() {
		if (modifierFired) {
			modifierFired = false;
			return;
		}

        if (mouseButton == LEFT) {
            if (drag == DragBehaviour.SELECT) {
                // clear the selection if shift is not held
                if (!shiftPressed()) graph.getSelection().clear();
                
                if (leftButtonDragging) {
                    // if left button was being dragged during selection,
                    // add the contents of the selection buffer to the selection
                    graph.getSelection().commitBuffer();
                } else if (!hovered.isEmpty()) {
                    // if the user was not dragging, then just a single click
                    // occurred.  if the user clicked on an element, then invert
                    // its selection status
                    graph.getSelection().invertSelectionOfElement(getNearestHovered());
                }
            }
            // left button is no longer being dragged
            leftButtonDragging = false;
            // if user was dragging to move, that is done now.
            drag = DragBehaviour.SELECT;
        }
    }

    // determine if the shift key is currently depressed
    public boolean shiftPressed() {
        return keyPressed && key == CODED && keyCode == SHIFT;
    }
    
    // get current camera position in PVector form
    public PVector getCamPosition() {
        float[] camPos = cam.getPosition();
        return new PVector(camPos[0], camPos[1], camPos[2]);
    }
    
    // get current camera look target in PVector form
    public PVector getCamLookat() {
        float[] camLook =cam.getLookAt();
        return new PVector(camLook[0], camLook[1], camLook[2]);
    }
    
    public ArrayList<GraphElement<?>> getHovered() {
        return hovered;
    }
    
    // returns the GraphElement nearest to the cursor position on the near 
    // frustum plane
    public GraphElement<?> getNearestHovered() {
        if (hovered.isEmpty()) return null;
        
        proj.calculatePickPoints(mouseX, mouseY);
        PVector mousePos = proj.ptStartPos;

        GraphElement<?> closest = hovered.get(0);
        float minDist = mousePos.dist(closest.getPosition());
        for (GraphElement<?> e : hovered) {
            float currElementDist = mousePos.dist(e.getPosition());
            if (minDist > currElementDist) {
                minDist = currElementDist;
                closest = e;
            }
        }
        return closest;
    }
    
    // when a GraphElement is moused over, it calls this with itself as a
    // parameter
    public void addToHovered(GraphElement<?> element) {
        hovered.add(element);
        
        // display new hovered element in info panel
        infoPanelFrame.displayInformationText(hovered);
    }

    // make sure that no GraphElement erroneously keeps mouseover state:
    // ControlP5's native onLeave() calls are based on the assumption that only
    // one controller will be hovered over at any given time, so onLeave() calls
    // cannot be depended upon.
    public void cleanHovered() {
        // the infopanel has a jarring flash every time its html contents are 
        // rerendered, so this boolean ensures it is not done more than necessary
        boolean infopanelNeedsUpdate = false;

        // every time the mouse hovers over a GraphElement, that element 
        // references itself in the hovered list.
        // iterate over each element in hovered.
        Iterator<GraphElement<?>> it = hovered.iterator();
        while (it.hasNext()) {
			GraphElement<?> e = it.next();
            if (!e.isInside()) {
                // if the element is not hovered over any more, call its  
                // respective method and remove it from the list.
                e.notHovered();
                
                // GraphElements cannot remove themselves because this iterative
                // cleanup process is necessary, and it is dangerous to remove
                // elements from a list while it is being iterated through.
                it.remove();
                
                // infopanel needs an update because the hovered contents have changed
                infopanelNeedsUpdate = true;
            }
        }

        if (infopanelNeedsUpdate) {
            // if hovered is now empty, render the selection.
            if (!hovered.isEmpty()) infoPanelFrame.displayInformationText(hovered);
            // if not, render the hovered elements
            else infoPanelFrame.displayInformationText(graph.getSelection());
        }
    }
    
    // mechanism for making selection color pulsate in grayscale
    public void updateSelectColor() {
        if (selectColor >= 0xFFBABABA) {
            selectColorRising = false;
        } else if (selectColor <= 0xFF5C5C5C) {
            selectColorRising = true;
        }
        
        if (selectColorRising) selectColor += 0x101010;
        else selectColor -= 0x50505;
    }
    
    // log event to user in infopanel event box
    public void logEvent(String s) {
        infoPanelFrame.logEvent(s);
    }
    
    // get SPARQL endpoint URI from control panel.
    public String getSparqlEndpoint() {
        return controlPanelFrame.controls.getHttpSparqlEndpoint();
    }
    
    // get File import uri from control panel.
    public String getFileImportPath() {
        return controlPanelFrame.controls.getFileImportPath();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        controlPanelFrame = new ControlPanelFrame();
        infoPanelFrame = new InfoPanelFrame();
        
        PApplet.main(new String[]{nodes.Nodes.class.getName()});
    }
    
    // enum for tracking user's current mouse dragging intent
    public enum DragBehaviour {
        SELECT, DRAG;
    }

    /*
     * Refreshes the visual right click menu list
     */
    private void refreshRightClickList() {
		if (rightClickList == null || (modifiers.isEmpty() && modifiersets.isEmpty()))
			return;

		rightClickList.clearButtons();
		uiModifiers.clear();

		for (Modifier m : modifiers) {
			if ((m.getType() == ModifierType.ALL || m.getType() == ModifierType.VIEW) && m.isCompatible()) {
				rightClickList.add(m.getTitle(), uiModifiers.size());
				uiModifiers.put(uiModifiers.size(), m);
			}
		}

		for (ModifierSet s : modifiersets) {
			if ((s.getType() != ModifierType.ALL || s.getType() != ModifierType.VIEW) && s.isCompatible()) {
				s.constructModifiers();

				for (Modifier m : s.getModifiers()) {
					if (m.isCompatible()) {
						rightClickList.add(m.getTitle(), uiModifiers.size());
						uiModifiers.put(uiModifiers.size(), m);
					}
				}
			}
		}
	}
}
