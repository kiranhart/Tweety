package ca.tweetzy.tweety.model;

import lombok.Data;

/**
 * Simple tuple for key-value pairs
 */
@Data
public final class Tuple<K, V> {

	/**
	 * The key
	 */
	private final K key;

	/**
	 * The value
	 */
	private final V value;

	/**
	 * Return this tuple in X - Y syntax
	 *
	 * @return
	 */
	public String toLine() {
		return key + " - " + value;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.toLine();
	}

}
