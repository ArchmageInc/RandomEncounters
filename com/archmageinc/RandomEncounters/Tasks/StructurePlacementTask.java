package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Structures.Structure;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class StructurePlacementTask extends BukkitRunnable {
    private final Structure structure;
    private final PlacedEncounter placedEncounter;
    private final EditSession session;
    private final CuboidClipboard cuboid;
    private final Vector v;
    private final HashMap<Vector,BaseBlock> lastQueue   =   new HashMap();
    private final HashMap<Vector,BaseBlock> finalQueue  =   new HashMap();
    private final int sx;
    private final int sy;
    private final int sz;
    private final int mx;
    private final int my;
    private final int mz;
    private int x;
    private int y;
    private int z;
    private int pass;
    private Iterator<Vector> lastItr;
    private Iterator<Vector> finalItr;
    private int maxLockTime;
    
    public StructurePlacementTask(PlacedEncounter encounter){
        if(encounter==null){
            throw new IllegalArgumentException("PlacedEncounter is required for structure placement!");
        }
        this.placedEncounter    =   encounter;
        this.structure          =   encounter.getEncounter().getStructure();
        this.cuboid             =   encounter.getEncounter().getStructure().getCuboid();
        this.pass               =   1;
        this.v                  =   new Vector(encounter.getLocation().getX(),encounter.getLocation().getY(),encounter.getLocation().getZ());
        this.sx                 =   -cuboid.getSize().getBlockX()/2;
        this.sy                 =   cuboid.getOffset().getBlockY();
        this.sz                 =   -cuboid.getSize().getBlockZ()/2;
        this.mx                 =   cuboid.getSize().getBlockX()/2;
        this.my                 =   cuboid.getSize().getBlockY()+cuboid.getOffset().getBlockY();
        this.mz                 =   cuboid.getSize().getBlockZ()/2;
        this.x                  =   sx;
        this.y                  =   sy;
        this.z                  =   sz;
        this.session            =   new EditSession((new BukkitWorld(encounter.getLocation().getWorld())),cuboid.getWidth()*cuboid.getLength()*cuboid.getHeight()*2);
    }
    
    private void checkBlock(int x, int y, int z){
        Vector bv        =   new Vector(x,y,z);
        Vector cv        =   new Vector(x+cuboid.getSize().getBlockX()/2,y-cuboid.getOffset().getBlockY(),z+cuboid.getSize().getBlockZ()/2);
        BaseBlock block  =   cuboid.getBlock(cv);
        if(block==null){
            return;
        }
        if(BlockType.shouldPlaceLast(block.getType())){
            lastQueue.put(bv.add(v), block);
        }else if(block.getType()==BlockType.WATER.getID() || block.getType()==BlockType.STATIONARY_WATER.getID() || block.getType()==BlockType.STATIONARY_LAVA.getID() || block.getType()==BlockType.LAVA.getID() || BlockType.shouldPlaceFinal(block.getType())){
            finalQueue.put(bv.add(v), block);
        }else{
            setBlock(bv.add(v),block);
        }
    }
    
    private void setBlock(Vector v,BaseBlock block){
        session.rawSetBlock(v, block);
        if(block.getType()!=0){
            placedEncounter.addBlockLocation(v.getBlockX(),v.getBlockY(),v.getBlockZ());
        }
    }
    
    private void setupIterators(){
        lastItr     =   lastQueue.keySet().iterator();
        finalItr    =   finalQueue.keySet().iterator();
    }
    
    private boolean isPlayerNearby(){
        Location location      =   placedEncounter.getLocation();
        List<Player> players   =   location.getWorld().getPlayers();
        for(Player player : players){
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage(player.getDisplayName()+" is "+location.distance(player.getLocation())+" away from placing structure "+placedEncounter.getName());
            }
            if(location.distance(player.getLocation())<structure.getLength() || location.distance(player.getLocation())<structure.getWidth()){
                if(RandomEncounters.getInstance().getLogLevel()>10){
                    RandomEncounters.getInstance().logMessage("A player is "+location.distance(player.getLocation())+" away from placing structure "+placedEncounter.getName()+". Backing down to 1ms");
                }
                return true;
            }
        }
        return false;
    }
    
    
    @Override
    public void run() {
        maxLockTime =   isPlayerNearby() ? 1 : RandomEncounters.getInstance().lockTime();
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
        timeLimit.add(Calendar.MILLISECOND, maxLockTime);
        while(lastItr.hasNext()){
            Vector bv       =   lastItr.next();
            BaseBlock block =   lastQueue.get(bv);
            setBlock(bv,block);
            if(Calendar.getInstance().after(timeLimit)){
                if(RandomEncounters.getInstance().getLogLevel()>11){
                    RandomEncounters.getInstance().logMessage("Structure placement needs more time for "+placedEncounter.getName()+" P2("+bv.getBlockX()+","+bv.getBlockY()+","+bv.getBlockZ()+")");
                }
                break;
            }
        }
        if(!lastItr.hasNext()){
            if(RandomEncounters.getInstance().getLogLevel()>10){
                RandomEncounters.getInstance().logMessage("Structure placement for "+placedEncounter.getName()+" completed second pass [P3: "+finalQueue.size()+"]");
            }
            pass++;
        }
    }
    
    private void thirdPass(){
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, maxLockTime);
        while(finalItr.hasNext()){
            Vector bv       =   finalItr.next();
            BaseBlock block =   finalQueue.get(bv);
            setBlock(bv,block);
            if(Calendar.getInstance().after(timeLimit)){
                if(RandomEncounters.getInstance().getLogLevel()>11){
                    RandomEncounters.getInstance().logMessage("Structure placement needs more time for "+placedEncounter.getName()+" P3("+bv.getBlockX()+","+bv.getBlockY()+","+bv.getBlockZ()+")");
                }
                break;
            }
        }
        if(!finalItr.hasNext()){
            if(RandomEncounters.getInstance().getLogLevel()>10){
                RandomEncounters.getInstance().logMessage("Structure placement for "+placedEncounter.getName()+" completed third pass");
            }
            pass++;
        }
    }
    
    private void firstPass(){
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, maxLockTime);
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
            if(RandomEncounters.getInstance().getLogLevel()>10){
                RandomEncounters.getInstance().logMessage("Structure placement for "+placedEncounter.getName()+" completed first pass [P2: "+lastQueue.size()+", P3: "+finalQueue.size()+"]");
            }
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>11){
                RandomEncounters.getInstance().logMessage("Structure placement needs more time for "+placedEncounter.getName()+" P1("+x+"/"+mx+","+y+"/"+my+","+z+"/"+mz+")");
            }
        }
        
    }
    
    private void stop(){
        if(RandomEncounters.getInstance().getLogLevel()>9){
            RandomEncounters.getInstance().logMessage("Structure placement finished for "+placedEncounter.getName());
        }        
        structure.placed();
        placedEncounter.setupEncounter(true);
        cancel();
    }
    
}
