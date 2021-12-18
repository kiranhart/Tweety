package ca.tweetzy.tweety.command.annotation;

import ca.tweetzy.tweety.command.PermsCommand;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used in the {@link PermsCommand} command, for example usage, see
 * https://github.com/kangarko/PluginTemplate/blob/main/src/main/java/org/mineacademy/template/model/Permissions.java
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Permission {

	public String value() default "";

	public boolean def() default false;
}
