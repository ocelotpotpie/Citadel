package com.untamedears.citadel.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.command.commands.BypassCommand;
import com.untamedears.citadel.command.commands.ConsoleCommands;
import com.untamedears.citadel.command.commands.FortifyCommand;
import com.untamedears.citadel.command.commands.GroupCommand;
import com.untamedears.citadel.command.commands.InfoCommand;
import com.untamedears.citadel.command.commands.MaterialsCommand;
import com.untamedears.citadel.command.commands.NonReinforceableCommand;
import com.untamedears.citadel.command.commands.OffCommand;
import com.untamedears.citadel.command.commands.PlayerStatsCommand;
import com.untamedears.citadel.command.commands.PrivateCommand;
import com.untamedears.citadel.command.commands.PublicCommand;
import com.untamedears.citadel.command.commands.ReinforceCommand;
import com.untamedears.citadel.command.commands.SecurableCommand;
import com.untamedears.citadel.command.commands.StatsCommand;
import com.untamedears.citadel.command.commands.VersionCommand;

public class CommandHandler {

	private Map<String, Command> commands = new LinkedHashMap<String, Command>();
	private Map<String, Command> identifiers = new HashMap<String, Command>();

    public void registerCommands() {
        this.addCommand(new InfoCommand());
        this.addCommand(new BypassCommand());
        this.addCommand(new FortifyCommand());
        this.addCommand(new ReinforceCommand());
        this.addCommand(new OffCommand());
        this.addCommand(new ConsoleCommands());
        this.addCommand(new GroupCommand());
        this.addCommand(new MaterialsCommand());
        this.addCommand(new NonReinforceableCommand());
        this.addCommand(new PrivateCommand());
        this.addCommand(new PublicCommand());
        this.addCommand(new SecurableCommand());
        this.addCommand(new StatsCommand());
        this.addCommand(new PlayerStatsCommand());
        this.addCommand(new VersionCommand());
    }

	public void addCommand(Command command){
		this.commands.put(command.getName().toLowerCase(), command);
		for(String ident : command.getIdentifiers()){
			this.identifiers.put(ident.toLowerCase(), command);
		}
	}
	
	public boolean dispatch(CommandSender sender, String label, String[] args){
		for(int argsIncluded = args.length; argsIncluded >= 0; argsIncluded--){
			StringBuilder identifier = new StringBuilder(label);
			for(int i = 0; i < argsIncluded; i++){
				identifier.append(" ").append(args[i]);
			}

			Command cmd = getCmdFromIdent(identifier.toString(), sender);
			if(cmd == null){
				continue;
			}
			String[] realArgs = (String[])Arrays.copyOfRange(args, argsIncluded, args.length);

			if(!cmd.isInProgress(sender)){
				if((realArgs.length < cmd.getMinArguments()) || (realArgs.length > cmd.getMaxArguments())){
					displayCommandHelp(cmd, sender);
					return true;
				}
				if((realArgs.length > 0) && (realArgs[0].equals("?"))){
					displayCommandHelp(cmd, sender);
					return true;
				}
			}
			
			try {
				cmd.execute(sender, realArgs);
			} catch(Exception e){
			  Citadel.printStackTrace(e);
			}
			return true;
		}
		return true;
	}
	
	private void displayCommandHelp(Command cmd, CommandSender sender){
		sender.sendMessage(new StringBuilder().append("§cCommand:§e " ).append(cmd.getName()).toString());
		sender.sendMessage(new StringBuilder().append("§cDescription:§e " ).append(cmd.getDescription()).toString());
		sender.sendMessage(new StringBuilder().append("§cUsage:§e ").append(cmd.getUsage()).toString());
	}

	private Command getCmdFromIdent(String ident, CommandSender executor) {
		ident = ident.toLowerCase();
		if(this.identifiers.containsKey(ident)){
			return (Command)this.identifiers.get(ident);
		}
		
		for(Command cmd : this.commands.values()){
			if(cmd.isIdentifier(executor, ident)){
				return cmd;
			}
		}
		
		return null;
	}
}
