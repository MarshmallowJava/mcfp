package mcfp;

import java.util.ArrayList;
import java.util.List;

public class MCFPClass implements INamed{

	private final List<String> importSource;

	private final String name, namespace;
	private final MCFPClassLoader classloader;

	private final List<MCFPFunction> functions = new ArrayList<>();
	private final List<MCFPClass> subclasses = new ArrayList<>();

	public MCFPClass(String name, String namespace, List<String> importSource, MCFPClassLoader classloader) {
		this.name = name;
		this.namespace = namespace;
		this.importSource = importSource;
		this.classloader = classloader;
	}

	void addFunction(MCFPFunction function) {
		this.functions.add(function);
	}

	void addSubClass(MCFPClass subclass) {
		this.subclasses.add(subclass);
	}

	public List<MCFPFunction> getFunctions(){
		return this.functions;
	}

	public List<MCFPClass> getSubClasses(){
		return this.subclasses;
	}

	public String getName() {
		return this.name;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public String getFQCN() {
		if(this.namespace.isEmpty()) {
			return this.name;
		}else {
			return this.namespace + "." + this.name;
		}
	}

	public List<String> getImportSource(){
		return this.importSource;
	}

	public MCFPClassLoader getMCFPClassLoader() {
		return this.classloader;
	}

	public MCFPFunction getFunction(String name) {
		for(MCFPFunction function : this.functions) {
			if(function.getName().equals(name)) {
				return function;
			}
		}

		return null;
	}

	@Override
	public String getFullName() {
		if(this.namespace.isEmpty()) {
			return "c" + this.name;
		}else {
			return this.namespace + ".c" + this.name;
		}
	}
	
	@Override
	public String toString() {
		return this.getFQCN();
	}
}
