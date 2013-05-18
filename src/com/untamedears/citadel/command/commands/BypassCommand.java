package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.CivPlayer;
import com.untamedears.citadel.entity.CivPlayer.Mode;

public class BypassCommand extends PlayerCommand {
	
    public BypassCommand() {
        super("Bypass Mode");
        setDescription("Toggles bypass mode");
        setUsage("/ctbypass");
        setArgumentRange(0,0);
		setIdentifiers(new String[] {"ctbypass", "ctb"});
    }

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		CivPlayer civPlayer = playerManager.getCivPlayer(player);
		Mode mode = civPlayer.getMode();
		Mode newMode;
		if(mode != Mode.BYPASS) {
			newMode = Mode.BYPASS;
		} else {
			newMode = Mode.NORMAL;
		}
		civPlayer.setMode(newMode);
		String status = newMode == Mode.BYPASS ? "enabled" : "disabled";
        sendMessage(sender, ChatColor.GREEN, "Bypass mode %s", status);
        return true;
	}

}
