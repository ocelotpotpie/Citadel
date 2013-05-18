package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;
import static com.untamedears.citadel.Utility.setSingleMode;
import groups.model.Group;
import groups.model.GroupMember;
import groups.model.Group.GroupStatus;
import groups.model.GroupMember.Role;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.CivPlayer;
import com.untamedears.citadel.entity.PlayerReinforcement.SecurityLevel;

/**
 * User: JonnyD & chrisrico
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class GroupCommand extends PlayerCommand {

	public GroupCommand() {
		super("Group Mode");
		setDescription("Toggle group mode");
		setUsage("/ctgroup §8<group-name>");
		setArgumentRange(1,1);
		setIdentifiers(new String[] {"ctgroup", "ctg"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		String groupName = args[0];
		Group group = groupMediator.getGroupByName(groupName);
		
		if(group == null){
			sendMessage(sender, ChatColor.RED, "Group doesn't exist");
			return true;
		}
		
		GroupStatus status = group.getStatus();
		if (status == GroupStatus.DISCIPLINED) {
			sendMessage(sender, ChatColor.RED, "Group under discipline");
			return true;
		}
		
		String senderName = sender.getName();
		GroupMember groupMember = group.getGroupMember(senderName);
		Role role = groupMember.getRole();

		boolean hasPermission = (role == Role.ADMIN || role == role.MODERATOR);
		if (!hasPermission) {
			sendMessage(sender, ChatColor.RED,
					"Invalid permission to use this group");
			return true;
		}
		
		if(group.isPersonal()){
			sendMessage(sender, ChatColor.RED, "You cannot share your default group");
			return true;
		}
		
		Player player = (Player) sender;
		CivPlayer civPlayer = playerManager.getCivPlayer(player);
		civPlayer.setGroup(group);
		
		setSingleMode(SecurityLevel.GROUP, civPlayer);
		return true;		
	}

}
