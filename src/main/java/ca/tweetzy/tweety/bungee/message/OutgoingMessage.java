package ca.tweetzy.tweety.bungee.message;

import ca.tweetzy.tweety.Valid;
import ca.tweetzy.tweety.bungee.BungeeAction;
import ca.tweetzy.tweety.bungee.BungeeListener;
import ca.tweetzy.tweety.debug.Debugger;
import ca.tweetzy.tweety.exception.TweetyException;
import ca.tweetzy.tweety.plugin.SimplePlugin;
import ca.tweetzy.tweety.remain.Remain;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * NB: This uses the standardized Tweety model where the first
 * String is the server name and the second String is the
 * {@link BungeeAction} by its name *written automatically*.
 */
public final class OutgoingMessage extends Message {

	/**
	 * The pending queue to write the message
	 */
	private final List<Object> queue = new ArrayList<>();

	/**
	 * Create a new outgoing message, see header of this class
	 *
	 * @param senderUid
	 * @param action
	 */
	public OutgoingMessage(UUID senderUid, BungeeAction action) {
		this(SimplePlugin.getInstance().getBungeeCord(), senderUid, action);
	}

	/**
	 * Create a new outgoing message, see header of this class
	 *
	 * @param listener
	 * @param senderUid
	 * @param action
	 */
	public OutgoingMessage(BungeeListener listener, UUID senderUid, BungeeAction action) {
		super(listener);

		setSenderUid(senderUid.toString());
		setServerName(Remain.getServerName());
		setAction(action);

		// -----------------------------------------------------------------
		// We are automatically writing the first two strings assuming the
		// first is the senders server name and the second is the action
		// -----------------------------------------------------------------

		queue.add(senderUid);
		queue.add(getServerName());
		queue.add(getAction());
	}

	/**
	 * Write the given strings into the message
	 *
	 * @param messages
	 */
	public void writeString(String... messages) {
		for (final String message : messages)
			write(message, String.class);
	}

	/**
	 * Write a boolean into the message
	 *
	 * @param bool
	 */
	public void writeBoolean(boolean bool) {
		write(bool, Boolean.class);
	}

	/**
	 * Write a byte into the message
	 *
	 * @param number
	 */
	public void writeByte(byte number) {
		write(number, Byte.class);
	}

	/**
	 * Write a double into the message
	 *
	 * @param number
	 */
	public void writeDouble(double number) {
		write(number, Double.class);
	}

	/**
	 * Write a float into the message
	 *
	 * @param number
	 */
	public void writeFloat(float number) {
		write(number, Float.class);
	}

	/**
	 * Write an integer into the message
	 *
	 * @param number
	 */
	public void writeInt(int number) {
		write(number, Integer.class);
	}

	/**
	 * Write a float into the message
	 *
	 * @param number
	 */
	public void writeLong(long number) {
		write(number, Long.class);
	}

	/**
	 * Write a short into the message
	 *
	 * @param number
	 */
	public void writeShort(short number) {
		write(number, Short.class);
	}

	/**
	 * Write an uuid into the message
	 *
	 * @param uuid
	 */
	public void writeUUID(UUID uuid) {
		write(uuid, UUID.class);
	}

	/**
	 * Write an object of the given type into the message
	 * <p>
	 * We move the head and ensure writing safety in accordance
	 * to the {@link BungeeAction#getContent()} length and
	 * data type at the given position
	 *
	 * @param object
	 * @param typeOf
	 */
	private void write(Object object, Class<?> typeOf) {
		Valid.checkNotNull(object, "Added object must not be null!");

		moveHead(typeOf);
		queue.add(object);
	}

	/**
	 * Send this message with the current data for the given player!
	 *
	 * @param player
	 */
	public void send(Player player) {
		player.sendPluginMessage(SimplePlugin.getInstance(), getChannel(), compileData());

		Debugger.debug("bungee", "Sending data on " + getChannel() + " channel from " + getAction() + " as " + player.getName() + " player to BungeeCord.");
	}

	/**
	 * Delegate write methods for the byte array data output
	 * based on the queue
	 *
	 * @return
	 */
	private byte[] compileData() {
		final ByteArrayDataOutput out = ByteStreams.newDataOutput();

		for (final Object object : queue)
			if (object instanceof String)
				out.writeUTF((String) object);

			else if (object instanceof Boolean)
				out.writeBoolean((Boolean) object);

			else if (object instanceof Byte)
				out.writeByte((Byte) object);

			else if (object instanceof Double)
				out.writeDouble((Double) object);

			else if (object instanceof Float)
				out.writeFloat((Float) object);

			else if (object instanceof Integer)
				out.writeInt((Integer) object);

			else if (object instanceof Long)
				out.writeLong((Long) object);

			else if (object instanceof Short)
				out.writeShort((Short) object);

			else if (object instanceof byte[])
				out.write((byte[]) object);

			else
				throw new TweetyException("Unsupported write of " + object.getClass().getSimpleName() + " to channel " + getChannel() + " with action " + getAction().toString());

		return out.toByteArray();
	}
}