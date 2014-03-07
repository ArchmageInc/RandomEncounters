package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.Encounter;
import com.archmageinc.RandomEncounters.Encounters.EncounterPlacer;
import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author ArchmageInc
 */
public class ChunkLocatorTask extends BukkitRunnable implements EncounterPlacer{
    private boolean wait                =   false;
    private final Set<Chunk> checked    =   new HashSet();
    private int i                       =   0;
    private final Chunk startingChunk;
    private final EncounterPlacer placer;
    private int r;
    private final int mr;
    private final int xr;
    private final int fr;
    private final int pr;
    private double d;
    private double a;
    private double t;
    private final int cx;
    private final int cz;
    private final double s;
    
    
    public ChunkLocatorTask(EncounterPlacer placer,Chunk startingChunk,int maxDistance,int minDistance){
        this.placer         =   placer;
        this.startingChunk  =   startingChunk;
        this.r              =   placer.getPattern()>0 ? minDistance : maxDistance;
        this.fr             =   placer.getPattern()>0 ? maxDistance : minDistance;
        this.pr             =   placer.getPattern()>0 ? 1 : -1;
        this.mr             =   minDistance;
        this.xr             =   maxDistance;
        this.d              =   .5*(6*this.r+Math.pow(-1,this.r)-1);
        this.a              =   Math.PI/this.d;
        this.t              =   (2*Math.PI)/a;
        this.cx             =   startingChunk.getX();
        this.cz             =   startingChunk.getZ();
        this.s              =   placer.getInitialAngle();
        if(RandomEncounters.getInstance().getLogLevel()>9){
            RandomEncounters.getInstance().logMessage("Prepairing to check chunks with a radius of ("+maxDistance+" - "+minDistance+") from chunk "+startingChunk.getX()+","+startingChunk.getZ());
        }
    }
    
    private boolean checkDistance(Chunk currentChunk){
        for(String proxName : placer.getProximities().keySet()){
            Encounter proxEnc   =   Encounter.getInstance(proxName);
            Long minDistance    =   placer.getProximities().get(proxName);
            Vector currentV     =   new Vector(currentChunk.getX(),0,currentChunk.getZ());
            if(proxEnc!=null){
                for(PlacedEncounter check : RandomEncounters.getInstance().getPlacedEncounters(proxEnc)){
                    Vector checkV   =   new Vector(check.getLocation().getChunk().getX(),0,check.getLocation().getChunk().getZ());
                    double distance =   currentV.distance(checkV);
                    if(distance<minDistance){
                        if(RandomEncounters.getInstance().getLogLevel()>9){
                            RandomEncounters.getInstance().logMessage("Proximity alert: "+placer.getEncounter().getName()+" attempted to be placed "+distance+" from "+proxName+" must be at least "+minDistance);
                        }
                        return false;
                    }
                }
            }else{
                RandomEncounters.getInstance().logError("Proximity definition for "+placer.getEncounter().getName()+": "+proxName+" missing.");
            }
        }
        
        return true;
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
        
        while(pr>0 ? r<=fr : r>=fr){
            d  =   .5*(6*r+Math.pow(-1,r)-1);
            a  =   Math.PI/d;
            t  =   (2*Math.PI)/a;
            while(i<t){

                int x   =   cx+((int) Math.round(Math.sin(a*i+s)*r));
                int z   =   cz+((int) Math.round(Math.cos(a*i+s)*r));

                Chunk currentChunk  =   startingChunk.getWorld().getChunkAt(x, z);
                if(checked.contains(currentChunk)){
                    i++;
                    continue;
                }
                checked.add(currentChunk);
                if(!checkDistance(currentChunk)){
                    i++;
                    continue;
                }
                
                (new ChunkCheckTask(this,currentChunk,placer.getEncounter())).runTaskTimer(RandomEncounters.getInstance(), 1, 1);
                if(RandomEncounters.getInstance().getLogLevel()>11){
                    RandomEncounters.getInstance().logMessage("Chunk Locator for "+placer.getEncounter().getName()+" starting to wait for chunk check at "+currentChunk.getX()+","+currentChunk.getZ());
                }
                wait    =   true;
                i++;
                if(Calendar.getInstance().after(timeLimit) || wait){
                    if(i>=t){
                       i=0;
                       r--;
                    }
                    break;
                } 
                
            }
            if(Calendar.getInstance().after(timeLimit)|| wait)
                break;
            r+=pr;
        }
        if(pr>0 ? r>fr : r<fr){
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
    public Map<String,Long> getProximities(){
        return placer.getProximities();
    }
    
    @Override
    public long getPattern(){
        return placer.getPattern();
    }
    
    @Override
    public Encounter getEncounter() {
        return placer.getEncounter();
    }

    @Override
    public double getInitialAngle() {
        return s;
    }
    
}
