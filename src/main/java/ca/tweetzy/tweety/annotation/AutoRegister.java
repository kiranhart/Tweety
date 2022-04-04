package ca.tweetzy.tweety.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Place this annotation over any of the following classes to make Tweety
 * automatically register it when the plugin starts, and properly reload it.
 * <p>
 * Supported classes:
 * - SimpleListener
 * - BungeeListener
 * <p>
 * In addition, the following classes will self-register automatically regardless
 * if you place this annotation on them or not:
 * - Tool (and its derivates such as Rocket)
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface AutoRegister {

	boolean hideIncompatibilityWarnings() default false;
}
