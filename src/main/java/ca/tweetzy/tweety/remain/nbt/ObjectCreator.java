package ca.tweetzy.tweety.remain.nbt;

import ca.tweetzy.tweety.Common;
import ca.tweetzy.tweety.exception.TweetyException;

import java.lang.reflect.Constructor;

/**
 * This Enum wraps Constructors for NMS classes
 *
 * @author tr7zw
 */
enum ObjectCreator {
	NMS_NBTTAGCOMPOUND(null, null, ca.tweetzy.tweety.remain.nbt.ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz()),
	NMS_BLOCKPOSITION(null, null, ca.tweetzy.tweety.remain.nbt.ClassWrapper.NMS_BLOCKPOSITION.getClazz(), int.class, int.class, int.class),
	NMS_COMPOUNDFROMITEM(MinecraftVersion.MC1_11_R1, null, ca.tweetzy.tweety.remain.nbt.ClassWrapper.NMS_ITEMSTACK.getClazz(), ca.tweetzy.tweety.remain.nbt.ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz()),
	;

	private Constructor<?> construct;
	private Class<?> targetClass;

	ObjectCreator(MinecraftVersion from, MinecraftVersion to, Class<?> clazz, Class<?>... args) {
		if ((clazz == null) || (from != null && MinecraftVersion.getVersion().getVersionId() < from.getVersionId()))
			return;
		if (to != null && MinecraftVersion.getVersion().getVersionId() > to.getVersionId())
			return;
		try {
			this.targetClass = clazz;
			this.construct = clazz.getDeclaredConstructor(args);
			this.construct.setAccessible(true);
		} catch (final Exception ex) {
			Common.error(ex, "Unable to find the constructor for the class '" + clazz.getName() + "'");
		}
	}

	/**
	 * Creates an Object instance with given args
	 *
	 * @param args
	 * @return Object created
	 */
	public Object getInstance(Object... args) {
		try {
			return this.construct.newInstance(args);
		} catch (final Exception ex) {
			throw new TweetyException(ex, "Exception while creating a new instance of '" + this.targetClass + "'");
		}
	}

}