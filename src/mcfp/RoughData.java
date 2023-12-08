package mcfp;

import java.util.ArrayList;
import java.util.List;

/**
 * プログラムからコメント、空行を除き
 * インデントと付随したデータです
 */
public class RoughData {

	private List<Data> data = new ArrayList<>();

	public void add(String src) {
		this.data.add(new Data(src));
	}

	public Data get(int index) {
		return this.data.get(index);
	}

	public int size() {
		return this.data.size();
	}

	public Cursor createCursor() {
		return new Cursor();
	}

	public void printData() {
		for(Data data : this.data) {
			data.print();
		}
	}

	public class Cursor{

		private int off;

		public boolean hasNext() {
			return this.off < RoughData.this.data.size();
		}

		public Data next() {
			return this.next(true);
		}

		public Data next(boolean flag) {
			return RoughData.this.data.get(flag ? this.off++ : this.off);
		}

	}

	public class Data{

		public String data;
		public int layer;

		public Data(String src) {
			this.data = src.trim();
			this.layer = this.countIndent(src);
		}

		private int countIndent(String data) {
			for(int i = 0;;i++) {
				if(!data.substring(0, 1).matches("\\s")) {
					return i;
				}
				data = data.substring(1);
			}
		}

		public void print() {
			System.out.println(layer + ": " + data);
		}
	}
}
