package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Structures.Structure;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class StructurePlacementTask extends BukkitRunnable {
    private final Structure structure;
    private final PlacedEncounter encounter;
    private final EditSession session;
    private final CuboidClipboard cuboid;
    private final Vector v;
    private final HashMap<Vector,BaseBlock> lastQueue   =   new HashMap();
    private final HashMap<Vector,BaseBlock> finalQueue  =   new HashMap();
    private final int sx                                =   0;
    private final int sy                                =   0;
    private final int sz                                =   0;
    private final int mx;
    private final int my;
    private final int mz;
    private int x;
    private int y;
    private int z;
    private int pass;
    private Iterator<Vector> lastItr;
    private Iterator<Vector> finalItr;
    
    public StructurePlacementTask(PlacedEncounter encounter,Structure structure,EditSession session,Vector v, CuboidClipboard cuboid){
        if(session==null || v==null || cuboid==null){
            throw new IllegalArgumentException("Session, Vector, or CuboidClipbard is null");
        }
        this.encounter  =   encounter;
        this.structure  =   structure;
        this.session    =   session;
        this.cuboid     =   cuboid;
        this.pass       =   1;
        this.v          =   v;
        this.mx         =   cuboid.getSize().getBlockX();
        this.my         =   cuboid.getSize().getBlockY();
        this.mz         =   cuboid.getSize().getBlockZ();
        this.x          =   sx;
        this.y          =   sy;
        this.z          =   sz;
    }
    
    private void checkBlock(int x, int y, int z){
        Vector bv        =   new Vector(x,y,z);
        BaseBlock block =   cuboid.getBlock(bv);
        if(block==null){
            return;
        }
        if(BlockType.shouldPlaceLast(block.getType())){
            lastQueue.put(bv.add(v), block);
        }else if(BlockType.shouldPlaceFinal(block.getType())){
            finalQueue.put(bv.add(v), block);
        }else{
            setBlock(bv.add(v),block);
        }
    }
    
    private void setBlock(Vector v,BaseBlock block){
        session.rawSetBlock(v, block);
    }
    
    private void setupIterators(){
        lastItr     =   lastQueue.keySet().iterator();
        finalItr    =   finalQueue.keySet().iterator();
    }
    
    @Override
    public void run() {
        switch(pass){
            case 1: firstPass();
                break;
            case 2: secondPass();
                break;
            case 3: thirdPass();
                break;
            default: stop();
                break;
        }
    }
    
    private void secondPass(){
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(lastItr.hasNext()){
            Vector bv       =   lastItr.next();
            BaseBlock block =   lastQueue.get(bv);
            setBlock(bv,block);
            if(Calendar.getInstance().after(timeLimit)){
                if(RandomEncounters.getInstance().getLogLevel()>9){
                    RandomEncounters.getInstance().logMessage("Structure placement needs more time for "+structure.getName()+" P2("+bv.getBlockX()+","+bv.getBlockY()+","+bv.getBlockZ()+")");
                }
                break;
            }
        }
        if(!lastItr.hasNext()){
            if(RandomEncounters.getInstance().getLogLevel()>9){
                RandomEncounters.getInstance().logMessage("Structure placement for "+structure.getName()+" completed second pass [P3: "+finalQueue.size()+"]");
            }
            pass++;
        }
    }
    
    private void thirdPass(){
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(finalItr.hasNext()){
            Vector bv       =   finalItr.next();
            BaseBlock block =   finalQueue.get(bv);
            setBlock(bv,block);
            if(Calendar.getInstance().after(timeLimit)){
                if(RandomEncounters.getInstance().getLogLevel()>9){
                    RandomEncounters.getInstance().logMessage("Structure placement needs more time for "+structure.getName()+" P3("+bv.getBlockX()+","+bv.getBlockY()+","+bv.getBlockZ()+")");
                }
                break;
            }
        }
        if(!finalItr.hasNext()){
            if(RandomEncounters.getInstance().getLogLevel()>9){
                RandomEncounters.getInstance().logMessage("Structure placement for "+structure.getName()+" completed third pass");
            }
            pass++;
        }
    }
    
    private void firstPass(){
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(x<mx){
            
            y   =   y>=my ? sy : y;
            while(y<my){
                
                z   =   z>=mz ? sz : z;
                while(z<mz){
                    checkBlock(x,y,z);
                    z++;
                    if(Calendar.getInstance().after(timeLimit)){
                        if(z>=mz){
                            y++;
                        }
                        break;
                    }
                }
                if(Calendar.getInstance().after(timeLimit)){
                    if(y>=my){
                        x++;
                    }
                    break;
                }
                y++;
            }
            if(Calendar.getInstance().after(timeLimit))
                break;
            x++;
        }
        if(x>=mx){
            setupIterators();
            pass++;
            if(RandomEncounters.getInstance().getLogLevel()>9){
                RandomEncounters.getInstance().logMessage("Structure placement for "+structure.getName()+" completed first pass [P2: "+lastQueue.size()+", P3: "+finalQueue.size()+"]");
            }
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>9){
                RandomEncounters.getInstance().logMessage("Structure placement needs more time for "+structure.getName()+" P1("+x+"/"+mx+","+y+"/"+my+","+z+"/"+mz+")");
            }
        }
        
    }
    
    private void stop(){
        if(RandomEncounters.getInstance().getLogLevel()>6){
            RandomEncounters.getInstance().logMessage("Structure placement finished for "+structure.getName());
        }        
        encounter.placeMobs();
        structure.placed();
        cancel();
    }
    
}
