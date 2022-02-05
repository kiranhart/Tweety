package ca.tweetzy.tweety.constants;

import ca.tweetzy.tweety.TweetyPlugin;

import java.util.UUID;

;

/**
 * Stores constants for this plugin
 */
public final class TweetyConstants {

	/**
	 * Represents a UUID consisting of 0's only
	 */
	public static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	public static final class NBT {

		/**
		 * Represents our NBT tag used in {@link NBTUtil}
		 */
		public static final String TAG = TweetyPlugin.getNamed() + "_NbtTag";

	}
}
