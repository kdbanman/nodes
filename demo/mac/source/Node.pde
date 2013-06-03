class Node extends Controller<Node> {
  
  int defaultCol = 0xFF1A4969;
  int hoverCol = 0xFF5FEA6D;
  int clickCol = 0xFFEA5F84;
  
  int currentCol = defaultCol;
  
  float size;
  
  UnProjector proj;
  
    
  // name of controller is the URI or literal value
  // UnProjector is for 3D extension of inside()
  Node(ControlP5 cp5, String name, UnProjector unProj) {
    super(cp5, name);

    proj = unProj;
    
    setView(new ControllerView() {
        public void display(PApplet p, Object n) {
          Node node = (Node) n;
          
          
          p.pushMatrix();
          p.fill(currentCol);
          // Translate(x,y,0) called already, but nodes are in 3D
          p.translate(0,0,node.getPosition().z);
          p.sphere(node.size); 
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
  
  void mouseReleasedOutside() {
    currentCol = defaultCol;
  }

}
