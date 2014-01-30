package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Expansion;
import com.archmageinc.RandomEncounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.Calendar;
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
            if(!placedEncounter.isSacked()){
                if(RandomEncounters.getInstance().getLogLevel()>8){
                    RandomEncounters.getInstance().logMessage("  - "+placedEncounter.getName()+" has "+placedEncounter.getEncounter().getExpansions().size()+" available expansions.");
                }

                for(Expansion expansion : placedEncounter.getExpansions()){
                    Calendar nextRun    =   (Calendar) expansion.getLastCheck().clone();
                    nextRun.add(Calendar.MINUTE, expansion.getDuration().intValue());
                    if(nextRun.before(Calendar.getInstance())){
                        expansion.checkExpansion();
                    }
                }
            }else{
                if(RandomEncounters.getInstance().getLogLevel()>8){
                    RandomEncounters.getInstance().logMessage(placedEncounter.getName()+" is sacked and cannot expand");
                }
            }
        }
    }
}
