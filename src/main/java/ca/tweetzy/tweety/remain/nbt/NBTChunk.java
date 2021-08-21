package ca.tweetzy.tweety.remain.nbt;

import org.bukkit.Chunk;

import ca.tweetzy.tweety.MinecraftVersion;
import ca.tweetzy.tweety.exception.TweetyException;

public class NBTChunk {

	private final Chunk chunk;

	public NBTChunk(Chunk chunk) {
		this.chunk = chunk;
	}

	/**
	 * Gets the NBTCompound used by spigots PersistentDataAPI. This method is only
	 * available for 1.16.4+!
	 *
	 * @return NBTCompound containing the data of the PersistentDataAPI
	 */
	public NBTCompound getPersistentDataContainer() {

		if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_16))
			throw new TweetyException("getPersistentDataContainer requires MC 1.16 or newer");

		return new NBTPersistentDataContainer(chunk.getPersistentDataContainer());
	}

}
