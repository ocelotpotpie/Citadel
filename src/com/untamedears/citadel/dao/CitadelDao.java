package com.untamedears.citadel.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.PersistenceException;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.LogLevel;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.lennardf1989.bukkitex.MyDatabase;
import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.DbUpdateAction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.ReinforcementKey;
import com.untamedears.citadel.entity.Test;

public class CitadelDao extends MyDatabase {
	private static final int CHUNK_SIZE = 16;

    private String sqlLogDirectory;
    private boolean sqlEnableLog;

    public static String MakeChunkId(Chunk chunk) {
        return String.format("%s:%d:%d", chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public CitadelDao(JavaPlugin plugin) {
        super(plugin);

        Configuration config = plugin.getConfig();
        sqlLogDirectory = config.getString("database.logdirectory", "sql-logs");
        sqlEnableLog = config.getBoolean("database.enablefilelog", false);

        initializeDatabase(
                config.getString("database.driver"),
                config.getString("database.url"),
                config.getString("database.username"),
                config.getString("database.password"),
                config.getString("database.isolation"),
                config.getBoolean("database.logging", false),
                config.getBoolean("database.rebuild", false)
        );

        config.set("database.rebuild", false);
        plugin.saveConfig();
    }

    @Override
    protected List<Class<?>> getDatabaseClasses() {
        return Arrays.asList(
                PlayerReinforcement.class, 
                ReinforcementKey.class,
                Test.class);
    }

    public Object save(Object object) {
        getDatabase().save(object);
        return object;
    }

    public void delete(Object object) {
        getDatabase().delete(object);
    }
    
    public Set<IReinforcement> findReinforcementsByGroup(String groupName){
        Set<PlayerReinforcement> result = getDatabase()
            .createQuery(PlayerReinforcement.class, "find reinforcement where name = :groupName")
    		.setParameter("groupName", groupName)
    		.findSet();    	
        for (PlayerReinforcement pr : result) {
            pr.setDbAction(DbUpdateAction.NONE);
        }
        return new TreeSet<IReinforcement>(result);
    }
    
    public List<? extends IReinforcement> findAllReinforcements(){
        List<PlayerReinforcement> result = getDatabase()
            .createQuery(PlayerReinforcement.class, "find reinforcement")
    		.findList();
        for (PlayerReinforcement pr : result) {
            pr.setDbAction(DbUpdateAction.NONE);
        }
        return new ArrayList<IReinforcement>(result);
    }

    public IReinforcement findReinforcement(Block block) {
        return findReinforcement(block.getLocation());
    }

    public IReinforcement findReinforcement(Location location) {
        return getDatabase().createQuery(PlayerReinforcement.class, "find reinforcement where x = :x and y = :y and z = :z and world = :world")
                .setParameter("x", location.getX())
                .setParameter("y", location.getY())
                .setParameter("z", location.getZ())
                .setParameter("world", location.getWorld().getName())
                .findUnique();
    }

    public TreeSet<IReinforcement> findReinforcementsInChunk(Chunk c){
        String chunkId = MakeChunkId(c);
    	Set<PlayerReinforcement> result = getDatabase()
                .createQuery(
                    PlayerReinforcement.class,
                    "find reinforcement where chunk_id = :chunk_id")
    			.setParameter("chunk_id", chunkId)
    			.findSet();
        // This manually resets each reinforcement DB state. The ORM calls the
        //  object's property setter methods which incorrectly flags the object
        //  for SAVE.
        for (PlayerReinforcement pr : result) {
            pr.setDbAction(DbUpdateAction.NONE);
        }
        return new TreeSet<IReinforcement>(result);
    }
    
    public int countReinforcements(){
    	SqlRow row = getDatabase().createSqlQuery("select count(*) as count from reinforcement").findUnique();
    	return row.getInteger("count");  
    }
    
    public int countGroups(){
    	SqlRow row = getDatabase().createSqlQuery("select count(*) as count from faction").findUnique();
    	return row.getInteger("count");  
    }

	public int countPlayerGroups(String playerName) {
    	SqlRow row = getDatabase().createSqlQuery("select count(*) as count from faction where founder = :founder")
    			.setParameter("founder", playerName)
    			.findUnique();
    	return row.getInteger("count"); 
	}
	
	public void executeUpdateQuery(String query) {
		SqlUpdate updateQuery = getDatabase().createSqlUpdate(query);
		getDatabase().execute(updateQuery);
	}

	public void updateDatabase(){
		//this for when Citadel 2.0 is loaded after an older version of Citadel was previously installed
		SqlUpdate createMemberTable = getDatabase().createSqlUpdate
				("CREATE TABLE IF NOT EXISTS member (member_name varchar(255) NOT NULL, PRIMARY KEY (member_name))");
		getDatabase().execute(createMemberTable);

		SqlUpdate createModeratorTable = getDatabase().createSqlUpdate
				("CREATE TABLE IF NOT EXISTS moderator (member_name varchar(255) NOT NULL, faction_name varchar(255) NOT NULL)");
		getDatabase().execute(createModeratorTable);
		
		SqlUpdate createPersonalGroupTable = getDatabase().createSqlUpdate
				("CREATE TABLE IF NOT EXISTS personal_group (group_name varchar(255) NOT NULL, owner_name varchar(255) NOT NULL)");
		getDatabase().execute(createPersonalGroupTable);

		try {
			SqlUpdate alterFactionAddPassword = getDatabase().createSqlUpdate
				("ALTER TABLE faction ADD password varchar(255) DEFAULT NULL");
			getDatabase().execute(alterFactionAddPassword);
		} catch(PersistenceException e){
			//column already exists
		}

        try {
            SqlUpdate addReinforcementVersion = getDatabase().createSqlUpdate(
                "ALTER TABLE reinforcement ADD COLUMN version INT NOT NULL DEFAULT 0");
            getDatabase().execute(addReinforcementVersion);
        } catch(PersistenceException e){
           	//column already exists
        }

        try {
            // The initial add column statement is our indicator if the DB
            //  needs this reconstruction.
            SqlUpdate addReinforcementChunkId = getDatabase().createSqlUpdate(
                "ALTER TABLE reinforcement ADD COLUMN chunk_id VARCHAR(255)");
            getDatabase().execute(addReinforcementChunkId);

            addReinforcementChunkId = getDatabase().createSqlUpdate(
                "UPDATE reinforcement SET chunk_id = " +
                "CONCAT(world, ':', CONVERT(IF(x >= 0, x, x - 15) DIV 16, CHAR), ':'," +
                "CONVERT(IF(z >= 0, z, z - 15) DIV 16, CHAR))");
            getDatabase().execute(addReinforcementChunkId);

            addReinforcementChunkId = getDatabase().createSqlUpdate(
                "ALTER TABLE reinforcement ADD INDEX ix_chunk_id (chunk_id)");
            getDatabase().execute(addReinforcementChunkId);
        } catch(PersistenceException e){
           	//column already exists
        }

        try {
            SqlUpdate addFactionDisabled = getDatabase().createSqlUpdate(
                "ALTER TABLE faction ADD COLUMN discipline_flags TINYINT NOT NULL DEFAULT 0");
            getDatabase().execute(addFactionDisabled);
        } catch(PersistenceException e){
           	//column already exists
        }
        
        if(Citadel.getPlugin().getServer().getPluginManager().isPluginEnabled("Groups")) {
        	updateGroups();
        }
    }
	
	public void updateGroups() {
		
		List<SqlRow> oldGroups = getDatabase().createSqlQuery(
				"SELECT * FROM faction WHERE CHAR_LENGTH(name) > 16")
				.findList();
		
		for(SqlRow row : oldGroups) {
			
			String name = row.getString("name");
			
			if(name.length() > 16) {
				int digit = 2;
				String truncatedName = name.substring(0, 14);
				String tempName = truncatedName.concat(String.valueOf(digit));
				
				boolean duplicate = true;
				while(duplicate) {
					SqlRow findGroup = getDatabase().createSqlQuery("SELECT * FROM faction WHERE name = :name")
							.setParameter("name", tempName)
							.findUnique();
					
					if(findGroup != null) {
						digit++;
						tempName = truncatedName.concat(String.valueOf(digit));
					} else {
						duplicate = false;
						
						SqlUpdate updateGroup = getDatabase().createSqlUpdate(
								"Update faction set name = :newName WHERE name = :oldName")
								.setParameter("newName", tempName)
								.setParameter("oldName", name);
						getDatabase().execute(updateGroup);
						
						break;
					}
					
					
				}
			}
		}

		List<SqlRow> groups = getDatabase().createSqlQuery(
				"SELECT * FROM faction")
				.findList();

		for(SqlRow row : groups) {

			String name = row.getString("name");
			int type = 0;
			String password = row.getString("password");
			int isPersonalGroup = 0;
			String owner = row.getString("founder");
			
			if(name.length() > 16) {
				
			}

			SqlRow personalGroupRow = getDatabase().createSqlQuery(
					"SELECT * FROM personal_group WHERE group_name = :name")
					.setParameter("name", name)
					.findUnique();

			if(personalGroupRow != null) {
				isPersonalGroup = 1;
			}
			
			SqlRow findGroup = getDatabase().createSqlQuery(
					"SELECT * FROM groups_group WHERE name = :name")
					.setParameter("name", name)
					.findUnique();
			
			int groupId;
			if(findGroup == null) {
				SqlUpdate insertGroup = getDatabase().createSqlUpdate(
						"INSERT INTO groups_group (name, personal, type, password, update_time, create_time)" +
						"VALUES (:name, :personal, :type, :password, now(), now())")
						.setParameter("name", name)
						.setParameter("personal", isPersonalGroup)
						.setParameter("type", type)
						.setParameter("password", password);
				getDatabase().execute(insertGroup);

				SqlRow insertedGroup = getDatabase().createSqlQuery(
						"SELECT * FROM groups_group WHERE name = :name")
						.setParameter("name", name)
						.findUnique();
				
				groupId = insertedGroup.getInteger("id");
			} else {
				groupId = findGroup.getInteger("id");
			}
			
			SqlRow findOwner = getDatabase().createSqlQuery(
					"SELECT * FROM groups_member WHERE name = :name")
					.setParameter("name", owner)
					.findUnique();
			
			int ownerId;
			if(findOwner == null) {
				SqlUpdate insertOwner = getDatabase().createSqlUpdate(
						"INSERT INTO groups_member (name, update_time, create_time) VALUES (:name, now(), now())")
						.setParameter("name", owner);
				getDatabase().execute(insertOwner);
				
				SqlRow insertedOwner = getDatabase().createSqlQuery(
						"SELECT * FROM groups_member WHERE name = :owner")
						.setParameter("name", owner)
						.findUnique();
				
				ownerId = insertedOwner.getInteger("id");
			} else {
				ownerId = findOwner.getInteger("id");
			}
			
			SqlRow findOwnerGroupMember = getDatabase().createSqlQuery(
					"SELECT * FROM groups_group_member WHERE group_id = :groupId AND member_id = :memberId")
					.setParameter("groupId", groupId)
					.setParameter("memberId", ownerId)
					.findUnique();

			if(findOwnerGroupMember == null) {
				SqlUpdate groupMember = getDatabase().createSqlUpdate(
						"INSERT INTO groups_group_member (group_id, member_id, role, update_time, create_time) " +
						"VALUES (:groupId, :memberId, 0, now(), now())")
						.setParameter("groupId", groupId)
						.setParameter("memberId", ownerId);
				getDatabase().execute(groupMember);
			}

			List<SqlRow> members = getDatabase().createSqlQuery(
					"SELECT * FROM faction_member WHERE faction_name = :name")
					.setParameter("name", name)
					.findList();

			for(SqlRow memberRow : members) {

				String memberName = memberRow.getString("member_name");
				if(memberName.length() > 16) {
					continue;
				}
				SqlRow member = getDatabase().createSqlQuery(
						"SELECT * FROM member WHERE member_name = :member")
						.setParameter("member", memberName)
						.findUnique();

				SqlRow findMember = getDatabase().createSqlQuery(
						"SELECT * FROM groups_member WHERE name = :name")
						.setParameter("name", memberName)
						.findUnique();
				
				int memberId;
				if(findMember == null) {
					SqlUpdate insertMember = getDatabase().createSqlUpdate(
							"INSERT INTO groups_member (name, update_time, create_time) VALUES (:name, now(), now())")
							.setParameter("name", memberName);
					getDatabase().execute(insertMember);

					SqlRow updatedMember = getDatabase().createSqlQuery(
							"SELECT * FROM groups_member WHERE name = :name")
							.setParameter("name", memberName)
							.findUnique();
					memberId = updatedMember.getInteger("id");
				} else {
					memberId = findMember.getInteger("id");
				}
				
				SqlRow findGroupMember = getDatabase().createSqlQuery(
						"SELECT * FROM groups_group_member WHERE group_id = :groupId AND member_id = :memberId")
						.setParameter("groupId", groupId)
						.setParameter("memberId", memberId)
						.findUnique();

				if(findGroupMember == null) {
					SqlUpdate groupMember = getDatabase().createSqlUpdate(
							"INSERT INTO groups_group_member (group_id, member_id, role, update_time, create_time) " +
							"VALUES (:groupId, :memberId, 2, now(), now())")
							.setParameter("groupId", groupId)
							.setParameter("memberId", memberId);
					getDatabase().execute(groupMember);
				}
			}
			
			List<SqlRow> moderators = getDatabase().createSqlQuery(
					"SELECT * FROM moderator WHERE faction_name = :name")
					.setParameter("name", name)
					.findList();
			
			for(SqlRow moderatorRow : moderators) {
				String moderatorName = moderatorRow.getString("member_name");
				if(moderatorName.length() > 16) {
					continue;
				}
				SqlRow moderator = getDatabase().createSqlQuery(
						"SELECT * FROM member WHERE member_name = :member")
						.setParameter("member", moderatorName)
						.findUnique();

				SqlRow findMember = getDatabase().createSqlQuery(
						"SELECT * FROM groups_member WHERE name = :name")
						.setParameter("name", moderatorName)
						.findUnique();
				
				int memberId;
				if(findMember == null) {
					SqlUpdate insertMember = getDatabase().createSqlUpdate(
							"INSERT INTO groups_member (name, update_time, create_time) " +
							"VALUES (:name, now(), now())")
							.setParameter("name", moderatorName);
					getDatabase().execute(insertMember);

					SqlRow updatedMember = getDatabase().createSqlQuery(
							"SELECT * FROM groups_member WHERE name = :name")
							.setParameter("name", moderatorName)
							.findUnique();
					memberId = updatedMember.getInteger("id");
				} else {
					memberId = findMember.getInteger("id");
				}
				
				SqlRow findGroupMember = getDatabase().createSqlQuery(
						"SELECT * FROM groups_group_member WHERE group_id = :groupId AND member_id = :memberId")
						.setParameter("groupId", groupId)
						.setParameter("memberId", memberId)
						.findUnique();

				if(findGroupMember == null) {
					SqlUpdate groupMember = getDatabase().createSqlUpdate(
							"INSERT INTO groups_group_member (group_id, member_id, role, update_time, create_time) " +
							"VALUES (:groupId, :memberId, 1, now(), now())")
							.setParameter("groupId", groupId)
							.setParameter("memberId", memberId);
					getDatabase().execute(groupMember);
				}
			}
		}
	}

    protected void prepareDatabaseAdditionalConfig(DataSourceConfig dataSourceConfig, ServerConfig serverConfig) {
        if (sqlEnableLog) {
            serverConfig.setLoggingLevel(LogLevel.SQL);
            serverConfig.setLoggingDirectory(sqlLogDirectory);
        }
    }
}
