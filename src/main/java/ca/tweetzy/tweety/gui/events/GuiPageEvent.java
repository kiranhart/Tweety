package ca.tweetzy.tweety.gui.events;

import ca.tweetzy.tweety.gui.Gui;
import ca.tweetzy.tweety.gui.GuiManager;

public class GuiPageEvent {
	final Gui gui;
	final GuiManager manager;
	final int lastPage;
	final int page;

	public GuiPageEvent(Gui gui, GuiManager manager, int lastPage, int page) {
		this.gui = gui;
		this.manager = manager;
		this.lastPage = lastPage;
		this.page = page;
	}
}
