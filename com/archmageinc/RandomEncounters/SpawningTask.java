package com.archmageinc.RandomEncounters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class SpawningTask extends BukkitRunnable   {
    protected PlacedEncounter placedEncounter;
    protected List<Mob> placements   =   new ArrayList();
    protected Iterator<Mob> itr;
    
    public SpawningTask(PlacedEncounter placedEncounter){
        this.placedEncounter    =   placedEncounter;
        placements              =   placedEncounter.getEncounter().getMobPlacements();
        itr                     =   placements.iterator();
    }
    
    public SpawningTask(Mob mob,PlacedEncounter placedEncounter){
        this.placedEncounter    =   placedEncounter;
        placements              =   mob.getPlacements();
        itr                     =   placements.iterator();
    }
    
    @Override
    public void run() {
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(itr.hasNext()){
            Mob mob =   itr.next();
            placedEncounter.addMob(PlacedMob.create(mob, placedEncounter));
            if(Calendar.getInstance().after(timeLimit))
                break;
        }
        if(itr.hasNext()){
            if(RandomEncounters.getInstance().getLogLevel()>8){
                RandomEncounters.getInstance().logMessage("More time needed for "+placedEncounter.getName()+" spawning.");
            }
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>8){
                RandomEncounters.getInstance().logMessage("Spawning Completed for "+placedEncounter.getName()+".");
            }
            cancel();
        }
    }
    
}
