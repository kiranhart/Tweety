package ca.tweetzy.tweety.command.annotation;

import ca.tweetzy.tweety.command.PermsCommand;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used in the {@link PermsCommand} command, for example usage, see
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface PermissionGroup {

	public String value() default "";
}