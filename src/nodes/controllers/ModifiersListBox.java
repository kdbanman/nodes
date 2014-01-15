package nodes.controllers;

import java.util.concurrent.CopyOnWriteArrayList;

import controlP5.Button;
import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.ListBox;
import controlP5.ListBoxItem;

/**
 * Modified version of the ListBox controller that ensures concurrency control using CopyOnWriteArrayLists
 *
 * @author KarimBaaba
 *
 */
public class ModifiersListBox extends ListBox {

	private final CopyOnWriteArrayList<ListBoxItem> mItems = new CopyOnWriteArrayList<ListBoxItem>();
	private final CopyOnWriteArrayList<Button> mButtons = new CopyOnWriteArrayList<Button>();

	public ModifiersListBox(ControlP5 theControlP5, String theName) {
		super(theControlP5, theName);
		items = mItems;
		buttons = mButtons;
	}

	public ModifiersListBox(ControlP5 theControlP5, ControllerGroup<?> theGroup, String theName, int theX, int theY, int theW, int theH) {
		super(theControlP5, theGroup, theName, theX, theY, theW, theH);
		items = mItems;
		buttons = mButtons;
	}
}
