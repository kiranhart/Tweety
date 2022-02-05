package ca.tweetzy.tweety.collection;

import ca.tweetzy.tweety.exception.TweetyException;
import ca.tweetzy.tweety.remain.Remain;
import ca.tweetzy.tweety.model.Common;
import ca.tweetzy.tweety.util.Valid;
import com.google.gson.*;
import lombok.NonNull;
import org.bukkit.configuration.MemorySection;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Serialized map enables you to save and retain values from your
 * configuration easily, such as locations, other maps or lists and
 * much more.
 */
public final class SerializedMap extends StrictCollection {

	/**
	 * The Google Json instance
	 */
	private final static Gson gson;

	static {
		// Fix google complicating things and breaking long formatting
		final GsonBuilder gsonBuilder = new GsonBuilder();

		gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);

		gson = gsonBuilder.create();
	}

	/**
	 * A fallback Json parser
	 */
	private final static JsonParser jsonSimple = new JsonParser();

	/**
	 * The internal map with values
	 */
	private final StrictMap<String, Object> map = new StrictMap<>();

	/**
	 * Should we remove entries on get for this map instance,
	 */
	private boolean removeOnGet = false;

	/**
	 * Creates a new serialized map with the given first key-value pair
	 *
	 * @param key
	 * @param value
	 */
	private SerializedMap(final String key, final Object value) {
		this();

		put(key, value);
	}

	public SerializedMap() {
		super("Cannot remove '%s' as it is not in the map!", "Value '%s' is already in the map!");
	}

	/**
	 * Put key-value pairs from another map into this map
	 * <p>
	 * If the key already exist, it is ignored
	 *
	 * @param anotherMap
	 */
	public void mergeFrom(final SerializedMap anotherMap) {
		for (final Map.Entry<String, Object> entry : anotherMap.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();

			if (key != null && value != null && !this.map.containsKey(key))
				this.map.put(key, value);
		}
	}

	/**
	 * @param key
	 * @return
	 * @see Map#containsKey(Object)
	 */
	public boolean containsKey(final String key) {
		return map.containsKey(key);
	}

	/**
	 * Puts a key:value pair into the map only if the values are not null
	 *
	 * @param associativeArray
	 * @return
	 */
	public SerializedMap putArray(final Object... associativeArray) {
		boolean string = true;
		String lastKey = null;

		for (final Object obj : associativeArray) {
			if (string) {
				lastKey = (String) obj;

			} else
				map.override(lastKey, obj);

			string = !string;
		}

		return this;
	}

	/**
	 * Add another map to this map
	 *
	 * @param anotherMap
	 * @return this
	 */
	public SerializedMap put(@NonNull SerializedMap anotherMap) {
		map.putAll(anotherMap.asMap());

		return this;
	}

	/**
	 * Puts the key-value pair into the map if the value is true
	 *
	 * @param key
	 * @param value
	 */
	public void putIfTrue(final String key, final boolean value) {
		if (value)
			put(key, value);
	}

	/**
	 * Puts the key-value pair into the map if the value is not null
	 *
	 * @param key
	 * @param value
	 */
	public void putIfExist(final String key, final Object value) {
		if (value != null)
			put(key, value);
	}

	/**
	 * Puts the map into this map if not null and not empty
	 * <p>
	 * This will put a NULL value into the map if the value is null
	 *
	 * @param key
	 * @param value
	 */
	public void putIf(final String key, final Map<?, ?> value) {
		if (value != null && !value.isEmpty())
			put(key, value);

			// This value is undesirable to save if null, so if YamlConfig is used
			// it will remove it from the config
		else
			map.getSource().put(key, null);
	}

	/**
	 * Puts the collection into map if not null and not empty
	 * <p>
	 * This will put a NULL value into the map if the value is null
	 *
	 * @param key
	 * @param value
	 */
	public void putIf(final String key, final Collection<?> value) {
		if (value != null && !value.isEmpty())
			put(key, value);

			// This value is undesirable to save if null, so if YamlConfig is used
			// it will remove it from the config
		else
			map.getSource().put(key, null);
	}

	/**
	 * Puts the boolean into map if true
	 * <p>
	 * This will put a NULL value into the map if the value is null
	 *
	 * @param key
	 * @param value
	 */
	public void putIf(final String key, final boolean value) {
		if (value)
			put(key, value);

			// This value is undesirable to save if null, so if YamlConfig is used
			// it will remove it from the config
		else
			map.getSource().put(key, null);
	}

	/**
	 * Puts the value into map if not null
	 * <p>
	 * This will put a NULL value into the map if the value is null
	 *
	 * @param key
	 * @param value
	 */
	public void putIf(final String key, final Object value) {
		if (value != null)
			put(key, value);

			// This value is undesirable to save if null, so if YamlConfig is used
			// it will remove it from the config
		else
			map.getSource().put(key, null);
	}

	/**
	 * Puts a new key-value pair in the map, failing if the value is null
	 * or if the old key exists
	 *
	 * @param key
	 * @param value
	 */
	public void put(final String key, final Object value) {
		Valid.checkNotNull(value, "Value with key '" + key + "' is null!");

		map.put(key, value);
	}

	/**
	 * Puts a new key-value pair in the map, failing if key is null
	 * and replacing the old key if exist
	 *
	 * @param key
	 * @param value
	 */
	public void override(final String key, final Object value) {
		Valid.checkNotNull(value, "Value with key '" + key + "' is null!");

		map.override(key, value);
	}

	/**
	 * Overrides all map values
	 *
	 * @param map
	 */
	public void overrideAll(SerializedMap map) {
		map.forEach(this::override);
	}

	/**
	 * Remove the given key, returning null if not set
	 *
	 * @param key
	 * @return
	 */
	public Object removeWeak(final String key) {
		return map.removeWeak(key);
	}

	/**
	 * Remove the given key, throwing error if not set
	 *
	 * @param key
	 * @return
	 */
	public Object remove(final String key) {
		return map.remove(key);
	}

	/**
	 * Remove a given key by value
	 *
	 * @param value
	 */
	public void removeByValue(final Object value) {
		map.removeByValue(value);
	}

	/*
	 * Checks if the clazz parameter can be assigned to the given value
	 */
	private void checkAssignable(final String path, final Object value, final Class<?> clazz) {
		if (!clazz.isAssignableFrom(value.getClass()) && !clazz.getSimpleName().equals(value.getClass().getSimpleName()))
			throw new TweetyException("Malformed map! Key '" + path + "' in the map must be " + clazz.getSimpleName() + " but got " + value.getClass().getSimpleName() + ": '" + value + "'");
	}

	/**
	 * Looks up a value by the string key, case ignored
	 *
	 * @param key
	 * @return
	 */
	public Object getValueIgnoreCase(final String key) {
		for (final Entry<String, Object> entry : map.entrySet())
			if (entry.getKey().equalsIgnoreCase(key))
				return entry.getValue();

		return null;
	}

	/**
	 * @param consumer
	 * @see Map#forEach(BiConsumer)
	 */
	public void forEach(final BiConsumer<String, Object> consumer) {
		for (final Entry<String, Object> e : map.entrySet())
			consumer.accept(e.getKey(), e.getValue());
	}

	/**
	 * Return the first entry or null if map is empty
	 *
	 * @return
	 */
	public Map.Entry<String, Object> firstEntry() {
		return isEmpty() ? null : map.getSource().entrySet().iterator().next();
	}

	/**
	 * @return
	 * @see Map#keySet()
	 */
	public Set<String> keySet() {
		return map.keySet();
	}

	/**
	 * @return
	 * @see Map#values()
	 */
	public Collection<Object> values() {
		return map.values();
	}

	/**
	 * @return
	 * @see Map#entrySet()
	 */
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	/**
	 * @return
	 * @see Map#size()
	 */
	public int size() {
		return map.size();
	}

	/**
	 * Get the Java map representation
	 *
	 * @return
	 */
	public Map<String, Object> asMap() {
		return map.getSource();
	}

	/**
	 * @return
	 * @see Map#isEmpty()
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * @param removeOnGet the removeOnGet to set
	 */
	public void setRemoveOnGet(boolean removeOnGet) {
		this.removeOnGet = removeOnGet;
	}

	// ----------------------------------------------------------------------------------------------------
	// Static
	// ----------------------------------------------------------------------------------------------------

	/**
	 * Create a new map with the first key-value pair
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public static SerializedMap of(final String key, final Object value) {
		return new SerializedMap(key, value);
	}

	/**
	 * Create new serialized map from key-value pairs like you would in PHP:
	 * <p>
	 * array(
	 * "name" => value,
	 * "name2" => value2,
	 * )
	 * <p>
	 * Except now you just use commas instead of =>'s
	 *
	 * @param array
	 * @return
	 */
	public static SerializedMap ofArray(final Object... array) {

		// If the first argument is a map already, treat as such
		if (array != null && array.length == 1) {
			final Object firstArgument = array[0];

			if (firstArgument instanceof SerializedMap)
				return (SerializedMap) firstArgument;

			if (firstArgument instanceof Map)
				return SerializedMap.of((Map<String, Object>) firstArgument);

			if (firstArgument instanceof StrictMap)
				return SerializedMap.of(((StrictMap<String, Object>) firstArgument).getSource());
		}

		final SerializedMap map = new SerializedMap();
		map.putArray(array);

		return map;
	}

	/**
	 * Parses the given object into Serialized map
	 *
	 * @param object
	 * @return the serialized map, or an empty map if object could not be parsed
	 */
	public static SerializedMap of(Object object) {

		if (object != null)
			object = Remain.getRootOfSectionPathData(object);

		if (object instanceof SerializedMap)
			return (SerializedMap) object;

		if (object instanceof Map || object instanceof MemorySection)
			return of(Common.getMapFromSection(object));

		return new SerializedMap();
	}

	/**
	 * Converts the given Map into a serializable map
	 *
	 * @param map
	 * @return
	 */
	public static SerializedMap of(final Map<String, Object> map) {
		final SerializedMap serialized = new SerializedMap();

		serialized.map.clear();
		serialized.map.putAll(map);

		return serialized;
	}

	/**
	 * Attempts to parse the given JSON into a serialized map
	 * <p>
	 * Values are not deserialized right away, they are converted
	 * when you call get() functions
	 *
	 * @param json
	 * @return
	 */
	public static SerializedMap fromJson(@NonNull final String json) {

		synchronized (jsonSimple) {
			if (json.isEmpty() || "[]".equals(json) || "{}".equals(json))
				return new SerializedMap();

			// Fallback to simple
			try {
				final Object parsed = jsonSimple.parse(json);

				if (parsed instanceof JsonObject)
					return SerializedMap.of(parsed);

				throw new TweetyException("Unable to deserialize " + (parsed != null ? parsed.getClass() : "unknown class") + " from: " + json);

			} catch (final Throwable secondThrowable) {
				Common.throwError(secondThrowable, "Failed to parse JSON from " + json);

				return null;
			}
		}
	}
}