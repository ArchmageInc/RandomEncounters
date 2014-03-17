package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.Calendar;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class BlockOwnerTask extends BukkitRunnable {
    private final PlacedEncounter owner;
    private final int[] blockLocations;
    private final MetadataValue mv;
    private int i   =   0;
    
    
    public BlockOwnerTask(PlacedEncounter placedEncounter){
        owner           =   placedEncounter;
        mv              =   new FixedMetadataValue(RandomEncounters.getInstance(),owner.getUUID());
        if(owner.getEncounter().getStructure()==null){
            blockLocations  =   new int[0];
        }else{
            blockLocations  =   owner.getBlockLocations();
        }
        if(RandomEncounters.getInstance().getLogLevel()>8){
            RandomEncounters.getInstance().logMessage("Block Owner Setup for "+placedEncounter.getName()+" checking "+(blockLocations.length/3)+" locations.");
        }
    }

    @Override
    public void run() {
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(i<blockLocations.length){
            int x   =   blockLocations[i];
            int y   =   blockLocations[i+1];
            int z   =   blockLocations[i+2];
            if(x+y+z!=0){
                owner.getLocation().getWorld().getBlockAt(x, y, z).setMetadata("placedEncounter", mv);
            }
            i += 3;
            if(Calendar.getInstance().after(timeLimit)){
                break;
            }
        }
        if(i>=blockLocations.length){
            if(RandomEncounters.getInstance().getLogLevel()>8){
                RandomEncounters.getInstance().logMessage("Block Owner Setup for "+owner.getName()+": Complete");
            }
            cancel();
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("Block Owner Setup for "+owner.getName()+" needs more time");
            }
        }
    }
    
}
