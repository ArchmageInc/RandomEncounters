package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.Encounter;
import com.archmageinc.RandomEncounters.Encounters.EncounterPlacer;
import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.Calendar;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class ChunkCheckTask extends BukkitRunnable{
    private final Encounter encounter;
    private final EncounterPlacer placer;
    private final Chunk chunk;
    private Location location;
    private boolean wait    =   false;
    private int x;
    private int y;
    private int z;
    private int sx    =   0;
    private int sy    =   0;
    private int sz    =   0;
    private int mx    =   16;
    private int my    =   0;
    private int mz    =   16;
    
    public ChunkCheckTask(EncounterPlacer placer,Chunk chunk, Encounter encounter){
        if(RandomEncounters.getInstance().getLogLevel()>9){
            RandomEncounters.getInstance().logMessage("Prepairing to check chunk: "+chunk.getX()+","+chunk.getZ()+" for encounter "+encounter.getName());
        }
        this.placer     =   placer;
        this.chunk      =   chunk;
        this.encounter  =   encounter;
        if(encounter.getStructure()==null){
            Block currentBlock;
            if(RandomEncounters.getInstance().getLogLevel()>0){
                RandomEncounters.getInstance().logWarning(encounter.getName()+" Has no structure, sending first air block for location!");
            }
            for(y=chunk.getWorld().getMaxHeight();y>0;y--){
                currentBlock  =   chunk.getBlock(7, y, 7);
                if(currentBlock.getType().isSolid()){
                    location    =   currentBlock.getRelative(BlockFace.UP).getLocation();
                    break;
                }
            }
            if(location==null){
                fail();
            }
            
        }else{
            my              =   encounter.getStructure().getMaxY().intValue();
            mz              =   16;
            sx              =   0;
            sy              =   encounter.getStructure().getMinY().intValue();
            sz              =   0;
            x               =   sx;
            z               =   sz;
            y               =   sy;
        }
    }
    
    @Override
    public void run() {
        if(wait){
            if(RandomEncounters.getInstance().getLogLevel()>10){
                RandomEncounters.getInstance().logMessage("Chunk Check for "+encounter.getName()+" still waiting for space check");
            }
            return;
        }else if(location!=null){
            success();
            return;
        }
        Block currentBlock,aboveBlock;
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        
        while(y<my){
            
            x   =   x>=mx ? sx : x;
            while(x<mx){
                
                z   =   z>=mz ? sz : z;
                while(z<mz){
                    currentBlock    =   chunk.getBlock(x, y, z);
                    aboveBlock      =   currentBlock.getRelative(BlockFace.UP);
                    if(
                        (encounter.getValidBiomes().isEmpty() || encounter.getValidBiomes().contains(currentBlock.getBiome()))

                        && (!encounter.getInvalidBiomes().contains(currentBlock.getBiome()))

                        /**
                        * The current block may not be:
                        */
                        && !encounter.getStructure().getInvalid().contains(currentBlock.getType())

                        /**
                        * The block above the current block must be
                        */

                        && encounter.getStructure().getTrump().contains(aboveBlock.getType())
                    ){
                        wait    =   true;
                        (new SpaceCheckTask(this,currentBlock,encounter.getStructure())).runTaskTimer(RandomEncounters.getInstance(), 1, 1);
                        if(RandomEncounters.getInstance().getLogLevel()>11){
                            RandomEncounters.getInstance().logMessage("Chunk Check for "+encounter.getName()+" starting to wait for space check at "+currentBlock.getX()+","+currentBlock.getY()+","+currentBlock.getZ());
                        }
                    }else{
                        if(RandomEncounters.getInstance().getLogLevel()>10){
                            RandomEncounters.getInstance().logMessage("Chunk Check for "+encounter.getName()+" "+currentBlock.getX()+","+currentBlock.getY()+","+currentBlock.getZ()+" is not a valid location");
                        }
                    }
                    
                    
                    z++;
                    if(Calendar.getInstance().after(timeLimit) || wait){
                        if(z>=mz){
                            x++;
                        }
                        break;
                    }
                }
                if(Calendar.getInstance().after(timeLimit)|| wait){
                    if(x>=mx){
                        y++;
                    }
                    break;
                }
                x++;
            }
            if(Calendar.getInstance().after(timeLimit)|| wait)
               break;
            y++;
        }
        
        if(y>=my){
            fail();
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("Chunk Check for "+encounter.getName()+" needs more time");
            }
        }
    }
    
    public void setLocation(Location location){
        this.location   =   location;
        wait            =   false;
    }
    
    private void success(){
        PlacedEncounter placedEncounter =   PlacedEncounter.create(encounter,location);
        placer.addPlacedEncounter(placedEncounter);
        cancel();
    }
    
    private void fail(){
        if(RandomEncounters.getInstance().getLogLevel()>9){
            RandomEncounters.getInstance().logMessage("Chunk Check for "+encounter.getName()+" found no valid locations in chunk "+chunk.getX()+","+chunk.getZ());
        }
        placer.addPlacedEncounter(null);
        cancel();
    }
}
