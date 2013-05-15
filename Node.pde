class Node extends Controller<Node> {
  
  int defaultCol = 0xFF5FAAEA;
  int hoverCol = 0xFF5FEA6D;
  int clickCol = 0xFFEA5F84;
  
  int currentCol = defaultCol;
  
  float size;
  
  
    
  // name of controller is the URI or "literal_XXX"
  // UnProjector is for 3D extension of inside()
  Node(ControlP5 cp5, String name, UnProjector proj) {
    super(cp5, name);
    
    setView(new ControllerView() {
        public void display(PApplet p, Object n) {
          Node node = (Node) n;
          
          PVector pos = node.getPosition();
          
          p.pushMatrix();
          p.translate(pos.x, pos.y, pos.z);
          p.fill(currentCol);
          p.sphere(node.size);
          p.popMatrix();
        }
      }
    );
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
