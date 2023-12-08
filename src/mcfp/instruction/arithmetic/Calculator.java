package mcfp.instruction.arithmetic;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import mcfp.INamed;
import mcfp.Namespace;
import mcfp.SyntaxException;

public class Calculator {

	private static final Map<String, Integer> PRIORITIES = new HashMap<>();
	private static final int TYPE_STRING = 0;
	private static final int TYPE_FORMULA = 1;
	private static final int TYPE_COMPARE = 2;

	static {
		PRIORITIES.put("=", 0);
		PRIORITIES.put("&", 0);
		PRIORITIES.put("|", 0);
		PRIORITIES.put("<", 1);
		PRIORITIES.put(">", 1);
		PRIORITIES.put("<=", 1);
		PRIORITIES.put(">=", 1);
		PRIORITIES.put("==", 1);
		PRIORITIES.put("!=", 1);
		PRIORITIES.put("+", 2);
		PRIORITIES.put("-", 2);
		PRIORITIES.put("*", 3);
		PRIORITIES.put("/", 3);
		PRIORITIES.put("%", 3);
	}

	//逆ポーランド記法に変換します
	public static List<String> convert(String formula){
		List<String> src = new ArrayList<>();

		final String pattern = "\\(|\\)|\\+|\\-|\\*|\\/|%|<|>|=|!|&|\\||[0-9A-Za-z_]+";

		try(Scanner scanner = new Scanner(new StringReader(formula));){
			scanner.useDelimiter("\\s*");

			while(true) {
				if(scanner.hasNext(pattern)) {
					src.add(scanner.next(pattern).trim());
				}else {
					break;
				}
			}
		}catch(Exception e) {
			throw new SyntaxException("expression is invalid");
		}

		//combine
		{
			List<String> sorted = new ArrayList<>();
			String buff = null;
			int type = -1;

			for(String token : src) {
				int tokenType = token.matches("[a-zA-Z0-9]") ? TYPE_STRING : token.matches("[<>!=]") ? TYPE_COMPARE : TYPE_FORMULA;

				if(buff == null) {
					buff = token;
					type = tokenType;
				}else {
					if(token.equals("=") && type == TYPE_COMPARE) {
						sorted.add(buff + "=");
						buff = null;
						type = -1;
					}else if(tokenType == TYPE_FORMULA || tokenType != type) {
						sorted.add(buff);
						buff = token;
						type = tokenType;
					}else {
						buff += token;
					}
				}
			}
			sorted.add(buff);
			src = sorted;
		}

		List<String> output = new ArrayList<>();
		Stack<String> stack = new Stack<>();

		for(String token : src) {
			if(token.matches("[0-9A-Za-z_]+")) {
				output.add(token);
			}else if(token.equals("(")) {
				stack.push(token);
			}else if(token.equals(")")) {
				while(!stack.empty()) {
					String data = stack.pop();
					if(data.equals("(")) {
						break;
					}else {
						output.add(data);
					}
				}
			}else if(PRIORITIES.containsKey(token)) {
				while(!stack.empty() && isHigher(stack.firstElement(), token)) {
					String data = stack.pop();
					if(data.equals("(")) {
						break;
					}else {
						output.add(data);
					}
				}
				stack.push(token);
			}
		}

		while(!stack.empty()) {
			output.add(stack.pop());
		}

		return output;
	}

	private static boolean isHigher(String op0, String op1) {
		if(PRIORITIES.containsKey(op0) && PRIORITIES.containsKey(op1)) {
			return PRIORITIES.get(op0) > PRIORITIES.get(op1);
		}else {
			return false;
		}
	}

	public static Type checkType(String formula) {
		return checkType(convert(formula));
	}

	public static Type checkType(List<String> formula) {
		Stack<Object> stack = new Stack<>();

		for(String token : formula) {
			if(isValidFormula(token)) {
				Type right = getType(stack.pop());
				Type left = getType(stack.pop());
				Type resultType = getType2(token);

				if(token.matches("&|\\|") && (right != Type.BOOL || left != Type.BOOL)) {
					throw new SyntaxException("'and' and 'or' can be used for bool types.");
				}
				if(right == left) {
					stack.push(resultType);
				}else {
					throw new SyntaxException("it cannot compare between different type values");
				}
			}else {
				stack.push(token);
			}
		}
		return getType(stack.pop());
	}

	private static Type getType(Object obj) {
		if(obj instanceof Type) return ((Type)obj);

		String token = (String) obj;
		if(token.matches("[a-zA-Z0-9]+")) return Type.INTEGER;

		throw new SyntaxException(token + ": is not formula elememt");
	}

	private static Type getType2(Object obj) {
		String token = (String) obj;
		if(token.matches("[+\\-*/%=]")) return Type.INTEGER;
		if(token.matches("<|>|==|!=|>=|<=|&|\\|")) return Type.BOOL;

		throw new SyntaxException(token + ": is not formula elememt");
	}

	private static boolean isValidFormula(String token) {
		return PRIORITIES.containsKey(token);
	}

