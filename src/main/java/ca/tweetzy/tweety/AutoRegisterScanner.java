package ca.tweetzy.tweety;

import ca.tweetzy.tweety.annotation.AutoRegister;
import ca.tweetzy.tweety.debug.Debugger;
import ca.tweetzy.tweety.exception.TweetyException;
import ca.tweetzy.tweety.model.Common;
import ca.tweetzy.tweety.model.HookManager;
import ca.tweetzy.tweety.model.Tuple;
import ca.tweetzy.tweety.model.discord.DiscordListener;
import ca.tweetzy.tweety.util.ReflectionUtil;
import ca.tweetzy.tweety.util.Valid;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

;

/**
 * Utilizes \@AutoRegister annotation to add auto registration support for commands, events and much more.
 */
final class AutoRegisterScanner {


	/**
	 * Scans your plugin for implements {@link Listener}
	 * and has "instance" method to be a singleton, your events are registered there automatically
	 * <p>
	 * If not, we only call the instance constructor in case there is any underlying registration going on
	 */
	public static void scanAndRegister() {
		// Ignore anonymous inner classes
		final Pattern anonymousClassPattern = Pattern.compile("\\w+\\$[0-9]$");

		try (final JarFile file = new JarFile(TweetyPlugin.getSource())) {

			for (final Enumeration<JarEntry> entry = file.entries(); entry.hasMoreElements(); ) {
				final JarEntry jar = entry.nextElement();
				final String name = jar.getName().replace("/", ".");

				// Ignore files such as settings.yml
				if (!name.endsWith(".class"))
					continue;

				try {
					final String className = name.substring(0, name.length() - 6);

					Class<?> clazz = null;

					// Look up the Java class, silently ignore if failing
					try {
						clazz = TweetyPlugin.class.getClassLoader().loadClass(className);

					} catch (final NoClassDefFoundError | ClassNotFoundException | IncompatibleClassChangeError error) {
						continue;
					}

					// Ignore abstract or anonymous classes
					if (!Modifier.isAbstract(clazz.getModifiers()) && !anonymousClassPattern.matcher(className).find()) {

						// Prevent beginner programmer mistake of forgetting to implement listener
						try {
							for (final Method method : clazz.getMethods())
								if (method.isAnnotationPresent(EventHandler.class))
									Valid.checkBoolean(Listener.class.isAssignableFrom(clazz), "Detected @EventHandler in " + clazz + ", make this class 'implements Listener' before using events there");

						} catch (final Error err) {
							// Ignore, likely caused by missing plugins
						}

						// Auto register classes
						final AutoRegister autoRegister = clazz.getAnnotation(AutoRegister.class);

						// Require our annotation to be used
						if (autoRegister != null) {
							Valid.checkBoolean(Modifier.isFinal(clazz.getModifiers()), "Please make " + clazz + " final for it to be registered automatically (or via @AutoRegister)");

							try {
								scan(clazz, autoRegister == null || !autoRegister.hideIncompatibilityWarnings());

							} catch (final NoClassDefFoundError | NoSuchFieldError ex) {
								Bukkit.getLogger().warning("Failed to auto register " + clazz + " due to it requesting missing fields/classes: " + ex.getMessage());

								// Ignore if no field is present

							} catch (final Throwable t) {
								final String error = Common.getOrEmpty(t.getMessage());

								if (t instanceof NoClassDefFoundError && error.contains("org/bukkit/entity")) {
									Bukkit.getLogger().warning("**** WARNING ****");

									if (error.contains("DragonFireball"))
										Bukkit.getLogger().warning("Your Minecraft version does not have DragonFireball class, we suggest replacing it with a Fireball instead in: " + clazz);
									else
										Bukkit.getLogger().warning("Your Minecraft version does not have " + error + " class you call in: " + clazz);
								} else
									Common.error(t, "Failed to auto register class " + clazz);
							}
						}
					}

				} catch (final Throwable t) {

					// Ignore exception in other class we loaded
					if (t instanceof VerifyError)
						continue;

					Common.error(t, "Failed to scan class '" + name + "' using Tweety!");
				}
			}

		} catch (final Throwable t) {
			Common.error(t, "Failed to scan classes to register - your classes using @AutoRegister will not function!");
		}
	}

