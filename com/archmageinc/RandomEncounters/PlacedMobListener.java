package com.archmageinc.RandomEncounters;

import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author ArchmageInc
 */
public class PlacedMobListener implements Listener{
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        PlacedMob entity    =   PlacedMob.getInstance(event.getEntity().getUniqueId());
        if(entity!=null){
            List<ItemStack> loot    =   entity.getDrop();
            for(ItemStack item : loot){
                event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), item);
            }
            entity.die();
        }
    }
}
