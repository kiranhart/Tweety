package ca.tweetzy.tweety;

import ca.tweetzy.tweety.annotation.AutoRegister;
import ca.tweetzy.tweety.debug.Debugger;
import ca.tweetzy.tweety.event.SimpleListener;
import ca.tweetzy.tweety.exception.TweetyException;
import ca.tweetzy.tweety.metrics.Metrics;
import ca.tweetzy.tweety.model.DiscordListener;
import ca.tweetzy.tweety.model.HookManager;
import ca.tweetzy.tweety.model.SimpleHologram;
import ca.tweetzy.tweety.model.SimpleScoreboard;
import ca.tweetzy.tweety.remain.Remain;
import ca.tweetzy.tweety.tool.Tool;
import ca.tweetzy.tweety.tool.ToolsListener;
import ca.tweetzy.tweety.model.Common;
import ca.tweetzy.tweety.util.MinecraftVersion;
import ca.tweetzy.tweety.util.MinecraftVersion.V;
import ca.tweetzy.tweety.util.ReflectionUtil;
import ca.tweetzy.tweety.util.Valid;
import ca.tweetzy.tweety.visual.BlockVisualizer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a basic Java plugin using enhanced library functionality
 */
public abstract class TweetyPlugin extends JavaPlugin {

	// ----------------------------------------------------------------------------------------
	// Static
	// ----------------------------------------------------------------------------------------

	/**
	 * The instance of this plugin
	 */
	private static volatile TweetyPlugin instance;

	/**
	 * Shortcut for getDescription().getVersion()
	 *
	 * @return plugin's version
	 */
	@Getter
	private static String version;

	/**
	 * Shortcut for getName()
	 *
	 * @return plugin's name
	 */
	@Getter
	private static String named;

	/**
	 * Shortcut for getFile()
	 *
	 * @return plugin's jar file
	 */
	@Getter
	private static File source;

	/**
	 * Shortcut for getDataFolder()
	 *
	 * @return plugins' data folder in plugins/
	 */
	@Getter
	private static File data;

	/**
	 * An internal flag to indicate that the plugin is being reloaded.
	 */
	@Getter
	private static volatile boolean reloading = false;

	/**
	 * Returns the instance of {@link TweetyPlugin}.
	 * <p>
	 * It is recommended to override this in your own {@link TweetyPlugin}
	 * implementation so you will get the instance of that, directly.
	 *
	 * @return this instance
	 */
	public static TweetyPlugin getInstance() {
		if (instance == null) {
			try {
				instance = JavaPlugin.getPlugin(TweetyPlugin.class);

			} catch (final IllegalStateException ex) {
				if (Bukkit.getPluginManager().getPlugin("PlugMan") != null)
					Bukkit.getLogger().severe("Failed to get instance of the plugin, if you reloaded using PlugMan you need to do a clean restart instead.");

				throw ex;
			}

			Objects.requireNonNull(instance, "Cannot get a new instance! Have you reloaded?");
		}

		return instance;
	}

	/**
	 * Get if the instance that is used across the library has been set. Normally it
	 * is always set, except for testing.
	 *
	 * @return if the instance has been set.
	 */
	public static final boolean hasInstance() {
		return instance != null;
	}

	// ----------------------------------------------------------------------------------------
	// Instance specific
	// ----------------------------------------------------------------------------------------

	/**
	 * For your convenience, event listeners and timed tasks may be set here to stop/unregister
	 * them automatically on reload
	 */
	private final Reloadables reloadables = new Reloadables();

	/**
	 * An internal flag to indicate whether we are calling the {@link #onReloadablesStart()}
	 * block. We register things using {@link #reloadables} during this block
	 */
	private boolean startingReloadables = false;

	// ----------------------------------------------------------------------------------------
	// Main methods
	// ----------------------------------------------------------------------------------------

	static {
		// Add console filters early - no reload support
		TweetyFilter.inject();
	}

	@Override
	public final void onLoad() {

		// Set the instance
		getInstance();

		// Cache results for best performance
		version = instance.getDescription().getVersion();
		named = instance.getName();
		source = instance.getFile();
		data = instance.getDataFolder();

		// Call parent
		onPluginLoad();
	}

