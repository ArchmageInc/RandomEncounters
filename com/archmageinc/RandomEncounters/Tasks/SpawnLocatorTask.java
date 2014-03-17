package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class SpawnLocatorTask extends BukkitRunnable  {
    private final PlacedEncounter placedEncounter;
    private final List<Location> locations  =   new ArrayList();
    private final int[] blockLocations;
    private int i=0;
    
    public SpawnLocatorTask(PlacedEncounter placedEncounter){
        this.placedEncounter    =   placedEncounter;
        if(placedEncounter.getEncounter().getStructure()==null){
            if(RandomEncounters.getInstance().getLogLevel()>0){
                RandomEncounters.getInstance().logWarning(placedEncounter.getName()+" has no structure: the entire chunk will be used.");
            }
            blockLocations  =   setChunkLocations();
        }else{
            blockLocations  =   placedEncounter.getBlockLocations();
        }
        if(RandomEncounters.getInstance().getLogLevel()>8){
            RandomEncounters.getInstance().logWarning("Spawn Locator for "+placedEncounter.getName()+" checking "+(blockLocations.length/3)+" locations.");
        }
    }
    
    @Override
    public void run(){
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(i<blockLocations.length){
            int x   =   blockLocations[i];
            int y   =   blockLocations[i+1];
            int z   =   blockLocations[i+2];
            if(x+y+z!=0){
                Location location   =   new Location(placedEncounter.getLocation().getWorld(),x,y,z);
                if(validLocation(location)){
                    location.add(0.5,1,0.5);
                    locations.add(location);
                }
            }
            i += 3;
            if(Calendar.getInstance().after(timeLimit)){
                break;
            }
        }
        if(i>=blockLocations.length){
            if(RandomEncounters.getInstance().getLogLevel()>8){
                RandomEncounters.getInstance().logMessage("Spawn location search complete for "+placedEncounter.getName()+", "+locations.size()+" found.");
            }
            placedEncounter.setSpawnLocations(locations);
            (new SpawningTask(placedEncounter)).runTaskTimer(RandomEncounters.getInstance(),1,1);
            cancel();
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("More time needed for "+placedEncounter.getName()+" spawn locations, "+locations.size()+" found so far.");
            }
        }
    }
    
    private boolean validLocation(Location location){
        Block block =   location.getBlock();
        return
                block.getType().isSolid()
                && block.getRelative(BlockFace.UP).isEmpty()
                && block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).isEmpty();
        
    }
    
    private int[] setChunkLocations(){
        int size        =   16*16*placedEncounter.getLocation().getWorld().getMaxHeight()*3;
        int[] tmp       =   new int[size];
        int cx          =   placedEncounter.getLocation().getChunk().getX();
        int cz          =   placedEncounter.getLocation().getChunk().getZ();
        int j           =   0;
        for(int x=0;x<16;x++){
            for(int y=0;y<placedEncounter.getLocation().getWorld().getMaxHeight();y++){
                for(int z=0;z<16;z++){
                    tmp[j]   =   x+cx*16;
                    tmp[j+1] =   y;
                    tmp[j+2] =   z+cz*16;
                    j += 3;
                }
            }
        }
        return tmp;
    }
}
