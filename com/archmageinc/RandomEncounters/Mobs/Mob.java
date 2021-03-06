package com.archmageinc.RandomEncounters.Mobs;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Treasures.Treasure;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Represents an instance of a Mob configuration
 * 
 * @author ArchmageInc
 */
public final class Mob{
    
    /**
     * The static set of singlton instances.
     */
    private static final HashSet<Mob> instances =   new HashSet();
    
    /**
     * The unique name of the Mob.
     */
    private String name;
    
    /**
     * The name each creature will be given.
     */
    private String tagName;
    
    /**
     * The type of entity spawned by this Mob.
     */
    private EntityType type;
    
    private String typeName;
    
    /**
     * The minimum number of creatures to spawn.
     */
    private Long min;
    
    /**
     * The maximum number of creatures to spawn.
     */
    private Long max;
    
    /**
     * The probability of additional creatures.
     */
    private Double probability;
    
    /**
     * Is this Mob enabled for spawning.
     */
    private Boolean enabled;
    
    /**
     * The equipment configuration to be placed on spawned creatures.
     */
    private JSONObject equipment;
    
    /**
     * The mob to spawn when the creature dies.
     */
    private Mob deathSpawn;
    
    /**
     * The unique name of the death spawn mob.
     */
    private String deathSpawnName;
    
    /**
     * The set of Treasures which will be dropped by spawned creatures.
     */
    private final Set<Treasure> treasures       =   new HashSet();
    
    /**
     * The set of MobGroups which will be spawned.
     */
    private final Set<MobGroup> mobGroups       =   new HashSet();
    
    /**
     * The set of potion effects that will be paced on spawned creatures.
     */
    private final Set<MobPotionEffect> effects  =   new HashSet();
    
    /**
     * Get the instance of the Mob based on the unique name
     * @param name The unique name of the Mob.
     * @return Returns the Mob if found, null otherwise.
     */
    public static Mob getInstance(String name){
        for(Mob instance : instances){
            if(instance.getName().equals(name)){
                return instance;
            }
        }
        return null;
    }
    
    /**
     * Get the instance of the Mob based on JSON configuration
     * 
     * @param jsonConfiguration The JSON configuration of the Mob.
     * @return 
     */
    public static Mob getInstance(JSONObject jsonConfiguration){
        return getInstance(jsonConfiguration,false);
    }
    
    public static Mob getInstance(JSONObject jsonConfiguration,Boolean force){
        String mobName  =   (String) jsonConfiguration.get("name");
        Mob mob         =   getInstance(mobName);
        if(mob==null){
            return new Mob(jsonConfiguration);
        }
        if(force){
            mob.reConfigure(jsonConfiguration);
        }
        return mob;
    }
    
    private void reConfigure(JSONObject jsonConfiguration){
        try{
            instances.remove(this);
            treasures.clear();
            effects.clear();
            mobGroups.clear();
            deathSpawn                  =   null;
            name                        =   (String) jsonConfiguration.get("name");
            typeName                    =   (String) jsonConfiguration.get("type");
            min                         =   jsonConfiguration.get("min")==null ? null : ((Number) jsonConfiguration.get("min")).longValue();
            max                         =   jsonConfiguration.get("max")==null ? null : ((Number) jsonConfiguration.get("max")).longValue();
            enabled                     =   (Boolean) jsonConfiguration.get("enabled");
            probability                 =   jsonConfiguration.get("probability")==null ? null : ((Number) jsonConfiguration.get("probability")).doubleValue();
            equipment                   =   (JSONObject) jsonConfiguration.get("equipment");
            tagName                     =   (String) jsonConfiguration.get("tagName");
            deathSpawnName              =   (String) jsonConfiguration.get("deathSpawn");
            type                        =   getType();
            JSONArray jsonTreasures     =   (JSONArray) jsonConfiguration.get("treasures");
            JSONArray jsonEffects       =   (JSONArray) jsonConfiguration.get("potionEffects");
            JSONArray jsonMobs          =   (JSONArray) jsonConfiguration.get("mobGroups");
            if(jsonTreasures!=null){
                for(int i=0;i<jsonTreasures.size();i++){
                    Treasure treasure   =   Treasure.getInstance((String) jsonTreasures.get(i));
                    if(treasure!=null){
                        treasures.add(treasure);
                    }else{
                        RandomEncounters.getInstance().logError("Invalid Treasure "+(String) jsonTreasures.get(i)+" for Mob "+name);
                    }
                }
            }
            
            if(jsonEffects!=null){
                for(int i=0;i<jsonEffects.size();i++){
                    effects.add(new MobPotionEffect((JSONObject) jsonEffects.get(i)));
                }
            }
            if(jsonMobs!=null){
                for(int i=0;i<jsonMobs.size();i++){
                    JSONObject mobGroup =   (JSONObject) jsonMobs.get(i);
                    if(name.equals((String) mobGroup.get("name"))){
                        RandomEncounters.getInstance().logWarning("Ignoring recursive mob group configuration for "+name);
                    }else{
                        mobGroups.add(new MobGroup(mobGroup));
                    }
                }
            }
            if(jsonMobs==null && type==null){
                RandomEncounters.getInstance().logError("Invalid mob type: "+(String) jsonConfiguration.get("type")+" for "+name);
            }
            instances.add(this);
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Mob configuration: "+e.getMessage());
        }
    }
    
    
    
