package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Structures.Structure;
import java.util.Calendar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class SpaceCheckTask extends BukkitRunnable{
    private final Structure structure;
    private final Block startingBlock;
    private final ChunkCheckTask chunkTask;
    private int pass    =   1;
    private int x;
    private int y;
    private int z;
    private final int sx;
    private final int sy;
    private final int sz;
    private final int mx;
    private final int my;
    private final int mz;
    
    public SpaceCheckTask(ChunkCheckTask chunkTask,Block startingBlock, Structure structure){
        this.chunkTask      =   chunkTask;
        this.structure      =   structure;
        this.startingBlock  =   startingBlock;
        mx                  =   (int) Math.ceil(structure.getWidth()/2);
        my                  =   structure.getHeight();
        mz                  =   (int) Math.ceil(structure.getLength()/2);
        sx                  =   -mx;
        sy                  =   1;
        sz                  =   -mz;
        x                   =   sx;
        z                   =   sz;
        y                   =   sy;
        if(RandomEncounters.getInstance().getLogLevel()>11){
            RandomEncounters.getInstance().logMessage("Space Check for "+structure.getName()+" prepairing to run starting at "+startingBlock.getX()+","+startingBlock.getY()+","+startingBlock.getZ());
        }
        
    }
    
    @Override
    public void run() {
       try{
        switch(pass){
            case 1: firstPass();
                break;
            case 2: secondPass();
                break;
            default: fail();
                break;
        }
       }catch(RuntimeException e){
          RandomEncounters.getInstance().logWarning("Woah nelly! "+e.getMessage());
       }
    }
    
    private void firstPass(){
        Block currentBlock,belowBlock;
        Boolean stop        =   false;
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(x<mx){
            
            z   =   z>=mz ? sz : z;
            while(z<mz){
                currentBlock  =   startingBlock.getRelative(x,0,z);
                belowBlock    =   currentBlock.getRelative(BlockFace.DOWN);
                if(RandomEncounters.getInstance().getLogLevel()>12){
                    RandomEncounters.getInstance().logMessage(structure.getName()+" P1("+x+","+y+","+z+"): "+currentBlock.getType().name());
                }
                if(structure.getInvalid().contains(currentBlock.getType()) || structure.getInvalid().contains(belowBlock.getType())){
                    if(RandomEncounters.getInstance().midas()){
                        currentBlock.setType(Material.GOLD_BLOCK);
                    }
                    stop    =   true;
                }
                if(Calendar.getInstance().after(timeLimit) || stop){
                    if(z>=mz){
                        x++;
                    }
                    break;
                }
                z++;
            }
            if(Calendar.getInstance().after(timeLimit) || stop)
                break;
            x++;
        }
        if(stop){
            fail();
        }else if(x>=mx){
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("First pass cleared for "+structure.getName()+" at "+startingBlock.getX()+","+startingBlock.getY()+","+startingBlock.getZ());
            }
            pass++;
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("Space Check for "+structure.getName()+" first pass needs more time");
            }
        }
    }
    
    private void secondPass(){
        Block currentBlock;
        Boolean stop        =   false;
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        
        while(y<my){
            
            x   =   x>=mx ? sx : x;
            while(x<mx){
                
                z   =   z<=mz ? sz : z;
                while(z<mz){
                    
                    currentBlock  =   startingBlock.getRelative(x,y,z);
                    if(RandomEncounters.getInstance().getLogLevel()>12){
                        RandomEncounters.getInstance().logMessage(structure.getName()+" P2("+x+","+y+","+z+"): "+currentBlock.getType().name());
                    }
                    if(!structure.getTrump().contains(currentBlock.getType())){
                        if(RandomEncounters.getInstance().midas()){
                            currentBlock.setType(Material.GOLD_BLOCK);
                        }
                        stop    =   true;
                    }
                    
                    z++;
                    if(Calendar.getInstance().after(timeLimit) || stop){
                        if(z>=mz){
                            x++;
                        }
                        break;
                    }
                        
                }
                if(Calendar.getInstance().after(timeLimit) || stop){
                    if(x>=mx){
                        y++;
                    }
                    break;
                }
                x++;
            }
            if(Calendar.getInstance().after(timeLimit) || stop)
                break;
            y++;
        }
        if(stop){
            fail();
        }else if(x>=mx){
            success();
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("Space check for "+structure.getName()+" second pass needs more time");
            }
        }
        
    }
    
    private void success(){
        Location location   =   startingBlock.getRelative(BlockFace.UP).getLocation();
        if(RandomEncounters.getInstance().getLogLevel()>10){
            RandomEncounters.getInstance().logMessage("Space Check for "+structure.getName()+" found location at "+location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ());
        }
        chunkTask.setLocation(location);
        cancel();
    }
    
    private void fail(){
        if(RandomEncounters.getInstance().getLogLevel()>11){
            RandomEncounters.getInstance().logMessage("Space Check for "+structure.getName()+" "+startingBlock.getX()+","+startingBlock.getY()+","+startingBlock.getZ()+" is not a valid location");
        }
        chunkTask.setLocation(null);
        cancel();
    }
}
