package com.archmageinc.RandomEncounters;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class RandomEncounters extends JavaPlugin {
    
    private static RandomEncounters instance;
    private int logLevel                                =   0;
    private boolean midas                               =   false;
    private Set<Encounter> encounters                   =   new HashSet();
    private HashSet<PlacedEncounter> placedEncounters   =   new HashSet();
    private BukkitTask expansionTask;
    
    public RandomEncounters(){
        RandomEncounters.instance   =   this;
    }
    
    public static RandomEncounters getInstance(){
        return RandomEncounters.instance;
    }
    
    @Override
    public void onEnable(){
        reloadConfig();
	logLevel        =   getConfig().getInt("debug.loglevel");
        midas           =   getConfig().getBoolean("debug.midas");
        expansionTask   =   new ExpansionTask().runTaskTimer(this, 1200, 1200);
        if(logLevel>0){
            logMessage("Log Level set to: "+logLevel);
        }
        if(midas){
            logMessage("Debug midas enabled");
        }
        getServer().getPluginManager().registerEvents(new PlacedMobListener(),this);
        getServer().getPluginManager().registerEvents(new WorldListener(),this);
        loadStructures();
        loadMobs();
        loadEncounters();
        loadPlacedEncounters();
    }
    
    @Override
    public void onDisable(){
        saveConfig();
        savePlacedEncounters();
    }
    
    public void loadStructures(){
        try{
            String structureFileName    =   getConfig().getString("structureConfig");
            JSONObject structureConfig  =   JSONReader.getInstance().read(getDataFolder()+"/"+structureFileName);
            JSONArray jsonStructures    =   (JSONArray) structureConfig.get("structures");
            if(jsonStructures!=null){
                for(int i=0;i<jsonStructures.size();i++){
                    Structure.getInstance((JSONObject) jsonStructures.get(i));
                }
                logMessage("Loaded "+jsonStructures.size()+" Structure configurations");
            }else{
                logWarning("No structure configurations found");
            }
        }catch(ClassCastException e){
            logError("Invalid base structure configuration: "+e.getMessage());
        }
        
    }
    
    public void loadMobs(){
        try{
            String mobFileName    =   getConfig().getString("mobConfig");
            JSONObject mobConfig  =   JSONReader.getInstance().read(getDataFolder()+"/"+mobFileName);
            JSONArray jsonMobs    =   (JSONArray) mobConfig.get("mobs");
            if(jsonMobs!=null){
                for(int i=0;i<jsonMobs.size();i++){
                    Mob.getInstance((JSONObject) jsonMobs.get(i));
                }
                logMessage("Loaded "+jsonMobs.size()+" Mob configurations");
            }else{
                logWarning("No Mob configurations found");
            }
        }catch(ClassCastException e){
            logError("Invalid base Mob configuration: "+e.getMessage());
        }
        
    }
    
    public void loadEncounters(){
        try{
            String encounterFileName    =   getConfig().getString("encounterConfig");
            JSONObject encounterConfig  =   JSONReader.getInstance().read(getDataFolder()+"/"+encounterFileName);
            JSONArray jsonEncounters    =   (JSONArray) encounterConfig.get("encounters");
            if(jsonEncounters!=null){
                for(int i=0;i<jsonEncounters.size();i++){
                    encounters.add(Encounter.getInstance((JSONObject) jsonEncounters.get(i)));
                }
                logMessage("Loaded "+jsonEncounters.size()+" Encounter configurations");
            }else{
                logWarning("No Encounter configurations found");
            }
        }catch(ClassCastException e){
            logError("Invalid base Encounter configuration: "+e.getMessage());
        }
        
    }
    
    public void loadPlacedEncounters(){
        if(encounters.isEmpty()){
            logError("Not attempting to load placed encounters due to no loaded encounter configurations");
            return;
        }
        try{
            String encounterFileName    =   getConfig().getString("savedEncounters");
            JSONObject encounterConfig  =   JSONReader.getInstance().read(getDataFolder()+"/"+encounterFileName);
            JSONArray jsonEncounters    =   (JSONArray) encounterConfig.get("savedEncounters");
            if(jsonEncounters!=null){
                for(int i=0;i<jsonEncounters.size();i++){
                    placedEncounters.add(PlacedEncounter.getInstance((JSONObject) jsonEncounters.get(i)));
                }
                logMessage("Loaded "+jsonEncounters.size()+" PlacedEncounter configurations");
            }else{
                logWarning("No PlacedEncounter configurations found");
            }
        }catch(ClassCastException e){
            logError("Invalid base PlacedEncounter configuration: "+e.getMessage());
        }
        
    }
    
    public void savePlacedEncounters(){
        if(placedEncounters.isEmpty())
            return;
        
        String encounterFileName        =   getConfig().getString("savedEncounters");
        JSONObject jsonConfiguration    =   new JSONObject();
        JSONArray savedEncounters       =   new JSONArray();
        for(PlacedEncounter encounter : placedEncounters){
            savedEncounters.add(encounter.toJSON());
        }
        jsonConfiguration.put("savedEncounters", savedEncounters);
        
        JSONReader.getInstance().write(getDataFolder()+"/"+encounterFileName, jsonConfiguration);
    }
    
    public void addPlacedEncounter(PlacedEncounter encounter){
        if(logLevel>5){
            logMessage("Placed encounter "+encounter.getName()+" at "+encounter.getLocation().toString());
        }
        placedEncounters.add(encounter);
        savePlacedEncounters();
    }
    
    public void logMessage(String message){
        getLogger().info("[v"+getDescription().getVersion()+"]: "+message);
    }
    
    public void logError(String message){
        getLogger().severe("[v"+getDescription().getVersion()+"]: "+message);
    }
    
    public void logWarning(String message){
        getLogger().warning("[v"+getDescription().getVersion()+"]: "+message);
    }
    public int getLogLevel(){
        return logLevel;
    }
    public boolean midas(){
        return midas;
    }
    public Set<Encounter> getEncounters(){
        return encounters;
    }
    public HashSet<PlacedEncounter> getPlacedEncounters(){
        return placedEncounters;
    }
    
}
