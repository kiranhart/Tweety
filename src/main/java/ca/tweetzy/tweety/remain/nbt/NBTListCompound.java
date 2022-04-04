package ca.tweetzy.tweety.remain.nbt;

/**
 * Cut down version of the {@link NBTCompound} for inside
 * {@link NBTCompoundList} This Compound implementation is missing the ability
 * for further subCompounds and Lists. This class probably will change in the
 * future
 *
 * @author tr7zw
 */
public class NBTListCompound extends NBTCompound {

	private final ca.tweetzy.tweety.remain.nbt.NBTList<?> owner;
	private Object compound;

	protected NBTListCompound(ca.tweetzy.tweety.remain.nbt.NBTList<?> parent, Object obj) {
		super(null, null);
		this.owner = parent;
		this.compound = obj;
	}

	public ca.tweetzy.tweety.remain.nbt.NBTList<?> getListParent() {
		return this.owner;
	}

	@Override
	public Object getCompound() {
		return this.compound;
	}

	@Override
	protected void setCompound(Object compound) {
		this.compound = compound;
	}

	@Override
	protected void saveCompound() {
		this.owner.save();
	}

}