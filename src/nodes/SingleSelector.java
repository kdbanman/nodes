/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nodes;

import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlP5;

/**
 *
 * only meant to be attached to GraphElement controllers
 */
public class SingleSelector implements CallbackListener {
    Selection selection;
    
    public SingleSelector(Selection s) {
        selection = s;
    }
    public void controlEvent(CallbackEvent event) {
        if(event.getAction() == ControlP5.ACTION_RELEASED
                && event.getController() instanceof GraphElement) {
            selection.invert((GraphElement) event.getController());
        }
    }
}
