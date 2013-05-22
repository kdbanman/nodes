import controlP5.*;
import peasy.*;

PeasyCam cam;
ControlP5 cp5;
UnProjector proj;

void setup() {
  int w = 800;
  int h = 600;
  size(w, h , P3D);
  
  cam = new PeasyCam(this, 0, 0, 0, 100);
  cam.setMinimumDistance(10);
  cam.setMaximumDistance(300);
  
  
  cp5 = new ControlP5(this);
  proj = new UnProjector();
  
  cp5.addFrameRate().setInterval(10).setPosition(0,height - 10);
  
  
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

// for HUD test:
void createMessageBox() {
  // create a group to store the messageBox elements
  messageBox = cp5.addGroup("messageBox",width/2 - 150,100,300);
  messageBox.setBackgroundHeight(120);
  messageBox.setBackgroundColor(color(0,100));
  messageBox.hideBar();
  
  // add a TextLabel to the messageBox.
  Textlabel l = cp5.addTextlabel("messageBoxLabel","Some MessageBox text goes here.",20,20);
  l.moveTo(messageBox);
  
  // add a textfield-controller with named-id inputbox
  // this controller will be linked to function inputbox() below.
  Textfield f = cp5.addTextfield("inputbox",20,36,260,20);
  f.captionLabel().setVisible(false);
  f.moveTo(messageBox);
  f.setColorForeground(color(20));
  f.setColorBackground(color(20));
  f.setColorActive(color(100));
  // add the OK button to the messageBox.
  // the name of the button corresponds to function buttonOK
  // below and will be triggered when pressing the button.
  Button b1 = cp5.addButton("buttonOK",0,65,80,80,24);
  b1.moveTo(messageBox);
  b1.setColorBackground(color(40));
  b1.setColorActive(color(20));
  // by default setValue would trigger function buttonOK, 
  // therefore we disable the broadcasting before setting
  // the value and enable broadcasting again afterwards.
  // same applies to the cancel button below.
  b1.setBroadcast(false); 
  b1.setValue(1);
  b1.setBroadcast(true);
  b1.setCaptionLabel("OK");
  // centering of a label needs to be done manually 
  // with marginTop and marginLeft
  //b1.captionLabel().style().marginTop = -2;
  //b1.captionLabel().style().marginLeft = 26;
  
  // add the Cancel button to the messageBox. 
  // the name of the button corresponds to function buttonCancel
  // below and will be triggered when pressing the button.
  Button b2 = cp5.addButton("buttonCancel",0,155,80,80,24);
  b2.moveTo(messageBox);
  b2.setBroadcast(false);
  b2.setValue(0);
  b2.setBroadcast(true);
  b2.setCaptionLabel("Cancel");
  b2.setColorBackground(color(40));
  b2.setColorActive(color(20));
  //b2.captionLabel().toUpperCase(false);
  // centering of a label needs to be done manually 
  // with marginTop and marginLeft
  //b2.captionLabel().style().marginTop = -2;
  //b2.captionLabel().style().marginLeft = 16;
}

// function buttonOK will be triggered when pressing
// the OK button of the messageBox.
void buttonOK(int theValue) {
  println("a button event from button OK.");
  messageBoxString = ((Textfield)cp5.controller("inputbox")).getText();
  messageBoxResult = theValue;
  messageBox.hide();
}


// function buttonCancel will be triggered when pressing
// the Cancel button of the messageBox.
void buttonCancel(int theValue) {
  println("a button event from button Cancel.");
  messageBoxResult = theValue;
  messageBox.hide();
}

// inputbox is called whenever RETURN has been pressed 
// in textfield-controller inputbox 
void inputbox(String theString) {
  println("got something from the inputbox : "+theString);
  messageBoxString = theString;
  messageBox.hide();
}

