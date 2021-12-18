package ca.tweetzy.tweety.menu;

import ca.tweetzy.tweety.collection.StrictMap;
import ca.tweetzy.tweety.exception.TweetyException;
import ca.tweetzy.tweety.menu.model.ItemCreator;
import ca.tweetzy.tweety.menu.model.MenuClickLocation;
import ca.tweetzy.tweety.remain.CompMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * A simple menu allowing players to drop or take items.
 *
 * from the container. You can connect this with your file storing
 * system to save or load items edited by players in the container.
 */
public abstract class MenuContainer extends Menu {

	/**
	 * The filler item we fill the bottom bar with for safety.
	 */
	protected static final ItemStack BOTTOM_BAR_FILLER_ITEM = ItemCreator.of(CompMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, " ").make();

	/**
	 * Create a new menu that can edit chances of the items you put inside.
	 *
	 * @param parent
	 * @param startMode
	 */
	protected MenuContainer(Menu parent) {
		super(parent);

		// Default the size to 3 rows (+ 1 bottom row is added automatically)
		this.setSize(9 * 3);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Getting items
	// ------------------------------------------------------------------------------------------------------------

	/*
	 * @see ca.tweetzy.tweety.menu.Menu#getItemAt(int)
	 */
	@Override
	public final ItemStack getItemAt(int slot) {

		final ItemStack customDrop = this.getDropAt(slot);

		if (customDrop != null)
			return customDrop;

		if (slot > this.getSize() - 9)
			return BOTTOM_BAR_FILLER_ITEM;

		return NO_ITEM;
	}

	/**
	 * Return the slot numbers for which you want to allow
	 * items to get edited in your menu (if you do not want
	 * to allow editing the entire container window).
	 *
	 * If you want users to edit chances for all items except
	 * bottom bar, simply always return true here.
	 *
	 * @param slot
	 * @return
	 */
	protected abstract boolean canEditItem(int slot);

	// ------------------------------------------------------------------------------------------------------------
	// Allowing clicking
	// ------------------------------------------------------------------------------------------------------------

	/*
	 * @see ca.tweetzy.tweety.menu.Menu#isActionAllowed(ca.tweetzy.tweety.menu.model.MenuClickLocation, int, org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack)
	 */
	@Override
	public final boolean isActionAllowed(final MenuClickLocation location, final int slot, final ItemStack clicked, final ItemStack cursor) {

		if (location != MenuClickLocation.MENU)
			return true;

		if (slot >= getSize() - 9)
			return false;

		if (!this.canEditItem(location, slot, clicked, cursor))
			return false;

		return true;
	}

	/**
	 * Return true for the slots you want players to be able to edit.
	 * By default we enable them to edit anything above the bottom bar.
	 *
	 * This is called from {@link #isActionAllowed(MenuClickLocation, int, ItemStack, ItemStack)} and
	 * by defaults forwards the call to {@link #canEditItem(int)}
	 *
	 * @param location
	 * @param slot
	 * @param clicked
	 * @param cursor
	 * @return
	 */
	protected boolean canEditItem(final MenuClickLocation location, final int slot, final ItemStack clicked, final ItemStack cursor) {
		return this.canEditItem(slot);
	}

	/**
	 * Return the item that should appear at the given slot,
	 * you should load items from your data file or cache here.
	 *
	 * @param slot
	 * @return
	 */
	protected abstract ItemStack getDropAt(int slot);

	// ------------------------------------------------------------------------------------------------------------
	// Handling clicking
	// ------------------------------------------------------------------------------------------------------------

	/*
	 * @see ca.tweetzy.tweety.menu.Menu#onMenuClick(org.bukkit.entity.Player, int, org.bukkit.event.inventory.InventoryAction, org.bukkit.event.inventory.ClickType, org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack, boolean)
	 */
	@Override
	protected final void onMenuClick(Player player, int slot, InventoryAction action, ClickType clickType, ItemStack cursor, ItemStack clicked, boolean cancelled) {

		if (this.canEditItem(slot) && slot < this.getSize() - 9) {

			// Call our handler
			clicked = this.onItemClick(slot, clickType, clicked);

			// Update item
			this.setItem(slot, clicked);
		}
	}

	/*
	 * @see ca.tweetzy.tweety.menu.Menu#onMenuClick(org.bukkit.entity.Player, int, org.bukkit.inventory.ItemStack)
	 */
	@Override
	protected final void onMenuClick(Player player, int slot, ItemStack clicked) {
		throw new TweetyException("unsupported call");
	}

	/**
	 * Called automatically when the given slot is clicked,
	 * you can edit the clicked item here or simply pass it through.
	 *
	 * @param slot
	 * @param clickType
	 * @param item
	 * @return
	 */
	protected abstract ItemStack onItemClick(int slot, ClickType clickType, @Nullable ItemStack item);

	// ------------------------------------------------------------------------------------------------------------
	// Handling saving
	// ------------------------------------------------------------------------------------------------------------

	/*
	 * @see ca.tweetzy.tweety.menu.Menu#onMenuClose(org.bukkit.entity.Player, org.bukkit.inventory.Inventory)
	 */
	@Override
	protected final void onMenuClose(Player player, Inventory inventory) {
		final StrictMap<Integer, ItemStack> items = new StrictMap<>();

		for (int slot = 0; slot < this.getSize() - 9; slot++)
			if (this.canEditItem(slot)) {
				final ItemStack item = inventory.getItem(slot);

				items.put(slot, item);
			}

		this.onMenuClose(items);
	}

	/**
	 * Called automatically when you should save all editable slots stored in the map
	 * by slot, with their items (nullable).
	 *
	 * @param items
	 */
	protected abstract void onMenuClose(StrictMap<Integer, ItemStack> items);

	// ------------------------------------------------------------------------------------------------------------
	// Decoration
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * @see ca.tweetzy.tweety.menu.Menu#getInfo()
	 */
	@Override
	protected String[] getInfo() {
		return new String[] {
				"This menu allows you to drop",
				"items to this container.",
				"",
				"Simply &2drag and drop &7items",
				"from your inventory here."
		};
	}
}
