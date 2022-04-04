package ca.tweetzy.tweety.model.chat.patterns;


import ca.tweetzy.tweety.model.chat.ColorFormatter;

import java.util.regex.Matcher;

/**
 * Date Created: April 02 2022
 * Time Created: 11:17 a.m.
 *
 * @author Kiran Hart
 */
public final class RainbowPattern implements Pattern {

	java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<RAINBOW([0-9]{1,3})>(.*?)</RAINBOW>");

	/**
	 * Applies a rainbow pattern to the provided String.
	 * Output might me the same as the input if this pattern is not present.
	 *
	 * @param string The String to which this pattern should be applied to
	 * @return The new String with applied pattern
	 */
	public String process(String string) {
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			String saturation = matcher.group(1);
			String content = matcher.group(2);
			string = string.replace(matcher.group(), ColorFormatter.rainbow(content, Float.parseFloat(saturation)));
		}
		return string;
	}
}