package ca.tweetzy.tweety.model;

import ca.tweetzy.tweety.menu.model.ItemCreator;
import ca.tweetzy.tweety.remain.CompMaterial;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

/**
 *
 */
@Getter
public class SimpleHologramStand extends SimpleHologram {

	/**
	 * The material this hologram will have
	 */
	private final CompMaterial material;

	/**
	 * Is this item stand small?
	 */
	private boolean small;

	/**
	 * Is this item stand glowing?
	 */
	private boolean glowing;

	/**
	 * Create a new simple hologram using armor stand showing the given material
	 *
	 * @param spawnLocation
	 * @param material
	 */
	public SimpleHologramStand(Location spawnLocation, CompMaterial material) {
		super(spawnLocation);

		this.material = material;
	}

	/**
	 * @see ca.tweetzy.tweety.model.SimpleHologram#createEntity()
	 */
	@Override
	protected final Entity createEntity() {
		final ArmorStand armorStand = this.getLastTeleportLocation().getWorld().spawn(this.getLastTeleportLocation(), ArmorStand.class);

		armorStand.setGravity(false);
		armorStand.setHelmet(ItemCreator.of(material).glow(this.glowing).make());
		armorStand.setVisible(false);
		armorStand.setSmall(this.small);

		return armorStand;
	}

	/**
	 *
	 * @param glowing
	 * @return
	 */
	public final SimpleHologram setGlowing(boolean glowing) {
		this.glowing = glowing;

		return this;
	}

	/**
	 * @param small the small to set
	 */
	public final SimpleHologram setSmall(boolean small) {
		this.small = small;

		return this;
	}
}
