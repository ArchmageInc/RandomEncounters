package com.archmageinc.RandomEncounters;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * The Main Class of the Plugin
 * 
 * @author ArchmageInc
 */
public class RandomEncounters extends JavaPlugin {
    
    /**
     * The static singleton instance of the Plugin.
     */
    private static RandomEncounters instance;
    
    /**
     * The loglevel determines how much to spam the console.
     */
    private int logLevel                                =   0;
    
    /**
     * Debug Midas turns checked blocks to gold for verification.
     */
    private boolean midas                               =   false;
    
    /**
     * The set of encounter configurations for the plugin.
     */
    private Set<Encounter> encounters                   =   new HashSet();
    
    /**
     * The set of PlacedEncounters / savedEncounters.
     */
    private HashSet<PlacedEncounter> placedEncounters   =   new HashSet();
    
    /**
     * The task responsible for checking expansions.
     */
    private BukkitTask expansionTask;
    
    
    /**
     * The main constructor for the plugin. 
     * 
     * This sets the "singleton" instance, since I cannot control how Bukkit loads plugins, 
     * I can't really use singleton here. Only one instantiation should be made, and that
     * is from Bukkit.
     */
    public RandomEncounters(){
        RandomEncounters.instance   =   this;
    }
    
    /**
     * Get the singleton instance of the plugin
     * 
     * @return 
     */
    public static RandomEncounters getInstance(){
        return RandomEncounters.instance;
    }
    
    /**
     * Start 'er up.
     * 
     * @TODO This needs to save the config to the file system if it doesn't already exist
     */
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
    
    
    /**
     * Shut 'er down.
     */
    @Override
    public void onDisable(){
        saveConfig();
        savePlacedEncounters();
    }
    
    /**
     * Load the structure configurations from the file system.
     * 
     * @TODO Needs to write the default structure file to the file system if it doesn't already exist
     */
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
    
    /**
     * Load the mob configurations from the file system.
     * 
     * @TODO Needs to write the default mob file to the file system if it doesn't already exist
     */
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
    
    /**
     * Load the encounter configurations from the file system.
     * 
     * @TODO Needs to write the default encounter file to the file system if it doesn't already exist
     */
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
    
    /**
     * Load the placed encounters and mobs from the file system.
     * 
     * @TODO Needs to write the default savedEncounter file to the file system if it doesn't already exist
     */
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
                    PlacedEncounter encounter   =   PlacedEncounter.getInstance((JSONObject) jsonEncounters.get(i));
                    if(!encounter.isSacked()){
                        placedEncounters.add(encounter);
                    }
                }
                logMessage("Loaded "+jsonEncounters.size()+" PlacedEncounter configurations");
            }else{
                logWarning("No PlacedEncounter configurations found");
            }
        }catch(ClassCastException e){
            logError("Invalid base PlacedEncounter configuration: "+e.getMessage());
        }
        
    }
    
    /**
     * Save the placed encounters and mobs to the file system.
     */
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
    
    /**
     * Add a PlacedEncounter to the list.
     * 
     * @param encounter The PlacedEncounter that was newly generated.
     */
    public void addPlacedEncounter(PlacedEncounter encounter){
        placedEncounters.add(encounter);
        if(logLevel>5){
            logMessage("Placed encounter "+encounter.getName()+" at "+encounter.getLocation().toString()+" there are "+placedEncounters.size()+" saved");
        }
        savePlacedEncounters();
    }
    
    /**
     * Remove a placed encounter from the list
     * 
     * @param encounter The PlacedEncounter to remove
     */
    public void removePlacedEncounter(PlacedEncounter encounter){
        placedEncounters.remove(encounter);
        if(logLevel>6){
            logMessage("Removing saved encounter: "+encounter.getName()+" at "+encounter.getLocation().toString()+" there are "+placedEncounters.size()+" saved");
        }
        savePlacedEncounters();
    }
    
    /**
     * Log a message to the console.
     * @param message 
     */
    public void logMessage(String message){
        getLogger().info("[v"+getDescription().getVersion()+"]: "+message);
    }
    
    /**
     * Log an error to the console.
     * @param message 
     */
    public void logError(String message){
        getLogger().severe("[v"+getDescription().getVersion()+"]: "+message);
    }
    
    /**
     * Log a warning to the console.
     * @param message 
     */
    public void logWarning(String message){
        getLogger().warning("[v"+getDescription().getVersion()+"]: "+message);
    }
    
    /**
     * Get the current log level.
     * @return 
     * @see RandomEncounters#logLevel
     */
    public int getLogLevel(){
        return logLevel;
    }
    
    /**
     * Get the midas configuration setting
     * @return 
     * @see RandomEncounters#midas
     */
    public boolean midas(){
        return midas;
    }
    
    /**
     * Get the set of Encounter configurations.
     * @return 
     */
    public Set<Encounter> getEncounters(){
        return encounters;
    }
    
    /**
     * Get the set of PlacedEncounters.
     * @return 
     */
    public HashSet<PlacedEncounter> getPlacedEncounters(){
        return placedEncounters;
    }
    
}