	@Override
	public final void onEnable() {

		// Solve reloading issues with PlugMan
		for (final StackTraceElement element : new Throwable().getStackTrace()) {
			if (element.toString().contains("com.rylinaux.plugman.util.PluginUtil.load")) {
				Common.warning("Detected PlugMan reload, which is poorly designed. "
						+ "It causes Bukkit not able to get our plugin from a static initializer."
						+ " It may or may not run. Use our own reload command or do a clean restart!");

				break;
			}
		}

		// Check if Tweety is correctly moved
		checkShading();

		if (!isEnabled())
			return;

		// Before all, check if necessary libraries and the minimum required MC version
		if (!checkLibraries0() || !checkServerVersions0()) {
			setEnabled(false);

			return;
		}

		// Load debug mode early
		Debugger.detectDebugMode();

		// Load our dependency system
		try {
			HookManager.loadDependencies();

		} catch (final Throwable throwable) {
			Common.throwError(throwable, "Error while loading " + getName() + " dependencies!");
		}

		// Return if plugin pre start indicated a fatal problem
		if (!isEnabled())
			return;

		try {
			if (!isEnabled())
				return;

			// --------------------------------------------
			// Call the main start method
			// --------------------------------------------

			// Hide plugin name before console messages
			final boolean hadLogPrefix = Common.ADD_LOG_PREFIX;
			Common.ADD_LOG_PREFIX = false;

			startingReloadables = true;

			try {
				AutoRegisterScanner.scanAndRegister();

			} catch (final Throwable t) {
				Remain.sneaky(t);

				return;
			}

			onReloadablesStart();

			startingReloadables = false;

			onPluginStart();
			// --------------------------------------------

			// Return if plugin start indicated a fatal problem
			if (!isEnabled())
				return;

			// Register our listeners
			registerEvents(new TweetyListener());

			if (areToolsEnabled())
				registerEvents(new ToolsListener());

			// Register DiscordSRV listener
			if (HookManager.isDiscordSRVLoaded()) {
				final DiscordListener.DiscordListenerImpl discord = DiscordListener.DiscordListenerImpl.getInstance();

				discord.resubscribe();
				discord.registerHook();

				reloadables.registerEvents(DiscordListener.DiscordListenerImpl.getInstance());
			}

			// Finish off by starting metrics (currently bStats)
			if (getMetricsPluginId() != -1)
				new Metrics(this, getMetricsPluginId());

			// Finally, place plugin name before console messages after plugin has (re)loaded
			Common.runLater(() -> Common.ADD_LOG_PREFIX = hadLogPrefix);

		} catch (final Throwable t) {
			displayError0(t);
		}
	}

	/**
	 * A dirty way of checking if Tweety has been shaded correctly
	 */
	private final void checkShading() {
		try {
			throw new ShadingException();
		} catch (final Throwable t) {
		}
	}

	/**
	 * The exception enabling us to check if for some reason {@link TweetyPlugin}'s instance
	 * does not match this class' instance, which is most likely caused by wrong repackaging
	 * or no repackaging at all (two plugins using Tweety must both have different packages
	 * for their own Tweety version).
	 * <p>
	 * Or, this is caused by a PlugMan, and we have no mercy for that.
	 */
	private final class ShadingException extends Throwable {
		private static final long serialVersionUID = 1L;

		public ShadingException() {
			if (!TweetyPlugin.getNamed().equals(getDescription().getName())) {
				Bukkit.getLogger().severe(Common.consoleLine());
				Bukkit.getLogger().severe("We have a class path problem in the Tweety library");
				Bukkit.getLogger().severe("preventing " + getDescription().getName() + " from loading correctly!");
				Bukkit.getLogger().severe("");
				Bukkit.getLogger().severe("This is likely caused by two plugins having the");
				Bukkit.getLogger().severe("same Tweety library paths - make sure you");
				Bukkit.getLogger().severe("relocale the package! If you are testing using");
				Bukkit.getLogger().severe("Ant, only test one plugin at the time.");
				Bukkit.getLogger().severe("");
				Bukkit.getLogger().severe("Possible cause: " + TweetyPlugin.getNamed());
				Bukkit.getLogger().severe("Tweety package: " + TweetyPlugin.class.getPackage().getName());
				Bukkit.getLogger().severe(Common.consoleLine());

				throw new TweetyException("Shading exception, see above for details.");
			}
		}
	}

