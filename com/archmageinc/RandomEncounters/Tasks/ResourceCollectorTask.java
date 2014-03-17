package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.ResourceCollection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents the task responsible for checking PlacedEncounter Expansions.
 * This is designed to run once per minute.
 * 
 * @author ArchmageInc
 */
public class ResourceCollectorTask extends BukkitRunnable{
    
    private final ResourceCollection collector;
    private int i                         =   0;
    private final List<ItemStack> items   =   new ArrayList();
    
    public ResourceCollectorTask(ResourceCollection collection){
        collector   =   collection;
        
    }
    
    @Override
    public void run() {
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(i<collector.getTotal()){
            double a    =   Math.random()*Math.PI*2;
            double r    =   Math.round(Math.random()*collector.getDistance());
            int cx      =   collector.getLocation().getChunk().getX()+((int) Math.round(Math.sin(a)*r));
            int cz      =   collector.getLocation().getChunk().getZ()+((int) Math.round(Math.cos(a)*r));
            Chunk chunk =   collector.getLocation().getWorld().getChunkAt(cx, cz);
            Block block =   getNonAirBlock(chunk);
            if(!PlacedEncounter.isBlockOwned(block)){
                items.addAll(getResources(block,0));
            }
                
            i++;
            if(Calendar.getInstance().after(timeLimit)){
                break;
            }
        }
        if(i>=collector.getTotal()){
            collector.collectResources(items);
            cancel();
        }
    }
    
    private List<ItemStack> getResources(Block block,int n){
        List<ItemStack> resourceItems  =   new ArrayList();
        if(collector.validResource(block.getType())){
            resourceItems.addAll(block.getDrops());
            block.setType(Material.AIR);
            if(n<10){
                resourceItems.addAll(getResources(block.getRelative(BlockFace.UP),n+1));
                resourceItems.addAll(getResources(block.getRelative(BlockFace.DOWN),n+1));
                resourceItems.addAll(getResources(block.getRelative(BlockFace.EAST),n+1));
                resourceItems.addAll(getResources(block.getRelative(BlockFace.WEST),n+1));
                resourceItems.addAll(getResources(block.getRelative(BlockFace.NORTH),n+1));
                resourceItems.addAll(getResources(block.getRelative(BlockFace.SOUTH),n+1));
            }
            
        }
        return resourceItems;
    }
    
    private Block getNonAirBlock(Chunk chunk){
        Block block     =   null;
        int n           =   0;
        while(n<100 && (block==null || block.getType().equals(Material.AIR))){
            int h   =   ((Long) Math.round(Math.random()*(collector.getMaxHeight()-collector.getMinHeight())+collector.getMinHeight())).intValue();
            int x   =   ((Long) Math.round(Math.random()*16)).intValue();
            int y   =   ((Long) Math.round(Math.random()*h)).intValue();
            int z   =   ((Long) Math.round(Math.random()*16)).intValue();
            
            block   =   chunk.getBlock(x, y, z);
            n++;
        }
        
        return block;
    }
}
