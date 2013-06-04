import java.util.HashMap;

class Edge extends Controller<Edge> {
  int defaultCol = 0xFF1A4969;
  int hoverCol = 0xFF5FEA6D;
  int clickCol = 0xFFEA5F84;
  
  int currentCol = defaultCol;
  
  float size;
  
  // edge shouldn't be long enough to touch its nodes. (cleaner looking)
  // NOTE: using this to directly scale theta in inside() causes a bit
  //       of scaling error, since theta is an angle, not a length
  float lengthScale;
  
  Node src;
  Node dst;
  
  // boolean value is true for fwd-direction predicates
  HashMap<String, Boolean> predicates;
  
  UnProjector proj;
  
  Edge(ControlP5 cp5, String name, UnProjector unProj, Node s, Node d) {
    super(cp5, name);
    
    src = s;
    dst = d;
    
    predicates = new HashMap<String, Boolean>();

    proj = unProj;
    
    setPosition();
    
    lengthScale = 0.9;
    
    setView(new ControllerView() {
        public void display(PApplet p, Object e) {
          Edge edge = (Edge) e;
          
          edge.setPosition();
          
          // get vector between the source and the destination nodes
          PVector between = edge.dst.getPosition().get();
          between.sub(edge.src.getPosition());
          
          
          p.pushMatrix();
          p.fill(currentCol);
          // Translate(x,y,0) called already, but nodes are in 3D
          p.translate(0,0,edge.getPosition().z);
          
          
          // Rotate towards the destination node to orient the edge
          PVector  target = between.get();
          target.normalize();
          
          PVector up = new PVector(0,0,1);
          
          PVector axis = target.cross(up);
          axis.normalize();
          
          float angle = PVector.angleBetween(target, up);
          
          p.rotate(-1*angle, axis.x, axis.y, axis.z);
          
          float len = between.mag() - edge.src.size - edge.dst.size;
          p.box(edge.size, edge.size, edge.lengthScale*len); 
          p.popMatrix();
        }
      }
    );
  }
  
  public boolean inside() {
    // NOTE:  steps 1** and 2** may be switched in dependency for speed.
    
    proj.calculatePickPoints(mouseX, mouseY);
    
    // vector mouse is from cursor inward orthogonally from the screen
    PVector mouse = proj.ptEndPos.get();
    mouse.sub(proj.ptStartPos);
    
    // get edge vector between the source and the destination nodes
    PVector between = dst.getPosition().get();
    between.sub(src.getPosition());
    
    //  find shortest distance between mouse and edge vectors (lines)
    //  1 **
    
    // get normalized vector orthogonal to both.
    // (p,q,r) of this vector defines the coefficients of all planes
    // orthogonal to both vectors: px + qy + rz = C
    PVector orthogonal = mouse.cross(between);
    orthogonal.normalize();
    
    // find constant C for plane containing mouse and edge lines.
    // because |orthogonal| == 1, C represents the displacement of
    // the plane along the orthogonal vector defining it
    float mouseC = orthogonal.x * proj.ptStartPos.x +
                    orthogonal.y * proj.ptStartPos.y +
                    orthogonal.z * proj.ptStartPos.z;
                    
    float edgeC = orthogonal.x * src.getPosition().x +
                    orthogonal.y * src.getPosition().y +
                    orthogonal.z * src.getPosition().z;
                    
    // hooray!
    float dist = abs(mouseC - edgeC);
    
    // test if shortest distance between edge and mouse lines is within
    // the size constraint of the edge
    if (dist < size) {
      
      //  determine whether or not the mouse vector is within the ongular
      //  sweep of the edge vector
      //  2 **
      
      // get angular displacement of edge
      PVector toSource = src.getPosition().get();
      toSource.sub(proj.ptStartPos);
      
      PVector toDest = dst.getPosition().get();
      toDest.sub(proj.ptStartPos);
      
      float theta = lengthScale * PVector.angleBetween(toSource, toDest);
      
      // get angles from mouse to source and from mouse to destination
      float phi1 = PVector.angleBetween(toSource, mouse);
      float phi2 = PVector.angleBetween(toDest, mouse);
      
      return phi1 < theta && phi2 < theta;
    } 
    
    return false;
  }
  
  public Edge setPosition() {
    PVector sPos = src.getPosition();
    PVector dPos = dst.getPosition();
    
    float dist = PVector.dist(sPos, dPos);
    
    // get the offset in midpoint between the nodes w.r.t their radii
    float scale = 0.5 * (1 + (src.size - dst.size) / dist);
    
    // get the (scaled) midpoint between the source and the destination
    PVector midpoint = PVector.lerp(sPos, dPos, scale);
    return setPosition(midpoint);
  }
  
  public Edge setSize(final int s) {
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

