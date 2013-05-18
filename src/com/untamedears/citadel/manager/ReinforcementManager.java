package com.untamedears.citadel.manager;

import groups.model.Group;

import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.CivPlayer;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.NaturalReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.ReinforcementMaterial;
import com.untamedears.citadel.storage.ReinforcementStorage;

public class ReinforcementManager {
	
	private ReinforcementStorage storage;
	private ConfigManager configManager;
	private Map<String, ReinforcementMaterial> reinforcementMaterial;
	
	public ReinforcementManager() {
		this.configManager = Citadel.getConfigManager();
		this.reinforcementMaterial = configManager.getReinforcementMaterials();
	}
	
	public ReinforcementStorage getStorage(){
		return this.storage;
	}
	
	public void setStorage(ReinforcementStorage storage){
		this.storage = storage;
	}
	
	public boolean isReinforcementMaterial(Material material) {
		return this.isReinforcementMaterial(material.name().toString());
	}
	
	public boolean isReinforcementMaterial(String materialName) {
		return this.reinforcementMaterial.containsKey(materialName);
	}

	public ReinforcementMaterial getReinforcementMaterial(Material material) {
		return this.reinforcementMaterial.get(material.name().toString());
	}
	
	public Map<String, ReinforcementMaterial> getReinforcementMaterials() {
		return this.reinforcementMaterial;
	}
	
	public static IReinforcement createNaturalReinforcement(Block block) {
		Material material = block.getType();
		int breakCount = Citadel.getConfigManager().getMaterialBreakCount(
				material.getId(), block.getY());
		if (breakCount <= 1) {
			return null;
		}
		NaturalReinforcement nr = new NaturalReinforcement(block, breakCount);
		Citadel.getReinforcementManager().addReinforcement(nr);
		return nr;
	}

	public IReinforcement createPlayerReinforcement(Block block, ReinforcementMaterial material, CivPlayer civPlayer) {
		PlayerReinforcement reinforcement = new PlayerReinforcement(block, material, civPlayer.getGroup(), civPlayer.getUsername(), civPlayer.getSecurityLevel());
		return addReinforcement(reinforcement);
	} 
	
	public IReinforcement getReinforcement(Block block){
		return this.storage.findReinforcement(block);
	}

	public IReinforcement getReinforcement(Location location) {
		return getReinforcement(location.getBlock());
	}
	
	public Set<IReinforcement> getReinforcementsByGroup(String groupName){
		return this.storage.findReinforcementsByGroup(groupName);
	}
	
	public IReinforcement addReinforcement(IReinforcement reinforcement){
		return this.storage.addReinforcement(reinforcement);
	}
	
	public void removeReinforcement(IReinforcement reinforcement){
		this.storage.removeReinforcement(reinforcement);
	}
	
	public int getReinforcementsAmount(){
		return this.storage.findReinforcementsAmount();
	}
}
