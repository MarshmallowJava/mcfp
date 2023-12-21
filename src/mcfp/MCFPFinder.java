package mcfp;

import java.util.ArrayList;
import java.util.List;

import mcfp.instruction.FunctionCaller.FunctionInfo;

public class MCFPFinder {

	/**
	 * 与えられた情報を用いてcaller目線から関数を検索します
	 * このときオーバーロードに対応します
	 */
	public static MCFPFunction findFunction(FunctionInfo info, MCFPClass caller) {
		MCFPClassLoader classloader = caller.getMCFPClassLoader();

		//クラス名が指定されているか
		if(info.isObviousClassName()) {
			String className = info.getClassName();

			//thisは呼び出しクラスで確定
			if(className.equals("this")) {
				return searchFunction(info.getFunctionName(), info.getArgumentCount(), caller);
			}else {
				MCFPClass mcfpClass = classloader.getClassByFullName(info.getClassName());
				return searchFunction(info.getFunctionName(), info.getArgumentCount(), mcfpClass);
			}
		//クラス名が確定していないので検索
		}else {
			String funcName = info.getFunctionName();
			MCFPFunction result = null;

			//まずは呼び出しクラスから検索
			result = searchFunction(funcName, info.getArgumentCount(), caller);
			if(result != null) return result;

			//インポート先から検索
			for(MCFPClass mcfpClass : getImportedClasses(caller)) {
				result = searchFunction(funcName, info.getArgumentCount(), mcfpClass);
				if(result != null) return result;
			}

			throw new SyntaxException(info.getFunctionName() + "(" + info.getArgumentCount() + ") is not defined");
		}
	}

	private static MCFPFunction searchFunction(String name, int args, MCFPClass mcfpClass) {
		for(MCFPFunction function : mcfpClass.getFunctions()) {
			if(function.getName().equals(name) && function.getArgumentNames().length == args) {
				return function;
			}
		}

		throw new SyntaxException(name + "(" + args + ") is not defined");
	}

//	public static MCFPClass findClass(String name, MCFPClass caller) {
//		MCFPClassLoader classloader = caller.getClassLoader();
//
//		String[] nameList = splitName(name);
//		String namespace = nameList[0];
//		String className = nameList[1];
//
//
//	}

	private static List<MCFPClass> getImportedClasses(MCFPClass mcfpClass){
		List<MCFPClass> list = new ArrayList<>();

		MCFPClassLoader classloader = mcfpClass.getMCFPClassLoader();
		for(String name : mcfpClass.getImportSource()) {
			String[] nameList = splitName(name);
			String namespace = nameList[0];
			String className = nameList[1];

			if(className.equals("*")) {
				list.addAll(classloader.getClassesByNamespace(namespace));
			}else {
				MCFPClass c = classloader.getClassByFullName(name);
				if(c == null) {
					throw new SyntaxException(name + ": is not defined");
				}else {
					list.add(c);
				}
			}
		}

		return list;
	}

	private static String[] splitName(String name) {
		String[] result = new String[2];

		if(name.contains(".")) {
			int index = name.lastIndexOf(".");

			result[0] = name.substring(0, index);
			result[1] = name.substring(index + 1);
		}else {
			result[0] = new String();
			result[1] = name;
		}

		return result;
	}
}
