package com.pgmann.customquests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

public class QuestsPlayer {
	CustomQuests p;
	Player player;
	String uuid;
	
	protected QuestsPlayer(CustomQuests p, Player player) {
		this.p=p;
		this.player=player;
		uuid = player.getUniqueId().toString();
	}
	
	public void talk(Villager npc) {
		String npcFullname = (String) p.getConfig().getValues(true).get("npc."+npc.getCustomName()+".title")+ChatColor.GOLD+" "+npc.getCustomName();
		String npcName = npc.getCustomName();
		String npcChatPrefix = ChatColor.GOLD+"["+npcFullname+"] "+ChatColor.WHITE;
		
		// Generate starting config for this NPC if necessary.
		if (p.config.getConfig().getValues(true).get(uuid+".npc."+npcName) == null) {
			p.config.getConfig().createSection(uuid+".npc."+npcName);
		}
		if (p.config.getConfig().getValues(true).get(uuid+".npc."+npcName+".quest") == null) {
			p.config.getConfig().set(uuid+".npc."+npcName+".quest", 1);
		}
		if (p.config.getConfig().getValues(true).get(uuid+".npc."+npcName+".stage") == null) {
			p.config.getConfig().set(uuid+".npc."+npcName+".stage", 1);
		}
		if (p.config.getConfig().getValues(true).get(uuid+".npc."+npcName+".asked") == null) {
			p.config.getConfig().set(uuid+".npc."+npcName+".asked", false);
		}
		
		// 1) What QUEST?
		// Check what quest the player is on.
		int questNum=(int) p.config.getConfig().getValues(true).get(uuid+".npc."+npcName+".quest");
		if (p.getConfig().getValues(true).get("npc."+npcName+".quests."+questNum) == null) {
			// If they've finished all available quests, print the prefxed npc.[name].quests.nomore
			player.sendMessage(p.colourise(npcChatPrefix+p.getConfig().getValues(true).get("npc."+npcName+".quests.nomore")));
			return;
		}
		
		// Get the quest name key
		String questRawName = (String) p.getConfig().getValues(true).get("npc."+npcName+".quests."+questNum);
		ConfigurationSection quest = p.getConfig().getConfigurationSection("quest."+questRawName);
		String questName = quest.getString("name");
		
		// 2) QUEST PROGRESS
		// Check how far through that quest they are
		int stageNum=(int) p.config.getConfig().getValues(true).get(uuid+".npc."+npcName+".stage");
		boolean asked=(boolean) p.config.getConfig().getValues(true).get(uuid+".npc."+npcName+".asked");
		
		// a) If they're only starting, give them the prefixed start message (quest.[name].start.message)
		if (stageNum == 1 && !asked) {
			player.sendMessage(p.colourise(npcChatPrefix+quest.getString("start.message")));
		}
		if (!asked) {
			player.sendMessage(p.colourise(npcChatPrefix+quest.getString("stages."+stageNum+".ask")));
			p.config.getConfig().set(uuid+".npc."+npcName+".asked", true);
			return;
		}
		
		// b) If they've already started the quest, check if they have the required items (or money, etc)
		String rawReq = quest.getString("stages."+stageNum+".data");
		String[] rawReqSplit = rawReq.split(" ");
		int reqNum = Integer.parseInt(rawReqSplit[0]);
		Material reqType = Material.getMaterial(rawReqSplit[1]);
		ItemStack req = new ItemStack(reqType, reqNum);
		
		// i. if they don't have the items, print the prefixed quest.[name].stages.[stage].fail
		if (!(player.getItemInHand().isSimilar(req) && player.getItemInHand().getAmount() >= reqNum)) {
			player.sendMessage(p.colourise(npcChatPrefix+quest.getString("stages."+stageNum+".fail")));
			return;
		}
		
		// ii. if they do have the items, print the prefixed quests.[name].stages.[stage].finish
		// and take the required items.
		int newAmount = player.getItemInHand().getAmount()-reqNum;
		Material reqMaterial = player.getItemInHand().getType();
		ItemStack newStack = new ItemStack(reqMaterial, newAmount);
		if (newAmount <= 0) {
			player.getInventory().setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR, 1));
		} else {
			player.getInventory().setItem(player.getInventory().getHeldItemSlot(), newStack);
		}
		
		player.sendMessage(p.colourise(npcChatPrefix+quest.getString("stages."+stageNum+".finish")));
		// - increase the player's [stage] for this quest.
		p.config.getConfig().set(uuid+".npc."+npcName+".stage", ((int)p.config.getConfig().get(uuid+".npc."+npcName+".stage"))+1);

		// Reset asked...
		p.config.getConfig().set(uuid+".npc."+npcName+".asked", false);
		// - if there's no more stages to that quest, print prefixed quest.[name].reward.message
		// give the player the items/money/other, as specified by quest.[name].reward.data (and type)
		// increase the quest number by one for that npc for that player.

		if (quest.get("stages."+(int)p.config.getConfig().get(uuid+".npc."+npcName+".stage")) == null) {
			// The stages have been exhausted.
			player.sendMessage(p.colourise(npcChatPrefix+quest.getString("reward.message")));
			
			// Work out what the reward is
			String rawRew = quest.getString("reward.data");
			String[] rawRewSplit = rawRew.split(" ");
			int rewNum = Integer.parseInt(rawRewSplit[0]);
			Material rewType = Material.getMaterial(rawRewSplit[1]);
			ItemStack rew = new ItemStack(rewType, rewNum);
			
			// Give them the reward!
			player.getInventory().addItem(rew);
			
			// Notify the console
			Bukkit.getConsoleSender().sendMessage(p.colourise(CustomQuests.prefix+ChatColor.YELLOW+player.getDisplayName()+ChatColor.WHITE+" finished "+ChatColor.GOLD+npcFullname+ChatColor.WHITE+"'s "+questName+" quest."));
			
			// Increase the quest count
			p.config.getConfig().set(uuid+".npc."+npcName+".quest", (int)p.config.getConfig().getValues(true).get(uuid+".npc."+npcName+".quest")+1);
			
			// Reset asked and the stage count...
			p.config.getConfig().set(uuid+".npc."+npcName+".stage", 1);
			p.config.getConfig().set(uuid+".npc."+npcName+".asked", false);
		}
	}
}
