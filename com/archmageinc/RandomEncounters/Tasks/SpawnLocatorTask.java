package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class SpawnLocatorTask extends BukkitRunnable  {
    private final PlacedEncounter placedEncounter;
    private int x;
    private int y;
    private int z;
    private final int sx;
    private final int sy;
    private final int sz;
    private final int mx;
    private final int my;
    private final int mz;
    private final List<Location> locations  =   new ArrayList();
    
    public SpawnLocatorTask(PlacedEncounter placedEncounter){
        this.placedEncounter    =   placedEncounter;
        if(placedEncounter.getEncounter().getStructure()==null){
            if(RandomEncounters.getInstance().getLogLevel()>0){
                RandomEncounters.getInstance().logWarning(placedEncounter.getName()+" has no structure: the entire chunk will be used.");
            }
            sx  =   placedEncounter.getLocation().getChunk().getBlock(0, 0, 0).getX();
            sy  =   0;
            sz  =   placedEncounter.getLocation().getChunk().getBlock(0, 0, 0).getZ();
            mx  =   placedEncounter.getLocation().getChunk().getBlock(15, 0, 0).getX();
            my  =   placedEncounter.getLocation().getWorld().getMaxHeight();
            mz  =   placedEncounter.getLocation().getChunk().getBlock(0, 0, 15).getZ();
        }else{
            sx  =   placedEncounter.getLocation().getBlockX()-(placedEncounter.getEncounter().getStructure().getWidth()/2);
            sy  =   placedEncounter.getLocation().getBlockY()-(placedEncounter.getEncounter().getStructure().getHeight()/2);
            sz  =   placedEncounter.getLocation().getBlockZ()-(placedEncounter.getEncounter().getStructure().getLength()/2);
            mx  =   placedEncounter.getLocation().getBlockX()+(placedEncounter.getEncounter().getStructure().getWidth()/2);
            my  =   placedEncounter.getLocation().getBlockY()+(placedEncounter.getEncounter().getStructure().getHeight()/2);
            mz  =   placedEncounter.getLocation().getBlockZ()+(placedEncounter.getEncounter().getStructure().getLength()/2);
            
        }
        x   =   sx;
        y   =   sy;
        z   =   sz;
    }
    
    @Override
    public void run() {
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(x<mx){
            
            y   =   y>=my ? sy : y;
            while(y<my){
                
                z   =   z>=mz ? sz : z;
                while(z<mz){
                    
                    
                    Block block =   placedEncounter.getLocation().getWorld().getBlockAt(x, y, z);
                    if(block.getType().isSolid() && block.getRelative(BlockFace.UP).getType().equals(Material.AIR) && block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().equals(Material.AIR)){
                        locations.add(block.getRelative(BlockFace.UP).getLocation().add(0.5, 0.5, 0.5));
                    }
                    
                    
                    z++;
                    if(Calendar.getInstance().after(timeLimit))
                        break;
                }
                y++;
                if(Calendar.getInstance().after(timeLimit))
                    break;
            }
            x++;
            if(Calendar.getInstance().after(timeLimit))
                break;
        }
        if(x<mx){
            if(RandomEncounters.getInstance().getLogLevel()>8){
                RandomEncounters.getInstance().logMessage("More time needed for "+placedEncounter.getName()+" spawn locations, "+locations.size()+" found so far.");
            }
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>8){
                RandomEncounters.getInstance().logMessage("Spawn location search complete for "+placedEncounter.getName()+", "+locations.size()+" found.");
            }
            placedEncounter.setSpawnLocations(locations);
            (new SpawningTask(placedEncounter)).runTaskTimer(RandomEncounters.getInstance(),1,1);
            cancel();
        }
    }
    
}
