package mcfp;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mcfp.instruction.Instruction;

public class MCFPCompiler {

	private String output;

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
	public void run(String output, String workspace) throws IOException{
		this.output = output;

		//ファイルをロード
		for(File file : this.files) {
			String path = file.getAbsolutePath();
			path = path.replaceAll("\\\\", ".");

			path = path.substring(workspace.length() + 1, path.lastIndexOf("."));
			if(path.contains(".")) path = path.substring(0, path.lastIndexOf("."));

			this.classloader.loadFile(file, path);
		}

		//存在確認をしつつ出力
		for(MCFPClass mcfpClass : this.classloader.getLoadedClasses()) {
			for(MCFPFunction function : mcfpClass.getFunctions()) {
				String name = this.classloader.getNamespace().add(function);
				this.writeFunction(name, function);
			}
		}

		//特にエラーが無かったのですべて出力
		this.flush();

		//データパック情報書き込み
		this.outputAttributes();
	}

	public void writeFunction(String path, MCFPFunction function) {
		File output = new File(this.output  + "/data/minecraft/functions/" + path + ".mcfunction");

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

	private void outputAttributes() throws IOException {

		//pack.mcmeta出力
		this.write("{\"pack\":{\"pack_format\":23,\"description\":\"this datapack was created by mcfp\"}}", "/pack.mcmeta");

		//メイン関数、ロード関数を獲得
		List<String> main = new ArrayList<>();
		List<String> load = new ArrayList<>();

		MCFPClassLoader classloader = this.getMCFPClassLoader();
		Namespace namespace = classloader.getNamespace();
		for(MCFPClass mcfpClass : classloader.getLoadedClasses()) {
			MCFPFunction mainFunction = mcfpClass.getFunction("main");
			MCFPFunction loadFunction = mcfpClass.getFunction("load");

			if(mainFunction != null && mainFunction.getArgumentNames().length == 0) {
				main.add(namespace.get(mainFunction.getFullName()));
			}

			if(loadFunction != null && loadFunction.getArgumentNames().length == 0) {
				load.add(namespace.get(loadFunction.getFullName()));
			}
		}

		//tagsに記述
		this.write(String.format("{\"values\":%s}", main), "/data/minecraft/tags/functions/tick.json");
		this.write(String.format("{\"values\":%s}", load), "/data/minecraft/tags/functions/load.json");
	}

	private void write(String data, String path) throws IOException {
		File file = new File(this.output + path);
		file.getParentFile().mkdirs();

		try (FileWriter fw = new FileWriter(file);BufferedWriter bw = new BufferedWriter(fw);){
			bw.write(data);
			bw.flush();
		}catch(IOException e) {
			throw e;
		}
	}

	public MCFPClassLoader getMCFPClassLoader() {
		return this.classloader;
	}
}
