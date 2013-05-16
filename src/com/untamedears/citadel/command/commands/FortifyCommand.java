package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;
import static com.untamedears.citadel.Utility.setMultiMode;
import groups.model.Group;
import groups.model.Group.GroupStatus;
import groups.model.Group.GroupType;
import groups.model.GroupMember;
import groups.model.GroupMember.Role;
import groups.model.Member;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.CivPlayer;
import com.untamedears.citadel.entity.CivPlayer.Mode;
import com.untamedears.citadel.entity.PlayerReinforcement.SecurityLevel;
import com.untamedears.citadel.entity.ReinforcementMaterial;

public class FortifyCommand extends PlayerCommand {

	public FortifyCommand() {
		super("Fority Mode");
		setDescription("Toggle fortification mode");
		setUsage("/ctfortify Â§8[security-level] [group]");
		setArgumentRange(0, 2);
		setIdentifiers(new String[] { "ctfortify", "ctf" });
	}

	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		CivPlayer civPlayer = playerManager.getCivPlayer(player);
		String username = civPlayer.getUsername();

		String securityLevelName = null;
		String groupName = null;
		if (args.length != 0) {
			securityLevelName = args[0];
			if (args.length == 2) {
				groupName = args[1];
			}
		}

		SecurityLevel securityLevel;
		if (securityLevelName != null) {
			securityLevel = SecurityLevel.valueOf(securityLevelName
					.toUpperCase());
		} else {
			securityLevel = SecurityLevel.PRIVATE;
		}

		if (securityLevel == SecurityLevel.GROUP) {
			Group group;
			if (groupName != null) {
				group = groupMediator.getGroupByName(groupName);
			} else {
				sender.sendMessage(new StringBuilder()
						.append("§cYou must specify a group in group fortification mode")
						.toString());
				sender.sendMessage(new StringBuilder().append("§cUsage:§e ")
						.append("/ctfortify §8group <group-name>").toString());
				return true;
			}

			if (group == null) {
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

			if (group.isPersonal()) {
				sendMessage(sender, ChatColor.RED,
						"You cannot share your default group");
				return true;
			}

			civPlayer.setGroup(group);
		} else {
			Member member = groupMediator.getMemberByUsername(username);
			Group personalGroup = member.getPersonalGroup();
			civPlayer.setGroup(personalGroup);
		}

		Material itemInHand = player.getItemInHand().getType();
		String itemInHandName = itemInHand.name();

		Map<String, ReinforcementMaterial> reinforcementMaterials = configManager
				.getReinforcementMaterials();
		ReinforcementMaterial reinforcementMaterial = reinforcementMaterials
				.get(itemInHandName);

		if (reinforcementMaterial != null) {
			civPlayer.setFortificationMaterial(reinforcementMaterial);
			civPlayer.reset();
		} else {
			sendMessage(sender, ChatColor.YELLOW,
					"Invalid reinforcement material %s", itemInHandName);
			return true;
		}

		setMultiMode(Mode.FORTIFICATION, securityLevel, civPlayer);
		return true;
	}

}
