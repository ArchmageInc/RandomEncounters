package com.archmageinc.RandomEncounters.Listeners;

import com.archmageinc.RandomEncounters.Mobs.PlacedMob;
import com.archmageinc.RandomEncounters.RandomEncounters;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * The listener for Mob Deaths
 * 
 * @author ArchmageInc
 */
public class PlacedMobListener implements Listener{
   
    /**
     * Listens to EntityDeaths and if it is a PlacedMob, let it know it died.
     * 
     * This should not drop the treasure as that is handled by PlacedMob
     * @param event 
     * @see PlacedMob#die() 
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        if(RandomEncounters.getInstance().getLogLevel()>9){
            RandomEncounters.getInstance().logMessage("Something died, let me figure out if it means anything.");
        }
        PlacedMob entity    =   PlacedMob.getInstance(event.getEntity().getUniqueId());
        if(entity!=null){
            entity.die();
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>9){
                RandomEncounters.getInstance().logMessage("The death was not of importance, carry on.");
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(RandomEncounters.getInstance().getLogLevel()>10){
            PlacedMob entity    =   PlacedMob.getInstance(event.getEntity().getUniqueId());
            if(entity!=null){
                RandomEncounters.getInstance().logMessage(entity.getPlacedEncounter().getName()+": "+entity.getMob().getName()+" ("+entity.getMob().getTypeName()+") has been hurt: "+event.getCause().toString());
            }
        }
    }
}
