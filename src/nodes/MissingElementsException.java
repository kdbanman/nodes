package nodes;

import java.util.ArrayList;

/**
 *
 * @author kdbanman
 */
public class MissingElementsException extends Exception {
        private ArrayList<String> missingNodes;
        private ArrayList<String> missingEdges;
        
        public MissingElementsException() {
            super("WARNING: Visually described GraphElements do not exist within Graph.");
            
            missingNodes = new ArrayList<>();
            missingEdges = new ArrayList<>();
        }
        
        public void addMissingNode(String id) {
            missingNodes.add(id);
        }
        
        public void addMissingEdge(String id) {
            missingEdges.add(id);
        }
        
        public boolean isEmpty() {
            return missingNodes.isEmpty() && missingEdges.isEmpty();
        }
        
        public String listMissingElements() {
            String list = "";
            
            for (String node : missingNodes) {
                list += "Misisng Node: " + node + "\n";
            }
            
            for (String edge : missingEdges) {
                list += "Missing Edge: " + edge + "\n";
            }
            
            return list;
        }
    }
