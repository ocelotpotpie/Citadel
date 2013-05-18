package com.untamedears.citadel.groups;

import groups.Groups;
import groups.manager.GroupManager;
import groups.model.Group;
import groups.model.Member;

import org.bukkit.Bukkit;

public class GroupMediator {

	private Groups groups = (Groups) Bukkit.getPluginManager().getPlugin(
			"Groups");
	private GroupManager groupManager = groups.getGroupManager();

	public GroupMediator() {
	}

	public Group getGroupByName(String name) {
		return groupManager.getGroupByName(name);
	}

	public Member getMemberByUsername(String username) {
		return groupManager.getMember(username);
	}
}
