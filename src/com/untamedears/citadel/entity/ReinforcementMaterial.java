package com.untamedears.citadel.entity;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ReinforcementMaterial implements Comparable<ReinforcementMaterial> {
    
    private int materialId;
    private int strength;
    private int requirements;

    public ReinforcementMaterial(int materialId, int strength, int requirements) {
        this.materialId = materialId;
        this.strength = strength;
        this.requirements = requirements;
    }

    public Material getMaterial() {
        return Material.getMaterial(materialId);
    }

    public int getMaterialId() {
        return materialId;
    }

    public int getStrength() {
        return strength;
    }

    public int getRequirements() {
        return requirements;
    }

    public ItemStack getRequiredMaterials() {
        return new ItemStack(getMaterial(), requirements);
    }
    
    public MaterialData getFlasher() {
        return new MaterialData(materialId);
    }

    public int compareTo(ReinforcementMaterial reinforcementMaterial) {
        return Integer.valueOf(strength).compareTo(reinforcementMaterial.getStrength());
    }

    @Override
    public String toString() {
        return String.format("name: %s, materialId: %d, strength: %d, requirements: %d", getMaterial().name(), materialId, strength, requirements);
    }
}
