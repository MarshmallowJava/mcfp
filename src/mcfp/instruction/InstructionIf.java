package mcfp.instruction;

import java.util.List;

import mcfp.Namespace;
import mcfp.Node;
import mcfp.SyntaxException;
import mcfp.Version;
import mcfp.instruction.arithmetic.Calculator;
import mcfp.instruction.arithmetic.Type;

public class InstructionIf extends InstructionBlockable{

	private List<String> condition;
	private int index;

	public InstructionIf(Node<String> node, Version version) {
		super(node, version);

		String data = node.getData();
		String condition = data.substring(data.indexOf("(") + 1, data.lastIndexOf(")")).trim();
		List<String> formula = Calculator.convert(condition);

		if(Calculator.checkType(formula) == Type.BOOL) {
			this.condition = formula;
			this.index = node.getParent().indexOf(node);
		}else {
			throw new SyntaxException("condition formula type should be bool");
		}
	}

	public static boolean condition(String data, Version version) {
		return data.matches("if\\s*\\(\\s*.+\\s*\\)\\s*:\\s*");
	}

	public static Instruction supply(Node<String> node, Version version) {
		return new InstructionIf(node, version);
	}

	@Override
	public String[] toCommands(Namespace namespace) {
		String[] conditions = Calculator.toCommands(this.condition, "$condition", namespace, this.getNameHolder());
		String[] result = new String[conditions.length + 1];

		for(int i = 0;i < conditions.length;i++) {
			result[i] = conditions[i];
		}
		result[result.length - 1] = String.format("execute if score condition matches 1 run function %s", namespace.add(this));

		return result;
	}

	@Override
	public String toString() {
		return String.format("if");
	}

	@Override
	public String getFullName() {
		return super.getParentName() + ".ifblock" + this.index;
	}
}
