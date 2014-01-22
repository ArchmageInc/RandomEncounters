package com.archmageinc.RandomEncounters;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.PigZombie;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class PlacedMob {
    protected static HashSet<PlacedMob> instances   =   new HashSet();
    protected UUID uuid;
    protected Mob mob;
    protected LivingEntity entity;
    protected PlacedEncounter encounter;
    
    public static PlacedMob getInstance(UUID uuid){
        for(PlacedMob instance : instances){
            if(instance.getUUID().equals(uuid)){
                return instance;
            }
        }
        return null;
    }
    
    public static PlacedMob getInstance(JSONObject jsonConfiguration,PlacedEncounter encounter){
        try{
            UUID jsonUUID       =   UUID.fromString((String) jsonConfiguration.get("uuid"));
            PlacedMob located   =   PlacedMob.getInstance(jsonUUID);
            if(located!=null){
                return located;
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid PlacedMob configuration: "+e.getMessage());
        }catch(IllegalArgumentException e){
            RandomEncounters.getInstance().logError("Invalid UUID in PlacedMob configuration: "+e.getMessage());
        }
        return new PlacedMob(jsonConfiguration,encounter);
    }
    
    public static PlacedMob create(Mob mob,PlacedEncounter encounter, Location location){
        return new PlacedMob(mob,encounter,location);
    }
    
    protected PlacedMob(Mob mob,PlacedEncounter encounter,Location location){
        
        this.mob        =   mob;
        this.encounter  =   encounter;
        Location spawnLocation  =   encounter.findSafeSpawnLocation();
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("Creating new placed mob "+mob.getName()+" at "+spawnLocation.toString());
        }
        if(spawnLocation==null){
            RandomEncounters.getInstance().logWarning("Attempted to spawn a mob, but no safe spawn was located");
            entity      =   (LivingEntity) location.getWorld().spawnEntity(location, mob.getType());
        }else{
            entity     =   (LivingEntity) location.getWorld().spawnEntity(spawnLocation, mob.getType());
        }
        uuid       =   entity.getUniqueId();
        entity.setRemoveWhenFarAway(false);
        mob.setEquipment(entity);
        
        if(mob.getType().equals(EntityType.WOLF)){
            ((Wolf) entity).setAngry(true);
        }
        if(mob.getType().equals(EntityType.PIG_ZOMBIE)){
            ((PigZombie) entity).setAngry(true);
        }
        /*
        TODO: We should setup entity equipment (Armor, etc...)
        */
        /*
        TODO: We should setup entitiy potion effects
        */
        instances.add(this);
    }
    
    protected PlacedMob(JSONObject jsonConfiguration,PlacedEncounter encounter){
        try{
            this.encounter   =   encounter;
            uuid             =   UUID.fromString((String) jsonConfiguration.get("uuid"));
            mob              =   Mob.getInstance((String) jsonConfiguration.get("mob"));
            if(mob==null){
                RandomEncounters.getInstance().logError("Missing Mob ("+(String) jsonConfiguration.get("mob")+") from PlacedMob configuration");
            }
            entity  =   getEntity();
            instances.add(this);
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid PlacedMob configuration: "+e.getMessage());
        }catch(IllegalArgumentException e){
            RandomEncounters.getInstance().logError("Invalid UUID in PlacedMob configuration: "+e.getMessage());
        }
    }    
    
    public UUID getUUID(){
        return uuid;
    }
    
    public final LivingEntity getEntity(){
        if(entity==null){
            for(World world : RandomEncounters.getInstance().getServer().getWorlds()){
                for(LivingEntity wentity : world.getLivingEntities()){
                    if(wentity.getUniqueId().equals(uuid)){
                        entity  =   wentity;
                        break;
                    }
                }
                if(entity!=null){
                    break;
                }
            }
        }
        return entity;
    }
    
    public List<ItemStack> getDrop(){
        return mob.getDrop();
    }
    
    public void die(){
        for(ItemStack item : getDrop()){
            getEntity().getWorld().dropItem(entity.getLocation(), item);
        }
        encounter.notifyMobDeath(this);
        instances.remove(this);
    }
    
    public JSONObject toJSON(){
        JSONObject jsonConfiguration    =   new JSONObject();
        
        jsonConfiguration.put("uuid", uuid.toString());
        jsonConfiguration.put("mob", mob.getName());
        
        return jsonConfiguration;
    }
}
