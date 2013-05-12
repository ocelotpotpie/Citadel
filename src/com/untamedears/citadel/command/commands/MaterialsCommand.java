package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.ReinforcementMaterial;
import com.untamedears.citadel.manager.ReinforcementManager;

public class MaterialsCommand extends PlayerCommand {

    public MaterialsCommand() {
        super("List Materials");
        setDescription("List the possible reinforcement materials, their strengths, and requirements");
        setUsage("/ctmaterials");
		setIdentifiers(new String[] {"ctmaterials", "ctmat"});
    }

	public boolean execute(CommandSender sender, String[] args) {
		ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
		Map<String, ReinforcementMaterial> reinforcementMaterials = reinforcementManager.getReinforcementMaterials();
		
		if (reinforcementMaterials.isEmpty()) {
	        sendMessage(sender, ChatColor.YELLOW, "No reinforcement materials available.");
	    } else {
	        List<ReinforcementMaterial> materials = new ArrayList<ReinforcementMaterial>(reinforcementMaterials.values());
	        Collections.sort(materials);
	        sendMessage(sender, ChatColor.GREEN, "Reinforcement materials:");
	        for (ReinforcementMaterial m : materials)
	            sendMessage(sender, ChatColor.GREEN, "%s has strength %d and requires %d units.", m.getMaterial().name(), m.getStrength(), m.getRequirements());
	    }
        return true;
	}
}
