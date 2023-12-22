package mcfp.instruction;

import mcfp.MCFPClass;
import mcfp.MCFPClassLoader;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.Version;
import mcfp.instruction.arithmetic.Calculator;

public class InstructionArithmetic extends Instruction{

	private String to, formula;

	public InstructionArithmetic(String to, String formula, MCFPClass caller) {
		super(caller);
		this.to = to;
		this.formula = formula;
	}

	public static boolean condition(String data, Version version) {
		try {
			Calculator.checkType(data.substring(data.indexOf("=") + 1), version);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Instruction supply(Node<String> node, Version version, MCFPClass caller) {
		String data = node.getData();
		String to = data.substring(0, data.indexOf("=")).trim();
		String formula = data.substring(data.indexOf("=") + 1);

		return new InstructionArithmetic(to, formula, caller);
	}

	@Override
	public String[] toCommands(MCFPClassLoader classloader, Namespace namespace) {
		return Calculator.toCommands(this.formula, this.to, namespace, this.getNameHolder(), this.getCaller(), classloader.getVersion());
	}

	@Override
	public String toString() {
		return "arithmetic";
	}
}
