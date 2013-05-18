package com.untamedears.citadel.listener;

import static com.untamedears.citadel.Utility.createPlayerReinforcement;
import static com.untamedears.citadel.Utility.isReinforced;
import static com.untamedears.citadel.Utility.reinforcementBroken;
import static com.untamedears.citadel.Utility.sendMessage;
import groups.model.Group;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.CivPlayer;
import com.untamedears.citadel.entity.CivPlayer.Mode;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement.SecurityLevel;
import com.untamedears.citadel.manager.PlayerManager;

public class PlayerListener implements Listener {

	private PlayerManager playerManager = Citadel.getPlayerManager();
	private BukkitScheduler scheduler = Citadel.getPlugin().getServer().getScheduler();
	private Map<String, Integer> playersInMinecart = new HashMap<String, Integer>();

    @EventHandler
    public void login(PlayerLoginEvent ple) {
    	Player player = ple.getPlayer();
		playerManager.getOrCreateCivPlayer(player);
    }

    @EventHandler
    public void quit(PlayerQuitEvent pqe) {
        Player player = pqe.getPlayer();
        String username = player.getName();
        playerManager.removeCivPlayer(username);
    	cancelPlayerInMinecartTask(username);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void bookshelf(PlayerInteractEvent pie) {
        if (pie.hasBlock() && pie.getMaterial() == Material.BOOKSHELF)
            interact(pie);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void bucketEmpty(PlayerBucketEmptyEvent pbee) {
        Material bucket = pbee.getBucket();
        if (Material.LAVA_BUCKET == bucket || Material.WATER_BUCKET == bucket) {
            Block block = pbee.getBlockClicked();
            BlockFace face = pbee.getBlockFace();
            Block relativeBlock = block.getRelative(face);
            // Protection for reinforced rails types from direct lava bucket drop.
            Material relativeType = relativeBlock.getType();
            if (Material.RAILS == relativeType || Material.POWERED_RAIL == relativeType ||
                    Material.DETECTOR_RAIL == relativeType) {
                if (isReinforced(relativeBlock)) {
                   pbee.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent pie) {
        try {
        	if (!pie.hasBlock())
        	{
        		return;
        	}
        
	        onMinecartInteract(pie);
	        onBlockInteract(pie);
        }
        catch(Exception e)
        {
            Citadel.printStackTrace(e);
        }
    }
    
    private void onBlockInteract(PlayerInteractEvent pie) {
    	Player player = pie.getPlayer();
    	Block block = pie.getClickedBlock();

    	AccessDelegate accessDelegate = AccessDelegate.getDelegate(block);
    	block = accessDelegate.getBlock();
    	
    	IReinforcement genericReinforcement = accessDelegate.getReinforcement();
    	PlayerReinforcement reinforcement = null;
    	if (genericReinforcement instanceof PlayerReinforcement) {
    		reinforcement = (PlayerReinforcement) genericReinforcement;
    	}

    	Action action = pie.getAction();
    	boolean isReinforced = reinforcement != null;
    	boolean rightClicked = action == Action.RIGHT_CLICK_BLOCK;
    	boolean isSecurable = isReinforced && rightClicked && reinforcement.isSecurable();
    	boolean isAccessible = isReinforced && reinforcement.isAccessible(player);
    	boolean adminCanAccess = player.hasPermission("citadel.admin.accesssecurable");
    	
    	if (isSecurable && !isAccessible && !adminCanAccess) {
    		Citadel.info(String.format(
    				"%s failed to access locked reinforcement at %s",
    				player.getName(), block.getLocation().toString()));
    		sendMessage(pie.getPlayer(), ChatColor.RED, "%s is locked", block.getType().name());
    		pie.setCancelled(true);
    	}
    	
    	if (pie.isCancelled()) {
    		return;
    	}

    	CivPlayer civPlayer = playerManager.getCivPlayer(player);
    	Mode mode = civPlayer.getMode();
    	switch (mode) {
    	case NORMAL:
    		if (isSecurable && !isAccessible && adminCanAccess) {
    			Citadel.info(String.format(
    					"[Admin] %s accessed locked reinforcement at %s",
    					player.getName(), block.getLocation().toString()));
    		}
    		return;
    	case FORTIFICATION:
    		return;
    	case INFO:
    		if (isReinforced) {
    			String status = reinforcement.getStatus();
    			SecurityLevel securityLevel = reinforcement.getSecurityLevel();
    			Group group = reinforcement.getGroup();
    			boolean isPersonal = group.isPersonal();
    			String groupName = "!NULL!";
    			ChatColor chatColor = ChatColor.RED;
    			String message = "!NULL!";
    			
    			if (player.hasPermission("citadel.admin.ctinfodetails")) {
    				message = String.format("Loc[%s]  Chunk[%s]", 
    						reinforcement.getId().toString(),
    						reinforcement.getChunkId());
    				sendMessage(player, ChatColor.GREEN, message);
    				
    				if (group != null) {
    					if (isPersonal) {
    						groupName = String.format("[%s] (Personal)", group.getName());
    					} else {
    						groupName = String.format("[%s]", group.getName());
    					}
    				}
    				
    				message = String.format(" Group%s  Durability[%d/%d]",
    						groupName,
    						reinforcement.getDurability(),
    						reinforcement.getMaterial().getStrength());
    				chatColor = ChatColor.GREEN;
    			} else if (reinforcement.isAccessible(player)) {
    				
    				if (group != null) {
    					groupName = group.getName();
    				}
    				
    				if(isPersonal){
    					message = String.format("%s, security: %s, group: %s (Personal)", status, securityLevel, groupName);
    				} else {
    					message = String.format("%s, security: %s, group: %s", status, securityLevel, groupName);
    				}
    				
    				chatColor = ChatColor.GREEN;
    			} else {
    				message = String.format("%s, security: %s", status, securityLevel);
    				chatColor = ChatColor.RED;
    			}
    			
				sendMessage(player, chatColor, message);
    		}
    		break;
    	case REINFORCEMENT:
    		if (reinforcement == null) {
    			// Break any natural reinforcement before placing the player reinforcement
    			if (genericReinforcement != null) {
    				reinforcementBroken(genericReinforcement);
    			}
    			createPlayerReinforcement(player, block);
    		}
    		break;
    	case REINFORCEMENT_SINGLE_BLOCK:
    		if (reinforcement.isBypassable(player)) {
    			
    			String message = "";
    			SecurityLevel reinforcementSecurityLevel = reinforcement.getSecurityLevel();
    			SecurityLevel selectedSecurityLevel = civPlayer.getSecurityLevel();
    			
    			if (reinforcementSecurityLevel != selectedSecurityLevel) {
    				reinforcement.setSecurityLevel(selectedSecurityLevel);
    				message = String.format("Changed security level %s", selectedSecurityLevel);
    			}
    			
    			Group group = reinforcement.getGroup();
    			Group selectedGroup = civPlayer.getGroup();
    			
    			if (!group.equals(selectedGroup)) {
    			
    				String selectedGroupName = selectedGroup.getName();
    				reinforcement.setGroupName(selectedGroupName);
    				
    				if(!message.equals("")) {
    					message = message + ". ";
    				}
    				
    				message = message + String.format("Changed group from %s to %s", group.getName(), selectedGroupName);
    			}
    			
    			if (!message.equalsIgnoreCase("")) {
    				Citadel.getReinforcementManager().addReinforcement(reinforcement);
    				sendMessage(player, ChatColor.GREEN, message);
    			}
    			
    		} else {
    			sendMessage(player, ChatColor.RED, "You are not permitted to modify this reinforcement");
    		}
    		civPlayer.reset();
    		break;
    	}
    }
    
    private void onMinecartInteract(PlayerInteractEvent event) {
    	if(!event.hasItem()) {
    		return;
    	}
    	
    	if(event.getMaterial() != Material.MINECART) {
    		return;
    	}
    	
    	Block block = event.getClickedBlock();
    	Location location = block.getLocation();
    	Player player = event.getPlayer();
    	
    	PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(location);    	
		if(reinforcement != null 
				&& !reinforcement.isAccessible(player)) {
			event.setCancelled(true);
			sendMessage(player, ChatColor.RED, "You don't have permission to use this rail");
		}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMinecartEnter(VehicleEnterEvent event) {
    	Vehicle vehicle = event.getVehicle();
    	if(!(vehicle instanceof Minecart)) {
    		return;
    	}
    	
    	Entity entity = event.getEntered();
    	if(!(entity instanceof Player)) {
    		return;
    	}
    	
    	final Minecart minecart = (Minecart) vehicle;
    	final World world = minecart.getWorld();
    	final Player player = (Player) entity;
    	final String username = player.getName();
    	
    	int delay = 20 * 2;
    	int period = 20 * 2;
    	int taskId = scheduler.scheduleSyncRepeatingTask(Citadel.getPlugin(), new Runnable() {
    		public void run() {
    			Location location = minecart.getLocation();
    			PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(location);
    			if(reinforcement != null 
    					&& !reinforcement.isAccessible(username)) {
	    			player.leaveVehicle();
	    			minecart.remove();
	    			world.dropItemNaturally(location, new ItemStack(Material.MINECART));
	    			sendMessage(player, ChatColor.RED, "You don't have permission to use this rail");
    			}
    		}
    	}, delay, period);
    	this.playersInMinecart.put(username.toLowerCase(), taskId);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMinecartExit(VehicleExitEvent event) {
    	Vehicle vehicle = event.getVehicle();
    	if(!(vehicle instanceof Minecart)) {
    		return;
    	}
    	
    	Entity entity = event.getExited();
    	if(!(entity instanceof Player)) {
    		return;
    	}
    	
    	Player player = (Player) entity;
    	String username = player.getName().toLowerCase();
    	cancelPlayerInMinecartTask(username);
    }
    
    private void cancelPlayerInMinecartTask(String username) {
    	if(this.playersInMinecart.containsKey(username))
		{
			int taskId = this.playersInMinecart.get(username);
			if(taskId > 0 && scheduler.isQueued(taskId))
			{
	            this.scheduler.cancelTask(taskId);
	            this.playersInMinecart.remove(username);
			}
		}
    }
}
