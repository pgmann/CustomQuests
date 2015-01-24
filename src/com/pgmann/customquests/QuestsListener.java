package com.pgmann.customquests;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class QuestsListener implements Listener{
	private CustomQuests p;

	protected QuestsListener(CustomQuests p) {
		this.p=p;
	}
	
	@EventHandler
	protected void onPlayerJoin(PlayerJoinEvent e) {
		// Add player's data file to the hashmap.
		p.user.put(e.getPlayer().getName(), new QuestsPlayer(p, e.getPlayer()));
	}
	
	@EventHandler
	protected void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		// Check for a villager
		if (e.getRightClicked() instanceof Villager) {
			Villager npc = (Villager) e.getRightClicked();
			// Make sure the villager is from Citizens and has a name
			if (npc.getCustomName() != null && npc.hasMetadata("NPC")) {
				// Check if the villager is meant to have quests attached to them.
				if (p.getConfig().getValues(true).get("npc."+npc.getCustomName()) != null) {
					// If the player is allowed, let them talk to the npc.
					if(p.commands.isAllowed((CommandSender) e.getPlayer(), "talk".split(" "))) {
						p.getPlayerData(e.getPlayer()).talk(npc);
						p.config.saveConfig();
					} else {
						p.commands.unauthorized(e.getPlayer(), "talk".split(" "));
					}
				}
			}
		}
	}
}
