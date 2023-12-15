package mcfp.instruction;

import mcfp.MCFPClass;
import mcfp.MCFPClassLoader;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.Version;

public class InstructionSimple extends Instruction{

	private String data;

	public InstructionSimple(String data, MCFPClass caller) {
		super(caller);
		this.data = data;
	}

	public static boolean condition(String data, Version version) {
		return version.isValidCommand(data.split(" ")[0]);
	}

	public static Instruction supply(Node<String> node, Version version, MCFPClass caller) {
		return new InstructionSimple(node.getData(), caller);
	}

	@Override
	public String[] toCommands(MCFPClassLoader classloader, Namespace namespace) {
		return new String[]{this.data};
	}

	@Override
	public String toString() {
		return this.data;
	}
}