	/**
	 * Check if both md5 chat and gson libraries are present,
	 * or suggest an additional plugin to fix their lack
	 *
	 * @return
	 */
	private final boolean checkLibraries0() {

		boolean md_5 = false;
		boolean gson = false;

		try {
			Class.forName("net.md_5.bungee.api.chat.BaseComponent");
			md_5 = true;
		} catch (final ClassNotFoundException ex) {
		}

		try {
			Class.forName("com.google.gson.JsonSyntaxException");
			gson = true;

		} catch (final ClassNotFoundException ex) {
		}

		if (!md_5 || !gson) {
			Bukkit.getLogger().severe(Common.consoleLine());
			Bukkit.getLogger().severe("Your Minecraft version (" + MinecraftVersion.getCurrent() + ")");
			Bukkit.getLogger().severe("lacks libraries " + getName() + " needs:");
			Bukkit.getLogger().severe("JSON Chat (by md_5) found: " + md_5);
			Bukkit.getLogger().severe("Gson (by Google) found: " + gson);
			Bukkit.getLogger().severe(" ");
			Bukkit.getLogger().severe("To fix that, please install BungeeChatAPI:");
			Bukkit.getLogger().severe(Common.consoleLine());
		}

		return true;
	}

	/**
	 * Check if the minimum required MC version is installed
	 *
	 * @return
	 */
	private final boolean checkServerVersions0() {

		// Call the static block to test compatibility early
		if (!MinecraftVersion.getCurrent().isTested())
			Common.logFramed(
					"*** WARNING ***",
					"Your Minecraft version " + MinecraftVersion.getCurrent() + " has not yet",
					"been officialy tested with the Tweety,",
					"the library that " + TweetyPlugin.getNamed() + " plugin uses.",
					"",
					"Loading the plugin at your own risk...",
					Common.consoleLine());

		// Check min version
		final V minimumVersion = getMinimumVersion();

		if (minimumVersion != null && MinecraftVersion.olderThan(minimumVersion)) {
			Common.logFramed(false,
					getName() + " requires Minecraft " + minimumVersion + " or newer to run.",
					"Please upgrade your server.");

			return false;
		}

		// Check max version
		final V maximumVersion = getMaximumVersion();

		if (maximumVersion != null && MinecraftVersion.newerThan(maximumVersion)) {
			Common.logFramed(false,
					getName() + " requires Minecraft " + maximumVersion + " or older to run.",
					"Please downgrade your server or",
					"wait for the new version.");

			return false;
		}

		return true;
	}

	/**
	 * Handles various startup problems
	 *
	 * @param throwable
	 */
	protected final void displayError0(Throwable throwable) {
		Debugger.printStackTrace(throwable);

		Common.log(
				"&4!-----------------------------------------------------!",
				" &cError loading " + getDescription().getName() + " v" + getDescription().getVersion() + ", plugin is disabled!",
				" &cRunning on " + getServer().getBukkitVersion() + " (" + MinecraftVersion.getServerVersion() + ") & Java " + System.getProperty("java.version"),
				"&4!-----------------------------------------------------!");

		if (throwable instanceof InvalidConfigurationException) {
			Common.log(" &cSeems like your config is not a valid YAML.");
			Common.log(" &cUse online services like");
			Common.log(" &chttp://yaml-online-parser.appspot.com/");
			Common.log(" &cto check for syntax errors!");

		} else if (throwable instanceof UnsupportedOperationException || throwable.getCause() != null && throwable.getCause() instanceof UnsupportedOperationException)
			if (getServer().getBukkitVersion().startsWith("1.2.5"))
				Common.log(" &cSorry but Minecraft 1.2.5 is no longer supported!");
			else {
				Common.log(" &cUnable to setup reflection!");
				Common.log(" &cYour server is either too old or");
				Common.log(" &cthe plugin broke on the new version :(");
			}

		{
			while (throwable.getCause() != null)
				throwable = throwable.getCause();

			String error = "Unable to get the error message, search above.";
			if (throwable.getMessage() != null && !throwable.getMessage().isEmpty() && !throwable.getMessage().equals("null"))
				error = throwable.getMessage();

			Common.log(" &cError: " + error);
		}
		Common.log("&4!-----------------------------------------------------!");

		getPluginLoader().disablePlugin(this);
	}

	// ----------------------------------------------------------------------------------------
	// Shutdown
	// ----------------------------------------------------------------------------------------

