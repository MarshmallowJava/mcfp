package mcfp.main;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mcfp.MCFPCompiler;
import mcfp.Version;

/**
 * Usage <workspaceName>
 */
public class Main {

	private static boolean showDetail = false;

	public static void main(String[] args) {
		if(args.length == 0) return;

		String output = "output";

		int index = 0;
		while(index < args.length) {
			if(args[index].startsWith("-")) {
				String option = args[index].substring(1);
				if(option.equalsIgnoreCase("d")) {
					output = args[++index];
				}
				if(option.equalsIgnoreCase("s")) {
					showDetail = true;
				}
				if(option.equalsIgnoreCase("version")) {
					System.out.println("mcfpc version0.0.1");
					return;
				}
			}else {
				break;
			}

			index++;
		}


		try {
			compile(new File(args[index]), output);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void compile(File folder, String output) throws IOException {
		if(showDetail) System.out.println("target file: " + folder.getAbsolutePath());
		if(showDetail) System.out.println("output file: " + output);

		//コンパイラを起動
		MCFPCompiler compiler = new MCFPCompiler(Version.MC1_20_2);

		//ファイルを追加
		for(File file : getFiles(folder, new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".mcfp") || pathname.isDirectory();
			}
		})) {
			compiler.addFile(file);
			if(showDetail) System.out.println("src file: " + file);
		}

		//コンパイル開始
		compiler.run(output, folder.getAbsolutePath());
	}

	private static List<File> getFiles(File file, FileFilter filter) {
		List<File> list = new ArrayList<>();

		if(file.isFile()) {
			if(showDetail) System.out.println(file.getAbsolutePath() + "is file.");

			list.add(file);
		}else {
			if(showDetail) System.out.println(file.getAbsolutePath() + "is folder.");

			File[] files = file.listFiles();

			if(files != null) {
				for(File sub : files) {
					if(showDetail) System.out.println("checking subfile:" + sub);

					if(filter.accept(sub)) {
						list.addAll(getFiles(sub, filter));
					}
				}
			}else {
				if(showDetail) System.out.println("target folder is empty!");
			}
		}

		return list;
	}
}
