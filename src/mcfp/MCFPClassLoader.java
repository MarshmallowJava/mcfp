package mcfp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mcfp.instruction.Instruction;

//.mcfp
//->instances
//->.mcfunction
public class MCFPClassLoader {

	private Version version;

	private List<MCFPClass> loadedClasses = new ArrayList<>();
	private Namespace namespace = new Namespace();

	public MCFPClassLoader(Version version) {
		this.version = version;
	}

	/**
	 * 指定されたファイルを読み込みます
	 * @param file ファイルパス
	 */
	public MCFPClass loadFile(File file) throws IOException{
		try (FileReader fr = new FileReader(file);BufferedReader br = new BufferedReader(fr);){

			//まずはインデントとデータを獲得
			RoughData roughData = new RoughData();
			{
				String data;
				while((data = br.readLine()) != null) {
					if(data.isEmpty() || data.matches("\\s+") || data.matches("\\s*#.*")) continue;
					roughData.add(data);
				}
			}

			String name = file.getName();
			name = name.substring(0, name.lastIndexOf("."));

			//ラフデータをノード形式に変換
			//正直ソースコードからノード形式のデータを得られるならjava/c方式でもpythonでもいい
			//ただmcFunctionは1行ずつ書き込む方式なので書き方が似ているpython方式にした
			Node<String> master = new Node<>("class " + name + ":");
			putNode(roughData.createCursor(), master);

			//ノード形式からインスタンスに変換
			MCFPClass mcfpClass = this.loadClass(master);

			this.loadedClasses.add(mcfpClass);

			return mcfpClass;
		}catch(IOException e) {
			throw e;
		}
	}

	private static void putNode(RoughData.Cursor cursor, Node<String> parent) {
		while(cursor.hasNext()) {
			RoughData.Data data = cursor.next();
			Node<String> node = new Node<>(data.data);

			parent.addChildren(node);
			if(cursor.hasNext()) {
				RoughData.Data next = cursor.next(false);

				if(data.layer < next.layer) {
					putNode(cursor, node);

					if(cursor.hasNext()) {
						next = cursor.next(false);
					}else {
						break;
					}
				}
				if(data.layer > next.layer) {
					break;
				}
			}
		}
	}

	private MCFPClass loadClass(Node<String> master) {
		String title = master.getData();
		String name = title.substring(title.lastIndexOf(" ") + 1, title.lastIndexOf(":")).trim();
		List<String> importSource = new ArrayList<>();

		MCFPClass mcfpClass = new MCFPClass(name, "", importSource,this);

		for(Node<String> child : master.getChildren()) {
			String title2 = child.getData();

			if(isFunction(title2)) {
				MCFPFunction func = this.loadFunction(child, mcfpClass);
				mcfpClass.addFunction(func);
			}else if(isClass(title2)){
				mcfpClass.addSubClass(this.loadClass(child));
			}else if(isImport(title2)){
				title2 = title2.trim();
				importSource.add(title2.substring(title.lastIndexOf(" ") + 1).trim());
			}else{
				throw new SyntaxException("Unsolved class element was found: " + title2);
			}
		}

		return mcfpClass;
	}

	private MCFPFunction loadFunction(Node<String> master, MCFPClass owner) {
		String title = master.getData();
		String name = title.substring(5, title.indexOf("(")).trim();
		String[] args = new String[0];

		{
			String arguments = title.substring(title.indexOf("(") + 1, title.lastIndexOf(")")).trim();
			if(!arguments.isEmpty()) {
				args = arguments.split(",");

				for(int i = 0;i < args.length;i++) {
					args[i] = args[i].trim();
				}
			}
		}

		MCFPFunction function = new MCFPFunction(name, args);
		function.setParentClass(owner);

		for(Node<String> node : master.getChildren()) {
			Instruction instruction = Instruction.toInstruction(node, this.version, owner);
			if(instruction == null) {
				throw new SyntaxException("Unsolve instruction was found");
			}else {
				function.addInstruction(instruction);
			}
		}

		return function;
	}

	private static boolean isFunction(String data) {
		if(!data.matches("func\\s+[a-zA-Z]+\\s*\\(.*\\)\\s*:\\s*")) return false;

		data = data.substring(data.indexOf("(") + 1, data.indexOf(")"));
		if(!data.matches("\\s*")) {
			String[] args = data.split(",");
			for(String argument : args) {
				if(!argument.trim().matches("[0-9A-Za-z_]+")) return false;
			}
		}
		return true;
	}

	private static boolean isClass(String data) {
		return data.matches("class\\s+[a-zA-Z]+\\s*:\\s*");
	}

	private static boolean isImport(String data) {
		return data.matches("import\\s+[0-9A-Za-z_.]+\\s*");
	}

	public Namespace getNamespace() {
		return this.namespace;
	}

	public List<MCFPClass> getLoadedClasses() {
		return this.loadedClasses;
	}

	public MCFPClass getClassByFullName(String name) {
		for(MCFPClass mcfpClass : this.loadedClasses) {
			if(mcfpClass.getFQCN().equals(name)) {
				return mcfpClass;
			}
		}

		return null;
	}

	public List<MCFPClass> getClassesByNamespace(String namespace){
		List<MCFPClass> list = new ArrayList<>();

		for(MCFPClass mcfpClass : this.loadedClasses) {
			if(mcfpClass.getNamespace().equals(namespace)) {
				list.add(mcfpClass);
			}
		}

		return list;
	}
}
