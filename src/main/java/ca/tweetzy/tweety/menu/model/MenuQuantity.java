package ca.tweetzy.tweety.menu.model;

import java.util.ArrayList;
import java.util.List;

import ca.tweetzy.tweety.Common;
import ca.tweetzy.tweety.menu.MenuQuantitable;

import lombok.RequiredArgsConstructor;

/**
 * Represents how much we should change an
 * ItemStack's size upon clicking the item in the menu.
 * <p>
 * For example use, see {@link MenuQuantitable}
 */
@RequiredArgsConstructor
public enum MenuQuantity {

	/**
	 * Chance drop chance by 0.1%
	 */
	ONE_TENTH(0.1),

	/**
	 * Chance drop chance by 0.5%
	 */
	HALF(0.5),

	/**
	 * Chance drop chance by 1%
	 */
	ONE(1),

	/**
	 * Change drop chance by 2%
	 */
	TWO(2),

	/**
	 * Change drop chance by 5%
	 */
	FIVE(5),

	/**
	 * Change drop chance by 10%
	 */
	TEN(10),

	/**
	 * Change drop chance by 20%
	 */
	TWENTY(20);

	/**
	 * The amount to change
	 */
	private final double amountPercent;

	/**
	 * Get the amount from 0.00 to 1.00
	 *
	 * @return
	 */
	public double getAmountDouble() {
		return this.amountPercent / 100.D;
	}

	/**
	 * Get the amount from 0.00% to 100.0%
	 *
	 * @return
	 */
	public double getAmountPercent() {
		return this.amountPercent;
	}

	/**
	 * Rotates the enum backwards
	 *
	 * @return the previous enum ordinal, or last if overflows
	 */
	public final MenuQuantity previous(boolean allowDecimals) {
		return Common.getNext(this, compileQuantities(allowDecimals), false);
	}

	/**
	 * Rotates the enum forward
	 *
	 * @return the next enum ordinal, or first if overflows
	 */
	public final MenuQuantity next(boolean allowDecimals) {
		return Common.getNext(this, compileQuantities(allowDecimals), true);
	}

	/*
	 * Helper to compile quantities including below 1%
	 */
	private List<MenuQuantity> compileQuantities(boolean includeDecimals) {
		final List<MenuQuantity> available = new ArrayList<>();

		for (final MenuQuantity quantity : values())
			if (includeDecimals || quantity.getAmountPercent() >= 1.00)
				available.add(quantity);

		return available;
	}
}