package mcfp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Version {

	public static final Version MC1_20_2 = new Version("mc1.20.2");

	private String name;
	private String[] commands;

	public Version(String name) {
		this.name = name;
		this.commands = loadData(this.name);
	}

	public boolean isValidCommand(String data) {
		for(String command : commands) {
			if(command.equals(data)) return true;
		}

		return false;
	}

	public String[] getCommandList() {
		return this.commands;
	}

	private static String[] loadData(String name) {
		try(FileReader fr = new FileReader(String.format("versions/%s.txt", name));BufferedReader br = new BufferedReader(fr);){
			List<String> buff = new ArrayList<>();

			String data;
			while((data = br.readLine()) != null) {
				buff.add(data);
			}

			return buff.toArray(new String[0]);
		}catch(IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
