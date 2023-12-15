package mcfp.instruction;

import java.util.ArrayList;
import java.util.List;

import mcfp.MCFPFunction;

public class MCFPDummyFunction extends MCFPFunction{

	private String name;
	private List<Instruction> instructions = new ArrayList<>();

	public MCFPDummyFunction() {
		super("dummy", null);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void add(Instruction instruction) {
		this.instructions.add(instruction);
		instruction.setParent(this);
	}

	@Override
	public List<Instruction> getInstructions(){
		return this.instructions;
	}

	@Override
	public String getFullName() {
		return this.name;
	}
}
