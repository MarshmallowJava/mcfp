package mcfp.instruction;

import java.util.List;

import mcfp.MCFPClass;
import mcfp.MCFPClassLoader;
import mcfp.Namespace;
import mcfp.SyntaxException;
import mcfp.instruction.arithmetic.Calculator;
import mcfp.instruction.arithmetic.Type;

public class InstructionGoto extends Instruction{

	private InstructionBlockable target;
	private List<String> condition;

	public InstructionGoto(InstructionBlockable target, String condition, MCFPClass caller) {
		super(caller);

		this.target = target;
		this.condition = Calculator.convert(condition);

		if(Calculator.checkType(this.condition) != Type.BOOL) {
			throw new SyntaxException("this area should be bool.");
		}
	}

	@Override
	public String[] toCommands(MCFPClassLoader classloader, Namespace namespace) {
		String[] condition = Calculator.toCommands(this.condition, namespace, this.target, this.getCaller());
		String[] result = new String[condition.length + 1];

		for(int i = 0;i < condition.length;i++) {
			result[i] = condition[i];
		}
		result[result.length - 1] = String.format("execute if score temp var matches 1 run function %s", namespace.add(this.target));

		return result;
	}
}
