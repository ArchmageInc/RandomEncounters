/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author bbrooks
 */
public class TreasurePlacementTask extends BukkitRunnable {
    private final Set<Chest> chests                 =   new HashSet();
    private final PlacedEncounter placedEncounter;
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
    private Iterator<Chest> itr;
    
    
    public TreasurePlacementTask(PlacedEncounter placedEncounter){
        this.placedEncounter    =   placedEncounter;
        this.sx                 =   placedEncounter.getLocation().getBlockX()-placedEncounter.getEncounter().getStructure().getWidth()/2;
        this.sy                 =   placedEncounter.getLocation().getBlockY()-placedEncounter.getEncounter().getStructure().getHeight()/2;
        this.sz                 =   placedEncounter.getLocation().getBlockZ()-placedEncounter.getEncounter().getStructure().getLength()/2;
        this.mx                 =   placedEncounter.getLocation().getBlockX()+placedEncounter.getEncounter().getStructure().getWidth()/2;
        this.my                 =   placedEncounter.getLocation().getBlockY()+placedEncounter.getEncounter().getStructure().getHeight()/2;
        this.mz                 =   placedEncounter.getLocation().getBlockZ()+placedEncounter.getEncounter().getStructure().getLength()/2;
        this.x                  =   sx;
        this.y                  =   sy;
        this.z                  =   sz;
        this.pass               =   1;
    }
    
    private void checkBlock(int bx,int by,int bz){
        if(placedEncounter.getLocation().getWorld().getBlockAt(bx,by,bz).getState() instanceof Chest){
            chests.add((Chest) placedEncounter.getLocation().getWorld().getBlockAt(bx,by,bz).getState());
        }
    }
    
    @Override
    public void run(){
        if(!placedEncounter.getEncounter().hasTreasures()){
            stop();
            return;
        }
        switch(pass){
            case 1: firstPass();
                break;
            case 2: secondPass();
                break;
            default: stop();
                break;
        }
    }
    
    private void setupIterators(){
        itr =   chests.iterator();
    }
    
    private void secondPass(){
        Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
        timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
        while(itr.hasNext()){
            Chest chest       =   itr.next();
            List<ItemStack> items   =   placedEncounter.getEncounter().getTreasure();
            for(ItemStack item : items){
                chest.getInventory().addItem(item);
            }
            
            
            if(Calendar.getInstance().after(timeLimit)){
                if(RandomEncounters.getInstance().getLogLevel()>9){
                    RandomEncounters.getInstance().logMessage("Treasure placement needs more time for "+placedEncounter.getName());
                }
                break;
            }
        }
        if(!itr.hasNext()){
            if(RandomEncounters.getInstance().getLogLevel()>9){
                RandomEncounters.getInstance().logMessage("Treasure placement for "+placedEncounter.getName()+" completed second pass");
            }
            pass++;
        }
    }
    
    private void firstPass() {
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
                RandomEncounters.getInstance().logMessage("Treasure placement for "+placedEncounter.getName()+" completed first pass [P2: "+chests.size()+"]");
            }
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>9){
                RandomEncounters.getInstance().logMessage("Treasure placement needs more time for "+placedEncounter.getName()+" P1("+x+"/"+mx+","+y+"/"+my+","+z+"/"+mz+")");
            }
        }
    }
    
    private void stop(){
        if(RandomEncounters.getInstance().getLogLevel()>6){
            RandomEncounters.getInstance().logMessage("Treasure placement finished for "+placedEncounter.getName());
        }
        cancel();
    }
    
}
