package mcfp.instruction;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import mcfp.SyntaxException;
import mcfp.instruction.arithmetic.Calculator;

/**
 * 関数を解釈し実行します
 * 引数にて数値計算が行われている場合Calculatorクラスを利用します
 */
public class FunctionCaller {

	public static FunctionInfo analyze(String data) {
		if(!isFunction(data)) throw new SyntaxException(data + ": is not function");
		data = data.trim();

		String name = data.substring(0, data.indexOf("(")).trim();
		List<String> arguments = split(data.substring(data.indexOf("(") + 1, data.length() - 1).trim());
		List<Object> args = new ArrayList<>();

		for(String arg : arguments) {
			if(isFunction(arg)) {
				args.add(analyze(arg));
			}else if(arg.matches("[0-9A-Za-z_]+")){
				args.add(arg);
			}else if(Calculator.isFormula(arg)){
				args.add(Calculator.convert(arg));
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

		@Override
		public String toString() {
			String result = this.name + ";" + arguments;
			return result;
		}
	}
}
