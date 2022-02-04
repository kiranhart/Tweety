package ca.tweetzy.tweety.constants;

import ca.tweetzy.tweety.TweetyPlugin;
import ca.tweetzy.tweety.util.Common;
import ca.tweetzy.tweety.util.TimeUtil;

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

	public static final class File {

		/**
		 * The name of our settings file
		 */
		public static final String SETTINGS = "settings.yml";

		/**
		 * The error.log file created automatically to log errors to
		 */
		public static final String ERRORS = "error.log";

		/**
		 * The debug.log file to log debug messages to
		 */
		public static final String DEBUG = "debug.log";

		/**
		 * The data.yml file (uses YAML) for saving various data
		 */
		public static final String DATA = "data.yml";
	}

	public static final class Header {

		/**
		 * The header for data.yml file
		 * <p>
		 */
		public static final String[] DATA_FILE = new String[]{
				"",
				"This file stores various data you create via the plugin.",
				""
		};

		/**
		 * The header that is put into the file that has been automatically
		 * updated and comments were lost.
		 * <p>
		 * Use {@link YamlConfig#setHeader()} to override this.
		 */
		public static final String[] UPDATED_FILE = new String[]{
				Common.configLine(),
				"",
				" Your file has been automatically updated at " + TimeUtil.getFormattedDate(),
				" to " + TweetyPlugin.getNamed() + " " + TweetyPlugin.getVersion(),
				"",
				" Unfortunatelly, due to how Bukkit saves all .yml files, it was not possible",
				" preserve the documentation comments in your file. We apologize.",
				"",
				" If you'd like to view the default file, you can either:",
				" a) Open the " + TweetyPlugin.getSource().getName() + " with a WinRar or similar",
				"",
				Common.configLine(),
				""
		};
	}

	public static final class NBT {

		/**
		 * Represents our NBT tag used in {@link NBTUtil}
		 */
		public static final String TAG = TweetyPlugin.getNamed() + "_NbtTag";

	}
}
