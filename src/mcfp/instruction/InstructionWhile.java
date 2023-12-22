package mcfp.instruction;

import java.util.List;

import mcfp.MCFPClass;
import mcfp.MCFPClassLoader;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.SyntaxException;
import mcfp.Version;
import mcfp.instruction.arithmetic.Calculator;
import mcfp.instruction.arithmetic.Type;

public class InstructionWhile extends InstructionBlockable{

	private List<String> condition;
	private int index;

	public InstructionWhile(Node<String> node, Version version, MCFPClass caller) {
		super(node, version, caller);

		String data = node.getData();
		String condition = data.substring(data.indexOf("(") + 1, data.lastIndexOf(")")).trim();
		List<String> formula = Calculator.convert(condition, version);

		if(Calculator.checkType(formula, version) == Type.BOOL) {
			this.condition = formula;
			this.index = node.getParent().indexOf(node);
			this.contents.add(new InstructionGoto(this, condition, caller, version));
		}else {
			throw new SyntaxException("condition formula type should be bool");
		}
	}

	public static boolean condition(String data, Version version) {
		return data.matches("while\\s*\\(\\s*.+\\s*\\)\\s*:\\s*");
	}

	public static Instruction supply(Node<String> node, Version version, MCFPClass caller) {
		return new InstructionWhile(node, version, caller);
	}

	@Override
	public String[] toCommands(MCFPClassLoader classloader, Namespace namespace) {
		String[] conditions = Calculator.toCommands(this.condition, "$condition", namespace, this.getNameHolder(), this.getCaller(), classloader.getVersion());
		String[] result = new String[conditions.length + 1];

		for(int i = 0;i < conditions.length;i++) {
			result[i] = conditions[i];
		}
		result[result.length - 1] = String.format("execute if score condition matches 1 run function %s", namespace.add(this));

		return result;
	}

	@Override
	public String toString() {
		return String.format("while");
	}

	@Override
	public String getFullName() {
		return super.getParentName() + ".whileblock" + this.index;
	}
}
