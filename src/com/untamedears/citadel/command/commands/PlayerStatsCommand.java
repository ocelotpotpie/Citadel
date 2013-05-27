package com.untamedears.citadel.command.commands;

import groups.model.GroupMember;
import groups.model.Member;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.command.CommandUtils;
import com.untamedears.citadel.command.PlayerCommand;

public class PlayerStatsCommand extends PlayerCommand {

	public PlayerStatsCommand() {
		super("View Player Stats");
		setDescription("View citadel player stats");
		setUsage("/ctgstats <player-name>");
		setArgumentRange(1, 1);
		setIdentifiers(new String[] {"ctpstats", "ctpst"});
	}

	public boolean execute(CommandSender sender, String[] args) {
//		String username = args[0];
//		Member member = groupMediator.getMemberByUsername(username);
//		Map<String, GroupMember> groupMembers = member.getGroupMembers();
//		
//		GroupManager groupManager = Citadel.getGroupManager();
//		Set<Faction> memberGroups = groupManager.getGroupsByMember(args[0]);
//		Set<Faction> moderatorGroups = groupManager.getGroupsByModerator(args[0]);
//		Set<Faction> founderGroups = groupManager.getGroupsByFounder(args[0]);
//		sender.sendMessage("Player name: "+args[0]);
//		if (founderGroups.size() > 0)
//			sender.sendMessage("Admin of groups: "+CommandUtils.joinFactionSet(founderGroups));
//		if (moderatorGroups.size() > 0)
//			sender.sendMessage("Moderator of groups: "+CommandUtils.joinFactionSet(moderatorGroups));
//		if (memberGroups.size() > 0)
//			sender.sendMessage("Member of groups: "+CommandUtils.joinFactionSet(memberGroups));
//
//		Faction group = null;
//		Member member = Citadel.getMemberManager().getMember(args[0]);
//		if (member != null) {
//			group = member.getPersonalGroup();
//		}
//		if (group != null) {
//			String personalGroupName = group.getName();
//			sender.sendMessage("Personal group reinforcements: ");
//			CommandUtils.printReinforcements(sender, args[0], CommandUtils.countReinforcements(personalGroupName));
//		} else {
//			sender.sendMessage("Player has no personal group.");
//		}
//		
//		return false;
		return true;
	}

}
