package mcfp.instruction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

import mcfp.INamed;
import mcfp.MCFPClass;
import mcfp.MCFPCompiler;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.SyntaxException;
import mcfp.Version;

public abstract class InstructionBlockable extends Instruction implements INamed{

	protected MCFPDummyFunction contents;

	public InstructionBlockable(Node<String> node, Version version, MCFPClass caller) {
		super(caller);

		this.contents = new MCFPDummyFunction();
		for(Node<String> child : node.getChildren()) {
			Instruction instruction = Instruction.toInstruction(child, version, caller);
			if(instruction == null) {
				throw new SyntaxException("Unsolved instruction was found");
			}else {
				this.contents.add(instruction);
				instruction.setParent(this);
			}
		}
	}

	@Override
	public void writeCommands(BufferedWriter writer, MCFPCompiler compiler) throws IOException {
		super.writeCommands(writer, compiler);

		Namespace namespace = compiler.getMCFPClassLoader().getNamespace();
		String name = namespace.add(this);
		compiler.writeFunction(new File("output/" + name + ".mcfunction"), this.contents);
	}

	public List<Instruction> getInstructions(){
		return this.contents.getInstructions();
	}
}
