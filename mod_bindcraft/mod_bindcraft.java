package MCP.mod_bindcraft;

import MCP.ApiController;
import MCP.overrides.ApiCore;
import MCP.Mod;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;

import java.util.List;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.lang.reflect.*;

import net.minecraft.src.*;

public class mod_bindcraft extends Mod
{
	public mod_bindcraft(ApiController ctrl)
	{
		super(ctrl);
		mc = (ApiCore)ctrl.mc();
	}
	
	@Override
	public String getModName()
	{
		return "BindCraft";
	}
	
	@Override
	public String getModAuthor()
	{
		return "PrettyPony";
	}
	
	@Override
	public String getModDescription()
	{
		return "A binding mod for Minecraft";
	}
	
	@Override
	public String getModVersion()
	{
		return "1.5.2";
	}
	
	@Override
	public String getModSystemVersion()
	{
		return "1.4";
	}
	
	@Override
	public String getMinecraftVersion()
	{
		return "1.5_01";
	}
	
	@Override
	public boolean onMouseButtonPress(int button, boolean pressed)
	{	
		if (pressed)
		{
			System.out.println("Button ID: " + button);
			
			for (int i = 0; i < mouseBinds.size(); i++)
			{
				if (mouseBinds.get(i).name.compareTo("" + button) == 0)
				{
					if (mouseBinds.get(i).isItemSpecific)
					{
						int itemID;
						
						itemID = mc.thePlayer.getCurrentEquippedItem().itemID;
						
						if (mouseBinds.get(i).itemID == itemID)
						{
							if (mouseBinds.get(i).itemID == itemID)
							{
								for (int j = 0; j < mouseBinds.get(i).commands.size(); j++)
								{
									mc.thePlayer.sendChatMessage(mouseBinds.get(i).commands.get(j));
								}
							}
						}
					}
					else
					{
						for (int j = 0; j < mouseBinds.get(i).commands.size(); j++)
						{
							mc.thePlayer.sendChatMessage(mouseBinds.get(i).commands.get(j));
						}
					}
				}
			}
		}
		
		return false;
	}
	
