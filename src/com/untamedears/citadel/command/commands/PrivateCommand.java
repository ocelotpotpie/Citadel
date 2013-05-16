package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.setSingleMode;
import groups.model.Group;
import groups.model.Member;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.CivPlayer;
import com.untamedears.citadel.entity.PlayerReinforcement.SecurityLevel;

public class PrivateCommand extends PlayerCommand {

	public PrivateCommand() {
		super("Private Mode");
		setDescription("Toggle private mode");
		setUsage("/ctprivate");
		setIdentifiers(new String[] {"ctprivate", "ctpr"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		CivPlayer civPlayer = playerManager.getCivPlayer(player);
		Member member = groupMediator.getMemberByUsername(player.getName());
		Group personalGroup = member.getPersonalGroup();
		civPlayer.setGroup(personalGroup);
		setSingleMode(SecurityLevel.PRIVATE, civPlayer);
		return true;
	}

}
