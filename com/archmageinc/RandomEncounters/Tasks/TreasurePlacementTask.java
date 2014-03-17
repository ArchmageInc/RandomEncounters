package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.Calendar;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class TreasurePlacementTask extends BukkitRunnable {
    private final PlacedEncounter placedEncounter;
    private final int[] blockLocations;
    private int i   =   0;
    private int t   =   0;
    
    
    public TreasurePlacementTask(PlacedEncounter placedEncounter){
        this.placedEncounter    =   placedEncounter;
        if(placedEncounter.getEncounter().getStructure()==null){
            if(RandomEncounters.getInstance().getLogLevel()>0){
                RandomEncounters.getInstance().logWarning("Tresure Placement: "+placedEncounter.getName()+" has no structure: No treasures will be placed");
            }
            blockLocations  =   new int[0];
        }else{
            blockLocations  =   placedEncounter.getBlockLocations();
        }
        if(RandomEncounters.getInstance().getLogLevel()>8){
            RandomEncounters.getInstance().logMessage("Treasure Placement for "+placedEncounter.getName()+" checking "+(blockLocations.length/3)+" locations.");
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
                Block block         =   placedEncounter.getLocation().getWorld().getBlockAt(x, y, z);
                if(block.getState() instanceof Chest){
                    List<ItemStack> items   =   placedEncounter.getEncounter().getTreasure();
                    ((Chest) block.getState()).getInventory().addItem(items.toArray(new ItemStack[0]));
                    t++;
                }
            }
            i += 3;
            if(Calendar.getInstance().after(timeLimit)){
                break;
            }
        }
        if(i>=blockLocations.length){
            if(RandomEncounters.getInstance().getLogLevel()>8){
                RandomEncounters.getInstance().logMessage("Treasure Placement complete for "+placedEncounter.getName()+": found "+t+" chests");
            }
            cancel();
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("More time needed for "+placedEncounter.getName()+" Treasure Placement: "+t+" found so far");
            }
        }
    }
    
}
