package MCP.mod_bindcraft;

import java.util.ArrayList;
import org. lwjgl.input.Keyboard;

public class bcKeyBinding
{
	public bcKeyBinding(String key, String command)
	{
		name = key.toUpperCase();
		code = Keyboard.getKeyIndex(key.toUpperCase());
		
		commands = new ArrayList<String>();
		commands.add(command);
	}
	
	public boolean addCompoundCommand(String command)
	{
		if (isToggle)
			return false;
		
		commands.add(command);
		isCompound = true;
		
		return true;
	}
	
	public boolean addToggleCommand(String command)
	{
		if (isToggle)
			return false;
		
		if (isCompound)
			return false;
		
		commands.add(command);
		isToggle = true;
		
		return true;
	}
	
	public String name;
	public int code;
	public ArrayList<String> commands;
	
	public boolean isCompound;
	public boolean isToggle;
	public boolean isItemSpecific;
	public int itemID;
	
	public int currentCommand;
}