package ca.tweetzy.tweety.bungee;

import ca.tweetzy.tweety.Common;
import ca.tweetzy.tweety.Valid;
import ca.tweetzy.tweety.bungee.message.IncomingMessage;
import ca.tweetzy.tweety.debug.Debugger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 * Represents a BungeeCord listener using a bungee channel
 * on which you can listen to receiving messages
 * <p>
 * This class is also a Listener for Bukkit events for your convenience
 */
@Getter
public abstract class BungeeListener implements Listener, PluginMessageListener {

	/**
	 * The channel
	 */
	private final String channel;

	/**
	 * The actions
	 */
	private final ca.tweetzy.tweety.bungee.BungeeAction[] actions;

	/**
	 * Create a new bungee suite with the given params
	 *
	 * @param channel
	 * @param listener
	 * @param actions
	 */
	protected BungeeListener(String channel, Class<? extends ca.tweetzy.tweety.bungee.BungeeAction> actionEnum) {
		Valid.checkNotNull(channel, "Channel cannot be null!");

		this.channel = channel;

		final ca.tweetzy.tweety.bungee.BungeeAction[] actions = toActions(actionEnum);
		Valid.checkNotNull(actions, "Actions cannot be null!");

		this.actions = actions;
	}

	private static ca.tweetzy.tweety.bungee.BungeeAction[] toActions(Class<? extends ca.tweetzy.tweety.bungee.BungeeAction> actionEnum) {
		Valid.checkNotNull(actionEnum);
		Valid.checkBoolean(actionEnum.isEnum(), "Enum expected, given: " + actionEnum);

		try {
			return (ca.tweetzy.tweety.bungee.BungeeAction[]) actionEnum.getMethod("values").invoke(null);

		} catch (final ReflectiveOperationException ex) {
			Common.log("Unable to get values() of " + actionEnum + ", ensure it is an enum!");
			ex.printStackTrace();

			return null;
		}
	}

	/**
	 * Handle the received message automatically if it matches our tag
	 */
	@Override
	public final void onPluginMessageReceived(String channelName, Player player, byte[] data) {

		// Cauldron/Thermos is unsupported for bungee
		if (Bukkit.getName().contains("Cauldron"))
			return;

		final IncomingMessage message = new IncomingMessage(data);

		Debugger.debug("bungee", "Channel " + message.getChannel() + " received " + message.getAction() + " message from " + message.getServerName() + " server.");
		onMessageReceived(player, message);
	}

	/**
	 * Called automatically when you receive a plugin message from Bungeecord,
	 * see https://spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel
	 *
	 * @param player
	 * @param message
	 */
	public abstract void onMessageReceived(Player player, IncomingMessage message);
}