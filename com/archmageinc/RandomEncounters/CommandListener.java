package com.archmageinc.RandomEncounters;

import com.archmageinc.RandomEncounters.Tasks.ChunkLocatorTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author ArchmageInc
 */
public class CommandListener implements CommandExecutor,EncounterPlacer{
    private Encounter radiusEncounter;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("re")){
            if(args.length<1){
                return false;
            }
            if(args[0].equalsIgnoreCase("place") || args[0].equalsIgnoreCase("check")){
                if((sender instanceof Player) && args.length<2){
                    sender.sendMessage("Usage: /re <place,check> <EncounterName> [<world> <x> <y> <z>]");
                    return true;
                }
                if(!(sender instanceof Player) && args.length<6){
                    sender.sendMessage("Usage: /re <place,check> <EncounterName> <world> <x> <y> <z>");
                    return true;
                }
                Location location;
                if(args.length>=6){
                    try{
                        String worldName    =   args[2];
                        Integer x           =   Integer.parseInt(args[3]);
                        Integer y           =   Integer.parseInt(args[4]);
                        Integer z           =   Integer.parseInt(args[5]);
                        World world         =   RandomEncounters.getInstance().getServer().getWorld(worldName);
                        if(world==null){
                            sender.sendMessage("World "+worldName+" was not found!");
                            return true;
                        }
                        location    =   new Location(world,x,y,z);
                    }catch(NumberFormatException e){
                        sender.sendMessage("Coordinates must be numeric!");
                        return true;
                    }
                }else{
                    location    =   ((Player) sender).getLocation();
                }
                String encounterName    =   args[1];
                if(args[0].equalsIgnoreCase("place")){
                    placeEncounter(sender,encounterName,location);
                }else if(args[0].equalsIgnoreCase("check")){
                    checkEncounter(sender,encounterName,location);
                }
                return true;
            }
            if(args[0].equalsIgnoreCase("radius")){
                if((sender instanceof Player) && args.length<2){
                    sender.sendMessage("Usage: /re radius <EncounterName> <distance> [<world> <chunk x> <chunk z>]");
                    return true;
                }
                if(!(sender instanceof Player) && args.length<6){
                    sender.sendMessage("Usage: /re radius <EncounterName> <distance> <world> <chunk x> <chunk z>");
                    return true;
                }
                Integer distance        =   0;
                try{
                    distance    =   Integer.parseInt(args[2]);
                }catch(NumberFormatException e){
                    sender.sendMessage("Distance must be numeric!");
                    return true;
                }
                Chunk chunk;
                if(args.length>=6){
                    try{
                        String worldName    =   args[3];
                        Integer x           =   Integer.parseInt(args[4]);
                        Integer z           =   Integer.parseInt(args[5]);
                        World world         =   RandomEncounters.getInstance().getServer().getWorld(worldName);
                        if(world==null){
                            sender.sendMessage("World "+worldName+" was not found!");
                            return true;
                        }
                        chunk    =   world.getChunkAt(x, z);
                    }catch(NumberFormatException e){
                        sender.sendMessage("Coordinates must be numeric!");
                        return true;
                    }
                }else{
                    chunk    =   ((Player) sender).getLocation().getChunk();
                }
                String encounterName    =   args[1];
                checkRadius(sender,encounterName,chunk,distance);
                return true;
            }
            if(args[0].equalsIgnoreCase("reload")){
                reloadConfigurations();
                return true;
            }
        }
        return false;
    }
    
    private void checkRadius(CommandSender sender,String encounterName,Chunk chunk,Integer distance){
        sender.sendMessage("This might take a while, and I will not tell you if I fail or succeed");
        Encounter encounter =   Encounter.getInstance(encounterName);
        if(encounter==null){
            sender.sendMessage("Encounter "+encounterName+" was not found!");
            return;
        }
        if(chunk==null){
            sender.sendMessage("Not a valid chunk!");
            return;
        }
        radiusEncounter =   encounter;
        (new ChunkLocatorTask(this,chunk,distance)).runTaskTimer(RandomEncounters.getInstance(), 1, 1);
        
    }
    
    private void checkEncounter(CommandSender sender,String encounterName,Location location){
        sender.sendMessage("This might take a while, and I will not tell you if I fail or succeed");
        Encounter encounter =   Encounter.getInstance(encounterName);
        if(encounter==null){
            sender.sendMessage("Encounter "+encounterName+" was not found!");
            return;
        }
        if(location==null){
            sender.sendMessage("Not a valid location!");
            return;
        }
        encounter.checkPlace(location.getChunk(),true);
    }
    
    private void placeEncounter(CommandSender sender,String encounterName,Location location){
        Encounter encounter =   Encounter.getInstance(encounterName);
        if(encounter==null){
            sender.sendMessage("Encounter "+encounterName+" was not found!");
            return;
        }
        if(location==null){
            sender.sendMessage("Not a valid location!");
            return;
        }
        RandomEncounters.getInstance().addPlacedEncounter(PlacedEncounter.create(encounter, location));
        sender.sendMessage("Encounter created dispite the terrain");
    }
    
    private void reloadConfigurations(){
        RandomEncounters.getInstance().loadConfigurations();
    }

    @Override
    public void addPlacedEncounter(PlacedEncounter newEncounter) {
        if(newEncounter!=null){
            RandomEncounters.getInstance().addPlacedEncounter(newEncounter);
        }
    }

    @Override
    public Encounter getEncounter() {
        return radiusEncounter;
    }
    
}
