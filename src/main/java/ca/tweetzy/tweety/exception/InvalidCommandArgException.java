package ca.tweetzy.tweety.exception;

/**
 * Thrown when a command has invalid argument
 */
public final class InvalidCommandArgException extends CommandException {

	private static final long serialVersionUID = 1L;

	public InvalidCommandArgException() {
		super();
	}

	public InvalidCommandArgException(String message) {
		super(message);
	}
}