package ca.tweetzy.tweety.gui.methods;

import ca.tweetzy.tweety.gui.events.GuiCloseEvent;

public interface Closable {
    void onClose(GuiCloseEvent event);
}
