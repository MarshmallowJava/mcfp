package mcfp.instruction;

import mcfp.SyntaxException;
import mcfp.Version;

public class MacroCaller {

	public static MacroInfo analyze(String data, Version version) {
		if(!isMacro(data, version)) throw new SyntaxException("data is not macro");

		return new MacroInfo(data);
	}

	public static boolean isMacro(String data, Version version) {
		if(data.matches("\\$\\(.*\\)")) {
			data = unpack(data);

			if(version.isValidCommand(data.split(" ")[0])) return true;
		}

		return false;
	}

	private static String unpack(String data) {
		return data.substring(2, data.length() - 1);
	}

	public static class MacroInfo {

		private String command;

		public MacroInfo(String data) {
			this.command = unpack(data);
		}

		public String getData() {
			return this.command;
		}
	}

}
