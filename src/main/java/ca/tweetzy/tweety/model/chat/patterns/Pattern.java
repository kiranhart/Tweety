package ca.tweetzy.tweety.model.chat.patterns;

/**
 * Date Created: April 02 2022
 * Time Created: 11:15 a.m.
 *
 * @author Kiran Hart
 */
public interface Pattern {

	/**
	 * Applies this pattern to the provided String.
	 * Output might be the same as the input if this pattern is not present.
	 *
	 * @param string The String to which this pattern should be applied to
	 * @return The new String with applied pattern
	 */
	String process(String string);
}
