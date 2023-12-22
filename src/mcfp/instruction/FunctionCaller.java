package mcfp.instruction;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mcfp.INamed;
import mcfp.MCFPClass;
import mcfp.MCFPFinder;
import mcfp.MCFPFunction;
import mcfp.Namespace;
import mcfp.SyntaxException;
import mcfp.Version;
import mcfp.instruction.arithmetic.Calculator;

/**
 * 関数を解釈し実行します
 * 引数にて数値計算が行われている場合Calculatorクラスを利用します
 */
public class FunctionCaller {

	public static FunctionInfo analyze(String data, Version version) {
		if(!isFunction(data)) throw new SyntaxException(data + ": is not function");
		data = data.trim();

		String name = data.substring(0, data.indexOf("(")).trim();
		List<String> arguments = split(data.substring(data.indexOf("(") + 1, data.length() - 1).trim());
		List<Object> args = new ArrayList<>();

		for(String arg : arguments) {
			if(isFunction(arg)) {
				args.add(analyze(arg, version));
			}else if(arg.matches("[0-9A-Za-z_]+")){
				args.add(arg);
			}else if(Calculator.isFormula(arg, version)){
				args.add(Calculator.convert(arg, version));
			}else {
			}
		}

		return new FunctionInfo(name, args);
	}

	private static List<String> split(String arguments) {
		List<String> list = new ArrayList<>();

		String buff = new String();
		int layer = 0;

		try(StringReader sr = new StringReader(arguments);){

			int data;
			while((data = sr.read()) != -1) {
				if(data == '(') {
					layer++;
				}else if(data == ')') {
					layer--;
				}else if(data == ',' && layer == 0) {
					list.add(buff.trim());
					buff = new String();
					continue;
				}

				buff += (char) data;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

		list.add(buff.trim());

		return list;
	}

	public static boolean isFunction(String data) {
		if(data.matches("\\s*[a-zA-Z][a-zA-Z0-9\\.]*\\s*\\(.*\\)\\s*")) {
			return true;
		}else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> prepare(FunctionInfo info, INamed owner, MCFPClass caller, Namespace namespace, Version version){
		List<String> commands = new ArrayList<>();

		MCFPFunction function = MCFPFinder.findFunction(info, caller);
		String[] varnames = function.getArgumentNames();

		int size = info.getArgumentCount();
		for(int i = 0;i < size;i++) {
			Object arg = info.arguments.get(i);

			//仮引数名
			String varname = varnames[i];
			varname = namespace.add(varname, function);

			//変数もしくは定数
			if(arg instanceof String) {
				String data = arg.toString();
				//定数
				if(data.matches("[0-9]+")) {
					commands.add(String.format("scoreboard players set %s var %s", varname, data));
				}else {
					commands.add(String.format("scoreboard players operation %s var = %s var", varname, namespace.get(namespace.searchLastDefined(data, owner))));
				}
			}

			//関数
			if(arg instanceof FunctionInfo) {
				FunctionInfo info2 = (FunctionInfo)arg;
				MCFPFunction function2 = MCFPFinder.findFunction(info2, caller);

				//呼び出し
				commands.addAll(prepare(info2, owner, caller, namespace, version));
				commands.add(String.format("function %s", namespace.get(function2.getFullName())));

				//結果を代入
				commands.add(String.format("scoreboard players operation %s var = result var", varname));
			}

			//数式
			if(arg instanceof List) {
				commands.addAll(Arrays.asList(Calculator.toCommands((List<String>)arg, "$" + varname, namespace, owner, caller, version)));
			}
		}

		return commands;
	}

	public static class FunctionInfo{

		//変数名
		private String className;
		private String name;

		//引数(Stringなら変数(定数), List<String>なら式, FunctionInfoなら関数)
		private List<Object> arguments = new ArrayList<>();

		public FunctionInfo(String name, List<Object> arguments) {
			if(name.contains(".")) {
				this.className = name.substring(0, name.lastIndexOf("."));
				this.name = name.substring(name.lastIndexOf(".") + 1);
			}else {
				this.name = name;
			}

			this.arguments = arguments;
		}

		public String getFunctionName() {
			return this.name;
		}

		public String getClassName() {
			return this.className;
		}

		public boolean isObviousClassName() {
			return this.className != null;
		}

		public List<Object> getArguments() {
			return this.arguments;
		}

		public int getArgumentCount() {
			return this.arguments.size();
		}

		@Override
		public String toString() {
			String result = this.name + ";" + arguments;
			return result;
		}
	}
}
