package mcfp.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderableMap<K, V> {

	private Map<K, V> map = new HashMap<>();
	private List<K> index = new ArrayList<>();

	public void add(K key, V value) {
		this.map.put(key, value);
		this.index.add(key);
	}

	public V remove(K key) {
		this.index.remove(key);
		return this.map.remove(key);
	}

	public void foreach(Function<K,V> f) {
		for(K key : this.index) {
			V value = this.map.get(key);
			f.run(key, value);
		}
	}

	public void sort(Comparator<K> c) {
		Collections.sort(this.index, c);
	}

	@FunctionalInterface
	public static interface Function<K, V>{
		public void run(K key, V value);
	}
}
