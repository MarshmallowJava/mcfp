package mcfp;

import java.util.ArrayList;
import java.util.List;

import mcfp.instruction.Instruction;

public class MCFPFunction implements INamed{

	private final String name;
	private final String[] args;

	private final List<Instruction> instructions = new ArrayList<>();

	private MCFPClass parentClass;

	public MCFPFunction(String name, String[] args) {
		this.name = name;
		this.args = args;
	}

	void setParentClass(MCFPClass parentClass) {
		this.parentClass = parentClass;
	}

	void addInstruction(Instruction instruction) {
		this.instructions.add(instruction);
		instruction.setParent(this);
	}

	public String getName() {
		return this.name;
	}

	public String[] getArgumentNames() {
		return this.args;
	}

	public List<Instruction> getInstructions(){
		return this.instructions;
	}

	@Override
	public String getFullName() {
		return this.parentClass.getFullName() + ".f" + this.name;
	}

	@Override
	public String toString() {
		return this.parentClass.getFQCN() + "::" + this.name;
	}

}
