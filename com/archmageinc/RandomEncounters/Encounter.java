package com.archmageinc.RandomEncounters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Represents an Encounter instance containing all configurations to create a PlacedEncounter
 * 
 * @author ArchmageInc
 */
public class Encounter {
    
    /**
     * The static instances of unique encounters.
     */
    private static HashSet<Encounter> instances   =   new HashSet();
    
    /**
     * The unique name of the encounter.
     */
    private String name;
    
    /**
     * The probability of the encounter's occurrence.
     */
    private Double probability;
    
    /**
     * Is the encounter enabled for future placement.
     */
    private Boolean enabled;
    
    /**
     * The structure of the encounter.
     */
    private Structure structure;
    
    /**
     * The set of Biomes where this encounter is allowed to be placed.
     * An empty set implies only invalid biome restrictions. 
     * If both sets are empty, the encounter may be placed in any biome.
     */
    private Set<Biome> validBiomes            =   new HashSet();
    
    /**
     * The set of Biomes where this encounter is not allowed to be placed.
     * An empty set implies only valid biome restrictions.
     * If both sets are empty, the encounter may be placed in any biome. 
     */
    private Set<Biome> invalidBiomes          =   new HashSet();
    
    /**
     * The set of Mobs that will be placed with this encounter.
     */
    private Set<Mob> mobs                     =   new HashSet();
    
    /**
     * The set of Treasures that will be placed in chests of the structure.
     */
    private Set<Treasure> treasures           =   new HashSet();
    
    /**
     * The set of Expansions that this encounter is allowed to spawn.
     */
    private HashSet<Expansion> expansions     =   new HashSet();
    
    /**
     * Get an instance of the encounter based on the name.
     * @param name The unique name of the encounter
     * @return Returns the Encounter if found, null otherwise.
     */
    public static Encounter getInstance(String name){
       for(Encounter instance : instances){
            if(instance.getName().equals(name)){
                return instance;
            }
        }
        return null;
    }
    
    /**
     * Get an instance of the encounter based on the JSON Configuration.
     * If the encounter is already loaded (based on name) that instance will be returned.
     * Otherwise, a new instance will be created based on the configuration.
     * 
     * @param jsonConfiguration The configuration of the encounter
     * @return Returns the Encounter for the given configuration
     */
    public static Encounter getInstance(JSONObject jsonConfiguration){
        return getInstance(jsonConfiguration,false);
    }
    
    public static Encounter getInstance(JSONObject jsonConfiguration,Boolean force){
        Encounter encounter     =   null;
        String encounterName    =   (String) jsonConfiguration.get("name");
        for(Encounter instance : instances){
            if(instance.getName().equalsIgnoreCase(encounterName)){
                encounter   =   instance;
            }
        }
        if(encounter==null){
            return new Encounter(jsonConfiguration);
        }
        if(force){
            encounter.reConfigure(jsonConfiguration);
        }
        return encounter;
    }
    
    private void reConfigure(JSONObject jsonConfiguration){
        try{
            instances.remove(this);
            validBiomes.clear();
            treasures.clear();
            expansions.clear();
            mobs.clear();
            name        =   (String) jsonConfiguration.get("name");
            enabled     =   (Boolean) jsonConfiguration.get("enabled");
            probability =   ((Number) jsonConfiguration.get("probability")).doubleValue();
            structure   =   Structure.getInstance((String) jsonConfiguration.get("structure"));
            JSONArray jsonValidBiomes   =   (JSONArray) jsonConfiguration.get("validBiomes");
            JSONArray jsonInvalidBiomes =   (JSONArray) jsonConfiguration.get("invalidBiomes");
            JSONArray jsonTreasures     =   (JSONArray) jsonConfiguration.get("treasures");
            JSONArray jsonExpansions    =   (JSONArray) jsonConfiguration.get("expansions");
            JSONArray jsonMobs          =   (JSONArray) jsonConfiguration.get("mobs");
            if(jsonValidBiomes!=null){
                for(int i=0;i<jsonValidBiomes.size();i++){
                    validBiomes.add(Biome.valueOf((String) jsonValidBiomes.get(i)));
                }
            }
            if(jsonInvalidBiomes!=null){
                for(int i=0;i<jsonInvalidBiomes.size();i++){
                    invalidBiomes.add(Biome.valueOf((String) jsonInvalidBiomes.get(i)));
                }
            }
            if(jsonTreasures!=null){
                for(int i=0;i<jsonTreasures.size();i++){
                    JSONObject jsonTreasure =   (JSONObject) jsonTreasures.get(i);
                    treasures.add(new Treasure(jsonTreasure));
                }
            }
            if(jsonExpansions!=null){
                for(int i=0;i<jsonExpansions.size();i++){
                    JSONObject jsonExpansion    =   (JSONObject) jsonExpansions.get(i);
                    expansions.add(new Expansion(jsonExpansion));
                }
            }
            if(jsonMobs!=null){
                for(int i=0;i<jsonMobs.size();i++){
                    mobs.add(Mob.getInstance((String) jsonMobs.get(i)));
                }
            }
            if(structure==null){
                RandomEncounters.getInstance().logError("Unable to find structure for encounter: "+name);
            }
            instances.add(this);
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Encounter configuration: "+e.getMessage());
        }
    }
    
