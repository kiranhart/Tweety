package ca.tweetzy.tweety.remain.nbt;

import ca.tweetzy.tweety.exception.TweetyException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Integer implementation for NBTLists
 *
 * @author tr7zw
 */
public class NBTIntArrayList extends NBTList<int[]> {

	private final ca.tweetzy.tweety.remain.nbt.NBTContainer tmpContainer;

	protected NBTIntArrayList(NBTCompound owner, String name, NBTType type, Object list) {
		super(owner, name, type, list);
		this.tmpContainer = new ca.tweetzy.tweety.remain.nbt.NBTContainer();
	}

	@Override
	protected Object asTag(int[] object) {
		try {
			final Constructor<?> con = ca.tweetzy.tweety.remain.nbt.ClassWrapper.NMS_NBTTAGINTARRAY.getClazz().getDeclaredConstructor(int[].class);
			con.setAccessible(true);
			return con.newInstance(object);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new TweetyException(e, "Error while wrapping the Object " + object + " to it's NMS object!");
		}
	}

	@Override
	public int[] get(int index) {
		try {
			final Object obj = ReflectionMethod.LIST_GET.run(this.listObject, index);
			ReflectionMethod.COMPOUND_SET.run(this.tmpContainer.getCompound(), "tmp", obj);
			final int[] val = this.tmpContainer.getIntArray("tmp");
			this.tmpContainer.removeKey("tmp");
			return val;
		} catch (final NumberFormatException nf) {
			return null;
		} catch (final Exception ex) {
			throw new TweetyException(ex);
		}
	}

}