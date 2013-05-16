package com.untamedears.citadel.entity;

import static com.untamedears.citadel.Utility.sendMessage;
import groups.model.Group;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.PlayerReinforcement.SecurityLevel;

public class CivPlayer {

	public enum Mode {
		NORMAL, 
		REINFORCEMENT, 
		REINFORCEMENT_SINGLE_BLOCK, 
		FORTIFICATION, 
		INFO, 
		BYPASS
	}

	private final Player player;
	private Mode mode;
	private ReinforcementMaterial fortificationMaterial;
	private long lastThrottledMessage;
	private Integer cancelModePid;
	private Group group;
	private SecurityLevel securityLevel;

	public CivPlayer(Player player) {
		this.player = player;
		reset();
	}

	public void reset() {
		mode = Mode.NORMAL;
		fortificationMaterial = null;
		securityLevel = SecurityLevel.PRIVATE;
	}

	public Player getPlayer() {
		return player;
	}

	public String getUsername() {
		return player.getName();
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public ReinforcementMaterial getReinforcementMaterial() {
		return fortificationMaterial;
	}

	public void setFortificationMaterial(
			ReinforcementMaterial fortificationMaterial) {
		this.fortificationMaterial = fortificationMaterial;
	}

	public long getLastThrottledMessage() {
		return lastThrottledMessage;
	}

	public void setLastThrottledMessage(long lastThrottledMessage) {
		this.lastThrottledMessage = lastThrottledMessage;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public SecurityLevel getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(SecurityLevel securityLevel) {
		this.securityLevel = securityLevel;
	}

	/**
	 * Prepares a scheduled task to reset the mode after a configured amount of
	 * time.
	 * 
	 * If a task is already scheduled it is canceled first.
	 */
	public void checkResetMode() {
		Citadel plugin = Citadel.getPlugin();
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		if (cancelModePid != null && scheduler.isQueued(cancelModePid))
			scheduler.cancelTask(cancelModePid);

		cancelModePid = scheduler.scheduleSyncDelayedTask(plugin,
				new Runnable() {
					public void run() {
						sendMessage(player, ChatColor.YELLOW, "%s mode off",
								mode.name());
						reset();
					}
				}, 20L * Citadel.getConfigManager().getAutoModeReset());
	}
}
