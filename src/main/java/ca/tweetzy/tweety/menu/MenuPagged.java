package ca.tweetzy.tweety.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import ca.tweetzy.tweety.Common;
import ca.tweetzy.tweety.MathUtil;
import ca.tweetzy.tweety.PlayerUtil;
import ca.tweetzy.tweety.Valid;
import ca.tweetzy.tweety.exception.TweetyException;
import ca.tweetzy.tweety.menu.button.Button;
import ca.tweetzy.tweety.menu.model.InventoryDrawer;
import ca.tweetzy.tweety.menu.model.ItemCreator;
import ca.tweetzy.tweety.remain.CompMaterial;
import ca.tweetzy.tweety.settings.SimpleLocalization;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * An advanced menu listing items with automatic page support
 *
 * @param <T> the item that each page consists of
 */
public abstract class MenuPagged<T> extends Menu {

	private final List<Integer> bottomSlots = new ArrayList<>();
	@Getter
	private Map<Integer, List<T>> pages;
	@Getter
	@Setter
	private int currentPage;
	private Button prevButton, nextButton;

	public MenuPagged(final Integer rows, final Integer itemsPerPage, final Iterable<T> pages) {
		this(null, rows, itemsPerPage, pages);
	}

	public MenuPagged(final Integer rows, final Iterable<T> pages) {
		this(null, rows, rows * 9, pages);
	}

	public MenuPagged(final Menu parent, final Integer rows, final Iterable<T> pages) {
		this(parent, rows, rows * 9, pages);
	}

	public MenuPagged(final Menu parent, final Integer rows, final Integer itemsPerPage, final Iterable<T> pages) {
		this(parent, rows, itemsPerPage, pages, false);
	}

	public MenuPagged(final Menu parent, final Integer rows, final Integer itemsPerPage, final Iterable<T> pages, final boolean returnMakesNewInstance) {
		super(parent, returnMakesNewInstance);

		final int items = getItemAmount(pages);
		final int autoPageSize = rows != null ? rows * 9 : items <= 9 ? 9 : items <= 9 * 2 ? 9 * 2 : items <= 9 * 3 ? 9 * 3 : items <= 9 * 4 ? 9 * 4 : 9 * 5;

		this.currentPage = 1;
		this.pages = Common.fillPages(itemsPerPage, pages);

		if (this.pages.size() > 1 && itemsPerPage == 54)
			this.pages = Common.fillPages(itemsPerPage - 9, pages);

		final int addExtraRow = autoPageSize != 9 * 6 && this.pages.size() > 1 ? 9 : 0;

		setSize(addExtraRow + autoPageSize);
		setButtons();

		for (int i = 1; i < 10; i++) {
			if (getSize() - i == previousButtonSlot() || getSize() - i == nextButtonSlot())
				continue;
			this.bottomSlots.add(getSize() - i);
		}
	}

	private int getItemAmount(final Iterable<T> pages) {
		int amount = 0;

		for (final T t : pages)
			amount++;

		return amount;
	}

	// Render the next/prev buttons
	private void setButtons() {
		final boolean hasPages = pages.size() > 1;

		// Set previous button
		prevButton = hasPages ? formPreviousButton() : Button.makeEmpty();

		// Set next page button
		nextButton = hasPages ? formNextButton() : Button.makeEmpty();
	}

	/**
	 * Automatically get the correct item from the actual page, including
	 * prev/next buttons
	 *
	 * @param slot the slot
	 * @return the item, or null
	 */
	@Override
	public ItemStack getItemAt(int slot) {
		// apply the header / footer first
		if (useHeader() && Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8).contains(slot))
			return headerItem();
		if (useFooter() && bottomSlots.contains(slot))
			return footerItem();

		int index = slot - startingSlot();

		if (slot >= startingSlot() && index < getCurrentPageItems().size()) {
			final T object = getCurrentPageItems().get(index);

			if (object != null)
				return convertToItemStack(object);
		}

		if (slot == previousButtonSlot())
			return prevButton.getItem();

		if (slot == nextButtonSlot())
			return nextButton.getItem();

