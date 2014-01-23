package com.archmageinc.RandomEncounters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class PlacedEncounter {
    protected UUID uuid;
    protected Location location;
    protected Encounter encounter;
    protected Set<PlacedMob> mobs                   =   new HashSet();
    protected Boolean sacked                        =   false;
    protected Set<UUID> placedExpansions            =   new HashSet();
    protected Set<Expansion> expansions             =   new HashSet();
    protected static Set<PlacedEncounter> instances =   new HashSet();
    protected List<Location> spawnLocations         =   new ArrayList();
    
    public static PlacedEncounter getInstance(UUID uuid){
        for(PlacedEncounter instance : instances){
            if(instance.getUUID().equals(uuid)){
                return instance;
            }
        }
        return null;
    }
    
    public static PlacedEncounter getInstance(JSONObject jsonConfiguration){
        try{
            UUID jsonUUID             =   UUID.fromString((String) jsonConfiguration.get("uuid"));
            PlacedEncounter located   =   PlacedEncounter.getInstance(jsonUUID);
            if(located!=null){
                return located;
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid PlacedEncounter configuration: "+e.getMessage());
        }catch(IllegalArgumentException e){
            RandomEncounters.getInstance().logError("Invalid UUID in PlacedEncounter configuration: "+e.getMessage());
        }
        return new PlacedEncounter(jsonConfiguration);
    }
    
    public static PlacedEncounter create(Encounter encounter,Location location){
        return new PlacedEncounter(encounter,location);
    }
    
    
    protected PlacedEncounter(JSONObject jsonConfiguration){
        try{
            uuid          =   UUID.fromString((String) jsonConfiguration.get("uuid"));
            sacked        =   (Boolean) jsonConfiguration.get("sacked");
            encounter     =   Encounter.getInstance((String) jsonConfiguration.get("encounter"));
            if(encounter==null){
                RandomEncounters.getInstance().logError("Missing Encounter ("+(String) jsonConfiguration.get("encounter")+") from PlacedEncounter configuration");
            }
            JSONArray jsonMobs   =   (JSONArray) jsonConfiguration.get("mobs");
            if(jsonMobs!=null){
                for(int i=0;i<jsonMobs.size();i++){
                    mobs.add(PlacedMob.getInstance((JSONObject) jsonMobs.get(i),this));
                }
            }
            JSONObject jsonLocation =   (JSONObject) jsonConfiguration.get("location");
            location                =   new Location(RandomEncounters.getInstance().getServer().getWorld((String) jsonLocation.get("world")),(Long) jsonLocation.get("x"),(Long) jsonLocation.get("y"),(Long) jsonLocation.get("z"));
            
            JSONArray jsonExpansions    =   (JSONArray) jsonConfiguration.get("expansions");
            if(jsonExpansions!=null){
                for(int i=0;i<jsonExpansions.size();i++){
                    JSONObject jsonExpansion    =   (JSONObject) jsonExpansions.get(i);
                    try{
                        UUID expansionUUID          =   UUID.fromString((String) jsonExpansion.get("uuid"));
                        placedExpansions.add(expansionUUID);
                    }catch(IllegalArgumentException e){
                        RandomEncounters.getInstance().logError("Invalid UUID in PlacedEncounter expansion configuration: "+e.getMessage());
                    }
                }
            }
            setupExpansions();
            instances.add(this);
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid PlacedEncounter configuration: "+e.getMessage());
        }catch(IllegalArgumentException e){
            RandomEncounters.getInstance().logError("Invalid UUID in PlacedEncounter configuration: "+e.getMessage());
        }
    }
    
    protected PlacedEncounter(Encounter encounter,Location location){
        this.uuid       =   UUID.randomUUID();
        this.encounter  =   encounter;
        this.location   =   location;
        encounter.getStructure().place(encounter,location);
        populateSafeSpawnLocations();
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("Prepairing to place "+encounter.getMobs().size()+" mobs for encounter "+encounter.getName());
        }
        for(Mob mob : encounter.getMobs()){
            Long count   =   mob.getCount();
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("  -Prepairing to place "+count+" "+mob.getType().name());
            }
            for(int i=0;i<count;i++){
                this.mobs.add(mob.placeMob(this,location));
            }
        }
        setupExpansions();
        instances.add(this);        
    }
    
    protected final void setupExpansions(){
        expansions.clear();
        for(Expansion expansion : encounter.getExpansions()){
            try {
                expansions.add(expansion.clone());
            } catch (CloneNotSupportedException e) {
                RandomEncounters.getInstance().logError("Clone failed for expansion: "+e.getMessage());
            }
        }
    }
    
    protected final void populateSafeSpawnLocations(){
        Structure structure =   encounter.getStructure();
        Integer minX        =   location.getBlockX()-(structure.getWidth()/2);
        Integer maxX        =   location.getBlockX()+(structure.getWidth()/2);
        Integer minY        =   location.getBlockY()-(structure.getHeight()/2);
        Integer maxY        =   location.getBlockY()+(structure.getHeight()/2);
        Integer minZ        =   location.getBlockZ()-(structure.getLength()/2);
        Integer maxZ        =   location.getBlockZ()+(structure.getLength()/2);
        for(int x=minX;x<maxX;x++){
            for(int y=minY;y<maxY;y++){
                for(int z=minZ;z<maxZ;z++){
                    Block block =   location.getWorld().getBlockAt(x, y, z);
                    if(block.getType().isSolid() && block.getRelative(BlockFace.UP).getType().equals(Material.AIR) && block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().equals(Material.AIR)){
                        spawnLocations.add(block.getLocation());
                    }
                }
            }
        }
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("Found "+spawnLocations.size()+" safe spawn locations for encounter");
        }
        if(spawnLocations.isEmpty()){
            RandomEncounters.getInstance().logWarning("Unable to locate any safe spawnning locations for encounter: "+uuid);
        }
    }
    
    public Location findSafeSpawnLocation(){
        if(spawnLocations.isEmpty()){
            populateSafeSpawnLocations();
        }
        if(spawnLocations.isEmpty()){
            return null;
        }
        Collections.shuffle(spawnLocations);
        return spawnLocations.get(1);
    }
    
    public void addExpansion(PlacedEncounter expansion){
        placedExpansions.add(expansion.getUUID());
    }
    
    public UUID getUUID(){
        return uuid;
    }
    
    public Location getLocation(){
        return location;
    }
    
    public void notifyMobDeath(PlacedMob mob){
        mobs.remove(mob);
    }
    
    public Encounter getEncounter(){
        return encounter;
    }
    
    public String getName(){
        return encounter.getName();
    }
    
    public Set<UUID> getPlacedExpansions(){
        return placedExpansions;
    }
    
    public Set<Expansion> getExpansions(){
        return expansions;
    }
    public JSONObject toJSON(){
        JSONObject jsonConfiguration    =   new JSONObject();
        jsonConfiguration.put("uuid", uuid.toString());
        jsonConfiguration.put("encounter",encounter.getName());
        jsonConfiguration.put("sacked",sacked);
        
        JSONObject jsonLocation         =   new JSONObject();
        jsonLocation.put("world",location.getWorld().getName());
        jsonLocation.put("x",location.getBlockX());
        jsonLocation.put("y",location.getBlockY());
        jsonLocation.put("z",location.getBlockZ());
        jsonConfiguration.put("location", jsonLocation);
        
        JSONArray jsonMobs              =   new JSONArray();
        for(PlacedMob mob : mobs){
            jsonMobs.add(mob.toJSON());
        }
        jsonConfiguration.put("mobs",jsonMobs);
        
        JSONArray jsonExpansions        =   new JSONArray();
        for(UUID expansion : placedExpansions){
            JSONObject jsonExpansion    =   new JSONObject();
            jsonExpansion.put("uuid",expansion.toString());
            jsonExpansions.add(jsonExpansion);
        }
        jsonConfiguration.put("expansions", jsonExpansions);
        
        return jsonConfiguration;
    }
}