	public static String[] toCommands(String formula, Namespace namespace, INamed holder) {
		return toCommands(convert(formula), namespace, holder);
	}

	public static String[] toCommands(String formula, String resultName, Namespace namespace, INamed holder) {
		return toCommands(convert(formula), resultName, namespace, holder);
	}

	public static String[] toCommands(List<String> formula, Namespace namespace, INamed holder) {
		return toCommands(formula, "$temp", namespace, holder);
	}

	/**
	 * 指定した逆ポーランド式をコマンドに変換します
	 * 最終的な式が整数値であればtemp[var]に格納
	 * 最終的な式が真偽値であればtemp[var]に真であれば1, 偽であれば0が格納される
	 */
	public static String[] toCommands(List<String> formula, String resultName, Namespace namespace, INamed holder) {
		List<String> result = new ArrayList<>();
		List<Integer> reserveList = new ArrayList<>();

		Stack<Object> stack = new Stack<>();
		for(String token : formula) {
			if(isValidFormula(token)) {
				Type type = getType2(token);

				if(type == Type.INTEGER) {
					if(token.equals("=")) {
						String right = popValue(stack, reserveList, namespace, holder);
						String left = null;

						Object obj = stack.pop();
						if(obj instanceof Integer) {
							throw new SyntaxException("value cannot contains value");
						}else {
							stack.push(obj);
							left = popValue(stack, reserveList, namespace, holder);
						}

						if(right.matches("[0-9]+")) {
							result.add(String.format("scoreboard players set %s var %s", left, right));
						}else {
							result.add(String.format("scoreboard players operation %s var = %s var", left, right));
						}

						stack.push(obj);
					}else {
						int tempId = reserveTemp(reserveList);
						String right = popValue(stack, reserveList, namespace, holder);
						String left = popValue(stack, reserveList, namespace, holder);

						if(left.matches("[0-9]+")) {
							result.add(String.format("scoreboard players set temp%08x var %s", tempId, left));
						}else {
							result.add(String.format("scoreboard players operation temp%08x var = %s var", tempId, left));
						}

						if(right.matches("[0-9]+")) {
							if(token.equals("+")) {
								result.add(String.format("scoreboard players add temp%08x var %s", tempId, right));
							}else if(token.equals("-")){
								result.add(String.format("scoreboard players remove temp%08x var %s", tempId, right));
							}else {
								result.add(String.format("scoreboard players set temp var %s", right));
								result.add(String.format("scoreboard players operation temp%08x var %s= temp var", tempId, token));
							}
						}else {
							result.add(String.format("scoreboard players operation temp%08x var %s= %s var", tempId, token, right));
						}

						stack.push(tempId);
					}
				}else if(type == Type.BOOL){
					int tempId = reserveTemp(reserveList);
					String right = popValue(stack, reserveList, namespace, holder);
					String left = popValue(stack, reserveList, namespace, holder);

					if(left.matches("[0-9]+")) {
						result.add(String.format("scoreboard players left var %s", left));
						left = "left";
					}
					if(right.matches("[0-9]+")) {
						result.add(String.format("scoreboard players right var %s", right));
						right = "right";
					}

					if(token.equals("!=")) {
						result.add(String.format("execute store result score temp%08x var unless score %s var = %s var", tempId, left, right));
					}else {
						if(token.equals("==")) token = "=";
						result.add(String.format("execute store result score temp%08x var if score %s var %s %s var", tempId, left, token, right));
					}

					stack.push(tempId);
				}
			}else {
				stack.push(token);
			}
		}

		resultName = convertName(resultName, namespace, holder);

		Object lastData = stack.pop();
		if(lastData instanceof String) {
			String value = lastData.toString();
			if(value.matches("[0-9]+")) {
				result.add(String.format("scoreboard players set %s var %s", resultName, lastData));
			}else {
				result.add(String.format("scoreboard players operation %s var = %s var", resultName, convertName(lastData.toString(), namespace, holder)));
			}
		}else {
			result.add(String.format("scoreboard players operation %s var = temp%08x var", resultName, lastData));
		}


		return result.toArray(new String[0]);
	}

	private static String popValue(Stack<Object> stack, List<Integer> reserveList, Namespace namespace, INamed holder) {
		Object obj = stack.pop();
		if(obj instanceof Integer) {
			int value = (int) obj;
			reserveList.remove((Integer)value);

			return String.format("temp%08x", value);
		}
		String str = obj.toString();

		if(str.matches("[0-9]+")){
			return str;
		}else {
			return convertName(str, namespace, holder);
		}
	}

	private static String convertName(String name, Namespace namespace, INamed holder) {
		String fqvn = holder.getFullName() + "." + name;
		String result = namespace.searchLastDefined(fqvn);

		if(result == null) {
			return namespace.add(name, holder);
		}else {
			return namespace.get(result);
		}
	}


	private static int reserveTemp(List<Integer> reserveList) {
		for(int i = 0;;i++) {
			if(!reserveList.contains(i)) {
				reserveList.add(i);
				return i;
			}
		}
	}
}