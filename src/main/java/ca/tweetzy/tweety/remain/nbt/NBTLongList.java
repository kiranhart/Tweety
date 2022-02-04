package ca.tweetzy.tweety.remain.nbt;

import ca.tweetzy.tweety.exception.TweetyException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Long implementation for NBTLists
 *
 * @author tr7zw
 */
public class NBTLongList extends NBTList<Long> {

	protected NBTLongList(NBTCompound owner, String name, NBTType type, Object list) {
		super(owner, name, type, list);
	}

	@Override
	protected Object asTag(Long object) {
		try {
			final Constructor<?> con = ClassWrapper.NMS_NBTTAGLONG.getClazz().getDeclaredConstructor(long.class);
			con.setAccessible(true);
			return con.newInstance(object);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new TweetyException(e, "Error while wrapping the Object " + object + " to it's NMS object!");
		}
	}

	@Override
	public Long get(int index) {
		try {
			final Object obj = ReflectionMethod.LIST_GET.run(listObject, index);
			return Long.valueOf(obj.toString().replace("L", ""));
		} catch (final NumberFormatException nf) {
			return 0L;
		} catch (final Exception ex) {
			throw new TweetyException(ex);
		}
	}

}
