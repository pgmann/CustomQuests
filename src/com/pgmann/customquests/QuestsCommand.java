package com.pgmann.customquests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings({"unused"})
public class QuestsCommand implements CommandExecutor {

	private CustomQuests p = null;
	private ArrayList<String> commands = new ArrayList<String>();

	public QuestsCommand(CustomQuests p) {
		this.p = p;

		commands.add("help");
		commands.add("active");
	}


	/**
	 * Handles a command.
	 * 
	 * @param sender The sender
	 * @param command The executed command
	 * @param label The alias used for this command
	 * @param args The arguments given to the command
	 * 
	 * @author Amaury Carrade
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("quest") && !command.getName().equalsIgnoreCase("quests")) {
			return false; // Should never happen
		}

		if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
			help(sender, args);
			return true;
		}

		String subcommandName = args[0].toLowerCase();

		// First: subcommand existence.
		if(!this.commands.contains(subcommandName)) {
			sender.sendMessage(p.colourise(CustomQuests.prefix+ChatColor.DARK_RED+"Invalid command. Use "+ChatColor.RED+"/quest help"+ChatColor.DARK_RED+" for a list of commands."));
			return true;
		}

		// Second: is the sender allowed?
		if(!isAllowed(sender, args)) {
			unauthorized(sender, args);
			return true;
		}

		// Third: instantiation
		try {
			Class<? extends QuestsCommand> cl = this.getClass();
			Class[] parametersTypes = new Class[]{CommandSender.class, Command.class, String.class, String[].class};

			Method doMethod = cl.getDeclaredMethod("do" + WordUtils.capitalize(subcommandName), parametersTypes);

			doMethod.invoke(this, new Object[]{sender, command, label, args});

			return true;

		} catch (NoSuchMethodException e) {
			// Unknown method => unknown subcommand.
			sender.sendMessage(p.colourise(CustomQuests.prefix+ChatColor.DARK_RED+"Invalid command. Use "+ChatColor.RED+"/quest help"+ChatColor.DARK_RED+" for a list of commands."));
			return true;

		} catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sender.sendMessage(p.colourise(CustomQuests.prefix + ChatColor.DARK_RED + "An error occured, see console for details. This is probably a bug, please report it!"));
			e.printStackTrace();
			return true; // An error message has been printed, so command was technically handled.
		}
	}

	/**
	 * Prints the plugin main help page.
	 * 
	 * @param sender The help will be displayer for this sender.
	 */
	private void help(CommandSender sender, String[] args) {

		String playerOnly = "";
		if(!(sender instanceof Player)) {
			playerOnly = ChatColor.STRIKETHROUGH+"";
		}


		sender.sendMessage(p.colourise("            ~~ " + CustomQuests.rawPrefix + " ~~            "));

		sender.sendMessage(p.colourise(ChatColor.YELLOW + "/quest help" + ChatColor.WHITE + ": Displays this help page"));
		if(isAllowed(sender, "active".split(" "))) sender.sendMessage(p.colourise(ChatColor.YELLOW+"/quest active" + ChatColor.WHITE + ": Shows your active quests"));

		if (!(sender instanceof Player)) {
			sender.sendMessage(p.colourise(ChatColor.DARK_AQUA + "Strikethrough commands can only be executed as a player."));
		}
	}

	/**
	 * This method checks if an user is allowed to send a command.
	 * 
	 * @param sender
	 * @param args
	 * 
	 * @return boolean The allowance status.
	 */
	protected boolean isAllowed(CommandSender sender, String[] args) {

		// The console is always allowed
		if(!(sender instanceof Player)) {
			return true;
		}

		else {

			if(sender.isOp()) {
				return true;
			}

			if(args.length == 0 ||  args[0].equalsIgnoreCase("help")) { // Help
				return true;
			}

			// Centralized way to manage permissions
			String permission = null;

			switch(args[0]) {
			
			case "active":
				permission = (args.length > 1) ? "quests.admin" : "quests.use";
				break;
			case "talk":
				// check if player can click villager to talk.
				permission = "quests.use";
				break;
			default:
				permission = "quests"; // Should never happen. But, just in case...
				break;
			}

			return ((Player) sender).hasPermission(permission);
		}
	}

	/**
	 * This method sends a message to a player who tries to use a command without permission.
	 * 
	 * @param sender
	 * @param args
	 */
	protected void unauthorized(CommandSender sender, String[] args) {
		if(args.length == 0) {
			return; // will never happen, but just in case of a mistake...
		}

		String message = null;
		switch(args[0]) {
		case "active":
			message = (args.length > 1) ? "You can't see other players' quests!" : "You can't see your active quests!";
			break;
		case "talk":
			// check if player can click villager to talk.
			message = "You can't talk to this villager!";
			break;
		}

		sender.sendMessage(p.colourise(CustomQuests.prefix + ChatColor.DARK_RED + message));
	}


	/**
	 * This command enables the spectator mode on someone.<br>
	 * Usage: /quest[s] active
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doActive(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 1) { // /quest[s] active
			if(sender instanceof Player) {
				sender.sendMessage(p.colourise(CustomQuests.prefix + "You have no active quests."));
			} else {
				sender.sendMessage(p.colourise(CustomQuests.prefix + "Usage: "+ChatColor.RED+"/quests active <player>"));
			}
		}

		else { // /quest[s] active <player>
			Player player = p.getServer().getPlayer(args[1]);
			if (player != null) {
				sender.sendMessage(p.colourise(CustomQuests.prefix + ChatColor.GOLD + player.getDisplayName() + ChatColor.WHITE + " has no active quests."));
			} else {
				sender.sendMessage(p.colourise(CustomQuests.prefix + ChatColor.GOLD + args[1] + ChatColor.WHITE + " isn't online!"));
			}
		}
	}
}
