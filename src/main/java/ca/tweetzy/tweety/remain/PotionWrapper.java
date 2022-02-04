package ca.tweetzy.tweety.remain;

import ca.tweetzy.tweety.util.Common;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.WordUtils;

/**
 * A simple class holding some of the potion names
 */
@RequiredArgsConstructor
public enum PotionWrapper {

	SLOW("SLOW", "Slowness"),
	STRENGTH("INCREASE_DAMAGE"),
	JUMP_BOOST("JUMP"),
	INSTANT_HEAL("INSTANT_HEALTH"),
	REGEN("REGENERATION");

	private final String bukkitName;
	private final String minecraftName;

	PotionWrapper(String bukkitName) {
		this(bukkitName, null);
	}

	public static String getLocalizedName(String name) {
		String localizedName = name;

		for (final PotionWrapper e : values())
			if (name.toUpperCase().replace(" ", "_").equals(e.bukkitName)) {
				localizedName = e.getMinecraftName();

				break;
			}

		return WordUtils.capitalizeFully(localizedName.replace("_", " "));
	}

	protected static String getBukkitName(String name) {
		name = name.toUpperCase().replace(" ", "_");

		for (final PotionWrapper wrapper : values())
			if (wrapper.toString().equalsIgnoreCase(name) || wrapper.minecraftName != null && wrapper.minecraftName.equalsIgnoreCase(name))
				return wrapper.bukkitName;

		return name;
	}

	public String getMinecraftName() {
		return Common.getOrDefault(minecraftName, bukkitName);
	}
}