package com.archmageinc.RandomEncounters.Structures;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Tasks.StructurePlacementTask;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Represents a Structure to be placed in the world.
 * 
 * All hooks to the WorldEdit API should remain within this file.
 * 
 * 
 * @author ArchmageInc
 * @see com.sk89q.worldedit
 */
public class Structure {
    
    /**
     * The unique name of the structure configuration.
     */
    private String name;
    
    /**
     * This file name including directories of the schematic file.
     */
    private String fileName;
    
    /**
     * The minimum spawn height of the structure.
     */
    private Long minY;
    
    /**
     * The maximum spawn height of the structure.
     */
    private Long maxY;
    
    /**
     * Has the structure been successfully loaded from the file system.
     */
    private Boolean loaded                        =   false;
    
    /**
     * The set of materials the structure is allowed to overwrite.
     */
    private final HashSet<Material> trump               =   new HashSet();
    
    /**
     * The set of materials the structure is not allowed to stand on.
     */
    private final HashSet<Material> invalid             =   new HashSet();
    
    /** 
     * The singleton instances of structure configurations.
     */
    private static final HashSet<Structure> instances   =   new HashSet();
    
    /**
     * The WorldEdit cuboid.
     */
    private CuboidClipboard cuboid;
    
    private boolean placing =   false;
    
    private final List<PlacedEncounter> queue   =   new ArrayList();
    
    /**
     * Get an instance of the Structure based on the name.
     * 
     * @param name The name of the structure configuration
     * @return Returns the Structure if found, null otherwise.
     */
    public static Structure getInstance(String name){
        for(Structure instance : instances){
            if(instance.getName().equals(name)){
                return instance;
            }
        }
        return null;
    }
    
    /**
     * Get an instance of the Structure based on the JSON configuration
     * 
     * @param jsonConfiguration The JSON configuration
     * @return Returns the Structure based on the configuration 
     */
    public static Structure getInstance(JSONObject jsonConfiguration){
        return Structure.getInstance(jsonConfiguration, false);
    }
    
    /**
     * Get an instance of the Structure based on the JSON configuration with the option to force reload
     * @param jsonConfiguration The JSON configuration
     * @param force Should the structure be forced into reloading
     * @return 
     */
    public static Structure getInstance(JSONObject jsonConfiguration,Boolean force){
        Structure structure =   null;
        String name         =   (String) jsonConfiguration.get("name");
        for(Structure instance : instances){
            if(instance.getName().equalsIgnoreCase(name)){
                structure   =   instance;
            }
        }
        if(structure==null){
            return new Structure(jsonConfiguration);
        }
        if(force){
            structure.reConfigure(jsonConfiguration);
        }
        return structure;
    }
    
    private void reConfigure(JSONObject jsonConfiguration){
        try{
            instances.remove(this);
            trump.clear();
            invalid.clear();
            loaded                  =   false;
            cuboid                  =   null;
            name                    =   (String) jsonConfiguration.get("name");
            fileName                =   (String) jsonConfiguration.get("file");
            minY                    =   ((Number) jsonConfiguration.get("minY")).longValue();
            maxY                    =   ((Number) jsonConfiguration.get("maxY")).longValue();
            JSONArray jsonTrump     =   (JSONArray) jsonConfiguration.get("trump");
            JSONArray jsonInvalid   =   (JSONArray) jsonConfiguration.get("invalid");
            if(jsonTrump!=null){
                for(int i=0;i<jsonTrump.size();i++){
                    trump.add(Material.getMaterial((String) jsonTrump.get(i)));
                }
            }
            if(jsonInvalid!=null){
                for(int i=0;i<jsonInvalid.size();i++){
                    invalid.add(Material.getMaterial((String) jsonInvalid.get(i)));
                }
            }
            loaded  =   load();
            if(loaded){
                instances.add(this);
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Structure configuration: "+e.getMessage());
        }
    }
    
    /**
     * Constructor for the structure based on the JSON Configuration.
     * 
     * @param jsonConfiguration 
     */
    private Structure(JSONObject jsonConfiguration){
        reConfigure(jsonConfiguration);
    }
    
    /**
     * Loads the structure from the schematic file on the file system.
     * @return Returns true if success, false otherwise.
     */
    private boolean load(){
        try{
            File file           =   new File(RandomEncounters.getInstance().getDataFolder()+"/"+fileName);
            SchematicFormat sf  =   SchematicFormat.getFormat(file);
            if(sf==null){
                RandomEncounters.getInstance().logError("Unable to detect schematic format for file: "+fileName);
                return false;
            }
            cuboid         =    sf.load(file);
            
        }catch(IOException e){
            RandomEncounters.getInstance().logError("Unable to load structure "+name+": "+e.getMessage());
            return false;
        } catch (DataException e) {
            RandomEncounters.getInstance().logError("Invalid structure schematic "+fileName+": "+e.getMessage());
            return false;
        }
        return true;
    }
    
    /**
     * Flip the structure randomly around x and z coordinates only.
     */
    private void flipRandom(){
        int angle   =  (int) Math.round(Math.random()*4)*90;
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("Flipping structure "+name+" "+angle+" degrees");
        }
        cuboid.rotate2D(angle);
    }
    
    /**
     * Place the structure for a given encounter at a location.
     * 
     * Does not check if it is safe, just places it. The location comes from a block in the world.
     * The Origin of the cuboid lines up with this some how. I have no idea what I was doing here,
     * but it seemed to work so.... yeah.
     * 
     * @param encounter The encounter configuration for this structure.
     */
    public void place(PlacedEncounter encounter){
        if(!loaded){
            RandomEncounters.getInstance().logWarning("Attempted to place a non-loaded structure: "+name);
            return;
        }
        if(placing){
            queue.add(encounter);
            return;
        }
        
        placing =   true;
        (new StructurePlacementTask(encounter)).runTaskTimer(RandomEncounters.getInstance(), 1, 2);
    }
    
    public void placed(){
        placing =   false;
        if(queue.isEmpty()){
            flipRandom();
        }else{
            PlacedEncounter encounter   =   queue.get(0);
            queue.remove(encounter);
            place(encounter);
        }
    }
    
    /**
     * Get the width of the structure
     * @return 
     */
    public int getWidth(){
        return loaded ? cuboid.getWidth() : 0;
    }
    
    /**
     * Get the height of the structure
     * @return 
     */
    public int getHeight(){
        return loaded ? cuboid.getHeight() : 0;
    }
    
    /**
     * Get the length of the structure.
     * @return 
     */
    public int getLength(){
        return loaded ? cuboid.getLength() : 0;
    }
    
    /**
     * Get the set of materials this structure can overwrite.
     * @return 
     */
    public Set<Material> getTrump(){
        return trump;
    }
    
    /**
     * Get the set of materials this structure cannot use as a base.
     * @return 
     */
    public Set<Material> getInvalid(){
        return invalid;
    }
    
    /**
     * Get the minimum spawn height of the structure.
     * @return 
     */
    public Long getMinY(){
        return minY;
    }
    
    /**
     * Get the maximum spawn height of the structure.
     * @return 
     */
    public Long getMaxY(){
        return maxY;
    }
    
    /**
     * Get the unique name of the structure configuration.
     * @return 
     */
    public String getName(){
        return name;
    }
    
    public CuboidClipboard getCuboid(){
        return cuboid;
    }
}
