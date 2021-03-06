package com.archmageinc.RandomEncounters;

import com.archmageinc.RandomEncounters.Structures.Structure;
import com.archmageinc.RandomEncounters.Listeners.WorldListener;
import com.archmageinc.RandomEncounters.Listeners.CommandListener;
import com.archmageinc.RandomEncounters.Utilities.JSONReader;
import com.archmageinc.RandomEncounters.Listeners.PlacedMobListener;
import com.archmageinc.RandomEncounters.Encounters.Encounter;
import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.Mobs.Mob;
import com.archmageinc.RandomEncounters.Treasures.Treasure;
import com.archmageinc.RandomEncounters.Tasks.ExpansionTask;
import com.archmageinc.RandomEncounters.Tasks.LocationLoadingTask;
import com.archmageinc.RandomEncounters.Tasks.ResourceCollectionTask;
import com.archmageinc.RandomEncounters.Utilities.LocationManager;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    private int logLevel                                        =   0;
    
    /**
     * Debug Midas turns checked blocks to gold for verification.
     */
    private boolean midas                                       =   false;
    
    /**
     * Maximum amount of time we are allowed to lock the server in ms.
     */
    private int maxLockTime                                     =   10;
    
    /**
     * The set of encounter configurations for the plugin.
     */
    private final Set<Encounter> encounters                     =   new HashSet();
    
    /**
     * The set of PlacedEncounters / savedEncounters.
     */
    private final HashSet<PlacedEncounter> placedEncounters     =   new HashSet();
    
    private final Set<Material> defaultTrump                    =   new HashSet();
    
    /**
     * The task responsible for checking expansions.
     */
    private BukkitTask expansionTask;
    
    private BukkitTask resourceCollectionTask;
    
    
    /**
     * The main constructor for the plugin. 
     * 
     * This sets the "singleton" instance, since I cannot control how Bukkit loads plugins, 
     * I can't really use singleton here. Only one instantiation should be made, and that
     * is from Bukkit.
     */
    public RandomEncounters(){
        this.instance   =   this;
    }
    
    /**
     * Get the singleton instance of the plugin
     * 
     * @return 
     */
    public static RandomEncounters getInstance(){
        return RandomEncounters.instance;
    }
    
    private boolean checkDependencies(){
        if(getServer().getPluginManager().getPlugin("WorldEdit")==null){
            return false;
        }
        String version  =   getServer().getPluginManager().getPlugin("WorldEdit").getDescription().getVersion();
        version         =   version.replaceAll("[^0-9.].*", "");
        logMessage("Found WorldEdit version "+version);
        String minimum  =   "5.5.8";
        String[] vals1  =   minimum.split("\\.");
        String[] vals2  =   version.split("\\.");
        int i=0;
        while(i<vals1.length && i<vals2.length && vals1[i].equals(vals2[i])) {
          i++;
        }

        if (i<vals1.length && i<vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff)<=0;
        }

        return Integer.signum(vals1.length - vals2.length)<=0;
    }
    
    /**
     * Start 'er up.
     * 
     * @TODO This needs to save the config to the file system if it doesn't already exist
     */
    @Override
    public void onEnable(){
        if(!checkDependencies()){
            logError("WorldEdit version 5.5.8 or greater is required! Refusing to start");
            return;
        }
        reloadConfig();
	logLevel                =   getConfig().getInt("debug.loglevel");
        midas                   =   getConfig().getBoolean("debug.midas");
        maxLockTime             =   getConfig().getInt("maxLockTime");
        expansionTask           =   new ExpansionTask().runTaskTimer(this, 1200, 1200);
        resourceCollectionTask  =   new ResourceCollectionTask().runTaskTimer(this,1600,1600);
        if(logLevel>0){
            logMessage("Log Level set to: "+logLevel);
        }
        if(midas){
            logMessage("Debug midas enabled");
        }
        getServer().getPluginManager().registerEvents(new PlacedMobListener(),this);
        getServer().getPluginManager().registerEvents(new WorldListener(),this);
        getCommand("re").setExecutor(new CommandListener());
        loadConfigurations();
        loadPlacedEncounters();
    }
    
    
    /**
     * Shut 'er down.
     */
    @Override
    public void onDisable(){
        saveConfig();
        savePlacedEncounters();
        logMessage("Saved "+placedEncounters.size()+" Placed Encounters.");
    }
    
    public void loadConfigurations(){
        loadDefaultTrump();
        loadStructures();
        loadTreasures();
        loadMobs();
        loadEncounters();
    }
    
    private void loadDefaultTrump(){
        try{
            String structureFileName    =   getConfig().getString("structureConfig");
            JSONObject structureConfig  =   JSONReader.getInstance().read(getDataFolder()+"/"+structureFileName);
            JSONArray jsonTrump         =   (JSONArray) structureConfig.get("defaultTrump");
            if(jsonTrump!=null){
                for(int i=0;i<jsonTrump.size();i++){
                    Material material   =   Material.getMaterial((String) jsonTrump.get(i));
                    if(material!=null){
                        defaultTrump.add(material);
                    }else{
                        logWarning("Invalid material "+(String) jsonTrump.get(i)+" in default trump");
                    }
                }
                logMessage("Loaded "+defaultTrump.size()+" Default trump materials");
            }
        }catch(ClassCastException e){
            logError("Invalid base default trump configuration: "+e.getMessage());
        }
    }
    
    /**
     * Load the structure configurations from the file system.
     * 
     * @TODO Needs to write the default structure file to the file system if it doesn't already exist
     */
    private void loadStructures(){
        try{
            String structureFileName    =   getConfig().getString("structureConfig");
            JSONObject structureConfig  =   JSONReader.getInstance().read(getDataFolder()+"/"+structureFileName);
            JSONArray jsonStructures    =   (JSONArray) structureConfig.get("structures");
            if(jsonStructures!=null){
                for(int i=0;i<jsonStructures.size();i++){
                    Structure.getInstance((JSONObject) jsonStructures.get(i),true);
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
    private void loadMobs(){
        try{
            String mobFileName    =   getConfig().getString("mobConfig");
            JSONObject mobConfig  =   JSONReader.getInstance().read(getDataFolder()+"/"+mobFileName);
            JSONArray jsonMobs    =   (JSONArray) mobConfig.get("mobs");
            if(jsonMobs!=null){
                for(int i=0;i<jsonMobs.size();i++){
                    Mob.getInstance((JSONObject) jsonMobs.get(i),true);
                }
                logMessage("Loaded "+jsonMobs.size()+" Mob configurations");
            }else{
                logWarning("No Mob configurations found");
            }
        }catch(ClassCastException e){
            logError("Invalid base Mob configuration: "+e.getMessage());
        }
        
    }
    
    private void loadTreasures(){
        try{
            String treasureFileName     =   getConfig().getString("treasureConfig");
            JSONObject treasureConfig   =   JSONReader.getInstance().read(getDataFolder()+"/"+treasureFileName);
            JSONArray jsonTreasures     =   (JSONArray) treasureConfig.get("treasures");
            if(jsonTreasures!=null){
                for(int i=0;i<jsonTreasures.size();i++){
                    Treasure.getInstance((JSONObject) jsonTreasures.get(i),true);
                }
                logMessage("Loaded "+jsonTreasures.size()+" Treasure configurations");
            }else{
                logWarning("No Treasure configurations found");
            }
        }catch(ClassCastException e){
            logError("Invalid base Treasure configuration: "+e.getMessage());
        }
    }
    
    /**
     * Load the encounter configurations from the file system.
     * 
     * @TODO Needs to write the default encounter file to the file system if it doesn't already exist
     */
    private void loadEncounters(){
        try{
            encounters.clear();
            String encounterFileName    =   getConfig().getString("encounterConfig");
            JSONObject encounterConfig  =   JSONReader.getInstance().read(getDataFolder()+"/"+encounterFileName);
            JSONArray jsonEncounters    =   (JSONArray) encounterConfig.get("encounters");
            if(jsonEncounters!=null){
                for(int i=0;i<jsonEncounters.size();i++){
                    encounters.add(Encounter.getInstance((JSONObject) jsonEncounters.get(i),true));
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
    private void loadPlacedEncounters(){
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
                    PlacedEncounter placedEncounter   =   PlacedEncounter.getInstance((JSONObject) jsonEncounters.get(i));
                    if(!placedEncounter.isSacked()){
                        placedEncounter.setLoadingTask(new LocationLoadingTask(placedEncounter,getDataFolder()+"/locations/"+placedEncounter.getUUID().toString()+".dat"));
                        placedEncounters.add(placedEncounter);
                    }
                }
                logMessage("Loaded "+jsonEncounters.size()+" PlacedEncounter configurations");
            }else{
                logWarning("No PlacedEncounter configurations found");
            }
        }catch(ClassCastException e){
            logError("Invalid base PlacedEncounter configuration: "+e.getMessage());
        }
        setupPlacedEncounterRelationships();
    }
    
    private void setupPlacedEncounterRelationships(){
        for(PlacedEncounter encounter : placedEncounters){
            for(UUID id : encounter.getChildren()){
                PlacedEncounter child   =   PlacedEncounter.getInstance(id);
                if(child!=null){
                    child.setParent(encounter);
                }
            }
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
            LocationManager.saveLocations(getDataFolder()+"/locations/"+encounter.getUUID().toString()+".dat", encounter.getBlockLocations());
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
        if(logLevel>6){
            logMessage("Removing saved encounter: "+encounter.getName()+" at "+encounter.getLocation().toString()+" there are "+placedEncounters.size()+" saved");
        }
        placedEncounters.remove(encounter);
        LocationManager.removeLocations(getDataFolder()+"/locations/"+encounter.getUUID().toString()+".dat");
        savePlacedEncounters();
    }
    
    /**
     * Log a message to the console.
     * @param message 
     */
    public void logMessage(String message){
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"[RandomEncounters] [v"+getDescription().getVersion()+"]: "+ChatColor.RESET+message);
    }
    
    /**
     * Log an error to the console.
     * @param message 
     */
    public void logError(String message){
        getServer().getConsoleSender().sendMessage(ChatColor.RED+"[RandomEncounters] [v"+getDescription().getVersion()+"]: "+ChatColor.RESET+message);
    }
    
    /**
     * Log a warning to the console.
     * @param message 
     */
    public void logWarning(String message){
        getServer().getConsoleSender().sendMessage(ChatColor.YELLOW+"[RandomEncounters] [v"+getDescription().getVersion()+"]: "+ChatColor.RESET+message);
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
     * Get the maximum time in ms we are allowed to lock the server.
     * @return 
     */
    public int lockTime(){
        int tasks   =   getServer().getScheduler().getPendingTasks().size();
        return ((Double) Math.ceil(maxLockTime/tasks)).intValue();
    }
    
    public Set<Material> getDefaultTrump(){
        return defaultTrump;
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
    
    public HashSet<PlacedEncounter> getPlacedEncounters(Encounter type){
        HashSet<PlacedEncounter> placements =   new HashSet();
        for(PlacedEncounter check : placedEncounters){
            if(check.getEncounter().equals(type)){
                placements.add(check);
            }
        }
        return placements;
    }
    
}
