package ca.tweetzy.tweety.cross;

import ca.tweetzy.tweety.util.ItemUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.WordUtils;

/**
 * A simple class holding some of the enchantments names
 */
@RequiredArgsConstructor
public enum EnchantmentWrapper {
	PROTECTION("PROTECTION_ENVIRONMENTAL"),
	FIRE_PROTECTION("PROTECTION_FIRE"),
	FEATHER_FALLING("PROTECTION_FALL"),
	BLAST_PROTECTION("PROTECTION_EXPLOSIONS"),
	PROJECTILE_PROTECTION("PROTECTION_PROJECTILE"),
	RESPIRATION("OXYGEN"),
	AQUA_AFFINITY("WATER_WORKER"),
	THORN("THORNS"),
	CURSE_OF_VANISHING("VANISHING_CURSE"),
	CURSE_OF_BINDING("BINDING_CURSE"),
	SHARPNESS("DAMAGE_ALL"),
	SMITE("DAMAGE_UNDEAD"),
	BANE_OF_ARTHROPODS("DAMAGE_ARTHROPODS"),
	LOOTING("LOOT_BONUS_MOBS"),
	SWEEPING_EDGE("SWEEPING"),
	EFFICIENCY("DIG_SPEED"),
	UNBREAKING("DURABILITY"),
	FORTUNE("LOOT_BONUS_BLOCKS"),
	POWER("ARROW_DAMAGE"),
	PUNCH("ARROW_KNOCKBACK"),
	FLAME("ARROW_FIRE"),
	INFINITY("ARROW_INFINITE"),
	LUCK_OF_THE_SEA("LUCK");

	private final String bukkitName;

	protected static String toBukkit(String name) {
		name = name.toUpperCase().replace(" ", "_");

		for (final EnchantmentWrapper e : values())
			if (e.toString().equals(name))
				return e.bukkitName;

		return name;
	}

	public static String toMinecraft(String name) {
		name = name.toUpperCase().replace(" ", "_");

		for (final EnchantmentWrapper e : values())
			if (name.equals(e.bukkitName))
				return ItemUtil.bountifyCapitalized(e);

		return WordUtils.capitalizeFully(name);
	}

	public String getBukkitName() {
		return bukkitName != null ? bukkitName : name();
	}
}