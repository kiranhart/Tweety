package ca.tweetzy.tweety.configuration;

import ca.tweetzy.tweety.TweetyPlugin;
import ca.tweetzy.tweety.remain.CompMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public class ConfigSetting {
	final Config config;
	final String key;

	public ConfigSetting(@NotNull Config config, @NotNull String key) {
		this.config = config;
		this.key = key;
	}

	public ConfigSetting(@NotNull Config config, @NotNull String key, @NotNull Object defaultValue, String... comment) {
		this.config = config;
		this.key = key;

		config.setDefault(key, defaultValue, comment);
	}

	public ConfigSetting(@NotNull Config config, @NotNull String key, @NotNull Object defaultValue, ConfigFormattingRules.CommentStyle commentStyle, String... comment) {
		this.config = config;
		this.key = key;

		config.setDefault(key, defaultValue, commentStyle, comment);
	}

	@NotNull
	public String getKey() {
		return key;
	}

	public List<Integer> getIntegerList() {
		return config.getIntegerList(key);
	}

	public List<String> getStringList() {
		return config.getStringList(key);
	}

	public boolean getBoolean() {
		return config.getBoolean(key);
	}

	public boolean getBoolean(boolean def) {
		return config.getBoolean(key, def);
	}

	public int getInt() {
		return config.getInt(key);
	}

	public int getInt(int def) {
		return config.getInt(key, def);
	}

	public long getLong() {
		return config.getLong(key);
	}

	public long getLong(long def) {
		return config.getLong(key, def);
	}

	public double getDouble() {
		return config.getDouble(key);
	}

	public double getDouble(double def) {
		return config.getDouble(key, def);
	}

	public String getString() {
		return config.getString(key);
	}

	public String getString(String def) {
		return config.getString(key, def);
	}

	public Object getObject() {
		return config.get(key);
	}

	public Object getObject(Object def) {
		return config.get(key, def);
	}

	public <T> T getObject(@NotNull Class<T> clazz) {
		return config.getObject(key, clazz);
	}

	public <T> T getObject(@NotNull Class<T> clazz, @Nullable T def) {
		return config.getObject(key, clazz, def);
	}

	public char getChar() {
		return config.getChar(key);
	}

	public char getChar(char def) {
		return config.getChar(key, def);
	}

	@NotNull
	public CompMaterial getMaterial() {
		String val = config.getString(key);
		CompMaterial mat = CompMaterial.fromString(config.getString(key));

		if (mat == null) {
			TweetyPlugin.getInstance().getLogger().log(Level.WARNING, String.format("Config value \"%s\" has an invalid material name: \"%s\"", key, val));
		}

		return mat != null ? mat : CompMaterial.STONE;
	}

	@NotNull
	public CompMaterial getMaterial(@NotNull CompMaterial def) {
		String val = config.getString(key);
		CompMaterial mat = val != null ? CompMaterial.fromString(val) : null;

		if (mat == null) {
			TweetyPlugin.getInstance().getLogger().log(Level.WARNING, String.format("Config value \"%s\" has an invalid material name: \"%s\"", key, val));
		}

		return mat != null ? mat : def;
	}
}
