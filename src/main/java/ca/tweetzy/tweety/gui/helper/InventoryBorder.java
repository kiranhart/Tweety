package ca.tweetzy.tweety.gui.helper;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Date Created: April 02 2022
 * Time Created: 12:48 p.m.
 *
 * @author Kiran Hart
 */
@UtilityClass
public final class InventoryBorder {

	public List<Integer> getBorders(final int rows) {
		final List<Integer> borders = new ArrayList<>();

		for (int index = 0; index < rows * 9; index++) {
			int row = index / 9;
			int column = (index % 9) + 1;

			if (row == 0 || row == rows - 1 || column == 1 || column == 9)
				borders.add(index);
		}

		return borders;
	}

	public List<Integer> getInsideBorders(final int rows) {
		final List<Integer> inner = new ArrayList<>();

		for (int index = 0; index < rows * 9; index++) {
			int row = index / 9;
			int column = (index % 9) + 1;

			if (row == 0 || row == rows - 1 || column == 1 || column == 9)
				continue;

			inner.add(index);
		}

		return inner;
	}
}
