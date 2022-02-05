package ca.tweetzy.tweety.gui.events;

import ca.tweetzy.tweety.gui.Gui;
import ca.tweetzy.tweety.gui.GuiManager;
import org.bukkit.entity.Player;

public class GuiOpenEvent extends GuiEvent {
    public GuiOpenEvent(GuiManager manager, Gui gui, Player player) {
        super(manager, gui, player);
    }
}
