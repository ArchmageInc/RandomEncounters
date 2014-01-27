package com.archmageinc.RandomEncounters;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        PlacedMob entity    =   PlacedMob.getInstance(event.getEntity().getUniqueId());
        if(entity!=null){
            entity.die();
        }
    }
}
