package mcfp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mcfp.instruction.Instruction;

//.mcfp
//->instances
//->.mcfunction
public class MCFPCompiler {

	private Version version;

	private List<File> files = new ArrayList<>();
	private Map<File, MCFPClass> loadedClasses = new HashMap<>();
	private Namespace namespace = new Namespace();

	public MCFPCompiler(Version version) {
		this.version = version;
	}

	/**
	 * コンパイル対象を追加します
	 * @param file ファイルパス
	 */
	public void addFile(File file) {
		this.files.add(file);
	}

	/**
	 * コンパイルを開始します
	 */
	public void run(String projectName) throws IOException{
		for(File file : this.files) {
			if(!this.isLoaded(file)) {
				this.loadFile(file);
			}
		}

		for(MCFPClass mcfpClass : this.loadedClasses.values()) {
			for(MCFPFunction function : mcfpClass.getFunctions()) {
				String name = "output/" + this.namespace.add(function) + ".mcfunction";
				this.writeFunction(new File(name), function);
			}
		}
	}

	public void writeFunction(File output, MCFPFunction function) {
		output.getParentFile().mkdirs();

		try (FileWriter fw = new FileWriter(output);BufferedWriter bw = new BufferedWriter(fw);){
			for(Instruction instruction : function.getInstructions()) {
				instruction.writeCommands(bw, this);
			}

			bw.flush();
		}catch(Exception e) {
			e.printStackTrace();
		}
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
			Node<String> master = new Node<>("class " + name + ":");
			putNode(roughData.createCursor(), master);

			//ノード形式からインスタンスに変換
			MCFPClass mcfpClass = this.loadClass(master);

			this.loadedClasses.put(file, mcfpClass);

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

		MCFPClass mcfpClass = new MCFPClass(name, "");

		for(Node<String> child : master.getChildren()) {
			String title2 = child.getData();

			if(isFunction(title2)) {
				MCFPFunction func = this.loadFunction(child);
				func.setParentClass(mcfpClass);
				mcfpClass.addFunction(func);
			}else if(isClass(title2)){
				mcfpClass.addSubClass(this.loadClass(child));
			}else {
				throw new SyntaxException("Unsolved class element was found: " + title2);
			}
		}

		return mcfpClass;
	}

	private MCFPFunction loadFunction(Node<String> master) {
		String title = master.getData();
		String name = title.substring(5, title.indexOf("(")).trim();

		MCFPFunction function = new MCFPFunction(name);

		for(Node<String> node : master.getChildren()) {
			Instruction instruction = Instruction.toInstruction(node, this.version);
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
				if(!argument.trim().matches("[a-zA-Z]+\\s+[a-zA-Z]+")) return false;
			}
		}
		return true;
	}

	private static boolean isClass(String data) {
		return data.matches("class\\s+[a-zA-Z]+\\s*:\\s*");
	}

	/**
	 * 指定されたファイルが既に読み込まれているかどうか
	 * @pram file ファイルパス
	 */
	public boolean isLoaded(File file) {
		return this.loadedClasses.containsKey(file);
	}

	public Namespace getNamespace() {
		return this.namespace;
	}
}
