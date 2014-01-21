package com.archmageinc.RandomEncounters;

import java.util.HashSet;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class ExpansionTask extends BukkitRunnable{
    
    @Override
    public void run() {
        for(PlacedEncounter placedEncounter : (HashSet<PlacedEncounter>) RandomEncounters.getInstance().getPlacedEncounters().clone()){
            for(Expansion expansion : placedEncounter.getEncounter().getExpansions()){
                expansion.checkExpansion(placedEncounter);
            }
        }
    }
}
