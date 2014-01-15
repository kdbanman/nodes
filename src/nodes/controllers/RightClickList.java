package nodes.controllers;

import java.util.concurrent.CopyOnWriteArrayList;

import controlP5.ControlP5;
import controlP5.MultiList;
import controlP5.MultiListButton;
import controlP5.Tab;

/**
 * ControlP5's MultiList doesn't seem to properly update button's location when the list location gets updated
 *
 * @see <a href="https://code.google.com/p/controlp5/source/browse/trunk/src/controlP5/MultiList.java">MultiList.java<?a>
 * @author KarimBaaba
 *
 */
public class RightClickList extends MultiList {
	//Need to keep track of the buttons that will require location update
	private final CopyOnWriteArrayList<MultiListButton> buttonsList = new CopyOnWriteArrayList<MultiListButton>();

	public RightClickList(ControlP5 theControlP5, String theName) {
		super(theControlP5, theName);
	}

	public RightClickList(ControlP5 theControlP5, Tab theParent, String theName, int theX, int theY, int theWidth, int theHeight) {
		super(theControlP5, theParent, theName, theX, theY, theWidth, theHeight);

	}

	@Override
	public MultiListButton add(String theName, int theValue)  {
		MultiListButton b = super.add(theName, theValue);
		buttonsList.add(b);
		return b;
	}

	//See original source code in class header
	public void updateLocation(float theX, float theY) {
		position.x = theX;
		position.y = theY;
		updateRect(position.x, position.y, width, _myDefaultButtonHeight);

		int yy = 0;

		for (MultiListButton b : buttonsList) {
			b.setPosition(theX, theY + yy);
			yy += _myDefaultButtonHeight;
		}
	}

	public boolean removeButton(MultiListButton b) {
		if (!buttonsList.contains(b))
			return false;

		buttonsList.remove(b);
		b.remove();

		return true;
	}

	public void clearButtons() {
		for (MultiListButton b : buttonsList) {
			removeButton(b);
		}
	}
}
