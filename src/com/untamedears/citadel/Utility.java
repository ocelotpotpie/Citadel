package com.untamedears.citadel;

import groups.model.Group;
import groups.model.Group.GroupStatus;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.CivPlayer.Mode;
import com.untamedears.citadel.entity.CivPlayer;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.NaturalReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement.SecurityLevel;
import com.untamedears.citadel.entity.ReinforcementKey;
import com.untamedears.citadel.entity.ReinforcementMaterial;
import com.untamedears.citadel.manager.PlayerManager;
import com.untamedears.citadel.manager.ReinforcementManager;

public class Utility {

	private static Map<SecurityLevel, MaterialData> securityMaterial;
	private static Random rng = new Random();

	public static Block getAttachedChest(Block block) {
		if (block.getType() == Material.CHEST)
			for (BlockFace face : new BlockFace[] { BlockFace.NORTH,
					BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST }) {
				Block b = block.getRelative(face);
				if (b.getType() == Material.CHEST) {
					return b;
				}
			}
		return null;
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

	public static IReinforcement createPlayerReinforcement(Player player, ReinforcementMaterial reinforcementMaterial, Block block) {
		ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
		PlayerManager playerManager = Citadel.getPlayerManager();
		
		int blockTypeId = block.getTypeId();
		if (reinforcementManager.getNonReinforceable().contains(blockTypeId)) {
			return null;
		}

		CivPlayer civPlayer = playerManager.getCivPlayer(player);
		Group group = civPlayer.getGroup();
		GroupStatus status = group.getStatus();
		if (group != null && status == GroupStatus.DISCIPLINED) {
			sendMessage(player, ChatColor.RED, "Group under discipline");
			return null;
		}
		
		String materialName = reinforcementMaterial.getMaterial().name();
		if (!reinforcementManager.getReinforcementMaterials().containsKey(materialName)) {
			sendMessage(
					player,
					ChatColor.RED,
					"Material in hand %s is not a valid reinforcement material.",
					materialName);
			civPlayer.reset();
			return null;
		}

		Material material = reinforcementMaterial.getMaterial();
		int requirements = reinforcementMaterial.getRequirements();
		if (!player.getInventory().contains(material, requirements)) {
			return null;
		}

		// workaround fix for 1.4.6, it doesnt remove the placed item if its
		// already removed for some reason?
		ItemStack requiredMaterials = reinforcementMaterial.getRequiredMaterials();
		Mode mode = civPlayer.getMode();
		if (mode == Mode.FORTIFICATION && blockTypeId == material.getId()) {
			ItemStack stack = player.getItemInHand();
			if (stack.getAmount() < requirements + 1) {
				sendMessage(player, ChatColor.RED,
						"Not enough material in hand to place and fortify this block");
				return null;
			}
			stack.setAmount(stack.getAmount()
					- (requirements + 1));
			player.setItemInHand(stack);
		} else {
			player.getInventory().removeItem(requiredMaterials);
		}
		
		// TODO: there will eventually be a better way to flush inventory
		// changes to the client
		player.updateInventory();
		PlayerReinforcement reinforcement = (PlayerReinforcement) reinforcementManager.createPlayerReinforcement(block, reinforcementMaterial, civPlayer);
		
		String securityLevelName = civPlayer.getSecurityLevel().name();
		if (securityLevelName.equalsIgnoreCase("group")) {
			securityLevelName = securityLevelName + "-" + group.getName();
		}
		
		sendThrottledMessage(player, ChatColor.GREEN,
				"Reinforced with %s at security level %s", materialName, securityLevelName);
		
		Citadel.warning(String.format("PlRein:%s:%d@%s,%d,%d,%d", player
				.getName(), material.getId(), block.getWorld()
				.getName(), block.getX(), block.getY(), block.getZ()));
		
		return reinforcement;
	}

	public static void sendMessage(CommandSender sender, ChatColor color,
			String messageFormat, Object... params) {
		sender.sendMessage(color + String.format(messageFormat, params));
	}

	public static void sendThrottledMessage(CommandSender sender,
			ChatColor color, String messageFormat, Object... params) {
		if (sender instanceof Player) {
			PlayerManager playerManager = Citadel.getPlayerManager();
			Player player = (Player) sender;
			CivPlayer civPlayer = playerManager.getCivPlayer(player);
			if (System.currentTimeMillis() - civPlayer.getLastThrottledMessage() > (1000 * 30)) {
				sendMessage(player, color, messageFormat, params);
			}
			civPlayer.setLastThrottledMessage(System.currentTimeMillis());
		}
	}

	public static boolean explodeReinforcement(Block block) {
		AccessDelegate delegate = AccessDelegate.getDelegate(block);
		IReinforcement reinforcement = delegate.getReinforcement();
		if (reinforcement == null) {
			reinforcement = (IReinforcement) createNaturalReinforcement(block);
		}
		if (reinforcement == null) {
			return false;
		}
		return reinforcementDamaged(reinforcement);
	}

	public static boolean isReinforced(Location location) {
		return getReinforcement(location) != null;
	}

	public static boolean isReinforced(Block block) {
		return getReinforcement(block) != null;
	}

	public static IReinforcement getReinforcement(Location location) {
		return getReinforcement(location.getBlock());
	}

	public static IReinforcement getReinforcement(Block block) {
		AccessDelegate delegate = AccessDelegate.getDelegate(block);
		IReinforcement reinforcement = delegate.getReinforcement();
		return reinforcement;
	}

	public static IReinforcement addReinforcement(IReinforcement reinforcement) {
		return Citadel.getReinforcementManager()
				.addReinforcement(reinforcement);
	}

	public static void removeReinforcement(IReinforcement reinforcement) {
		Citadel.getReinforcementManager().removeReinforcement(reinforcement);
	}

	public static boolean isAuthorizedPlayerNear(
			PlayerReinforcement reinforcement, double distance) {
		
		Integer x = reinforcement.getX();
		Integer y = reinforcement.getY();
		Integer z = reinforcement.getZ();
		String world = reinforcement.getWorld();
		
		World reinWorld = Citadel.getPlugin().getServer()
				.getWorld(world);
		
		Location reinLocation = new Location(reinWorld, (double) x,
				(double) y, (double) z);
		
		double minX = reinLocation.getX() - distance;
		double minZ = reinLocation.getZ() - distance;
		double maxX = reinLocation.getX() + distance;
		double maxZ = reinLocation.getZ() + distance;
		
		List<Player> onlinePlayers = reinWorld.getPlayers();
		
		boolean result = false;
		try {
			for (Player player : onlinePlayers) {
				if (player.isDead()) {
					continue;
				}
				
				Location playerLocation = player.getLocation();
				double playerX = playerLocation.getX();
				double playerZ = playerLocation.getZ();
				
				// Simple bounding box check to quickly rule out Players
				// before doing the more expensive playerLocation.distance
				if (playerX < minX || playerX > maxX || playerZ < minZ
						|| playerZ > maxZ) {
					continue;
				}
				
				if (!reinforcement.isAccessible(player)
						&& !player
								.hasPermission("citadel.admin.accesssecurable")) {
					continue;
				}
				
				double distanceSquared = playerLocation.distance(reinLocation);
				if (distanceSquared <= distance) {
					result = true;
					break;
				}
			}
		} catch (ConcurrentModificationException e) {
			Citadel.warning("ConcurrentModificationException at redstonePower() in BlockListener");
		}
		return result;
	}

	public static boolean maybeReinforcementDamaged(Block block) {
		AccessDelegate delegate = AccessDelegate.getDelegate(block);
		IReinforcement reinforcement = delegate.getReinforcement();
		return reinforcement != null && reinforcementDamaged(reinforcement);
	}

	public static boolean reinforcementDamaged(IReinforcement reinforcement) {
		reinforcement.setDurability(reinforcement.getDurability() - 1);
		boolean cancelled = reinforcement.getDurability() > 0;
		if (reinforcement.getDurability() <= 0) {
			cancelled = reinforcementBroken(reinforcement);
		} else {
			if (reinforcement instanceof PlayerReinforcement) {
				Citadel.info("Reinforcement damaged at "
						+ reinforcement.getBlock().getLocation().toString());
			}
			Citadel.getReinforcementManager().addReinforcement(reinforcement);
		}
		return cancelled;
	}

	public static boolean reinforcementBroken(IReinforcement reinforcement) {
		Citadel.getReinforcementManager().removeReinforcement(reinforcement);
		if (reinforcement instanceof PlayerReinforcement) {
			PlayerReinforcement pr = (PlayerReinforcement) reinforcement;
			Citadel.info("Reinforcement destroyed at "
					+ pr.getBlock().getLocation().toString());

			if (rng.nextDouble() <= pr.getHealth()) {
				Location location = pr.getBlock().getLocation();
				ReinforcementMaterial material = pr.getMaterial();
				location.getWorld().dropItem(location,
						material.getRequiredMaterials());
			}
			return pr.isSecurable();
		}
		return false; // implicit isSecureable() == false
	}

	public static SecurityLevel getSecurityLevel(String[] args, Player player) {
		if (args.length > 0) {
			try {
				return SecurityLevel.valueOf(args[0].toUpperCase());
			} catch (IllegalArgumentException e) {
				sendMessage(player, ChatColor.RED, "Invalid access level %s",
						args[0]);
				return null;
			}
		}
		return SecurityLevel.PRIVATE;
	}

	private static List<Mode> MULTI_MODE = Arrays.asList(Mode.FORTIFICATION,
			Mode.INFO, Mode.REINFORCEMENT, Mode.BYPASS);

	public static void setMultiMode(Mode mode, SecurityLevel securityLevel,
			CivPlayer civPlayer) {
		if (!MULTI_MODE.contains(mode))
			return;

		Player player = civPlayer.getPlayer();
		Group group = civPlayer.getGroup();

		Mode currentMode = civPlayer.getMode();
		SecurityLevel currentSecurityLevel = civPlayer
				.getSecurityLevel();
		if (currentMode == mode && currentSecurityLevel == securityLevel) {
			civPlayer.reset();
			sendMessage(player, ChatColor.GREEN, "%s mode off", mode.name());
		} else {
			civPlayer.setMode(mode);
			civPlayer.setSecurityLevel(securityLevel);
			switch (mode) {
			case REINFORCEMENT_SINGLE_BLOCK:
				sendMessage(player, ChatColor.GREEN, "%s mode %s", mode.name(),
						securityLevel.name());
			case REINFORCEMENT:
				sendMessage(player, ChatColor.GREEN, "%s mode %s", mode.name(),
						securityLevel.name());
				break;
			case FORTIFICATION:
				String materialName = civPlayer.getReinforcementMaterial()
						.getMaterial().name();
				sendMessage(player, ChatColor.GREEN, "%s mode %s, %s",
						mode.name(), materialName, securityLevel.name());
				break;
			case INFO:
				sendMessage(player, ChatColor.GREEN, "%s mode on", mode.name());
				break;
			case BYPASS:
				sendMessage(player, ChatColor.GREEN, "%s mode on", mode.name());
				break;
			}
			civPlayer.checkResetMode();
		}
	}

	public static void setSingleMode(SecurityLevel securityLevel,
			CivPlayer civPlayer) {
		Group group = civPlayer.getGroup();
		Mode currentMode = civPlayer.getMode();
		if (currentMode != Mode.REINFORCEMENT_SINGLE_BLOCK) {
			civPlayer.setSecurityLevel(securityLevel);
			civPlayer.setMode(Mode.REINFORCEMENT_SINGLE_BLOCK);
			sendMessage(civPlayer.getPlayer(), ChatColor.GREEN,
					"Single block reinforcement mode %s", securityLevel.name()
							+ ".");
		}
	}

	public static String getTruncatedMaterialMessage(String prefix,
			List<Integer> materials) {
		StringBuilder builder = new StringBuilder();
		for (int materialId : materials) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(Material.getMaterial(materialId).name());
		}
		builder.insert(0, prefix);
		return builder.toString();
	}
}
