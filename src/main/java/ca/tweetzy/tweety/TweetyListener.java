package ca.tweetzy.tweety;

import ca.tweetzy.tweety.model.HookManager;
import ca.tweetzy.tweety.model.TweetyScoreboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServiceRegisterEvent;

/**
 * Listens for some events we handle for you automatically
 */
public final class TweetyListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event) {
		TweetyScoreboard.clearBoardsFor(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServiceRegister(ServiceRegisterEvent event) {
		HookManager.updateVaultIntegration();
	}
}
