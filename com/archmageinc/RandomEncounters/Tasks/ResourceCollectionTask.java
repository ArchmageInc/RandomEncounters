package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents the task responsible for checking PlacedEncounter Expansions.
 * This is designed to run once per minute.
 * 
 * @author ArchmageInc
 */
public class ResourceCollectionTask extends BukkitRunnable{
    
    private Iterator<PlacedEncounter> itr;
    private HashSet<PlacedEncounter> encounters;
    private boolean wait                            =   false;
    
    public ResourceCollectionTask(){
        encounters  =   (HashSet<PlacedEncounter>) RandomEncounters.getInstance().getPlacedEncounters().clone();
        itr         =   encounters.iterator();
        
    }
    
    @Override
    public void run() {
        if(!wait){
            encounters  =   (HashSet<PlacedEncounter>) RandomEncounters.getInstance().getPlacedEncounters().clone();
            itr         =   encounters.iterator();
        }
        if(RandomEncounters.getInstance().getLogLevel()>5){
            RandomEncounters.getInstance().logMessage("Running resource collection checks: "+encounters.size());
        }
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(itr.hasNext()){
            itr.next().runCollectionChecks();         
            if(Calendar.getInstance().after(timeLimit)){
                break;
            }
        }
        if(itr.hasNext()){
            wait    =   true;
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("More time needed for resource collection");
            }
        }else{
            wait    =   false;
        }
    }
}
