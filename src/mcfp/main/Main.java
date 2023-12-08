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

	public static void main(String[] args) {
		try {
			compile(new File("mcfpsrc"));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void compile(File folder) throws IOException {
		MCFPCompiler compiler = new MCFPCompiler(Version.MC1_20_2);

		for(File file : getFiles(folder, new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".mcfp");
			}
		})) {
			compiler.addFile(file);
		}

		compiler.run(folder.getName());
	}

	public static List<File> getFiles(File file, FileFilter filter) {
		List<File> list = new ArrayList<>();

		if(file.isFile()) {
			list.add(file);
		}else {
			for(File sub : file.listFiles(filter)) {
				list.addAll(getFiles(sub, filter));
			}
		}

		return list;
	}
}