	@Override
	public final void onDisable() {

		try {
			onPluginStop();
		} catch (final Throwable t) {
			Common.log("&cPlugin might not shut down property. Got " + t.getClass().getSimpleName() + ": " + t.getMessage());
		}

		unregisterReloadables();

		try {
			for (final Player online : Remain.getOnlinePlayers())
				SimpleScoreboard.clearBoardsFor(online);

		} catch (final Throwable t) {
			Common.log("Error clearing scoreboards for players..");

			t.printStackTrace();
		}

		try {
			for (final Player online : Remain.getOnlinePlayers()) {
				// TODO CLOSE ALL INVENTORIES
			}
		} catch (final Throwable t) {
			Common.log("Error closing menu inventories for players..");

			t.printStackTrace();
		}

		Objects.requireNonNull(instance, "Instance of " + getName() + " already nulled!");
		instance = null;
	}

	// ----------------------------------------------------------------------------------------
	// Delegate methods
	// ----------------------------------------------------------------------------------------

	/**
	 * Called before the plugin is started, see {@link JavaPlugin#onLoad()}
	 */
	protected void onPluginLoad() {
	}

	/**
	 * The main loading method, called when we are ready to load
	 */
	protected abstract void onPluginStart();

	/**
	 * The main method called when we are about to shut down
	 */
	protected void onPluginStop() {
	}

	/**
	 * Invoked before settings were reloaded.
	 */
	protected void onPluginPreReload() {
	}

	/**
	 * Invoked after settings were reloaded.
	 */
	protected void onPluginReload() {
	}

	/**
	 * Register your commands, events, tasks and files here.
	 * <p>
	 * This is invoked when you start the plugin, call /reload, or the {@link #reload()}
	 * method.
	 */
	protected void onReloadablesStart() {
	}

	// ----------------------------------------------------------------------------------------
	// Reload
	// ----------------------------------------------------------------------------------------

	/**
	 * Attempts to reload the plugin
	 */
	public final void reload() {
		final boolean hadLogPrefix = Common.ADD_LOG_PREFIX;
		Common.ADD_LOG_PREFIX = false;

		Common.log(Common.consoleLineSmooth());
		Common.log(" ");
		Common.log("Reloading plugin " + this.getName() + " v" + getVersion());
		Common.log(" ");

		reloading = true;

		try {
			Debugger.detectDebugMode();

			unregisterReloadables();

			// Load our dependency system
			try {
				HookManager.loadDependencies();

			} catch (final Throwable throwable) {
				Common.throwError(throwable, "Error while loading " + getName() + " dependencies!");
			}

			onPluginPreReload();
			reloadables.reload();

			SimpleHologram.onReload();

			onPluginReload();

			// Something went wrong in the reload pipeline
			if (!isEnabled())
				return;

			startingReloadables = true;

			// Register classes
			AutoRegisterScanner.scanAndRegister();

			onReloadablesStart();

			startingReloadables = false;

			if (HookManager.isDiscordSRVLoaded()) {
				DiscordListener.DiscordListenerImpl.getInstance().resubscribe();

				reloadables.registerEvents(DiscordListener.DiscordListenerImpl.getInstance());
			}

			Common.log(Common.consoleLineSmooth());

		} catch (final Throwable t) {
			Common.throwError(t, "Error reloading " + getName() + " " + getVersion());

		} finally {
			Common.ADD_LOG_PREFIX = hadLogPrefix;

			reloading = false;
		}
	}

	private final void unregisterReloadables() {
		BlockVisualizer.stopAll();

		if (HookManager.isDiscordSRVLoaded())
			DiscordListener.clearRegisteredListeners();

		try {
			HookManager.unloadDependencies(this);
		} catch (final NoClassDefFoundError ex) {
		}

		getServer().getMessenger().unregisterIncomingPluginChannel(this);
		getServer().getMessenger().unregisterOutgoingPluginChannel(this);

		getServer().getScheduler().cancelTasks(this);
	}

	// ----------------------------------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------------------------------

	/**
	 * Convenience method for quickly registering events in all classes in your plugin that
	 * extend the given class.
	 * <p>
	 * NB: You must have a no arguments constructor otherwise it will not be registered
	 * <p>
	 * TIP: Set your Debug key in your settings.yml to ["auto-register"] to see what is registered.
	 *
	 * @param extendingClass
	 */
	protected final <T extends Listener> void registerAllEvents(final Class<T> extendingClass) {

		Valid.checkBoolean(!extendingClass.equals(Listener.class), "registerAllEvents does not support Listener.class due to conflicts, create your own middle class instead");
		Valid.checkBoolean(!extendingClass.equals(SimpleListener.class), "registerAllEvents does not support SimpleListener.class due to conflicts, create your own middle class instead");

		classLookup:
		for (final Class<? extends T> pluginClass : ReflectionUtil.getClasses(instance, extendingClass)) {

			// AutoRegister means the class is already being registered
			if (pluginClass.isAnnotationPresent(AutoRegister.class))
				continue;

			for (final Constructor<?> con : pluginClass.getConstructors()) {
				if (con.getParameterCount() == 0) {
					final T instance = (T) ReflectionUtil.instantiate(con);

					Debugger.debug("auto-register", "Auto-registering events in " + pluginClass);
					registerEvents(instance);

					continue classLookup;
				}
			}

			Debugger.debug("auto-register", "Skipping auto-registering events in " + pluginClass + " because it lacks at least one no arguments constructor");
		}
	}

