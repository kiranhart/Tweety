package ca.tweetzy.tweety.remain.nbt;

import ca.tweetzy.tweety.exception.TweetyException;
import org.bukkit.block.Block;

public class NBTBlock {

	private final Block block;
	private final NBTChunk nbtChunk;

	public NBTBlock(Block block) {
		this.block = block;
		if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R3))
			throw new TweetyException("NBTBlock is only working for 1.16.4+!");
		this.nbtChunk = new NBTChunk(block.getChunk());
	}

	public NBTCompound getData() {
		return this.nbtChunk.getPersistentDataContainer().getOrCreateCompound("blocks").getOrCreateCompound(this.block.getX() + "_" + this.block.getY() + "_" + this.block.getZ());
	}

}