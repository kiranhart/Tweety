package ca.tweetzy.tweety.util;

import ca.tweetzy.tweety.exception.InvalidWorldException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Utility class for serializing objects to writeable YAML data and back.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializeUtil {

	/**
	 * Converts a {@link Location} into "world x y z yaw pitch" string
	 * Decimals not supported, use {@link #deserializeLocationD(Object)} for them
	 *
	 * @param loc
	 * @return
	 */
	public static String serializeLoc(final Location loc) {
		return loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + (loc.getPitch() != 0F || loc.getYaw() != 0F ? " " + Math.round(loc.getYaw()) + " " + Math.round(loc.getPitch()) : "");
	}

	/**
	 * Converts a {@link Location} into "world x y z yaw pitch" string with decimal support
	 * Unused, you have to call this in your save() method otherwise we remove decimals and use the above method
	 *
	 * @param loc
	 * @return
	 */
	public static String serializeLocD(final Location loc) {
		return loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + (loc.getPitch() != 0F || loc.getYaw() != 0F ? " " + loc.getYaw() + " " + loc.getPitch() : "");
	}

	/**
	 * Converts a {@link PotionEffect} into a "type duration amplifier" string
	 *
	 * @param effect
	 * @return
	 */
	public static String serializePotionEffect(final PotionEffect effect) {
		return effect.getType().getName() + " " + effect.getDuration() + " " + effect.getAmplifier();
	}

	// ------------------------------------------------------------------------------------------------------------
	// Converting stored strings from your files back into classes
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Converts a string into location, see {@link #deserializeLocation(Object)} for how strings are saved
	 * Decimals not supported, use {@link #deserializeLocationD(Object)} to use them
	 *
	 * @param raw
	 * @return
	 */
	public static Location deserializeLocation(Object raw) {
		if (raw == null)
			return null;

		if (raw instanceof Location)
			return (Location) raw;

		raw = raw.toString().replace("\"", "");

		final String[] parts = raw.toString().contains(", ") ? raw.toString().split(", ") : raw.toString().split(" ");
		Valid.checkBoolean(parts.length == 4 || parts.length == 6, "Expected location (String) but got " + raw.getClass().getSimpleName() + ": " + raw);

		final String world = parts[0];
		final World bukkitWorld = Bukkit.getWorld(world);
		if (bukkitWorld == null)
			throw new InvalidWorldException("Location with invalid world '" + world + "': " + raw + " (Doesn't exist)", world);

		final int x = Integer.parseInt(parts[1]), y = Integer.parseInt(parts[2]), z = Integer.parseInt(parts[3]);
		final float yaw = Float.parseFloat(parts.length == 6 ? parts[4] : "0"), pitch = Float.parseFloat(parts.length == 6 ? parts[5] : "0");

		return new Location(bukkitWorld, x, y, z, yaw, pitch);
	}

	/**
	 * Converts a string into a location with decimal support
	 * Unused but you can use this for your own parser storing exact decimals
	 *
	 * @param raw
	 * @return
	 */
	public static Location deserializeLocationD(Object raw) {
		if (raw == null)
			return null;

		if (raw instanceof Location)
			return (Location) raw;

		raw = raw.toString().replace("\"", "");

		final String[] parts = raw.toString().contains(", ") ? raw.toString().split(", ") : raw.toString().split(" ");
		Valid.checkBoolean(parts.length == 4 || parts.length == 6, "Expected location (String) but got " + raw.getClass().getSimpleName() + ": " + raw);

		final String world = parts[0];
		final World bukkitWorld = Bukkit.getWorld(world);

		if (bukkitWorld == null)
			throw new InvalidWorldException("Location with invalid world '" + world + "': " + raw + " (Doesn't exist)", world);

		final double x = Double.parseDouble(parts[1]), y = Double.parseDouble(parts[2]), z = Double.parseDouble(parts[3]);
		final float yaw = Float.parseFloat(parts.length == 6 ? parts[4] : "0"), pitch = Float.parseFloat(parts.length == 6 ? parts[5] : "0");

		return new Location(bukkitWorld, x, y, z, yaw, pitch);
	}

	/**
	 * Convert a raw object back to {@link PotionEffect}
	 *
	 * @param raw
	 * @return
	 */
	public static PotionEffect deserializePotionEffect(final Object raw) {
		if (raw == null)
			return null;

		if (raw instanceof PotionEffect)
			return (PotionEffect) raw;

		final String[] parts = raw.toString().split(" ");
		Valid.checkBoolean(parts.length == 3, "Expected PotionEffect (String) but got " + raw.getClass().getSimpleName() + ": " + raw);

		final String typeRaw = parts[0];
		final PotionEffectType type = PotionEffectType.getByName(typeRaw);

		final int duration = Integer.parseInt(parts[1]);
		final int amplifier = Integer.parseInt(parts[2]);

		return new PotionEffect(type, duration, amplifier);
	}
}