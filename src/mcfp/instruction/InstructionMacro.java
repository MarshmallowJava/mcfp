package mcfp.instruction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mcfp.INamed;
import mcfp.MCFPClass;
import mcfp.MCFPClassLoader;
import mcfp.MCFPCompiler;
import mcfp.Namespace;
import mcfp.Node;
import mcfp.SyntaxException;
import mcfp.Version;

public class InstructionMacro extends Instruction implements INamed{

	private String data;
	private List<String> variables = new ArrayList<>();

	private int index;

	public InstructionMacro(Node<String> node, MCFPClass caller) {
		super(caller);

		this.data = node.getData();
		this.index = node.getParent().indexOf(node);

		int size = this.data.length();
		for(int i = 0;i < size;i++) {
			char ch = this.data.charAt(i);

			if(ch == '$') {
				if(i < size - 1) {
					char ch1 = this.data.charAt(i + 1);

					if(ch1 == '(') {
						i++;

						String name = new String();
						while((ch1 = this.data.charAt(++i)) != ')') {
							name += ch1;
						}

						this.variables.add(name);
					}
				}
			}
		}
	}

	public static boolean condition(String data, Version version) {
		if(!version.isValidCommand(data.split(" ")[0])) return false;

		int count = 0;
		for(int i = 0;i < data.length() + 1;i++) {
			if(i < data.length() && data.charAt(i) == '$') {
				count++;
			}else {
				if(count % 2 == 1) return true;
			}
		}

		return false;
	}

	public static Instruction supply(Node<String> node, Version version, MCFPClass caller) {
		return new InstructionMacro(node, caller);
	}

	@Override
	public void writeCommands(BufferedWriter writer, MCFPCompiler compiler) throws IOException {
		super.writeCommands(writer, compiler);

		Namespace namespace = compiler.getMCFPClassLoader().getNamespace();
		String name = namespace.add(this);

		MCFPDummyFunction func = new MCFPDummyFunction();
		func.add(new InstructionSimple("$" + this.data, this.getCaller()));

		compiler.writeFunction(name, func);
	}

	@Override
	public String[] toCommands(MCFPClassLoader classloader, Namespace namespace) {
		int size = this.variables.size();
		String[] result = new String[size + 2];

		result[0] = "data modify storage temp: _ set value {}";
		for(int i = 0;i < size;i++) {
			String name = this.variables.get(i);
			String fqvn = namespace.searchLastDefined(this.getFullName() + "." + name);

			if(fqvn == null) {
				throw new SyntaxException("variable was not defined: " + name);
			}else {
				result[i + 1] = String.format("execute store result storage temp: _.%s int 1 run scoreboard players get %s var", name, namespace.get(fqvn));
			}
		}

		result[result.length - 1] = String.format("function %s with storage temp: _", namespace.add(this));

		return result;
	}

	@Override
	public String toString() {
		return "macro: " + this.data;
	}

	@Override
	public String getFullName() {
		return this.getParentName() + ".macroblock" + this.index;
	}
}
