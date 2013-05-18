package com.untamedears.citadel.manager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import com.untamedears.citadel.entity.CivPlayer;

public class PlayerManager {

	private Map<String, CivPlayer> players = new HashMap<String, CivPlayer>();

	public PlayerManager() {

	}

	public void addCivPlayer(CivPlayer civPlayer) {
		String username = civPlayer.getUsername().toLowerCase();
		players.put(username, civPlayer);
	}

	public void removeCivPlayer(CivPlayer civPlayer) {
		removeCivPlayer(civPlayer.getUsername());
	}

	public void removeCivPlayer(Player player) {
		removeCivPlayer(player.getName());
	}

	public void removeCivPlayer(String username) {
		players.remove(username.toLowerCase());
	}

	public CivPlayer getOrCreateCivPlayer(Player player) {
		CivPlayer civPlayer = getCivPlayer(player);
		if (civPlayer == null) {
			civPlayer = new CivPlayer(player);
			addCivPlayer(civPlayer);
		}
		return civPlayer;
	}

	public CivPlayer getCivPlayer(Player player) {
		String username = player.getName().toLowerCase();
		return players.get(username);
	}
}
