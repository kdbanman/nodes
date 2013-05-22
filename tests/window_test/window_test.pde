// this works fine, but there is a relatively rare bug.  the line commented with
// CRASH POINT!!
// throws a nullpointerexception once in a while right at startup.  it may be
// correlated with me clicking around like an idiot

import controlP5.*;
import peasy.*;

import java.awt.Frame;
import java.awt.BorderLayout;

PeasyCam cam;
ControlP5 cp5;
TitleFrame cf;
CallbackListener cb;

UnProjector proj;

boolean dragging;
int newNode;

void setup() {
  int w = 800;
  int h = 600;
  size(w, h , P3D);
  
  
  cam = new PeasyCam(this, 0, 0, 0, 300);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(1000);
  
  cam.setRightDragHandler(null);

  cp5 = new ControlP5(this);
  
  cf = addTitleFrame("titles", 400, 200);
  
  dragging = false;
  newNode = 5;
  cb = new CallbackListener() {
    public void controlEvent(CallbackEvent theEvent) {
      switch(theEvent.getAction()) {
        
        case(ControlP5.ACTION_ENTER):
        // CRASH POINT!!
        cf.nameOf.setText(theEvent.getController().getName());
        cursor(HAND);
        break;
        
        case(ControlP5.ACTION_LEAVE):
        cf.nameOf.setText(cf.def);
        cursor(ARROW);
        break;
        
        case(ControlP5.ACTION_PRESSED):
        dragging = true;
        break;
        
        case(ControlP5.ACTION_RELEASEDOUTSIDE):
        
        if (theEvent.getController() instanceof Node && mouseButton == RIGHT) {
          Node cont = (Node) theEvent.getController();
          proj.calculatePickPoints(mouseX, mouseY);
          PVector loc = PVector.lerp(proj.ptStartPos, proj.ptEndPos, 0.1);
          Node n = new Node(cp5, "n"+Integer.toString(newNode), proj)
                .setPosition(loc.x, loc.y, loc.z)
                .setSize(int(cf.sizer.getValue()));
          new Edge(cp5, "eTo" + Integer.toString(newNode), proj, cont, n)
                .setSize(10);
          newNode++;
        }
        dragging = false;
        break;
        
        case(ControlP5.ACTION_RELEASED):
        dragging = false;
        break;
      }
    }
  };
  
  cp5.addCallback(cb);
  
  proj = new UnProjector();

  
  Node n1 = new Node(cp5, "n1", proj)  
                  .setPosition(0,0,0)
                  .setSize(30);
  Node n2 = new Node(cp5, "n2", proj)
                   .setPosition(50,50,0)
                   .setSize(20);
  Node n3 = new Node(cp5, "n3", proj)
                   .setPosition(-40,-120,-50)
                   .setSize(10);
  Node n4 = new Node(cp5, "n4", proj)
                   .setPosition(200,200,200)
                   .setSize(10);
  
  new Edge(cp5, "e13", proj, n1, n3).setSize(5);
  new Edge(cp5, "e23", proj, n2, n3).setSize(5);
  new Edge(cp5, "e34", proj, n3, n4).setSize(5);
    
}

void draw() {
  proj.captureViewMatrix((PGraphics3D) this.g);
  
  proj.calculatePickPoints(mouseX, mouseY);
  pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
  
  background(#E07924);
}


TitleFrame addTitleFrame(String theName, int theWidth, int theHeight) {
  Frame f = new Frame(theName);
  TitleFrame p = new TitleFrame(this, theWidth, theHeight);
  f.add(p);
  p.init();
  f.setTitle(theName);
  f.setSize(p.w, p.h);
  f.setLocation(800, 800);
  f.setResizable(true);
  f.setVisible(true);
  return p;
}

public class TitleFrame extends PApplet {

  int w, h;
  
  String def = "<hover over graph elements>";

  Textlabel nameOf;
  Slider sizer;
  
  public void setup() {
    size(w, h);
    frameRate(25);
    cp5 = new ControlP5(this);
    
    nameOf = cp5.addTextlabel("name")
                .setText(def)
                .setPosition(10, 10)
                .setColorValue(0xffffff00)
                .setFont(createFont("Georgia",20))
                ;
    sizer = cp5.addSlider("sizer")
               .setPosition(10, 50)
               .setSize(200, 20)
               .setRange(5,300)
               .setValue(20);
  }

  public void draw() {
      background(0);
      
  }
  
  private TitleFrame() {
  }

  public TitleFrame(Object theParent, int theWidth, int theHeight) {
    parent = theParent;
    w = theWidth;
    h = theHeight;
  }


  public ControlP5 control() {
    return cp5;
  }
  
  
  ControlP5 cp5;

  Object parent;

  
}

