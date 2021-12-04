package ca.tweetzy.tweety.plugin;

import org.bukkit.inventory.ItemStack;
import ca.tweetzy.tweety.model.PacketListener;
import ca.tweetzy.tweety.model.SimpleEnchantment;
import ca.tweetzy.tweety.remain.CompMaterial;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.StructureModifier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Listens to and intercepts packets using Tweety inbuilt features
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class EnchantmentPacketListener extends PacketListener {

	/**
	 * The singleton of this class to auto register it.
	 */
	@Getter(value = AccessLevel.MODULE)
	private static volatile PacketListener instance = new EnchantmentPacketListener();

	/**
	 * Registers our packet listener for some of the more advanced features of Tweety
	 */
	@Override
	public void onRegister() {

		// Auto placement of our lore when items are custom enchanted
		this.addSendingListener(PacketType.Play.Server.SET_SLOT, event -> {

			final StructureModifier<ItemStack> itemModifier = event.getPacket().getItemModifier();
			ItemStack item = itemModifier.read(0);

			if (item != null && !CompMaterial.isAir(item.getType())) {
				item = SimpleEnchantment.addEnchantmentLores(item);

				// Write the item
				if (item != null)
					itemModifier.write(0, item);
			}
		});
	}
}