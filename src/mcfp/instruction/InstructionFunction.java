package mcfp.instruction;

import java.util.List;

import mcfp.MCFPClass;
import mcfp.MCFPClassLoader;
import mcfp.MCFPFinder;
import mcfp.MCFPFunction;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.Version;

public class InstructionFunction extends Instruction{

	private FunctionCaller.FunctionInfo funcInfo;

	public InstructionFunction(String data, MCFPClass caller, Version version) {
		super(caller);
		this.funcInfo = FunctionCaller.analyze(data, version);
	}

	public static boolean condition(String data, Version version) {
		return FunctionCaller.isFunction(data);
	}

	public static InstructionFunction supply(Node<String> data, Version version, MCFPClass caller) {
		return new InstructionFunction(data.getData(), caller, version);
	}

	@Override
	public String[] toCommands(MCFPClassLoader classloader, Namespace namespace) {
		MCFPFunction function = MCFPFinder.findFunction(this.funcInfo, this.getCaller());
		List<String> prepare = FunctionCaller.prepare(this.funcInfo, this.getParent(), this.getCaller(), namespace, classloader.getVersion());
		int size = prepare.size();

		String[] commands = new String[size + 1];
		for(int i = 0;i < size;i++) {
			commands[i] = prepare.get(i);
		}
		commands[commands.length - 1] = String.format("function %s", namespace.add(function));

		return commands;
	}
}
