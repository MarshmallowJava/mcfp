package mcfp.instruction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import mcfp.INamed;
import mcfp.MCFPCompiler;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.SyntaxException;
import mcfp.Version;

public abstract class InstructionBlockable extends Instruction implements INamed{

	protected MCFPDummyFunction contents;
	protected InstructionBlockable parent;

	public InstructionBlockable(Node<String> node, Version version) {
		this.contents = new MCFPDummyFunction();
		for(Node<String> child : node.getChildren()) {
			Instruction instruction = Instruction.toInstruction(child, version);
			if(instruction == null) {
				throw new SyntaxException("Unsolved instruction was found");
			}else {
				this.contents.add(instruction);
				instruction.setParent(this);

				if(instruction instanceof InstructionBlockable) {
					((InstructionBlockable)instruction).parent = this;
				}
			}
		}
	}

	@Override
	public void writeCommands(BufferedWriter writer, MCFPCompiler compiler) throws IOException {
		super.writeCommands(writer, compiler);

		Namespace namespace = compiler.getNamespace();
		String name = namespace.add(this);
		compiler.writeFunction(new File("output/" + name + ".mcfunction"), this.contents);
	}

	@Override
	public String getParentName() {
		if(this.parent == null) {
			return super.getParentName();
		}else {
			return this.parent.getFullName();
		}
	}
}
