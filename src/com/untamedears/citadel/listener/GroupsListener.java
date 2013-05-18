package com.untamedears.citadel.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.dao.CitadelDao;

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
