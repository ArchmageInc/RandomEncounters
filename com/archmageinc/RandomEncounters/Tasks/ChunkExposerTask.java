package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.Calendar;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class ChunkExposerTask extends BukkitRunnable{
    private int i                       =   0;
    private final Chunk startingChunk;
    private int r;
    private double d;
    private double a;
    private double t;
    private final int cx;
    private final int cz;
    
    
    public ChunkExposerTask(Chunk startingChunk,int radius){
        this.startingChunk  =   startingChunk;
        this.r              =   radius;
        this.d              =   .5*(6*this.r+Math.pow(-1,this.r)-1);
        this.a              =   Math.PI/this.d;
        this.t              =   (2*Math.PI)/a;
        this.cx             =   startingChunk.getX();
        this.cz             =   startingChunk.getZ();
    }
    
    @Override
    public void run() {
       
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        
        while(r>0){
            d  =   .5*(6*r+Math.pow(-1,r)-1);
            a  =   Math.PI/d;
            t  =   (2*Math.PI)/a;
            while(i<t){

                int x   =   cx+((int) Math.round(Math.sin(a*i)*r));
                int z   =   cz+((int) Math.round(Math.cos(a*i)*r));
                
                
                if(RandomEncounters.getInstance().getLogLevel()>9){
                    RandomEncounters.getInstance().logMessage("Chunk Exposer exposing chunk at "+x+","+z);
                }
                startingChunk.getWorld().getChunkAt(x, z);
                
                i++;
                if(Calendar.getInstance().after(timeLimit)){
                    if(i>=t){
                       i=0;
                       r--;
                    }
                    break;
                } 
                
            }
            if(Calendar.getInstance().after(timeLimit))
                break;
            r--;
            i=0;
        }
        if(r<=0){
            RandomEncounters.getInstance().logMessage("Exposure task complete");
            cancel();
        }
    }
}
