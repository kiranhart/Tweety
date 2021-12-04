package ca.tweetzy.tweety.remain.nbt;

import java.lang.reflect.Method;

import ca.tweetzy.tweety.exception.TweetyException;

final class CheckUtil {

	public static boolean isAvailable(Method method) {
		if (MinecraftVersion.getVersion().getVersionId() < method.getAnnotation(AvailableSince.class).version().getVersionId())
			throw new TweetyException("The Method '" + method.getName() + "' is only avaliable for the Versions " + method.getAnnotation(AvailableSince.class).version() + "+, but still got called!");

		return true;
	}

}
