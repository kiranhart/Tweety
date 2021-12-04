package ca.tweetzy.tweety.constants;

import ca.tweetzy.tweety.command.annotation.Permission;
import ca.tweetzy.tweety.command.annotation.PermissionGroup;
import ca.tweetzy.tweety.plugin.SimplePlugin;

@PermissionGroup
public class TweetyPermissions {

	@Permission(value = "Receive plugin update notifications on join.")
	public static final String NOTIFY_UPDATE;

	static {
		NOTIFY_UPDATE = SimplePlugin.getNamed().toLowerCase() + ".notify.update";
	}
}