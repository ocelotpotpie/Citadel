package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.setSingleMode;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.CivPlayer;
import com.untamedears.citadel.entity.PlayerReinforcement.SecurityLevel;

public class PublicCommand extends PlayerCommand {

	public PublicCommand() {
		super("Public Mode");
		setDescription("Toggles public mode");
		setUsage("/ctpublic");
		setIdentifiers(new String[] {"ctpublic", "ctpu"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		CivPlayer civPlayer = playerManager.getCivPlayer(player);
		setSingleMode(SecurityLevel.PUBLIC, civPlayer);
		return true;
	}

}
