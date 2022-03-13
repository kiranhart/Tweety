package ca.tweetzy.tweety.remain.nbt;

import org.bukkit.block.BlockState;

/**
 * NBT class to access vanilla tags from TileEntities. TileEntities don't
 * support custom tags. Use the NBTInjector for custom tags. Changes will be
 * instantly applied to the Tile, use the merge method to do many things at
 * once.
 *
 * @author tr7zw
 */
public class NBTTileEntity extends NBTCompound {

	private final BlockState tile;

	/**
	 * @param tile BlockState from any TileEntity
	 */
	public NBTTileEntity(BlockState tile) {
		super(null, null);
		if (tile == null || (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3) && !tile.isPlaced()))
			throw new NullPointerException("Tile can't be null/not placed!");
		this.tile = tile;
	}

	@Override
	public Object getCompound() {
		return ca.tweetzy.tweety.remain.nbt.NBTReflectionUtil.getTileEntityNBTTagCompound(this.tile);
	}

	@Override
	protected void setCompound(Object compound) {
		ca.tweetzy.tweety.remain.nbt.NBTReflectionUtil.setTileEntityNBTTagCompound(this.tile, compound);
	}

	/**
	 * Gets the NBTCompound used by spigots PersistentDataAPI. This method is only
	 * available for 1.14+!
	 *
	 * @return NBTCompound containing the data of the PersistentDataAPI
	 */
	public NBTCompound getPersistentDataContainer() {
		if (this.hasKey("PublicBukkitValues"))
			return this.getCompound("PublicBukkitValues");
		else {
			final ca.tweetzy.tweety.remain.nbt.NBTContainer container = new ca.tweetzy.tweety.remain.nbt.NBTContainer();
			container.addCompound("PublicBukkitValues").setString("__nbtapi",
					"Marker to make the PersistentDataContainer have content");
			this.mergeCompound(container);
			return this.getCompound("PublicBukkitValues");
		}
	}

}