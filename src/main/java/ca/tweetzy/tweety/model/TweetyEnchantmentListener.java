package ca.tweetzy.tweety.model;

import ca.tweetzy.tweety.EntityUtil;
import ca.tweetzy.tweety.MinecraftVersion;
import ca.tweetzy.tweety.MinecraftVersion.V;
import ca.tweetzy.tweety.remain.Remain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Map.Entry;
import java.util.function.BiConsumer;

/**
 * Listens and executes events for {@link SimpleEnchantment}
 * <p>
 * @deprecated Internal use only!
 */
@Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TweetyEnchantmentListener implements Listener {

	@Getter
	private static volatile Listener instance = new TweetyEnchantmentListener();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		final Entity damager = event.getDamager();

		if (damager instanceof LivingEntity)
			execute((LivingEntity) damager, (enchant, level) -> enchant.onDamage(level, (LivingEntity) damager, event));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent event) {
		if (!Remain.isInteractEventPrimaryHand(event))
			return;

		execute(event.getPlayer(), (enchant, level) -> enchant.onInteract(level, event));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreakBlock(BlockBreakEvent event) {
		execute(event.getPlayer(), (enchant, level) -> enchant.onBreakBlock(level, event));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onShoot(ProjectileLaunchEvent event) {
		try {
			final ProjectileSource projectileSource = event.getEntity().getShooter();

			if (projectileSource instanceof LivingEntity) {
				final LivingEntity shooter = (LivingEntity) projectileSource;

				execute(shooter, (enchant, level) -> enchant.onShoot(level, shooter, event));
				EntityUtil.trackHit(event.getEntity(), hitEvent -> execute(shooter, (enchant, level) -> enchant.onHit(level, shooter, hitEvent)));
			}
		} catch (final NoSuchMethodError ex) {
			if (MinecraftVersion.atLeast(V.v1_4))
				ex.printStackTrace();
		}
	}

	private void execute(LivingEntity source, BiConsumer<SimpleEnchantment, Integer> executer) {
		try {
			final ItemStack hand = source instanceof Player ? ((Player) source).getItemInHand() : source.getEquipment().getItemInHand();

			if (hand != null)
				for (final Entry<SimpleEnchantment, Integer> e : SimpleEnchantment.findEnchantments(hand).entrySet())
					executer.accept(e.getKey(), e.getValue());

		} catch (final NoSuchMethodError ex) {
			if (Remain.hasItemMeta())
				ex.printStackTrace();
		}
	}
}
