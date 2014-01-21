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
 *
 * @author ArchmageInc
 */
public class Encounter {
    
    protected static HashSet<Encounter> instances   =   new HashSet();
    protected String name;
    protected Double probability;
    protected Boolean enabled;
    protected Structure structure;
    protected Set<Biome> validBiomes            =   new HashSet();
    protected Set<Biome> invalidBiomes          =   new HashSet();
    protected Set<Mob> mobs                     =   new HashSet();
    protected Set<Treasure> treasures           =   new HashSet();
    protected Set<Expansion> expansions         =   new HashSet();
    
    public static Encounter getInstance(String name){
       for(Encounter instance : instances){
            if(instance.getName().equals(name)){
                return instance;
            }
        }
        return null;
    }
    
    public static Encounter getInstance(JSONObject jsonConfiguration){
        try{
            for(Encounter instance : instances){
                if(instance.getName().equals((String) jsonConfiguration.get("name"))){
                    return instance;
                }
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Encounter configuration: "+e.getMessage());
        }
        return new Encounter(jsonConfiguration);
    }
    
    protected Encounter(JSONObject jsonConfiguration){
        try{
            name        =   (String) jsonConfiguration.get("name");
            enabled     =   (Boolean) jsonConfiguration.get("enabled");
            probability =   (Double) jsonConfiguration.get("probability");
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
            instances.add(this);
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Encounter configuration: "+e.getMessage());
        }
    }
    
    public PlacedEncounter checkPlace(Chunk chunk){
        Double random   =   Math.random();
        
        if(random<probability){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("Probability hit for encounter "+name+" ("+random.toString()+","+probability.toString());
            }
            Location location   =   Locator.getInstance().checkChunk(chunk, this);
            if(location!=null){
                return PlacedEncounter.create(this, location);
            }
        }
        return null;
    }
    
    public String getName(){
        return name;
    }
    public Structure getStructure(){
        return structure;
    }
    public Set<Biome> getValidBiomes(){
        return validBiomes;
    }
    public Set<Biome> getInvalidBiomes(){
        return invalidBiomes;
    }
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
    public Set<Mob> getMobs(){
        return mobs;
    }
    
    public Double getProbability(){
        return probability;
    }
    
    public Set<Expansion> getExpansions(){
        return expansions;
    }
}
