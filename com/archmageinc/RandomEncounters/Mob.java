package com.archmageinc.RandomEncounters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class Mob{
    
    protected static HashSet<Mob> instances =   new HashSet();
    
    protected String name;
    protected EntityType type;
    protected Long min;
    protected Long max;
    protected Double probability;
    protected Boolean enabled;
    protected Set<Treasure> treasures   =   new HashSet();
    
    public static Mob getInstance(JSONObject jsonConfiguration){
        try{
            for(Mob instance : instances){
                if(instance.getName().equals((String) jsonConfiguration.get("name"))){
                    return instance;
                }
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Mob configuration: "+e.getMessage());
        }
        return new Mob(jsonConfiguration);
    }
    
    public static Mob getInstance(String name){
        for(Mob instance : instances){
            if(instance.getName().equals(name)){
                return instance;
            }
        }
        return null;
    }
    
    protected Mob(JSONObject jsonConfiguration){
        try{
            name                    =   (String) jsonConfiguration.get("name");
            type                    =   EntityType.fromName((String) jsonConfiguration.get("type"));
            min                     =   (Long) jsonConfiguration.get("min");
            max                     =   (Long) jsonConfiguration.get("max");
            enabled                 =   (Boolean) jsonConfiguration.get("enabled");
            probability             =   (Double) jsonConfiguration.get("probability");
            JSONArray jsonTreasures =   (JSONArray) jsonConfiguration.get("treasures");
            if(jsonTreasures!=null){
                for(int i=0;i<jsonTreasures.size();i++){
                    treasures.add(new Treasure((JSONObject) jsonTreasures.get(i)));
                }
            }
            /*
            TODO: We should add configuration options for entity equipment (Armor, etc...)
            */
            /*
            TODO: We should add configuration options for potion effects
            */
            instances.add(this);
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Mob configuration: "+e.getMessage());
        }
    }
    
    public PlacedMob placeMob(PlacedEncounter encounter,Location location){
        return PlacedMob.create(this,encounter, location);
        
    }
    
    public Long getCount(){
        Long count   =   min;
        for(int i=min.intValue();i<max;i++){
            if(Math.random()<probability)
                count++;
        }
        return count;
    }
    
    public List<ItemStack> getDrop(){
        List<ItemStack> list    =   new ArrayList();
        for(Treasure treasure : treasures){
            List<ItemStack> loot    =   treasure.get();
            if(!loot.isEmpty()){
                list.addAll(loot);
            }
        }
        return list;
    }
    
    public String getName(){
        return name;
    }
    public EntityType getType(){
        return type;
    }
}
