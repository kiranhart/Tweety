package ca.tweetzy.tweety.gui.events;

import ca.tweetzy.tweety.gui.Gui;
import ca.tweetzy.tweety.gui.GuiManager;
import org.bukkit.entity.Player;

public class GuiCloseEvent extends GuiEvent {
	public GuiCloseEvent(GuiManager manager, Gui gui, Player player) {
		super(manager, gui, player);
	}
}
