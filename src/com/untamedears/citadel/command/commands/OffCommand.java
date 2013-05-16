package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.CivPlayer;

public class OffCommand extends PlayerCommand {

	public OffCommand() {
		super("Off Mode");
		setDescription("Toggles citadel off");
		setUsage("/ctoff");
		setIdentifiers(new String[] {"ctoff", "cto"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		CivPlayer civPlayer = playerManager.getCivPlayer(player);
        civPlayer.reset();
        sendMessage(player, ChatColor.GREEN, "All Citadel modes set to normal");
		return true;
	}

}
