package mcfp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mcfp.instruction.Instruction;

public class MCFPCompiler {

	private List<File> files = new ArrayList<>();

	private MCFPClassLoader classloader;

	public MCFPCompiler(Version version) {
		this.classloader = new MCFPClassLoader(version);
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
	public void run(String output) throws IOException{
		for(File file : this.files) {
			this.classloader.loadFile(file);
		}

		for(MCFPClass mcfpClass : this.classloader.getLoadedClasses()) {
			for(MCFPFunction function : mcfpClass.getFunctions()) {
				String name = output + "/" + this.classloader.getNamespace().add(function) + ".mcfunction";
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

	public MCFPClassLoader getClassLoader() {
		return this.classloader;
	}
}
