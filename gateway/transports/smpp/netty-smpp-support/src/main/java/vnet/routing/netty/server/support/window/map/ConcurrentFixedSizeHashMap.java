/**
 * 
 */
package vnet.routing.netty.server.support.window.map;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author obergner
 * 
 */
public class ConcurrentFixedSizeHashMap<K, V> extends AbstractMap<K, V>
		implements ConcurrentMap<K, V>, Serializable {

	private static final long serialVersionUID = 6258421005923593444L;

	/* ---------------- Public operations -------------- */

	/** The array of keys. Keys that are null are not used. */
	private final K[] keys;

	/** The array of values which correspond by index with the keys array. */
	private final V[] values;

	/** The number of valid entries */
	private int size;

	/** The last search index. This makes putting and getting more efficient. */
	private int lastSearchIndex;

	/**
	 * Constructor
	 * 
	 * @param maxEntries
	 *            The maximum number of entries this map can hold
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentFixedSizeHashMap(final int maxEntries) {
		this.keys = (K[]) new Object[maxEntries];
		this.values = (V[]) new Object[maxEntries];
	}

	/**
	 * Constructor
	 * 
	 * @param map
	 *            The map
	 * @param maxEntries
	 *            The maximum number of entries this map can hold
	 */
	public ConcurrentFixedSizeHashMap(final Map<? extends K, ? extends V> map,
			final int maxEntries) {
		this(maxEntries);
		putAll(map);
	}

	/**
	 * @return True if this MicroMap is full
	 */
	public boolean isFull() {
		return this.size == this.keys.length;
	}

	/**
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() {
		return this.size;
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(final Object key) {
		return findKey(0, key) != -1;
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(final Object value) {
		return findValue(0, value) != -1;
	}

	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public V get(final Object key) {
		// Search for id
		final int index = findKey(key);

		if (index != -1) {
			// Return message
			return this.values[index];
		}

		// Failed to find id
		return null;
	}

	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(final K key, final V value) {
		// Search for id
		final int index = findKey(key);

		if (index != -1) {
			// Replace existing message
			final V oldValue = this.values[index];
			this.values[index] = value;
			return oldValue;
		}

		// Is there room for a new entry?
		if (this.size < this.keys.length) {
			// Store at first null index and continue searching after null index
			// next time
			final int nullIndex = nextNullKey(this.lastSearchIndex);
			this.lastSearchIndex = nextIndex(nullIndex);
			this.keys[nullIndex] = key;
			this.values[nullIndex] = value;
			this.size++;

			return null;
		} else {
			throw new IllegalStateException("Map full");
		}
	}

	@Override
	public V putIfAbsent(final K key, final V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean replace(final K key, final V oldValue, final V newValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public V replace(final K key, final V value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public V remove(final Object key) {
		// Search for id
		final int index = findKey(key);

		if (index != -1) {
			// Store message
			final V oldValue = this.values[index];

			this.keys[index] = null;
			this.values[index] = null;
			this.size--;

			return oldValue;
		}

		return null;
	}

	@Override
	public boolean remove(final Object key, final Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(final Map<? extends K, ? extends V> map) {
		for (final java.util.Map.Entry<? extends K, ? extends V> e : map
				.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	/**
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		for (int i = 0; i < this.keys.length; i++) {
			this.keys[i] = null;
			this.values[i] = null;
		}

		this.size = 0;
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {
			@Override
			public Iterator<K> iterator() {
				return new Iterator<K>() {
					@Override
					public boolean hasNext() {
						return this.i < ConcurrentFixedSizeHashMap.this.size - 1;
					}

					@Override
					public K next() {
						// Just in case... (WICKET-428)
						if (!hasNext()) {
							throw new NoSuchElementException();
						}

						// Find next id
						this.i = nextKey(nextIndex(this.i));

						// Get id
						return ConcurrentFixedSizeHashMap.this.keys[this.i];
					}

					@Override
					public void remove() {
						ConcurrentFixedSizeHashMap.this.keys[this.i] = null;
						ConcurrentFixedSizeHashMap.this.values[this.i] = null;
						ConcurrentFixedSizeHashMap.this.size--;
					}

					int i = -1;
				};
			}

			@Override
			public int size() {
				return ConcurrentFixedSizeHashMap.this.size;
			}
		};
	}

	/**
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<V> values() {
		return new AbstractList<V>() {
			@Override
			public V get(final int index) {
				if (index > ConcurrentFixedSizeHashMap.this.size - 1) {
					throw new IndexOutOfBoundsException();
				}
				int keyIndex = nextKey(0);

				for (int i = 0; i < index; i++) {
					keyIndex = nextKey(keyIndex + 1);
				}

				return ConcurrentFixedSizeHashMap.this.values[keyIndex];
			}

			@Override
			public int size() {
				return ConcurrentFixedSizeHashMap.this.size;
			}
		};
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return new AbstractSet<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return new Iterator<Entry<K, V>>() {
					@Override
					public boolean hasNext() {
						return this.index < ConcurrentFixedSizeHashMap.this.size;
					}

					@Override
					public Entry<K, V> next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}

						this.keyIndex = nextKey(nextIndex(this.keyIndex));

						this.index++;

						return new Map.Entry<K, V>() {
							@Override
							public K getKey() {
								return ConcurrentFixedSizeHashMap.this.keys[keyIndex];
							}

							@Override
							public V getValue() {
								return ConcurrentFixedSizeHashMap.this.values[keyIndex];
							}

							@Override
							public V setValue(final V value) {
								final V oldValue = ConcurrentFixedSizeHashMap.this.values[keyIndex];

								ConcurrentFixedSizeHashMap.this.values[keyIndex] = value;

								return oldValue;
							}
						};
					}

					@Override
					public void remove() {
						ConcurrentFixedSizeHashMap.this.keys[this.keyIndex] = null;
						ConcurrentFixedSizeHashMap.this.values[this.keyIndex] = null;
					}

					int keyIndex = -1;

					int index = 0;
				};
			}

			@Override
			public int size() {
				return ConcurrentFixedSizeHashMap.this.size;
			}
		};
	}

	/**
	 * Computes the next index in the id or message array (both are the same
	 * length)
	 * 
	 * @param index
	 *            The index
	 * @return The next index, taking into account wraparound
	 */
	private int nextIndex(final int index) {
		return (index + 1) % this.keys.length;
	}

	/**
	 * Finds the index of the next non-null id. If the map is empty, -1 will be
	 * returned.
	 * 
	 * @param start
	 *            Index to start at
	 * @return Index of next non-null id
	 */
	private int nextKey(final int start) {
		int i = start;

		do {
			if (this.keys[i] != null) {
				return i;
			}

			i = nextIndex(i);
		} while (i != start);

		return -1;
	}

	/**
	 * Finds the index of the next null id. If no null id can be found, the
	 * map is full and -1 will be returned.
	 * 
	 * @param start
	 *            Index to start at
	 * @return Index of next null id
	 */
	private int nextNullKey(final int start) {
		int i = start;

		do {
			if (this.keys[i] == null) {
				return i;
			}

			i = nextIndex(i);
		} while (i != start);

		return -1;
	}

	/**
	 * Finds a id by starting at lastSearchIndex and searching from there. If
	 * the id is found, lastSearchIndex is advanced so the next id search can
	 * find the next id in the array, which is the most likely to be retrieved.
	 * 
	 * @param id
	 *            Key to find in map
	 * @return Index of matching id or -1 if not found
	 */
	private int findKey(final Object key) {
		if (this.size > 0) {
			// Find id starting at search index
			final int index = findKey(this.lastSearchIndex, key);

			// Found match?
			if (index != -1) {
				// Start search at the next index next time
				this.lastSearchIndex = nextIndex(index);

				// Return index of id
				return index;
			}
		}

		return -1;
	}

	/**
	 * Searches for a id from a given starting index.
	 * 
	 * @param id
	 *            The id to find in this map
	 * @param start
	 *            Index to start at
	 * @return Index of matching id or -1 if not found
	 */
	private int findKey(final int start, final Object key) {
		int i = start;

		do {
			if (key.equals(this.keys[i])) {
				return i;
			}

			i = nextIndex(i);
		} while (i != start);

		return -1;
	}

	/**
	 * Searches for a message from a given starting index.
	 * 
	 * @param start
	 *            Index to start at
	 * @param message
	 *            The message to find in this map
	 * @return Index of matching message or -1 if not found
	 */
	private int findValue(final int start, final Object value) {
		int i = start;

		do {
			if (value.equals(this.values[i])) {
				return i;
			}

			i = nextIndex(i);
		} while (i != start);

		return -1;
	}
}
