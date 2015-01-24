package com.pgmann.customquests;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomQuests extends JavaPlugin {

	protected HashMap <String, QuestsPlayer> user = new HashMap<String, QuestsPlayer>();

	QuestsCommand commands;
	static String rawPrefix = ChatColor.YELLOW+"CustomQuests"+ChatColor.WHITE;
	static String prefix = ChatColor.WHITE+"["+rawPrefix+ChatColor.WHITE+"] ";
	ConfigAccessor config;

	@Override
	public void onEnable() {
		getServer().getConsoleSender().sendMessage(colourise(rawPrefix+" by "+ChatColor.YELLOW+"pgmann"+ChatColor.WHITE+" is enabled!"));

		// Generate the default config file if none exists
		saveDefaultConfig();
		config=new ConfigAccessor(this, "users");
		config.saveDefaultConfig();

		// Add players already online to this plugin's database
		for (Player player : getServer().getOnlinePlayers()) {
			user.put(player.getName(), new QuestsPlayer(this, player));
		}

		// Register the command listener
		commands = new QuestsCommand(this);
		getCommand("quests").setExecutor(commands);
		getCommand("quest").setExecutor(commands);

		// Register the event listener
		getServer().getPluginManager().registerEvents(new QuestsListener(this), this);
	}

	@Override
	public void onDisable() {
		config.saveConfig();
		getServer().getConsoleSender().sendMessage(colourise(rawPrefix+" is now disabled."));
	}

	public QuestsPlayer getPlayerData(Player player) {
		return user.get(player.getName());
	}
	
	public String colourise(String rawText) {
		return ChatColor.translateAlternateColorCodes('&', rawText);
	}
}
