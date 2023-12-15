package mcfp.instruction;

import mcfp.INamed;
import mcfp.MCFPClass;
import mcfp.MCFPClassLoader;
import mcfp.MCFPFunction;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.SyntaxException;
import mcfp.Version;

public class InstructionFunction extends Instruction{

	private FunctionCaller.FunctionInfo funcInfo;

	public InstructionFunction(String data, MCFPClass caller) {
		super(caller);
		this.funcInfo = FunctionCaller.analyze(data);
	}

	public static boolean condition(String data, Version version) {
		return FunctionCaller.isFunction(data);
	}

	public static InstructionFunction supply(Node<String> data, Version version, MCFPClass caller) {
		return new InstructionFunction(data.getData(), caller);
	}

	@Override
	public String[] toCommands(MCFPClassLoader classloader, Namespace namespace) {
		String name = namespace.searchLastDefined(this.getParentName() + ".f" + this.funcInfo.getFunctionName());

		if(name == null) {
			throw new SyntaxException(this.funcInfo.getFunctionName() + " is not defined");
		}else {
			INamed owner = this.getParent();
			while(!(owner instanceof MCFPFunction)) {
				if(owner instanceof InstructionBlockable) {
					owner = ((InstructionBlockable) owner).getParent();
				}
			}

			name = namespace.get(name);
			return new String[] {"function " + name};
		}
	}

}
