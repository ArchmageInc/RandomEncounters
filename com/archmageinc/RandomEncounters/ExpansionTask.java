package com.archmageinc.RandomEncounters;

import java.util.Calendar;
import java.util.HashSet;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class ExpansionTask extends BukkitRunnable{
    
    @Override
    public void run() {
        if(RandomEncounters.getInstance().getLogLevel()>8){
            RandomEncounters.getInstance().logMessage("Running Expansion Checks: "+RandomEncounters.getInstance().getPlacedEncounters().size());
        }
        for(PlacedEncounter placedEncounter : (HashSet<PlacedEncounter>) RandomEncounters.getInstance().getPlacedEncounters().clone()){
            if(RandomEncounters.getInstance().getLogLevel()>8){
                RandomEncounters.getInstance().logMessage("  - "+placedEncounter.getName()+" has "+placedEncounter.getEncounter().getExpansions().size()+" available expansions.");
            }
            
            for(Expansion expansion : placedEncounter.getExpansions()){
                Calendar nextRun    =   (Calendar) expansion.getLastCheck().clone();
                nextRun.add(Calendar.MINUTE, expansion.getDuration().intValue());
                if(nextRun.before(Calendar.getInstance())){
                    expansion.checkExpansion(placedEncounter);
                    expansion.updateLastCheck();
                }
            }
            
        }
    }
}