	private static void scan(Class<?> clazz, boolean printWarnings) {
		if (DiscordListener.class.isAssignableFrom(clazz) && !HookManager.isDiscordSRVLoaded()) {
			if (printWarnings) {
				Bukkit.getLogger().warning("**** WARNING ****");
				Bukkit.getLogger().warning("DiscordListener requires DiscordSRV. The following class will not be registered: " + clazz.getName()
						+ ". To hide this message, put @AutoRegister(hideIncompatibilityWarnings=true) over the class.");
			}

			return;
		}

		final TweetyPlugin plugin = TweetyPlugin.getInstance();
		final Tuple<RegisterMode, Object> tuple = findInstance(clazz);

		final RegisterMode mode = tuple.getKey();
		final Object instance = tuple.getValue();

		boolean eventsRegistered = false;

		if (SimpleListener.class.isAssignableFrom(clazz)) {
			enforceModeFor(clazz, mode, RegisterMode.SINGLETON);

			plugin.registerEvents((SimpleListener<?>) instance);
			eventsRegistered = true;
		} else if (DiscordListener.class.isAssignableFrom(clazz)) {

			// Automatically registered in its constructor
			enforceModeFor(clazz, mode, RegisterMode.SINGLETON);
		} else if (instance instanceof Listener) {
			// Pass-through to register events later
		} else
			throw new TweetyException("@AutoRegister cannot be used on " + clazz);

		// Register events if needed
		if (!eventsRegistered && instance instanceof Listener)
			plugin.registerEvents((Listener) instance);

		Debugger.debug("auto-register", "Automatically registered " + clazz);
	}

	private static Tuple<RegisterMode, Object> findInstance(Class<?> clazz) {
		final Constructor<?>[] constructors = clazz.getDeclaredConstructors();

		Object instance = null;
		RegisterMode mode = null;

		// Strictly limit the class to one no args constructor
		if (constructors.length == 1) {
			final Constructor<?> constructor = constructors[0];

			if (constructor.getParameterCount() == 0) {
				final int modifiers = constructor.getModifiers();

				// Case 1: Public constructor
				if (Modifier.isPublic(modifiers)) {
					instance = ReflectionUtil.instantiate(constructor);
					mode = RegisterMode.NO_ARGS_CONSTRUCTOR;
				}

				// Case 2: Singleton
				else if (Modifier.isPrivate(modifiers)) {
					Field instanceField = null;

					for (final Field field : clazz.getDeclaredFields()) {
						final int fieldMods = field.getModifiers();

						if (Modifier.isPrivate(fieldMods) && Modifier.isStatic(fieldMods) && (Modifier.isFinal(fieldMods) || Modifier.isVolatile(fieldMods)))
							instanceField = field;
					}

					if (instanceField != null) {
						instance = ReflectionUtil.getFieldContent(instanceField, (Object) null);
						mode = RegisterMode.SINGLETON;
					}
				}
			}

		}

		Valid.checkNotNull(instance, "Your class " + clazz + " using @AutoRegister must EITHER have 1) one public no arguments constructor,"
				+ " OR 2) one private no arguments constructor plus a 'private static final " + clazz.getSimpleName() + " instance' instance field.");

		return new Tuple<>(mode, instance);
	}

	private static void enforceModeFor(Class<?> clazz, RegisterMode actual, RegisterMode required) {
		Valid.checkBoolean(required == actual, clazz + " using @AutoRegister must have " + (required == RegisterMode.NO_ARGS_CONSTRUCTOR ? "a single public no args constructor"
				: "one private no args constructor plus a 'private static final " + clazz.getSimpleName() + "instance' field to be a singleton'"));
	}

	enum RegisterMode {
		NO_ARGS_CONSTRUCTOR,
		SINGLETON
	}
}
