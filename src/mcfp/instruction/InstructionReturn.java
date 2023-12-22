package mcfp.instruction;

import java.util.List;

import mcfp.MCFPClass;
import mcfp.MCFPClassLoader;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.Version;
import mcfp.instruction.arithmetic.Calculator;

public class InstructionReturn extends Instruction{

	private List<String> formula;

	public InstructionReturn(String data, MCFPClass caller, Version version) {
		super(caller);
		this.formula = Calculator.convert(data.substring("return".length()).trim(), version);
	}

	public static boolean condition(String data, Version version) {
		return data.matches("return\\s+.*");
	}

	public static Instruction supply(Node<String> data, Version version, MCFPClass caller) {
		return new InstructionReturn(data.getData(), caller, version);
	}

	@Override
	public String[] toCommands(MCFPClassLoader classloader, Namespace namespace) {
		return Calculator.toCommands(formula, "$result", namespace, this.getNameHolder(), this.getCaller(), classloader.getVersion());
	}
}
