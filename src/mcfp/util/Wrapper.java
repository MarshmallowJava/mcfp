package mcfp.util;

public class Wrapper<T> {

	private T value;

	public Wrapper() {
	}

	public Wrapper(T value) {
		this.set(value);
	}

	public void set(T value) {
		this.value = value;
	}

	public T get() {
		return this.value;
	}

	public int hashCode() {
		if(this.value == null) return super.hashCode();

		return this.value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this.value == null && obj == null) return true;
		if(this.value != null && obj == null) return false;
		if(this.value == null && obj != null) return false;

		if(obj instanceof Wrapper) {
			return this.value.equals(((Wrapper<?>)obj).get());
		}

		return this.value.equals(obj);
	}

	public String toString() {
		if(this.value == null) return "null";

		return this.value.toString();
	}
}