	ItemStack[] getRecipeInput(IRecipe recipe)
	{
		ItemStack[] stack = null;
		
		try
		{		
			if (recipe instanceof ShapedRecipes)
			{
				Field field = ((ShapedRecipes)recipe).getClass().getDeclaredField("d");
				field.setAccessible(true);
				stack = (ItemStack[])field.get((ShapedRecipes)recipe);
				field.setAccessible(false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return stack;
	}
	
	int getRecipeWidth(IRecipe recipe)
	{
		int width = -1;
		
		try
		{
			if (recipe instanceof ShapedRecipes)
			{
				Field field = ((ShapedRecipes)recipe).getClass().getDeclaredField("b");
				field.setAccessible(true);
				width = ((Integer)field.get((ShapedRecipes)recipe)).intValue();
				field.setAccessible(false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return width;
	}
	
	int getRecipeHeight(IRecipe recipe)
	{
		int height = -1;
		
		try
		{
			if (recipe instanceof ShapedRecipes)
			{
				Field field = ((ShapedRecipes)recipe).getClass().getDeclaredField("c");
				field.setAccessible(true);
				height = ((Integer)field.get((ShapedRecipes)recipe)).intValue();
				field.setAccessible(false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return height;
	}
	
	@Override
	public void onTick()
	{
		try
		{
			if (mc.currentScreen instanceof GuiCrafting)
				{
				for (int i = 0; i < craftingBinds.size(); i++)
				{
					if (Keyboard.isKeyDown(craftingBinds.get(i).code) && timer == 0)
					{
						for (int j = 0; j < craftingBinds.get(i).commands.size(); j++)
						{
							int itemID = -1;
							
							try
							{
								itemID = Integer.parseInt(craftingBinds.get(i).commands.get(j));
							}
							catch (Exception e)
							{
								System.out.println("Item ID " + craftingBinds.get(i).commands.get(j) + " is not valid.");
							}
							
							craftItem(itemID);
							
							timer = 5;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (timer > 0)
			timer--;
	}
	
	void craftItem(int itemID)
	{
		List recipes = CraftingManager.getInstance().func_25193_b();
		
		for (int i = 0; i < recipes.size(); i++)
		{
			if (recipes.get(i) instanceof ShapedRecipes)
			{
				ShapedRecipes sr = (ShapedRecipes)recipes.get(i);
				
				if (sr.func_25117_b().itemID == itemID)
				{
					ItemStack[] input = getRecipeInput((IRecipe)recipes.get(i));
					fillCraftTable(input, getRecipeWidth((IRecipe)recipes.get(i)), getRecipeHeight((IRecipe)recipes.get(i)));
					Container c = ((GuiContainer)mc.currentScreen).inventorySlots;
					
					if (c.getSlot(0).getStack() != null)
					{
						for (int j = 10; j <= 45; j++)
						{
							Slot slot = c.getSlot(j);
							
							if (slot.getStack() != null)
							{
								ItemStack stack = slot.getStack();
								
								if (stack.itemID == itemID)
								{
									if (stack.stackSize < stack.getMaxStackSize())
									{
										sendClick(c, 0, 0);
										sendClick(c, j, 0);
										return;
									}
								}
							}
						}
						
						for (int j = 10; j <= 45; j++)
						{
							Slot slot = c.getSlot(j);
							
							if (c.getSlot(j).getStack() == null)
							{
								sendClick(c, 0, 0);
								sendClick(c, j, 0);
							}
						}
					}
				}
			}
		}
	}
	
	void fillCraftTable(ItemStack[] input, int width, int height)
	{
		for (int y = 1; y <= height; y++)
		{
			for (int x = 1; x <= width; x++)
			{
				int slotID = x + (3 * (y - 1));
				int index = (x + (width * (y - 1))) - 1;
				
				if (input[index] != null)
				{
					fillCraftSlot(slotID, input[index].itemID);
				}
			}
		}
	}

	void fillCraftSlot(int slotID, int itemID)
	{
		Container c = ((GuiContainer)mc.currentScreen).inventorySlots;
		
		for (int i = 10; i <= 45; i++)
		{
			Slot slot = c.getSlot(i);
			
			if (slot.getStack() != null)
			{			
				if (slot.getStack().itemID == itemID)
				{
					sendClick(c, i, 0);
					sendClick(c, slotID, 1);
					sendClick(c, i, 0);
				}
			}
		}
	}
	
	@Override
	public boolean onKeyPress(int key, boolean pressed)
	{
		if (pressed)
			handleBinds(key);
		
		return false;
	}
	
	@Override
	public void onMinecraftStarted()
	{
		log("*** INITIALIZING BINDCRAFT ***");
		
		craftingBinds = new ArrayList<bcKeyBinding>();
		mouseBinds = new ArrayList<bcKeyBinding>();
		startupBinds = new ArrayList<bcKeyBinding>();
		disconnectBinds = new ArrayList<bcKeyBinding>();
		keyBindings = new ArrayList<bcKeyBinding>();
		
		File file = new File(mc.getMinecraftDir(), "keyBinds.txt");
		
		if (!file.exists())
		{
			return;
		}
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String s = reader.readLine();
			while (s != null)
			{
				String[] chunks = s.split(" ");
				
				if (chunks[0].compareToIgnoreCase("add") == 0)
				{
					String key = chunks[1];
					String command = "";
					
					for (int i = 2;i < chunks.length; i++)
					{
						command = command + chunks[i] + " ";
					}
					
					command.trim();
					
					for (int i = 0; i < keyBindings.size(); i++)
					{
						if (key.compareToIgnoreCase(keyBindings.get(i).name) == 0)
						{
							keyBindings.get(i).addCompoundCommand(command);
						}
					}
				}
				else if (chunks[0].compareToIgnoreCase("toggle") == 0)
				{
					String key = chunks[1];
					String command = "";
					
					for (int i = 2; i < chunks.length; i++)
					{
						command = command + chunks[i] + " ";
					}
					
					command.trim();
					
					for (int i = 0; i < keyBindings.size(); i++)
					{
						if (key.compareToIgnoreCase(keyBindings.get(i).name) == 0)
						{
							keyBindings.get(i).addToggleCommand(command);
						}
					}
				}
				else if (chunks[0].compareToIgnoreCase("startup") == 0)
				{
					String command = "";
					
					for (int i = 1; i < chunks.length; i++)
					{
						command = command + chunks[i] + " ";
					}
					
					command.trim();
					
					startupBinds.add(new bcKeyBinding("startup", command));
					log("*** ADDED BIND FOR STARTUP");
				}
				else if (chunks[0].compareToIgnoreCase("disconnect") == 0)
				{
					String command = "";
					
					for (int i = 1; i < chunks.length; i++)
					{
						command = command + chunks[i] + " ";
					}
					
					command.trim();
					
					disconnectBinds.add(new bcKeyBinding("disconnect", command));
					log("*** ADDED BIND FOR DISCONNECT");
				}
				else if (chunks[0].compareToIgnoreCase("mouse") == 0)
				{
					if (chunks[1].compareToIgnoreCase("item") == 0)
					{
						String item = chunks[2];
						String button = chunks[3];
						String c = "";
						
						for (int i = 4; i < chunks.length; i++)
						{
							c = c + chunks[i] + " ";
						}
						
						c.trim();
						
						for (int i = 0; i < mouseBinds.size(); i++)
						{
							if (button.compareTo(mouseBinds.get(i).name) == 0)
							{
								if (mouseBinds.get(i).isItemSpecific)
								{
									int itemID = Integer.parseInt(item);
									
									if (mouseBinds.get(i).itemID == itemID)
									{
										mouseBinds.get(i).addCompoundCommand(c);
									}
									else
									{
										bcKeyBinding bind = new bcKeyBinding(button, c);
										bind.isItemSpecific = true;
										
										try
										{
											bind.itemID = mc.thePlayer.getCurrentEquippedItem().itemID;
										}
										catch (Exception e)
										{
										}
										
										mouseBinds.add(bind);
									}
								}
								else
								{
									bcKeyBinding bind = new bcKeyBinding(button, c);
									bind.isItemSpecific = true;
									
									try
									{
										bind.itemID = mc.thePlayer.getCurrentEquippedItem().itemID;
									}
									catch (Exception e)
									{
									}
									
									mouseBinds.add(bind);
								}
							}
						}
						
						bcKeyBinding bind = new bcKeyBinding(button, c);
						bind.isItemSpecific = true;
						
						try
						{
							bind.itemID = mc.thePlayer.getCurrentEquippedItem().itemID;
						}
						catch (Exception e)
						{
						}
						
						mouseBinds.add(bind);
					}
					else
					{					
						String key = chunks[1];
						String command = "";
						
						for (int i = 2; i < chunks.length; i++)
						{
							command = command + chunks[i] + " ";
						}
						
						command.trim();
						
						boolean duplicate = false;
						for (int i = 0; i < mouseBinds.size(); i++)
						{
							if (key.compareToIgnoreCase(mouseBinds.get(i).name) == 0)
							{
								log("*** COMPOUNDING MOUSE BIND ***");
								mouseBinds.get(i).addCompoundCommand(command);
								duplicate = true;
							}
						}
						
						if (!duplicate)
						{
							mouseBinds.add(new bcKeyBinding(key, command));
							log("*** ADDED BIND FOR MOUSE ***");
						}
					}
				}
				else
				{
					String key = chunks[0];
					String command = "";
					
					for (int i = 1; i < chunks.length; i++)
					{
						command = command + chunks[i] + " ";
					}
					
					command.trim();
					
					for (int i = 0; i < keyBindings.size(); i++)
					{
						if (key.compareToIgnoreCase(keyBindings.get(i).name) == 0)
						{
							log("*** ERROR: BIND ALREADY EXISTS ***");
						}
					}
					
					keyBindings.add(new bcKeyBinding(key, command));
					log("*** ADDED BIND FOR KEY: " + key);
				}
				
				s = reader.readLine();
			}
			
			reader.close();					
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void onMinecraftEnding()
	{
		log("*** ENDING BINDCRAFT ***");
		
		try
		{
			File file = new File(mc.getMinecraftDir(), "keyBinds.txt");
			PrintWriter writer = new PrintWriter(new FileWriter(file));
			
			for (int i = 0; i < mouseBinds.size(); i++)
			{
				if (mouseBinds.get(i).isItemSpecific)
				{
					for (int j = 0; j < mouseBinds.get(i).commands.size(); j++)
					{
						writer.println("mouse item " + mouseBinds.get(i).itemID + " " + mouseBinds.get(i).name + " " + mouseBinds.get(i).commands.get(j));
					}
				}
				else
				{				
					for (int j = 0; j < mouseBinds.get(i).commands.size(); j++)
					{
						writer.println("mouse " + mouseBinds.get(i).name + " " + mouseBinds.get(i).commands.get(j));
					}
				}
			}
			
			for (int i = 0; i < startupBinds.size(); i++)
			{
				writer.println("startup " + startupBinds.get(i).commands.get(0));
			}
			
			for (int i = 0; i < disconnectBinds.size(); i++)
			{
				writer.println("disconnect " + disconnectBinds.get(i).commands.get(0));
			}
			
			for (int i = 0; i < keyBindings.size(); i++)
			{
				writer.println(keyBindings.get(i).name + " " + keyBindings.get(i).commands.get(0));
				
				if (keyBindings.get(i).isCompound)
				{
					for (int j = 1; j < keyBindings.get(i).commands.size(); j++)
					{
						writer.println("add " + keyBindings.get(i).name + " " + keyBindings.get(i).commands.get(j));
					}
				}
				else if (keyBindings.get(i).isToggle)
				{
					for (int j = 1; j < keyBindings.get(i).commands.size(); j++)
					{
						writer.println("toggle " + keyBindings.get(i).name + " " + keyBindings.get(i).commands.get(j));
					}
				}
			}
			
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void onGameStarted()
	{
		for (int i = 0; i < startupBinds.size(); i++)
		{
			mc.thePlayer.sendChatMessage(startupBinds.get(i).commands.get(0));
		}
	}
	
	@Override
	public void onGameEnding()
	{
		for (int i = 0; i < disconnectBinds.size(); i++)
		{
			mc.thePlayer.sendChatMessage(disconnectBinds.get(i).commands.get(0));
		}
	}
	
	@Override
	public boolean onConsoleCommand(String command, String param)
	{
		try
		{
			System.out.println(command);
			if (command.startsWith("bind"))
			{
				String[] chunks = param.split(" ");
				if (chunks[0].compareToIgnoreCase("toggle") == 0)
				{
					String key = chunks[1].toUpperCase();
					String c = "";
					
					for (int i = 2; i < chunks.length; i++)
					{
						c = c + chunks[i] + " ";
					}
					
					c.trim();
					
					for (int i = 0; i < keyBindings.size(); i++)
					{
						if (key.compareToIgnoreCase(keyBindings.get(i).name) == 0)
						{
							boolean result = keyBindings.get(i).addToggleCommand(command);
							
							if (result)
								mc.ingameGUI.addChatMessage("Bind added");
							else
								mc.ingameGUI.addChatMessage("Could not add bind.");
						}
					}
				}
				else if (chunks[0].compareToIgnoreCase("list") == 0)
				{
					if (chunks.length == 1)
					{
						int startIndex = 0;
						
						for (int i = startIndex; i < startIndex + 20; i++)
						{
							if (i < keyBindings.size())
							{
								mc.ingameGUI.addChatMessage("Bound key: " + keyBindings.get(i).name);
							}
						}
						
						return true;
					}
					
					if (chunks[1].compareToIgnoreCase("mouse") == 0)
					{
						
						return true;
					}
						
				
					String key = chunks[1].toUpperCase().trim();
					
					String regex = "\\d+";					
					Pattern p = Pattern.compile(regex);
					Matcher m = p.matcher(key);
					int page = -1;
					
					while (m.find())
					{
						String num = m.group();
						page = Integer.parseInt(num);
						System.out.println("Match found");
					}
					
					if (page != -1)
					{				
						int startIndex = (page * 20);
							
						for (int i = startIndex; i < startIndex + 20; i++)
						{
							if (i < keyBindings.size())
							{
								mc.ingameGUI.addChatMessage("Bound key: " + keyBindings.get(i).name);
							}
						}
						
						return true;
					}
					
					if (key.compareToIgnoreCase("startup") == 0)
					{
						mc.ingameGUI.addChatMessage("" + startupBinds.size() + " command(s) bound to startup.");
						
						for (int i = 0; i < startupBinds.size(); i++)
						{
							mc.ingameGUI.addChatMessage("" + startupBinds.get(i).commands.get(0));
						}
					}
					
					for (int i = 0; i < keyBindings.size(); i++)
					{
						if (key.compareToIgnoreCase(keyBindings.get(i).name) == 0)
						{
							String type = "standard";
							
							if (keyBindings.get(i).isCompound)
								type = "compound";
							else if (keyBindings.get(i).isToggle)
								type = "toggle";
						
							mc.ingameGUI.addChatMessage("" + keyBindings.get(i).commands.size() + " command(s) bound to key " + keyBindings.get(i).name + ": " + type);
							
							for (int j = 0; j < keyBindings.get(i).commands.size(); j++)
							{
								mc.ingameGUI.addChatMessage("" + keyBindings.get(i).commands.get(j));
							}
							
							return true;
						}
					}
					
					mc.ingameGUI.addChatMessage("Key binding for key: " + key + " not found.");
				}
				else if (chunks[0].compareToIgnoreCase("startup") == 0)
				{
					String c = "";
					
					for (int i = 1; i < chunks.length; i++)
					{
						c = c + chunks[i] + " ";
					}
					
					c.trim();
					
					startupBinds.add(new bcKeyBinding("startup", c));
					mc.ingameGUI.addChatMessage("" + command + " added to startup commands.");
				}
				else if (chunks[0].compareToIgnoreCase("disconnect") == 0)
				{
					String c = "";
					
					for (int i = 1; i < chunks.length; i++)
					{
						c = c + chunks[i] + " ";
					}
					
					c.trim();
					
					disconnectBinds.add(new bcKeyBinding("disconnect", c));
					mc.ingameGUI.addChatMessage("" + command + " added to disconnect commands.");
				}
				else if (chunks[0].compareToIgnoreCase("help") == 0)
				{
					mc.ingameGUI.addChatMessage("/bind <key> <command>");
					mc.ingameGUI.addChatMessage("/bind startup <command>");
					mc.ingameGUI.addChatMessage("/bind disconnect <command>");
					mc.ingameGUI.addChatMessage("/bind add <key> <command> - compound command");
					mc.ingameGUI.addChatMessage("/bind toggle <key> <command> - toggle command");
					mc.ingameGUI.addChatMessage("/bind list <key>");
					mc.ingameGUI.addChatMessage("/unbind <key>");
					mc.ingameGUI.addChatMessage("/unbind startup");
				}
				else if (chunks[0].compareToIgnoreCase("mouse") == 0)
				{					
					if (chunks[1].compareToIgnoreCase("item") == 0)
					{
						String button = chunks[2];
						int buttonID = Integer.parseInt(button);
						String c = "";
						
						for (int i = 3; i < chunks.length; i++)
						{
							c = c + chunks[i] + " ";
						}
						
						c.trim();
						
						for (int i = 0; i < mouseBinds.size(); i++)
						{
							if (button.compareTo(mouseBinds.get(i).name) == 0)
							{
								if (mouseBinds.get(i).isItemSpecific)
								{
									int itemID = mc.thePlayer.getCurrentEquippedItem().itemID;
									
									if (mouseBinds.get(i).itemID == itemID)
									{
										mouseBinds.get(i).addCompoundCommand(c);
										return true;
									}
									else
									{
										bcKeyBinding bind = new bcKeyBinding(button, c);
										bind.isItemSpecific = true;
										
										try
										{
											bind.itemID = mc.thePlayer.getCurrentEquippedItem().itemID;
										}
										catch (Exception e)
										{
											return true;
										}
										
										mouseBinds.add(bind);
									}
								}
								else
								{
									bcKeyBinding bind = new bcKeyBinding(button, c);
									bind.isItemSpecific = true;
									
									try
									{
										bind.itemID = mc.thePlayer.getCurrentEquippedItem().itemID;
									}
									catch (Exception e)
									{
										return true;
									}
									
									mouseBinds.add(bind);
								}
							}
						}
						
						bcKeyBinding bind = new bcKeyBinding(button, c);
						bind.isItemSpecific = true;
						
						try
						{
							bind.itemID = mc.thePlayer.getCurrentEquippedItem().itemID;
						}
						catch (Exception e)
						{
							return true;
						}
						
						mouseBinds.add(bind);
						
					}
					
					String button = chunks[1];					
					int buttonID;
					buttonID = Integer.parseInt(button);
					
					String c = "";
					
					for (int i = 2; i < chunks.length; i++)
					{
						c = c + chunks[i] + " ";
					}
					
					c.trim();
					
					for (int i = 0; i < mouseBinds.size(); i++)
					{
						if (button.compareTo(mouseBinds.get(i).name) == 0)
						{
							mouseBinds.get(i).addCompoundCommand(c);
							mc.ingameGUI.addChatMessage("Compound bind added");
							return true;
						}
					}
					
					mouseBinds.add(new bcKeyBinding(button, c));
					mc.ingameGUI.addChatMessage("Bind added");
				}
				else if (chunks[0].compareToIgnoreCase("craft") == 0)
				{
					String key = chunks[1];
					String item = chunks[2];
					
					for (int i = 0; i < craftingBinds.size(); i++)
					{
						if (craftingBinds.get(i).name.compareToIgnoreCase(key) == 0)
						{
							craftingBinds.get(i).addCompoundCommand(item);
							mc.ingameGUI.addChatMessage("Compound bind added");
							return true;
						}
					}
					
					craftingBinds.add(new bcKeyBinding(key, item));					
					mc.ingameGUI.addChatMessage("Bind added");
				}
				else
				{					
					String key = chunks[0].toUpperCase();
					String c = "";
					
					for (int i = 1; i < chunks.length; i++)
					{
						c = c + chunks[i]+ " ";
					}
					
					c.trim();
					
					for (int i = 0; i < keyBindings.size(); i++)
					{
						if (key.compareToIgnoreCase(keyBindings.get(i).name) == 0)
						{
							keyBindings.get(i).addCompoundCommand(c);
							mc.ingameGUI.addChatMessage("Compound bind added");
							return true;
						}
					}
					
					keyBindings.add(new bcKeyBinding(key, c));
					mc.ingameGUI.addChatMessage("Bind added");
				}
				
				return true;
			}
			else if (command.startsWith("unbind"))
			{
				String[] chunks = param.split(" ");
				if (chunks[0].compareToIgnoreCase("mouse") == 0)
				{
					if (chunks[1].compareToIgnoreCase("item") == 0)
					{
						int itemID = mc.thePlayer.getCurrentEquippedItem().itemID;
						
						for (int i = 0; i < mouseBinds.size(); i++)
						{
							if (mouseBinds.get(i).isItemSpecific && mouseBinds.get(i).itemID == itemID)
							{
								mouseBinds.remove(i);
								mc.ingameGUI.addChatMessage("Bind removed");							
								return true;
							}
						}
						
						mc.ingameGUI.addChatMessage("Bind not found");						
					}
					else
					{
						String button = chunks[1];
						
						for (int i = 0; i < mouseBinds.size(); i++)
						{
							if (button.compareToIgnoreCase(mouseBinds.get(i).name) == 0)
							{
								mouseBinds.remove(i);
								mc.ingameGUI.addChatMessage("Bind removed");							
								return true;
							}
						}
						
						mc.ingameGUI.addChatMessage("Bind not found");
					}
				}
				else if (chunks[0].compareToIgnoreCase("craft") == 0)
				{
					String key = chunks[1];
					
					for (int i = 0; i < craftingBinds.size(); i++)
					{
						if (craftingBinds.get(i).name.compareToIgnoreCase(key) == 0)
						{
							craftingBinds.remove(i);
							mc.ingameGUI.addChatMessage("Bind removed");
							return true;
						}
					}
				}
				
				String key = param.trim();
				
				if (key.compareToIgnoreCase("startup") == 0)
				{
					startupBinds.clear();
					mc.ingameGUI.addChatMessage("Bind removed");
				}
				else if (key.compareToIgnoreCase("disconnect") == 0)
				{
					disconnectBinds.clear();
					mc.ingameGUI.addChatMessage("Bind removed");
				}
				else if (key.compareToIgnoreCase("-a") == 0)
				{
					startupBinds.clear();
					disconnectBinds.clear();
					mouseBinds.clear();
					keyBindings.clear();
				}
				else
				{
					boolean removed = false;
					for (int i = 0; i < keyBindings.size(); i++)
					{
						if (key.compareToIgnoreCase(keyBindings.get(i).name) == 0)
						{
							keyBindings.remove(i);
							mc.ingameGUI.addChatMessage("Bind removed");
							return true;
						}
					}

					mc.ingameGUI.addChatMessage("Bind not found");
				}
				
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void sendClick(Container container, int slot, int button)
	{
		System.out.println("Sending click. Window ID: " + container.windowId + " Slot: " + slot + " Button: " + button);
		mc.playerController.func_27174_a(container.windowId, slot, button, false, mc.thePlayer);
	}
	
	private void handleBinds(int eventKey)
	{
		try
		{
			for (int i = 0; i < keyBindings.size(); i++)
			{
				if (eventKey == keyBindings.get(i).code)
				{
					if (keyBindings.get(i).isCompound)
					{
						for (int j = 0; j < keyBindings.get(i).commands.size(); j++)
						{
							mc.thePlayer.sendChatMessage(keyBindings.get(i).commands.get(j));
						}
					}
					else if (keyBindings.get(i).isToggle)
					{
						mc.ingameGUI.addChatMessage("" + keyBindings.get(i).commands.get(keyBindings.get(i).currentCommand));
						mc.thePlayer.sendChatMessage(keyBindings.get(i).commands.get(keyBindings.get(i).currentCommand));
						
						keyBindings.get(i).currentCommand++;
						
						if (keyBindings.get(i).currentCommand >= keyBindings.get(i).commands.size())
							keyBindings.get(i).currentCommand = 0;
					}
					else
					{
						mc.thePlayer.sendChatMessage(keyBindings.get(i).commands.get(0));
					}
				}
			}					
		}
		catch (Exception e)
		{
		}
	}
	
	private ApiCore mc;
	
	private ArrayList<bcKeyBinding> craftingBinds;
	private ArrayList<bcKeyBinding> mouseBinds;
	private ArrayList<bcKeyBinding> startupBinds;
	private ArrayList<bcKeyBinding> disconnectBinds;
	private ArrayList<bcKeyBinding> keyBindings;
	
	int timer;
}