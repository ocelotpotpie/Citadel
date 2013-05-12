package com.untamedears.citadel.listener;

import java.util.List;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.manager.GroupManager;

public class GroupsListener implements Listener {

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		
		Plugin plugin = event.getPlugin();
		String pluginName = plugin.getName();
		if(!pluginName.equals("Groups")) {
			return;
		}
		
		CitadelDao dao = Citadel.getDao();
		dao.updateGroups();
	}
}
