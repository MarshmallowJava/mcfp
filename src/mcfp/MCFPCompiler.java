package mcfp;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mcfp.instruction.Instruction;

public class MCFPCompiler {

	private List<File> files = new ArrayList<>();
	private Map<File, byte[]> buff = new HashMap<>();

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

		//特にエラーが無かったのですべて出力
		this.flush();
	}

	public void writeFunction(File output, MCFPFunction function) {
		output.getParentFile().mkdirs();

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();OutputStreamWriter osw = new OutputStreamWriter(baos);BufferedWriter bw = new BufferedWriter(osw);){
			for(Instruction instruction : function.getInstructions()) {
				instruction.writeCommands(bw, this);
			}

			bw.flush();

			//一旦保存
			this.buff.put(output, baos.toByteArray());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void flush() {
		for(File file : this.buff.keySet()) {
			try (FileOutputStream fos = new FileOutputStream(file);BufferedOutputStream bos = new BufferedOutputStream(fos);){
				for(byte data : this.buff.get(file))
					bos.write(data);

				bos.flush();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		this.buff.clear();
	}

	public MCFPClassLoader getMCFPClassLoader() {
		return this.classloader;
	}
}
