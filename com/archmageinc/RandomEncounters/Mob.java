package com.archmageinc.RandomEncounters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
    protected JSONObject equipment;
    
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
            name                        =   (String) jsonConfiguration.get("name");
            type                        =   EntityType.fromName((String) jsonConfiguration.get("type"));
            min                         =   (Long) jsonConfiguration.get("min");
            max                         =   (Long) jsonConfiguration.get("max");
            enabled                     =   (Boolean) jsonConfiguration.get("enabled");
            probability                 =   (Double) jsonConfiguration.get("probability");
            equipment                   =   (JSONObject) jsonConfiguration.get("equipment");
            JSONArray jsonTreasures     =   (JSONArray) jsonConfiguration.get("treasures");
            if(jsonTreasures!=null){
                for(int i=0;i<jsonTreasures.size();i++){
                    treasures.add(new Treasure((JSONObject) jsonTreasures.get(i)));
                }
            }

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
    
    protected ItemStack getEquipmentItem(String type){
        ItemStack item      =   null;
        try{
            if(equipment!=null){
                JSONObject object   =   (JSONObject) equipment.get(type);
                if(object!=null){
                    Treasure treasure  =   new Treasure(object);
                    item               =   treasure.getOne();
                }
            }
            
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid mob equipment configuration for "+name+" "+type);
        }
        return item;
    }
    
    protected Float getDropProbability(String type){
        Float chance    =   ((Integer) 0).floatValue();
        try{
            if(equipment!=null){
               JSONObject object   =   (JSONObject) equipment.get(type);
                if(object!=null){
                    chance  =   ((Long) object.get("dropProbability")).floatValue();
                } 
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid mob equipment configuration for "+name+" "+type);
        }
        return chance;
    }
    
    public void setEquipment(LivingEntity entity){
        ItemStack hand      =   getEquipmentItem("hand");
        if(hand!=null){
             entity.getEquipment().setItemInHand(hand);
             entity.getEquipment().setItemInHandDropChance(getDropProbability("hand"));
        }
        ItemStack helmet    =   getEquipmentItem("helmet");
        if(helmet!=null){
             entity.getEquipment().setHelmet(helmet);
             entity.getEquipment().setHelmetDropChance(getDropProbability("helmet"));
        }
        ItemStack chestplate    =   getEquipmentItem("chestplate");
        if(chestplate!=null){
             entity.getEquipment().setChestplate(chestplate);
             entity.getEquipment().setChestplateDropChance(getDropProbability("chestplate"));
        }
        ItemStack leggings    =   getEquipmentItem("leggings");
        if(leggings!=null){
             entity.getEquipment().setLeggings(leggings);
             entity.getEquipment().setLeggingsDropChance(getDropProbability("leggings"));
        }
        ItemStack boots    =   getEquipmentItem("boots");
        if(boots!=null){
             entity.getEquipment().setBoots(boots);
             entity.getEquipment().setBootsDropChance(getDropProbability("boots"));
        }
        
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