    /**
     * Constructor for the Mob based on JSON configuration.
     * @param jsonConfiguration 
     */
    private Mob(JSONObject jsonConfiguration){
        reConfigure(jsonConfiguration);
    }
    
    
    public List<Mob> getPlacements(){
        List<Mob> toPlace    =   new ArrayList();
        if(mobGroups.isEmpty()){
            Long count  =   getCount();
            for(int i=0;i<count;i++){
                toPlace.add(this);
            }
        }else{
            for(MobGroup mobGroup : mobGroups){
                toPlace.addAll(mobGroup.getPlacements());
            }
        }
        return toPlace;
    }
    /**
     * Spawn the creatures and create a PlacedMob at a given location for a given PlacedEncounter.
     * @param encounter The PlacedEncounter that the creature will belong to.
     * @return Returns the newly created PlacedMob
     */
    public Set<PlacedMob> placeMob(PlacedEncounter encounter){
        HashSet<PlacedMob> placements   =   new HashSet();
        if(mobGroups.isEmpty()){
            Long count   =   getCount();
            for(int i=0;i<getCount();i++){
                placements.add(PlacedMob.create(this, encounter));
            }
            
        }else{
            for(MobGroup mobGroup : mobGroups){
                placements.addAll(mobGroup.placeGroup(encounter));
            }
        }        
        return placements;
    }
    
    /**
     * Gets an Equipment item based on the equipment type name.
     * 
     * @param type The type name of the equipment
     * @return Returns the equipment item if found, null otherwise.
     * @see org.bukkit.inventory.EntityEquipment
     */
    private ItemStack getEquipmentItem(String type){
        ItemStack item      =   null;
        try{
            if(equipment!=null){
                JSONObject object   =   (JSONObject) equipment.get(type);
                if(object!=null){
                    Treasure treasure   =   Treasure.getInstance((String) object.get("treasure"));
                    if(treasure!=null){
                        item               =   treasure.getOne();
                    }else{
                        RandomEncounters.getInstance().logError("Invalid "+type+" item "+(String) object.get("treasure")+" for Mob "+name);
                    }
                }
            }
            
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid mob equipment configuration for "+name+" "+type+": "+e.getMessage());
        }
        return item;
    }
    
    /**
     * Gets the drop probability of the equipment based on the type.
     * 
     * @param type The name of the equipment type
     * @return Returns the drop probability of the equipment.
     * @see org.bukkit.inventory.EntityEquipment
     */
    private Float getDropProbability(String type){
        Float chance    =   ((Integer) 0).floatValue();
        try{
            if(equipment!=null){
               JSONObject object   =   (JSONObject) equipment.get(type);
                if(object!=null){
                    chance  =   ((Number) object.get("dropProbability")).floatValue();
                } 
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid mob equipment configuration for "+name+" "+type+": "+e.getMessage());
        }
        return chance;
    }
    
    /**
     * Sets the equipment based on this configuration for the given LivingEntity
     * 
     * @param entity The entity on which to place the equipment.
     * @see org.bukkit.inventory.EntityEquipment
     */
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
    
    /**
     * Sets the potion effects based on this configuration for the given entity.
     * 
     * @param entity The LivingEntity on which to place the effects.
     */
    public void setEffects(LivingEntity entity){
        for(MobPotionEffect effect : effects){
            effect.checkApply(entity);
        }
    }
    
    /**
     * Get the randomly generated number of creatures to place
     * 
     * @return 
     */
    private Long getCount(){
        Long count   =   min;
        for(int i=min.intValue();i<max;i++){
            if(Math.random()<probability)
                count++;
        }
        return count;
    }
    
    /**
     * Get the randomly generated list of items to drop upon the creatures death.
     * 
     * @return 
     */
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
    
    /**
     * Return the unique name of this Mob configuration
     * 
     * @return 
     */
    public String getName(){
        return name;
    }
    
    /**
     * Get the EntityType associated with this Mob Configuration.
     * @return 
     */
    public EntityType getType(){
        /*
        @TODO: This is terrible, but how MC has implemented wither skeletons.
        */
        if(typeName!=null){
            if(typeName.toLowerCase().equals("witherskeleton")){
                type    =   EntityType.SKELETON;
            }else{
                type    =   EntityType.fromName(typeName);
            }
        }
        return type;
    }
    
    /**
     * Get the EntityType as a string.
     * This really only concerns EntityTypes not in the EntityType enum
     * @return 
     */
    public String getTypeName(){
        return typeName;
    }
    
    /**
     * Get the tagName for the creature.
     * @return 
     */
    public String getTagName(){
        return tagName;
    }
    
    /**
     * Get the Mob that should spawn when this Mob dies.
     * 
     * @return 
     */
    public Mob getDeathSpawn(){
        if(deathSpawn==null && deathSpawnName!=null){
            deathSpawn  =   Mob.getInstance(deathSpawnName);
            if(deathSpawn==null){
                RandomEncounters.getInstance().logWarning("Unable to find Death Spawn configuration "+deathSpawnName+" for "+name);
            }
        }
        return deathSpawn;
    }
}
