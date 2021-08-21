package ca.tweetzy.tweety.exception;

import ca.tweetzy.tweety.debug.Debugger;

/**
 * Represents our core exception. All exceptions of this
 * kind are logged automatically to the error.log file
 */
public class TweetyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new exception and logs it
	 *
	 * @param t
	 */
	public TweetyException(Throwable t) {
		super(t);

		Debugger.saveError(t);
	}

	/**
	 * Create a new exception and logs it
	 *
	 * @param message
	 */
	public TweetyException(String message) {
		super(message);

		Debugger.saveError(this, message);
	}

	/**
	 * Create a new exception and logs it
	 *
	 * @param message
	 * @param t
	 */
	public TweetyException(Throwable t, String message) {
		super(message, t);

		Debugger.saveError(t, message);
	}

	/**
	 * Create a new exception and logs it
	 */
	public TweetyException() {
		Debugger.saveError(this);
	}

	@Override
	public String getMessage() {
		return "Report: " + super.getMessage();
	}
}