package mcfp;

import java.util.HashMap;
import java.util.Map;

/**
 * 文字列からハッシュ文字列を作成し短縮を行います
 * 衝突した場合は再ハッシュが行われます
 */
public class Namespace {

	private Map<String, String> map = new HashMap<>();

	public String get(String name) {
		return this.map.get(name);
	}

	public String add(INamed holder) {
		return this.addElement(holder.getFullName());
	}

	public String add(String name, INamed holder) {
		if(name.startsWith("$")) return name.substring(1);

		return this.addElement(holder.getFullName() + "." + name);
	}

	private String addElement(String name) {
		if(this.map.containsKey(name)) return this.map.get(name);

		int hash = name.hashCode();
		String shash = String.format("%08x", hash);

		while(true) {
			if(!this.map.containsValue(shash)) break;
			shash = String.format("%08x", ++hash);
		}

		this.map.put(name, shash);

		return shash;
	}

	public Map<String, Integer> getConstList(){
		Map<String, Integer> list = new HashMap<>();

		for(String key : this.map.keySet()) {
			String name = key.substring(key.lastIndexOf(".") + 1);
			if(name.matches("[0-9]+")) {
				list.put(this.map.get(key), Integer.parseInt(name));
			}
		}

		return list;
	}

	public Map<String, String> getDatabase(){
		return this.map;
	}

	/**
	 * 指定の変数名の内最も深い階層で定義されたものの完全名を返却します
	 */
	public String searchLastDefined(String name){
		int count = 0;
		String result = null;
		String[] path0 = name.split("\\.");

		keys:for(String key : this.map.keySet()) {
			String[] path1 = key.split("\\.");

			if(path0[path0.length-1].equals(path1[path1.length-1])) {
				for(int i = 0;i < path1.length - 1;i++) {
					if(!path0[i].equals(path1[i])) {
						continue keys;
					}
				}

				if(count < path1.length) {
					count = path1.length;
					result = key;
				}
			}
		}

		return result;
	}
}
