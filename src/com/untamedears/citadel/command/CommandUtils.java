package com.untamedears.citadel.command;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.manager.ReinforcementManager;

public final class CommandUtils {
	public static HashMap<Material,Integer> countReinforcements(String name) {
		HashMap<Material,Integer> hash = new HashMap<Material,Integer>();
		ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
		Set<IReinforcement> set = reinforcementManager.getReinforcementsByGroup(name);
		Material mat;
		for (IReinforcement r : set) {
			PlayerReinforcement pr = (PlayerReinforcement)r;
			mat = pr.getMaterial().getMaterial();
			if (hash.containsKey(mat)) {
				hash.put(mat, hash.get(mat)+1);
			} else {
				hash.put(mat, 1);
			}
		}
		
		return hash;
	}
	
	public static void printReinforcements(CommandSender sender, String name, HashMap<Material, Integer> reinforcements) {
		sender.sendMessage("Group name: "+name);
		Set<Material> mats = reinforcements.keySet();
		for (Material m : mats) {
			sender.sendMessage(m.name()+": "+reinforcements.get(m));
		}
	}
}
