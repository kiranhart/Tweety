package ca.tweetzy.tweety.exception;

import ca.tweetzy.tweety.debug.Debugger;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents our core exception. All exceptions of this
 * kind are logged automatically to the error.log file
 */
public class TweetyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Should we save thrown exceptions to error.log file automatically when they are thrown?
	 */
	@Getter
	@Setter
	private static boolean errorSavedAutomatically = true;

	/**
	 * Create a new exception and logs it
	 *
	 * @param t
	 */
	public TweetyException(Throwable t) {
		super(t);

		if (errorSavedAutomatically)
			Debugger.saveError(t);
	}

	/**
	 * Create a new exception and logs it
	 *
	 * @param message
	 */
	public TweetyException(String message) {
		super(message);

		if (errorSavedAutomatically)
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

		if (errorSavedAutomatically)
			Debugger.saveError(t, message);
	}

	/**
	 * Create a new exception and logs it
	 */
	public TweetyException() {

		if (errorSavedAutomatically)
			Debugger.saveError(this);
	}

	@Override
	public String getMessage() {
		return "Report: " + super.getMessage();
	}
}