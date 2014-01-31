package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.Encounter;
import com.archmageinc.RandomEncounters.Encounters.EncounterPlacer;
import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.Calendar;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class ChunkLocatorTask extends BukkitRunnable implements EncounterPlacer{
    private boolean wait            =   false;
    private final int distance;
    private final Chunk startingChunk;
    private final EncounterPlacer placer;
    private final int sx;
    private final int sz;
    private final int mx;
    private final int mz;
    private int x;
    private int z;
    
    
    
    public ChunkLocatorTask(EncounterPlacer placer,Chunk startingChunk,int distance){
        this.placer         =   placer;
        this.startingChunk  =   startingChunk;
        this.distance       =   distance;
        sx                  =   startingChunk.getX()-distance;
        sz                  =   startingChunk.getZ()-distance;
        mx                  =   startingChunk.getX()+distance;
        mz                  =   startingChunk.getZ()+distance;
        x                   =   sx;
        z                   =   sz;
        if(RandomEncounters.getInstance().getLogLevel()>9){
            RandomEncounters.getInstance().logMessage("Prepairing to check chunks with a distance of "+distance+" from chunk "+startingChunk.getX()+","+startingChunk.getZ());
        }
    }
    
    @Override
    public void run() {
        if(wait){
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("Chunk Locator for "+placer.getEncounter().getName()+" still waiting for chunk check");
            }
            return;
        }
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(x<mx){
            
            z   =   z>=mz ? sz : z;
            while(z<mz){
                Chunk currentChunk  =   startingChunk.getWorld().getChunkAt(x, z);
                (new ChunkCheckTask(this,currentChunk,placer.getEncounter())).runTaskTimer(RandomEncounters.getInstance(), 1, 1);
                if(RandomEncounters.getInstance().getLogLevel()>11){
                    RandomEncounters.getInstance().logMessage("Chunk Locator for "+placer.getEncounter().getName()+" starting to wait for chunk check at "+currentChunk.getX()+","+currentChunk.getZ());
                }
                wait    =   true;
                z++;
                if(Calendar.getInstance().after(timeLimit) || wait){
                    if(z>=mz){
                        x++;
                    }
                    break;
                }
                   
            }
            if(Calendar.getInstance().after(timeLimit) || wait)
                break;
            x++;
        }
        if(x>=mx){
            fail();
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>11){
               RandomEncounters.getInstance().logMessage("Chunk Locator for "+placer.getEncounter().getName()+" needs more time");
            }
        }
    }

    @Override
    public void addPlacedEncounter(PlacedEncounter newEncounter) {
        if(newEncounter==null){
            wait    =   false;
        }else{
            placer.addPlacedEncounter(newEncounter);
            cancel();
        }
    }
    
    private void fail(){
        if(RandomEncounters.getInstance().getLogLevel()>8){
            RandomEncounters.getInstance().logMessage("Chunk Locator for "+placer.getEncounter().getName()+" did not find any available locations");
        }
        placer.addPlacedEncounter(null);
        cancel();
    }

    @Override
    public Encounter getEncounter() {
        return placer.getEncounter();
    }
    
}
