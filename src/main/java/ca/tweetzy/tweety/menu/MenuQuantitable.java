package ca.tweetzy.tweety.menu;

import ca.tweetzy.tweety.MathUtil;
import ca.tweetzy.tweety.Valid;
import ca.tweetzy.tweety.menu.button.Button;
import ca.tweetzy.tweety.menu.model.ItemCreator;
import ca.tweetzy.tweety.menu.model.MenuQuantity;
import ca.tweetzy.tweety.model.Replacer;
import ca.tweetzy.tweety.remain.CompMaterial;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Advanced menu concept allowing to change quality of an item by more than 1 on
 * a single click.
 * <p>
 * For example: You want to chance the spawn percentage from 1% to 100% so you
 * set the editing quantity to 20 and you only need to click the item 5 times
 * instead of 99999 times.
 * <p>
 * We added this as an interface so you can extend all other kinds of menus
 */
public interface MenuQuantitable {

	/* ------------------------------------------------------------------------------- */
	/* Quantity */
	/* ------------------------------------------------------------------------------- */

	/**
	 * Get the current quantity of editing
	 *
	 * @return the quantity edit
	 */
	@NonNull
	MenuQuantity getQuantity();

	/**
	 * Set a new quantity of editing
	 *
	 * @param newQuantity the new quantity
	 */
	void setQuantity(@NonNull MenuQuantity newQuantity);

	/**
	 * A utility method to get the chance quantity we are changing with each click,
	 * formatted with % sign appended.
	 *
	 * @return
	 */
	default String getCurrentQuantityPercent() {
		final double percent = getQuantity().getAmountPercent();

		return (allowDecimalQuantities() ? MathUtil.formatTwoDigits(percent) : String.valueOf((int) percent)) + (quantitiesArePercents() ? "%" : "");
	}

	/**
	 * Get the next edit quantity from click from 0.0 to 1.0
	 *
	 * @param clickType the click type
	 * @return the next quantity (higher or lower depending on the click)
	 */
	default double getNextQuantityDouble(ClickType clickType) {
		return clickType == ClickType.LEFT ? -getQuantity().getAmountDouble() : getQuantity().getAmountDouble();
	}

	/**
	 * Get the next edit quantity from click from 0.0 to 100.0
	 *
	 * @param clickType the click type
	 * @return the next quantity (higher or lower depending on the click)
	 */
	default double getNextQuantityPercent(ClickType clickType) {
		return clickType == ClickType.LEFT ? -getQuantity().getAmountPercent() : getQuantity().getAmountPercent();
	}

	/**
	 * Get the button that is responsible for setting the quantity edit
	 * Implemented by default.
	 *
	 * @param menu the menu
	 * @return the button that is responsible for setting the quantity edit
	 */
	default Button getQuantityButton(Menu menu) {
		return new Button() {

			@Override
			public final void onClickedInMenu(Player player, Menu clickedMenu, ClickType clickType) {
				final MenuQuantity nextQuantity = clickType == ClickType.LEFT ? getQuantity().previous(allowDecimalQuantities()) : getQuantity().next(allowDecimalQuantities());
				Valid.checkNotNull(nextQuantity, "Next quantity cannot be null. Current: " + getQuantity() + " Click: " + clickType);

				setQuantity(nextQuantity);

				menu.restartMenu("&9Editing quantity set to " + getCurrentQuantityPercent());
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(
								CompMaterial.STRING,
								"Edit Quantity: &7" + getCurrentQuantityPercent(),
								"",
								"&8< &7Left click to decrease",
								"&8> &7Right click to increase")
						.make();
			}
		};
	}

	/**
	 * Return where we should put the button to edit how much we want to add/remove
	 * from container items in one click. Defaults to the bottom center slot.
	 *
	 * Return -1 to hide the button.
	 *
	 * @return
	 */
	default int getQuantityButtonPosition() {
		return ((Menu) this).getBottomCenterSlot();
	}

	/**
	 * Should we allow editing quantities below 1% such as 0.5%?
	 * Do not use for itemstack amounts.
	 *
	 * @return
	 */
	default boolean allowDecimalQuantities() {
		return false;
	}

	/**
	 * Are quantities in percents? We simply use this to append % after them
	 * in item lores.
	 *
	 * @return
	 */
	default boolean quantitiesArePercents() {
		return false;
	}

	/* ------------------------------------------------------------------------------- */
	/* Level-related */
	/* ------------------------------------------------------------------------------- */

	default ItemStack addLevelToItem(ItemStack item, int level) {
		return this.addLevelToItem(item, String.valueOf(level));
	}

	default ItemStack addLevelToItem(ItemStack item, String level) {

		// Paint the item with the drop chance lore
		final List<String> dropChanceLore = Replacer.replaceArray(Arrays.asList(

				// Lore
				"",
				"&7" + getLevelLoreLabel() + ": &6{level}",
				"",
				"   &8(Mouse click)",
				"  &7&l< &4-{quantity}    &2+{quantity} &7&l>"),

				// Variables
				"level", level,
				"quantity", getCurrentQuantityPercent());

		return ItemCreator.of(item.clone()).clearLore().lore(dropChanceLore).makeMenuTool();
	}

	default String getLevelLoreLabel() {
		return "Current level";
	}
}
