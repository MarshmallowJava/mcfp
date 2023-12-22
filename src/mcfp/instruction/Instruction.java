package mcfp.instruction;

import java.io.BufferedWriter;
import java.io.IOException;

import mcfp.INamed;
import mcfp.MCFPClass;
import mcfp.MCFPClassLoader;
import mcfp.MCFPCompiler;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.Version;
import mcfp.util.OrderableMap;
import mcfp.util.Wrapper;

public abstract class Instruction {

	private static OrderableMap<Condition, Supplier> instructions = new OrderableMap<>();

	static {
		instructions.add(InstructionReturn::condition, InstructionReturn::supply);
		instructions.add(InstructionMacro::condition, InstructionMacro::supply);
		instructions.add(InstructionSimple::condition, InstructionSimple::supply);
		instructions.add(InstructionIf::condition, InstructionIf::supply);
		instructions.add(InstructionWhile::condition, InstructionWhile::supply);
		instructions.add(InstructionFunction::condition, InstructionFunction::supply);
		instructions.add(InstructionArithmetic::condition, InstructionArithmetic::supply);
	}

	private INamed parent;
	private MCFPClass caller;

	public Instruction(MCFPClass caller) {
		this.caller = caller;
	}

	public void setParent(INamed parent) {
		this.parent = parent;
	}

	protected INamed getParent() {
		return this.parent;
	}

	public MCFPClass getCaller() {
		return this.caller;
	}

	public void writeCommands(BufferedWriter writer, MCFPCompiler compiler) throws IOException {
		MCFPClassLoader classloader = compiler.getMCFPClassLoader();

		for(String cmd : this.toCommands(classloader, classloader.getNamespace())) {
			writer.write(cmd);
			writer.newLine();
		}
	}

	public abstract String[] toCommands(MCFPClassLoader classloader, Namespace namespace);

	protected String getParentName() {
		return this.parent.getFullName();
	}

	protected INamed getNameHolder() {
		return new INamed() {
			public String getFullName() {
				return getParentName();
			}
		};
	}

	public static Instruction toInstruction(Node<String> node, Version version, MCFPClass caller) {
		String title = node.getData();
		Wrapper<Instruction> wrapper = new Wrapper<Instruction>();

		instructions.foreach((k, v)->{
			if(wrapper.get() == null && k.check(title, version)) {
				wrapper.set(v.supply(node, version, caller));
			}
		});

		return wrapper.get();
	}

	@FunctionalInterface
	static interface Condition{

		public boolean check(String data, Version version);
	}

	@FunctionalInterface
	static interface Supplier{

		public Instruction supply(Node<String> data, Version version, MCFPClass caller);
	}
}
