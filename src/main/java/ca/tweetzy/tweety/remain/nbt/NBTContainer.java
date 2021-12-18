package ca.tweetzy.tweety.remain.nbt;

import ca.tweetzy.tweety.exception.TweetyException;

import java.io.InputStream;

/**
 * A Standalone {@link NBTCompound} implementation. All data is just kept inside
 * this Object.
 *
 * @author tr7zw
 *
 */
public class NBTContainer extends NBTCompound {

	private Object nbt;

	/**
	 * Creates an empty, standalone NBTCompound
	 */
	public NBTContainer() {
		super(null, null);
		nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
	}

	/**
	 * Takes in any NMS Compound to wrap it
	 *
	 * @param nbt
	 */
	public NBTContainer(Object nbt) {
		super(null, null);
		if (nbt == null) {
			throw new NullPointerException("The NBT-Object can't be null!");
		}
		if (!ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().isAssignableFrom(nbt.getClass())) {
			throw new TweetyException("The object '" + nbt.getClass() + "' is not a valid NBT-Object!");
		}
		this.nbt = nbt;
	}

	/**
	 * Reads in a NBT InputStream
	 *
	 * @param inputsteam
	 */
	public NBTContainer(InputStream inputsteam) {
		super(null, null);
		this.nbt = NBTReflectionUtil.readNBT(inputsteam);
	}

	/**
	 * Parses in a NBT String to a standalone {@link NBTCompound}. Can throw a
	 * {@link TweetyException} in case something goes wrong.
	 *
	 * @param nbtString
	 */
	public NBTContainer(String nbtString) {
		super(null, null);
		if (nbtString == null) {
			throw new NullPointerException("The String can't be null!");
		}
		try {
			nbt = ReflectionMethod.PARSE_NBT.run(null, nbtString);

		} catch (final Exception ex) {
			throw new TweetyException(ex, "Unable to parse Malformed Json!");
		}
	}

	@Override
	public Object getCompound() {
		return nbt;
	}

	@Override
	public void setCompound(Object tag) {
		nbt = tag;
	}

}