    /**
     * Protected Constructor for the encounter based on JSON configuration.
     * This should only be called from the getInstance method.
     * 
     * @param jsonConfiguration The JSONObject configuration of the encounter.
     * @see Encounter#getInstance(org.json.simple.JSONObject)
     */
    private Encounter(JSONObject jsonConfiguration){
        reConfigure(jsonConfiguration);
    }
    
    /**
     * Check the placement of the encounter for a given Chunk. 
     * The if successful, the encounter WILL be placed in the chunk.
     * @param chunk The chunk to check for placement
     * @return Returns the PlacedEncounter on successful placement, null otherwise
     */
    public PlacedEncounter checkPlace(Chunk chunk){
        return checkPlace(chunk,false);
    }
    
    public PlacedEncounter checkPlace(Chunk chunk,Boolean force){
        Double random   =   Math.random();
        
        if(force || random<probability){
            if(RandomEncounters.getInstance().getLogLevel()>6){
                if(force){
                    RandomEncounters.getInstance().logMessage("Forcing probability override for encounter "+name);
                }else{
                    RandomEncounters.getInstance().logMessage("Probability hit for encounter "+name+" ("+random.toString()+","+probability.toString());
                }
            }
            Location location   =   Locator.getInstance().checkChunk(chunk, this);
            if(location!=null){
                return PlacedEncounter.create(this, location);
            }
        }
        return null;
    }
    
    /**
     * Get the unique name of the encounter.
     * @return Returns the name of the encounter
     * @see Encounter#name
     */
    public String getName(){
        return name;
    }
    
    /**
     * Get the structure associated to this encounter.
     * @return Returns the structure
     * @see Encounter#structure
     */
    public Structure getStructure(){
        return structure;
    }
    
    /**
     * Get the set of valid biomes for the encounter.
     * @return Returns the set of valid biomes
     * @see Encounter#validBiomes
     */
    public Set<Biome> getValidBiomes(){
        return validBiomes;
    }
    
    /**
     * Get the set of invalid biomes for the encounter.
     * @return Returns the set of invalid biomes
     * @see Encounter#invalidBiomes
     */
    public Set<Biome> getInvalidBiomes(){
        return invalidBiomes;
    }
    
    /**
     * Returns a list of randomly generated treasure items for the encounter
     * @return Returns the list of items for treasure
     * @see Encounter#treasures
     * @see Treasure#get() 
     */
    public List<ItemStack> getTreasure(){
        List<ItemStack> list    =   new ArrayList();
        for(Treasure treasure : treasures){
            List<ItemStack> t   =   treasure.get();
            if(!t.isEmpty()){
                list.addAll(treasure.get());
            }
        }
        return list;
    }
    
    public List<Mob> getMobPlacements(){
        List<Mob> placements    =   new ArrayList();
        for(Mob mob : mobs){
            placements.addAll(mob.getPlacements());
        }
        return placements;
    }
    
    /**
     * Returns the probability of occurrence of this encounter 
     * @return 
     * @see Encounter#probability
     */
    public Double getProbability(){
        return probability;
    }
    
    /**
     * Returns the set of available expansions for this encounter
     * @return 
     * @see Encounter#expansions
     */
    public HashSet<Expansion> getExpansions(){
        return expansions;
    }
}