	/**
	 * Convenience method for quickly registering events for this plugin
	 *
	 * @param listener
	 */
	protected final void registerEvents(final Listener listener) {
		if (startingReloadables)
			reloadables.registerEvents(listener);
		else
			getServer().getPluginManager().registerEvents(listener, this);

		if (listener instanceof DiscordListener)
			((DiscordListener) listener).register();
	}

	/**
	 * Convenience method for quickly registering a single event
	 *
	 * @param listener
	 */
	protected final void registerEvents(final SimpleListener<? extends Event> listener) {
		if (startingReloadables)
			reloadables.registerEvents(listener);

		else
			listener.register();
	}

	// ----------------------------------------------------------------------------------------
	// Additional features
	// ----------------------------------------------------------------------------------------

	/**
	 * The the minimum MC version to run
	 * <p>
	 * We will prevent loading it automatically if the server's version is
	 * below the given one
	 *
	 * @return
	 */
	public MinecraftVersion.V getMinimumVersion() {
		return null;
	}

	/**
	 * The maximum MC version for this plugin to load
	 * <p>
	 * We will prevent loading it automatically if the server's version is
	 * above the given one
	 *
	 * @return
	 */
	public MinecraftVersion.V getMaximumVersion() {
		return null;
	}

	/**
	 * @return -1 by default, or the founded year
	 */
	public int getFoundedYear() {
		return -1;
	}

	/**
	 * If you want to use bStats.org metrics system,
	 * simply return the plugin ID (https://bstats.org/what-is-my-plugin-id)
	 * here and we will automatically start tracking it.
	 * <p>
	 * Defaults to -1 which means disabled
	 *
	 * @return
	 */
	public int getMetricsPluginId() {
		return -1;
	}

	/**
	 * Tweety automatically can filter console commands for you, including
	 * messages from other plugins or the server itself, preventing unnecessary console spam.
	 * <p>
	 * You can return a list of messages that will be matched using "startsWith OR contains" method
	 * and will be filtered.
	 *
	 * @return
	 */
	public Set<String> getConsoleFilter() {
		return new HashSet<>();
	}

	/**
	 * When processing regular expressions, limit executing to the specified time.
	 * This prevents server freeze/crash on malformed regex (loops).
	 *
	 * @return time limit in milliseconds for processing regular expression
	 */
	public int getRegexTimeout() {
		throw new TweetyException("Must override getRegexTimeout()");
	}

	/**
	 * Strip colors from checked message while checking it against a regex?
	 *
	 * @return
	 */
	public boolean regexStripColors() {
		return true;
	}

	/**
	 * Should Pattern.CASE_INSENSITIVE be applied when compiling regular expressions in {@link Common#compilePattern(String)}?
	 * <p>
	 * May impose a slight performance penalty but increases catches.
	 *
	 * @return
	 */
	public boolean regexCaseInsensitive() {
		return true;
	}

	/**
	 * Should Pattern.UNICODE_CASE be applied when compiling regular expressions in {@link Common#compilePattern(String)}?
	 * <p>
	 * May impose a slight performance penalty but useful for non-English servers.
	 *
	 * @return
	 */
	public boolean regexUnicode() {
		return true;
	}

	/**
	 * Should we remove diacritical marks before matching regex?
	 * Defaults to true
	 *
	 * @return
	 */
	public boolean regexStripAccents() {
		return true;
	}

	/**
	 * Should we replace accents with their non accented friends when
	 * checking two strings for similarity in ChatUtil?
	 *
	 * @return defaults to true
	 */
	public boolean similarityStripAccents() {
		return true;
	}

	/**
	 * Should we listen for {@link Tool} in this plugin and
	 * handle clicking events automatically? Disable to increase performance
	 * if you do not want to use our tool system. Enabled by default.
	 *
	 * @return
	 */
	public boolean areToolsEnabled() {
		return true;
	}
}
