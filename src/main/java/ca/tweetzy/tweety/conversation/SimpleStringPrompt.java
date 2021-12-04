package ca.tweetzy.tweety.conversation;

import java.util.function.Consumer;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import ca.tweetzy.tweety.Valid;
import ca.tweetzy.tweety.settings.SimpleLocalization;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * A prompt that validates textual answers.
 */
@NoArgsConstructor
@AllArgsConstructor
public class SimpleStringPrompt extends SimplePrompt {

	/**
	 * The question you can set in the constructor already
	 */
	@Setter(value = AccessLevel.PROTECTED)
	private String question = null;

	/**
	 * What happens when a valid text is entered
	 */
	@Setter(value = AccessLevel.PROTECTED)
	private Consumer<String> successAction;

	/**
	 * Create a new prompt with bare question
	 *
	 * @param question
	 */
	public SimpleStringPrompt(String question) {
		this(question, null);
	}

	/**
	 * Create a new simple prompt with optionally returning to previous menu
	 *
	 * @param openMenu
	 */
	public SimpleStringPrompt(boolean openMenu) {
		super(openMenu);
	}

	/**
	 * The menu question
	 *
	 * @see ca.tweetzy.tweety.conversation.SimplePrompt#getPrompt(org.bukkit.conversations.ConversationContext)
	 */
	@Override
	protected String getPrompt(final ConversationContext ctx) {
		Valid.checkNotNull(question, "Please either call setQuestion or override getPrompt");

		return question;
	}

	/**
	 * Return true if input is not empty, it is advised to override this
	 *
	 * @see ca.tweetzy.tweety.conversation.SimplePrompt#isInputValid(org.bukkit.conversations.ConversationContext, java.lang.String)
	 */
	@Override
	protected boolean isInputValid(final ConversationContext context, final String input) {
		return !input.isEmpty();
	}

	/**
	 * Show the message when the input is not a number
	 *
	 * @see ca.tweetzy.tweety.conversation.SimplePrompt#getFailedValidationText(org.bukkit.conversations.ConversationContext, java.lang.String)
	 */
	@Override
	protected String getFailedValidationText(final ConversationContext context, final String invalidInput) {
		return SimpleLocalization.Commands.INVALID_STRING.replace("{input}", invalidInput);
	}

	/**
	 * Parse through
	 *
	 * @see org.bukkit.conversations.ValidatingPrompt#acceptValidatedInput(org.bukkit.conversations.ConversationContext, java.lang.String)
	 */
	@Override
	protected Prompt acceptValidatedInput(@NonNull final ConversationContext context, @NonNull final String input) {
		if (this.successAction != null)
			this.successAction.accept(input);

		else
			this.onValidatedInput(context, input);

		return END_OF_CONVERSATION;
	}

	/**
	 * Override this if you want a single question prompt and we have reached the end
	 *
	 * @param context
	 * @param input
	 */
	protected void onValidatedInput(ConversationContext context, String input) {
	}

	/**
	 * Show the question with the action to the player
	 *
	 * @param player
	 * @param question
	 * @param successAction
	 */
	public static void show(final Player player, final String question, final Consumer<String> successAction) {
		new SimpleStringPrompt(question, successAction).show(player);
	}
}
