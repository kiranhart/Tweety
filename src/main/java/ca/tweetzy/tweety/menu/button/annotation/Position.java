package ca.tweetzy.tweety.menu.button.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ca.tweetzy.tweety.menu.button.StartPosition;

/**
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Position {

	int value() default 0;

	StartPosition start() default StartPosition.TOP_LEFT;
}
