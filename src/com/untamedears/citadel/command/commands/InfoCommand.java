package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.setMultiMode;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.CivPlayer;
import com.untamedears.citadel.entity.CivPlayer.Mode;
import com.untamedears.citadel.entity.PlayerReinforcement.SecurityLevel;

public class InfoCommand extends PlayerCommand {

	public InfoCommand() {
		super("Info Mode");
		setDescription("Toggle info mode");
		setUsage("/ctinfo");
		setIdentifiers(new String[] {"ctinfo", "cti"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		CivPlayer civPlayer = playerManager.getCivPlayer(player);
		
		SecurityLevel securityLevel = civPlayer.getSecurityLevel();
        if (securityLevel == null) {
        	return false;
        }
        
        setMultiMode(Mode.INFO, SecurityLevel.PUBLIC, civPlayer);
		return true;
	}

}
