package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.Expansion;
import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.HashSet;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents the task responsible for checking PlacedEncounter Expansions.
 * This is designed to run once per minute.
 * 
 * @author ArchmageInc
 */
public class ExpansionTask extends BukkitRunnable{
    
    @Override
    public void run() {
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("Running Expansion Checks: "+RandomEncounters.getInstance().getPlacedEncounters().size());
        }
        for(PlacedEncounter placedEncounter : (HashSet<PlacedEncounter>) RandomEncounters.getInstance().getPlacedEncounters().clone()){
            for(Expansion expansion : placedEncounter.getExpansions()){
                expansion.checkExpansion();
            }
        }
    }
}
