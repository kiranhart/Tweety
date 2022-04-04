package ca.tweetzy.tweety.model.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Date Created: April 03 2022
 * Time Created: 5:12 p.m.
 *
 * @author Kiran Hart
 */
@AllArgsConstructor
public enum Gradient {

	BLUE("26a0da", "6dd5ed"),
	AQUA("4CB8C4", "3CD3AD"),
	ORANGE("FFB75E", "ED8F03"),
	RED("ff6a00", "ee0979"),
	GREEN("3CAB49", "5DE76E");

	@Getter
	final String from, to;
}
