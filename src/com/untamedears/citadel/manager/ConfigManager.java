package com.untamedears.citadel.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.NaturalReinforcementConfig;
import com.untamedears.citadel.entity.NaturalReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.ReinforcementMaterial;

public class ConfigManager {
	
	private String driver;
	private String url;
	private String username;
	private String password;
	private String isolation;
	private boolean logging;
	private boolean rebuild;
	
	private int autoModeReset;
	private boolean verboseLogging;
	private double redstoneDistance;
	private int groupsAllowed;
	private long cacheMaxAge;
	private int cacheMaxChunks;
	
	private Map<String, ReinforcementMaterial> reinforcementMaterials;
	private List<Integer> securable;
	private List<Integer> nonReinforceable;

	private File main;
	private FileConfiguration config;
	private FileConfiguration cleanConfig;
	
	public void load() {
		this.reinforcementMaterials = new HashMap<String, ReinforcementMaterial>();
		this.securable = new ArrayList<Integer>();
		Citadel plugin = Citadel.getPlugin();
		this.config = plugin.getConfig();
		this.cleanConfig = new YamlConfiguration();
		this.main = new File(plugin.getDataFolder() + File.separator + "config.yml");
		
		boolean exists = main.exists();
		
		if(exists) {
			try {
				config.options().copyDefaults();
				config.load(main);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			config.options().copyDefaults(true);
		}
		
		driver = loadString("database.driver");
		url = loadString("database.url");
		username = loadString("database.username");
		password = loadString("database.password");
		isolation = loadString("database.isolation");
		logging = loadBoolean("database.logging");
		rebuild = loadBoolean("database.rebuild");
		
        autoModeReset = loadInt("general.autoModeReset");
        verboseLogging = loadBoolean("general.verboseLogging");
        redstoneDistance = loadDouble("general.redstoneDistance");
        groupsAllowed = loadInt("general.groupsAllowed");
        cacheMaxAge = loadLong("caching.max_age");
        cacheMaxChunks = loadInt("caching.max_chunks");
        
        for (Object object : loadList("materials")) {
        	LinkedHashMap<?,?> map = (LinkedHashMap<?,?>) object;
        	
        	String name = map.get("name").toString();
        	int strength = (Integer) map.get("strength");
        	int requirements = (Integer) map.get("requirements");
        	
        	int id = getMaterialId(name);
        	if(id != 0) {
	        	ReinforcementMaterial reinforcementMaterial = new ReinforcementMaterial(id, strength, requirements);
	        	this.reinforcementMaterials.put(name, reinforcementMaterial);
        	}
        }
        
        for (String name : loadStringList("additionalSecurable")) {
           int id = getMaterialId(name);
           if(id != 0) {
        	   securable.add(id);
           }
        }
        
        for (String name : loadStringList("nonReinforceable")) {
        	int id = getMaterialId(name);
            if(id != 0) {
         	   nonReinforceable.add(id);
            }
        }
        
        ConfigurationSection naturalReinforcements =
            config.getConfigurationSection("naturalReinforcements");
        if (naturalReinforcements != null) {
            for (String materialName : naturalReinforcements.getKeys(false)) {
                ConfigurationSection materialConfig =
                    naturalReinforcements.getConfigurationSection(materialName);
                if (materialConfig == null) {
                    Citadel.warning("Misconfigured Natural Reinforcement: " + materialName);
                    continue;
                }
                NaturalReinforcementConfig natReinCfg = new NaturalReinforcementConfig(materialConfig);
                NaturalReinforcement.CONFIGURATION.put(natReinCfg.getMaterialId(), natReinCfg);
            }
        }
	}
	
	private Integer getMaterialId(String name) {
		Material material = Material.matchMaterial(name);
		int id = 0;
        if (material != null) {
        	id = material.getId();
        } else {
        	try {
        		id = Integer.parseInt(name);
        	} catch (NumberFormatException e) {
        		Citadel.warning("Invalid additionalSecurable material " + name);
        	}
        }
        return id;
	}

	private Boolean loadBoolean(String path) {
        if (config.isBoolean(path)) {
            boolean value = config.getBoolean(path);
            cleanConfig.set(path, value);
            return value;
        }
        return false;
    }

    private String loadString(String path) {
        if (config.isString(path)) {
            String value = config.getString(path);
            cleanConfig.set(path, value);
            return value;
        }

        return "";
    }

    private int loadInt(String path) {
        if (config.isInt(path)) {
            int value = config.getInt(path);
            cleanConfig.set(path, value);
            return value;
        }

        return 0;
    }
    
    private Long loadLong(String path) {
    	if (config.isLong(path)) {
    		long value = config.getLong(path);
    		cleanConfig.set(path, value);
    	}
    	return 0L;
    }

    private double loadDouble(String path) {
        if (config.isDouble(path)) {
            double value = config.getDouble(path);
            cleanConfig.set(path, value);
            return value;
        }
        return 0;
    }
    
    private List<String> loadStringList(String path) {
    	if(config.isList(path)) {
    		List<String> value = config.getStringList(path);
    		cleanConfig.set(path, value);
    		return value;
    	}
    	return null;
    }
    
    private List<?> loadList(String path) {
    	if(config.isList(path)) {
    		List<?> value = config.getList(path);
    		cleanConfig.set(path, value);
    		return value;
    	}
    	return null;
    }
    
    public void save() {
        try {
            cleanConfig.save(main);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public double getRedstoneDistance(){
		return this.redstoneDistance;
	}
	
	public void setRedstoneDistance(double rd){
		this.redstoneDistance = rd;
	}
	
	public int getAutoModeReset(){
		return this.autoModeReset;
	}
	
	public void setAutoModeReset(int amr){
		this.autoModeReset = amr;
	}
	
	public int getGroupsAllowed() {
		return this.groupsAllowed;
	}
	
	public void setGroupsAllowed(int ga){
		this.groupsAllowed = ga;
	}
	
	public boolean getVerboseLogging(){
		return this.verboseLogging;
	}
	
	public void setVerboseLogging(boolean vl){
		this.verboseLogging = vl;
	}
	
	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIsolation() {
		return isolation;
	}

	public void setIsolation(String isolation) {
		this.isolation = isolation;
	}

	public boolean isLogging() {
		return logging;
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}

	public boolean isRebuild() {
		return rebuild;
	}

	public void setRebuild(boolean rebuild) {
		this.rebuild = rebuild;
	}

	public Map<String, ReinforcementMaterial> getReinforcementMaterials() {
		return reinforcementMaterials;
	}

	public void setReinforcementMaterials(
			Map<String, ReinforcementMaterial> reinforcementMaterials) {
		this.reinforcementMaterials = reinforcementMaterials;
	}

	public List<Integer> getSecurable() {
		return securable;
	}

	public void setSecurable(List<Integer> securable) {
		this.securable = securable;
	}

	public List<Integer> getNonReinforceable() {
		return nonReinforceable;
	}

	public void setNonReinforceable(List<Integer> nonReinforceable) {
		this.nonReinforceable = nonReinforceable;
	}

	public File getMain() {
		return main;
	}

	public void setMain(File main) {
		this.main = main;
	}

	public long getCacheMaxAge(){
		return this.cacheMaxAge;
	}
	
	public void setCacheMaxAge(long cma){
		this.cacheMaxAge = cma;
	}
	
	public int getCacheMaxChunks(){
		return this.cacheMaxChunks;
	}

	public void setCacheMaxChunks(int cmc){
		this.cacheMaxChunks = cmc;
	}

    public int getMaterialBreakCount(int materialId, int blockY){
        NaturalReinforcementConfig natReinCfg =
            NaturalReinforcement.CONFIGURATION.get(materialId);
        if (natReinCfg == null) {
            return 1;
        }
        return natReinCfg.generateDurability(blockY);
    }
}
