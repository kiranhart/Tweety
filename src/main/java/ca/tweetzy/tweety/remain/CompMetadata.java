package ca.tweetzy.tweety.remain;

import ca.tweetzy.tweety.TweetyPlugin;
import ca.tweetzy.tweety.constants.TweetyConstants;
import ca.tweetzy.tweety.remain.nbt.NBTCompound;
import ca.tweetzy.tweety.remain.nbt.NBTItem;
import ca.tweetzy.tweety.util.Common;
import ca.tweetzy.tweety.util.MinecraftVersion;
import ca.tweetzy.tweety.util.MinecraftVersion.V;
import ca.tweetzy.tweety.util.Valid;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;

/**
 * Utility class for persistent metadata manipulation
 * <p>
 * We apply scoreboard tags to ensure permanent metadata storage
 * if supported, otherwise it is lost on reload
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompMetadata {

	/**
	 * The tag delimiter
	 */
	private final static String DELIMITER = "%-%";

	// ----------------------------------------------------------------------------------------
	// Setting metadata
	// ----------------------------------------------------------------------------------------

	/**
	 * A shortcut for setting a tag with key-value pair on an item
	 *
	 * @param item
	 * @param key
	 * @param value
	 * @return
	 */
	public static ItemStack setMetadata(final ItemStack item, final String key, final String value) {
		Valid.checkNotNull(item, "Setting NBT tag got null item");

		final NBTItem nbt = new NBTItem(item);
		final NBTCompound tag = nbt.addCompound(TweetyConstants.NBT.TAG);

		tag.setString(key, value);
		return nbt.getItem();
	}

	/**
	 * Attempts to set a persistent metadata for entity
	 *
	 * @param entity
	 * @param tag
	 */
	public static void setMetadata(final Entity entity, final String tag) {
		setMetadata(entity, tag, tag);
	}

	/**
	 * Attempts to set a persistent metadata tag with value for entity
	 *
	 * @param entity
	 * @param key
	 * @param value
	 */
	public static void setMetadata(final Entity entity, final String key, final String value) {
		Valid.checkNotNull(entity);

		if (Remain.hasScoreboardTags()) {
			final String tag = format(key, value);

			if (!entity.getScoreboardTags().contains(tag))
				entity.addScoreboardTag(tag);

		} else {
			entity.setMetadata(key, new FixedMetadataValue(TweetyPlugin.getInstance(), value));
		}
	}

	// Format the syntax of stored tags
	private static String format(final String key, final String value) {
		return TweetyPlugin.getNamed() + DELIMITER + key + DELIMITER + value;
	}

	/**
	 * Sets persistent tile entity metadata
	 *
	 * @param tileEntity
	 * @param key
	 * @param value
	 */
	public static void setMetadata(final BlockState tileEntity, final String key, final String value) {
		Valid.checkNotNull(tileEntity);
		Valid.checkNotNull(key);
		Valid.checkNotNull(value);

		if (MinecraftVersion.atLeast(V.v1_14)) {
			Valid.checkBoolean(tileEntity instanceof TileState, "BlockState must be instance of a TileState not " + tileEntity);

			setNamedspaced((TileState) tileEntity, key, value);
			tileEntity.update();

		} else {
			tileEntity.setMetadata(key, new FixedMetadataValue(TweetyPlugin.getInstance(), value));
			tileEntity.update();
		}
	}

	private static void setNamedspaced(final TileState tile, final String key, final String value) {
		tile.getPersistentDataContainer().set(new NamespacedKey(TweetyPlugin.getInstance(), key), PersistentDataType.STRING, value);
	}

	// ----------------------------------------------------------------------------------------
	// Getting metadata
	// ----------------------------------------------------------------------------------------

	/**
	 * A shortcut from reading a certain key from an item's given compound tag
	 *
	 * @param item
	 * @param key
	 * @return
	 */
	public static String getMetadata(final ItemStack item, final String key) {
		Valid.checkNotNull(item, "Reading NBT tag got null item");

		if (item == null || CompMaterial.isAir(item.getType()))
			return null;

		final String compoundTag = TweetyConstants.NBT.TAG;
		final NBTItem nbt = new NBTItem(item);

		final String value = nbt.hasKey(compoundTag) ? nbt.getCompound(compoundTag).getString(key) : null;

		return Common.getOrNull(value);
	}

	/**
	 * Attempts to get the entity's metadata, first from scoreboard tag,
	 * second from Bukkit metadata
	 *
	 * @param entity
	 * @param key
	 * @return the tag, or null
	 */
	public static String getMetadata(final Entity entity, final String key) {
		Valid.checkNotNull(entity);

		if (Remain.hasScoreboardTags())
			for (final String line : entity.getScoreboardTags()) {
				final String tag = getTag(line, key);

				if (tag != null && !tag.isEmpty())
					return tag;
			}

		final String value = entity.hasMetadata(key) ? entity.getMetadata(key).get(0).asString() : null;

		return Common.getOrNull(value);
	}

	// Parses the tag and gets its value
	private static String getTag(final String raw, final String key) {
		final String[] parts = raw.split(DELIMITER);

		return parts.length == 3 && parts[0].equals(TweetyPlugin.getNamed()) && parts[1].equals(key) ? parts[2] : null;
	}

	/**
	 * Return saved tile entity metadata, or null if none
	 *
	 * @param tileEntity
	 * @param key        or null if none
	 * @return
	 */
	public static String getMetadata(final BlockState tileEntity, final String key) {
		Valid.checkNotNull(tileEntity);
		Valid.checkNotNull(key);

		if (MinecraftVersion.atLeast(V.v1_14)) {
			Valid.checkBoolean(tileEntity instanceof TileState, "BlockState must be instance of a TileState not " + tileEntity);

			return getNamedspaced((TileState) tileEntity, key);
		}

		final String value = tileEntity.hasMetadata(key) ? tileEntity.getMetadata(key).get(0).asString() : null;

		return Common.getOrNull(value);
	}

	private static String getNamedspaced(final TileState tile, final String key) {
		final String value = tile.getPersistentDataContainer().get(new NamespacedKey(TweetyPlugin.getInstance(), key), PersistentDataType.STRING);

		return Common.getOrNull(value);
	}

	// ----------------------------------------------------------------------------------------
	// Checking for metadata
	// ----------------------------------------------------------------------------------------

	/**
	 * Return true if the given itemstack has the given key stored at its compound
	 * tag {@link ca.tweetzy.tweety.constants.TweetyConstants.NBT#TAG}
	 *
	 * @param item
	 * @param key
	 * @return
	 */
	public static boolean hasMetadata(final ItemStack item, final String key) {
		Valid.checkBoolean(MinecraftVersion.atLeast(V.v1_7), "NBT ItemStack tags only support MC 1.7.10+");
		Valid.checkNotNull(item);

		if (CompMaterial.isAir(item.getType()))
			return false;

		final NBTItem nbt = new NBTItem(item);
		final NBTCompound tag = nbt.getCompound(TweetyConstants.NBT.TAG);

		return tag != null && tag.hasKey(key);
	}

	/**
	 * Returns if the entity has the given tag by key, first checks scoreboard tags,
	 * and then bukkit metadata
	 *
	 * @param entity
	 * @param key
	 * @return
	 */
	public static boolean hasMetadata(final Entity entity, final String key) {
		Valid.checkNotNull(entity);

		if (Remain.hasScoreboardTags())
			for (final String line : entity.getScoreboardTags())
				if (hasTag(line, key))
					return true;

		return entity.hasMetadata(key);
	}

	/**
	 * Return true if the given tile entity block such as {@link CreatureSpawner} has
	 * the given key
	 *
	 * @param tileEntity
	 * @param key
	 * @return
	 */
	public static boolean hasMetadata(final BlockState tileEntity, final String key) {
		Valid.checkNotNull(tileEntity);
		Valid.checkNotNull(key);

		if (MinecraftVersion.atLeast(V.v1_14)) {
			Valid.checkBoolean(tileEntity instanceof TileState, "BlockState must be instance of a TileState not " + tileEntity);

			return hasNamedspaced((TileState) tileEntity, key);
		}

		return tileEntity.hasMetadata(key);
	}

	private static boolean hasNamedspaced(final TileState tile, final String key) {
		return tile.getPersistentDataContainer().has(new NamespacedKey(TweetyPlugin.getInstance(), key), PersistentDataType.STRING);
	}

	// Parses the tag and gets its value
	private static boolean hasTag(final String raw, final String tag) {
		final String[] parts = raw.split(DELIMITER);

		return parts.length == 3 && parts[0].equals(TweetyPlugin.getNamed()) && parts[1].equals(tag);
	}

	/**
	 * Sets a temporary metadata to entity. This metadata is NOT persistent
	 * and is removed on server stop, restart or reload.
	 * <p>
	 * Use {@link #setMetadata(Entity, String)} to set persistent custom tags for entities.
	 *
	 * @param entity
	 * @param tag
	 */
	public static void setTempMetadata(final Entity entity, final String tag) {
		entity.setMetadata(tag, new FixedMetadataValue(TweetyPlugin.getInstance(), tag));
	}

	/**
	 * Sets a temporary metadata to entity. This metadata is NOT persistent
	 * and is removed on server stop, restart or reload.
	 * <p>
	 * Use {@link #setMetadata(Entity, String)} to set persistent custom tags for entities.
	 *
	 * @param entity
	 * @param tag
	 * @param key
	 */
	public static void setTempMetadata(final Entity entity, final String tag, final Object key) {
		entity.setMetadata(tag, new FixedMetadataValue(TweetyPlugin.getInstance(), key));
	}

	/**
	 * Return entity metadata value or null if has none
	 * <p>
	 * Only usable if you set it using the {@link #setTempMetadata(Entity, String, Object)} with the key parameter
	 * because otherwise the tag is the same as the value we return
	 *
	 * @param entity
	 * @param key
	 * @return
	 */
	public static MetadataValue getTempMetadata(final Entity entity, final String key) {
		return entity.hasMetadata(key) ? entity.getMetadata(key).get(0) : null;
	}

	/**
	 * Return true if player has the given temporary metadata
	 *
	 * @param player
	 * @param tag
	 * @return
	 */
	public static boolean hasTempMetadata(final Entity player, final String tag) {
		return player.hasMetadata(tag);
	}

	/**
	 * Remove temporary metadata from the entity
	 *
	 * @param player
	 * @param key
	 */
	public static void removeTempMetadata(final Entity player, final String key) {
		if (player.hasMetadata(key))
			player.removeMetadata(key, TweetyPlugin.getInstance());
	}
}