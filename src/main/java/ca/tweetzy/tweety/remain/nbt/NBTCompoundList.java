package ca.tweetzy.tweety.remain.nbt;

import ca.tweetzy.tweety.exception.TweetyException;

/**
 * {@link NBTListCompound} implementation for NBTLists
 *
 * @author tr7zw
 */
public class NBTCompoundList extends NBTList<NBTListCompound> {

	protected NBTCompoundList(NBTCompound owner, String name, NBTType type, Object list) {
		super(owner, name, type, list);
	}

	/**
	 * Adds a new Compound to the end of the List and returns it.
	 *
	 * @return The added {@link NBTListCompound}
	 */
	public NBTListCompound addCompound() {
		return (NBTListCompound) this.addCompound(null);
	}

	/**
	 * Adds a copy of the Compound to the end of the List and returns it.
	 * When null is given, a new Compound will be created
	 *
	 * @param comp
	 * @return
	 */
	public NBTCompound addCompound(NBTCompound comp) {
		try {
			final Object compound = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
			if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId())
				ReflectionMethod.LIST_ADD.run(this.listObject, this.size(), compound);
			else
				ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, compound);
			this.getParent().saveCompound();
			final NBTListCompound listcomp = new NBTListCompound(this, compound);
			if (comp != null)
				listcomp.mergeCompound(comp);
			return listcomp;
		} catch (final Exception ex) {
			throw new TweetyException(ex);
		}
	}

	/**
	 * Adds a new Compound to the end of the List.
	 *
	 * @param empty
	 * @return True, if compound was added
	 * @deprecated Please use addCompound!
	 */
	@Override
	@Deprecated
	public boolean add(NBTListCompound empty) {
		return this.addCompound(empty) != null;
	}

	@Override
	public void add(int index, NBTListCompound element) {
		if (element != null)
			throw new TweetyException("You need to pass null! ListCompounds from other lists won't work.");
		try {
			final Object compound = ca.tweetzy.tweety.remain.nbt.ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
			if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId())
				ReflectionMethod.LIST_ADD.run(this.listObject, index, compound);
			else
				ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, compound);
			super.getParent().saveCompound();
		} catch (final Exception ex) {
			throw new TweetyException(ex);
		}
	}

	@Override
	public NBTListCompound get(int index) {
		try {
			final Object compound = ReflectionMethod.LIST_GET_COMPOUND.run(this.listObject, index);
			return new NBTListCompound(this, compound);
		} catch (final Exception ex) {
			throw new TweetyException(ex);
		}
	}

	@Override
	public NBTListCompound set(int index, NBTListCompound element) {
		throw new TweetyException("This method doesn't work in the ListCompound context.");
	}

	@Override
	protected Object asTag(NBTListCompound object) {
		return null;
	}

}