		return useBackground() ? backgroundItem() : null;
	}

	/**
	 * Return the button to list the previous page,
	 * override to customize it.
	 */
	public Button formPreviousButton() {
		return new Button() {
			final boolean canGo = currentPage > 1;

			@Override
			public void onClickedInMenu(final Player pl, final Menu menu, final ClickType click) {
				if (canGo) {
					currentPage = MathUtil.range(currentPage - 1, 1, pages.size());
					updatePage();
				}
			}

			@Override
			public ItemStack getItem() {
				final int previousPage = currentPage - 1;
				return ItemCreator
						.of(canGo ? CompMaterial.ARROW : useFooter() ? CompMaterial.fromItem(footerItem()) : useBackground() ? CompMaterial.fromItem(backgroundItem()) : CompMaterial.AIR)
						.name(previousPage == 0 ? SimpleLocalization.Menu.PAGE_FIRST : SimpleLocalization.Menu.PAGE_PREVIOUS.replace("{page}", String.valueOf(previousPage)))
						.build().make();
			}
		};
	}

	/**
	 * Return the button to list the next page,
	 * override to customize it.
	 */
	public Button formNextButton() {
		return new Button() {
			final boolean canGo = currentPage < pages.size();

			@Override
			public void onClickedInMenu(final Player pl, final Menu menu, final ClickType click) {
				if (canGo) {
					currentPage = MathUtil.range(currentPage + 1, 1, pages.size());
					updatePage();
				}
			}

			@Override
			public ItemStack getItem() {
				final boolean lastPage = currentPage == pages.size();
				return ItemCreator
						.of(canGo ? CompMaterial.ARROW : useHeader() ? CompMaterial.fromItem(headerItem()) : useBackground() ? CompMaterial.fromItem(backgroundItem()) : CompMaterial.AIR)
						.name(lastPage ? SimpleLocalization.Menu.PAGE_LAST : SimpleLocalization.Menu.PAGE_NEXT.replace("{page}", String.valueOf(currentPage + 1)))
						.build().make();
			}
		};
	}

	// Re-inits the menu and plays the anvil sound
	private void updatePage() {
		setButtons();
		redraw();
		registerButtons();

		Menu.getSound().play(getViewer());
		PlayerUtil.updateInventoryTitle(getViewer(), compileTitle0());
	}

	// Compile title and page numbers
	private String compileTitle0() {
		final boolean canAddNumbers = addPageNumbers() && pages.size() > 1;

		return getTitle() + (canAddNumbers ? " &8" + currentPage + "/" + pages.size() : "");
	}

	/**
	 * Automatically prepend the title with page numbers
	 * <p>
	 * Override for a custom last-minute implementation, but
	 * ensure to call the super method otherwise no title will
	 * be set in {@link InventoryDrawer}
	 */
	@Override
	protected final void onDisplay(final InventoryDrawer drawer) {
		drawer.setTitle(compileTitle0());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onMenuClick(final Player player, final int slot, final InventoryAction action, final ClickType click, final ItemStack cursor, final ItemStack clicked, final boolean cancelled) {
		int index = slot - startingSlot();
		if (slot >= startingSlot() && index < getCurrentPageItems().size()) {
			final T obj = getCurrentPageItems().get(index);

			if (obj != null) {
				final val prevType = player.getOpenInventory().getType();
				onPageClick(player, obj, click);

				if (updateButtonOnClick() && prevType == player.getOpenInventory().getType())
					player.getOpenInventory().getTopInventory().setItem(index, getItemAt(index));
			}
		}
	}

	/**
	 * Return the {@link ItemStack} representation of an item on a certain page
	 * <p>
	 * Use {@link ItemCreator} for easy creation.
	 *
	 * @param item the given object, for example Arena
	 * @return the itemstack, for example diamond sword having arena name
	 */
	protected abstract ItemStack convertToItemStack(T item);

	/**
	 * Called automatically when an item is clicked
	 *
	 * @param player the player who clicked
	 * @param item   the clicked item
	 * @param click  the click type
	 */
	protected abstract void onPageClick(Player player, T item, ClickType click);

	/**
	 * Utility: Shall we send update packet when the menu is clicked?
	 *
	 * @return true by default
	 */
	protected boolean updateButtonOnClick() {
		return true;
	}

	/**
	 * Return true if you want our system to add page/totalPages suffix after
	 * your title, true by default
	 */
	protected boolean addPageNumbers() {
		return true;
	}

	/**
	 * Return true if you want to fill the empty slots within the inventory
	 * with a "placeholder/background" item
	 */
	protected boolean useBackground() {
		return true;
	}

	/**
	 * Should a header row / line be placed?
	 */
	protected boolean useHeader() {
		return false;
	}

	/**
	 * Should a footer row / line be placed?
	 */
	protected boolean useFooter() {
		return false;
	}

	/**
	 * What item should be used as the header?
	 */
	protected ItemStack headerItem() {
		return CompMaterial.YELLOW_STAINED_GLASS_PANE.toItem();
	}

	/**
	 * What item should be used as the footer?
	 */
	protected ItemStack footerItem() {
		return CompMaterial.YELLOW_STAINED_GLASS_PANE.toItem();
	}

	/**
	 * Set which slot the previous button should be placed in.
	 *
	 * @return the slot of the previous button
	 */
	protected int previousButtonSlot() {
		return getSize() - 6;
	}

	/**
	 * Set which slot the next button should be placed in.
	 *
	 * @return the slot of the next button
	 */
	protected int nextButtonSlot() {
		return getSize() - 4;
	}

	/**
	 * If you want items to begin placing from a specific slot
	 * then override this with the slot number
	 * ex. 9 will place items starting from the second row onward
	 *
	 * @return the slot items should begin placing at
	 */
	protected int startingSlot() {
		return 0;
	}

	/**
	 * The background item of the menu
	 *
	 * @return the item that should be used as a filler/background item
	 */
	protected ItemStack backgroundItem() {
		return CompMaterial.BLACK_STAINED_GLASS_PANE.toItem();
	}

	// Do not allow override
	@Override
	public final void onButtonClick(final Player player, final int slot, final InventoryAction action, final ClickType click, final Button button) {
		super.onButtonClick(player, slot, action, click, button);
	}

	// Do not allow override
	@Override
	public final void onMenuClick(final Player player, final int slot, final ItemStack clicked) {
		throw new TweetyException("Simplest click unsupported");
	}

	// Get all items in a page
	private List<T> getCurrentPageItems() {
		Valid.checkBoolean(pages.containsKey(currentPage - 1), "The menu has only " + pages.size() + " pages, not " + currentPage + "!");
		return pages.get(currentPage - 1);
	}
}