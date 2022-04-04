package ca.tweetzy.tweety.remain.nbt;

import org.bukkit.persistence.PersistentDataContainer;

import java.util.Map;

public class NBTPersistentDataContainer extends NBTCompound {

	private final PersistentDataContainer container;

	public NBTPersistentDataContainer(PersistentDataContainer container) {
		super(null, null);
		this.container = container;
	}

	@Override
	public Object getCompound() {
		return ReflectionMethod.CRAFT_PERSISTENT_DATA_CONTAINER_TO_TAG.run(this.container);
	}

	@Override
	protected void setCompound(Object compound) {
		final Map<Object, Object> map = (Map<Object, Object>) ReflectionMethod.CRAFT_PERSISTENT_DATA_CONTAINER_GET_MAP.run(this.container);
		map.clear();
		ReflectionMethod.CRAFT_PERSISTENT_DATA_CONTAINER_PUT_ALL.run(this.container, compound);
	}

}