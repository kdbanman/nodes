class Node extends Controller<Node> {
  
  int defaultCol = 0xFF5FAAEA;
  int hoverCol = 0xFF5FEA6D;
  int clickCol = 0xFFEA5F84;
  
  int currentCol = defaultCol;
  
  float size;
  
  UnProjector proj;
  
    
  // name of controller is the URI or "literal_XXX"
  // UnProjector is for 3D extension of inside()
  Node(ControlP5 cp5, String name, UnProjector unProj) {
    super(cp5, name);

    proj = unProj;
    
    setView(new ControllerView() {
        public void display(PApplet p, Object n) {
          Node node = (Node) n;
          
          
          
          p.pushMatrix();
          p.fill(currentCol);
          p.sphere(node.size); // Translate() called already
          p.popMatrix();
        }
      }
    );
  }

  public boolean inside() {

    proj.calculatePickPoints(mouseX, mouseY);
    
    // vector mouse is from cursor inward orthogonally from the screen
    PVector mouse = proj.ptEndPos.get();
    mouse.sub(proj.ptStartPos);

    // vector obj is from the cursor to the position of the node
    PVector obj = getPosition().get();;
    obj.sub(proj.ptStartPos);

    // theta is the angle between the mouse vector and the object vector
    float theta = PVector.angleBetween(mouse, obj);

    // phi is the angular displacement of the radius of the node
    float phi = atan(size/obj.mag());
    
    
    if (keyPressed) {
      //print(obj);
      //print(theta + " " + phi + "\n");
      PVector t = getPosition().get();
      pushMatrix();
      printMatrix();
      translate(t.x,t.y,t.z);
      fill(0xFFFFFFFF);
      box(30);
      popMatrix();
    }

    // the cursor is inside the node if theta is less than phi
    return theta < phi;
  }
  
  public Node setPosition(final float x, final float y, final float z) {
    return setPosition(new PVector(x, y, z));
  }
  
  public Node setSize(final int s) {
    size = s;
    return setSize(s, s);
  }
  
  void onClick() {
    currentCol = clickCol;
  }
  
  void onEnter() {
    currentCol = hoverCol;
  }
  
  void onLeave() {
    currentCol = defaultCol;
  }
  

}
