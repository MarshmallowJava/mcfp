package mcfp.instruction;

import mcfp.Namespace;
import mcfp.Node;
import mcfp.Version;
import mcfp.instruction.arithmetic.Calculator;

public class InstructionArithmetic extends Instruction{

	private String to, formula;

	public InstructionArithmetic(String to, String formula) {
		this.to = to;
		this.formula = formula;
	}

	public static boolean condition(String data, Version version) {
		if(data.matches("[a-zA-z][0-9a-zA-Z]*\\s*[+\\-*/%]?=\\s*[0-9a-zA-z+\\-*/%()<>=!\\s]+")) {
			try {
				Calculator.checkType(data.substring(data.indexOf("=") + 1));
				return true;
			}catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}else {
			return false;
		}
	}

	public static Instruction supply(Node<String> node, Version version) {
		String data = node.getData();
		String to = data.substring(0, data.indexOf("=")).trim();
		String formula = data.substring(data.indexOf("=") + 1);

		return new InstructionArithmetic(to, formula);
	}

	@Override
	public String[] toCommands(Namespace namespace) {
		return Calculator.toCommands(this.formula, this.to, namespace, this.getNameHolder());
	}

	@Override
	public String toString() {
		return "arithmetic";
	}
}
