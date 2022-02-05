package ca.tweetzy.tweety.gui.methods;

import ca.tweetzy.tweety.gui.events.GuiDropItemEvent;

public interface Droppable {
    boolean onDrop(GuiDropItemEvent event);
}
