package com.archmageinc.RandomEncounters.Encounters;

import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Utilities.Accountant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author ArchmageInc
 */
public class Vault {
    
    private final PlacedEncounter placedEncounter;
    private final Set<Location> locations               =   new HashSet();
    private boolean full                                =   false;
    private final HashMap<Material,Integer> ledger      =   new HashMap();
    private static final HashMap<Location,Vault> owners =   new HashMap();
    
    public Vault(PlacedEncounter placedEncounter){
        this.placedEncounter =   placedEncounter;
        findChests();
    }
    
    private void findChests(){
        int sx  =   placedEncounter.getLocation().getBlockX()-placedEncounter.getEncounter().getStructure().getWidth();
        int sy  =   placedEncounter.getLocation().getBlockY()-placedEncounter.getEncounter().getStructure().getHeight();
        int sz  =   placedEncounter.getLocation().getBlockZ()-placedEncounter.getEncounter().getStructure().getLength();
        int mx  =   placedEncounter.getLocation().getBlockX()+placedEncounter.getEncounter().getStructure().getWidth();
        int my  =   placedEncounter.getLocation().getBlockY()+placedEncounter.getEncounter().getStructure().getHeight();
        int mz  =   placedEncounter.getLocation().getBlockZ()+placedEncounter.getEncounter().getStructure().getLength();
        int x   =   sx;
        int y   =   sy;
        int z   =   sz;
        while(x<mx){
            y   =   y>=my ? sy : y;
            while(y<my){
                z   =   z>=mz ? sz : z;
                while(z<mz){
                    Block block =   placedEncounter.getLocation().getWorld().getBlockAt(x,y,z);
                    if(block.getState() instanceof Chest){
                        if(!owners.containsKey(block.getLocation())){
                            locations.add(block.getLocation());
                            owners.put(block.getLocation(), this);
                        }else{
                            if(RandomEncounters.getInstance().getLogLevel()>5){
                                RandomEncounters.getInstance().logWarning(getVaultName()+" attempted to grab a chest from "+owners.get(block.getLocation()).getVaultName());
                            }
                        }
                    }
                    z++;
                }
                y++;
            }
            x++;
        }
        if(locations.isEmpty()){
            full    =   true;
        }
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage(getVaultName()+" located "+locations.size()+" storage inventories");
        }
        initializeLedger();
    }
    
    public PlacedEncounter getEncounter(){
        return placedEncounter;
    }
    
    public boolean isFull(){
        return full;
    }
    
    public List<ItemStack> deposit(HashMap<Material,Integer> resources){
        return deposit(Accountant.convert(resources));
    }
    public List<ItemStack> deposit(List<ItemStack> items){
        return deposit(items.toArray(new ItemStack[0]));
    }
    public List<ItemStack> deposit(ItemStack[] items){
        if(RandomEncounters.getInstance().getLogLevel()>5){
            RandomEncounters.getInstance().logMessage(getVaultName()+ " - Attempting to deposit "+items.length+" item stacks");
        }
        List<ItemStack> leftovers   =   Arrays.asList(items);
        if(!locations.isEmpty() && items.length>0){
            Iterator<Location> litr =   locations.iterator();
            while(litr.hasNext()){
                Location location   =   litr.next();
                if(validInventory(location,litr)){
                    Inventory inventory =   ((Chest) location.getBlock().getState()).getBlockInventory();
                    leftovers           =   new ArrayList(inventory.addItem(items).values());
                    if(leftovers.isEmpty()){
                        if(RandomEncounters.getInstance().getLogLevel()>9){
                            RandomEncounters.getInstance().logMessage(getVaultName()+" - Successfully deposited all items");
                        }
                        break;
                    }
                }
            }
        }
        ledgerTransaction(Accountant.subtract(Accountant.convert(items), Accountant.convert(leftovers)),1);
        full    =   leftovers.isEmpty();
        return leftovers;
    }
    
    public List<ItemStack> withdraw(HashMap<Material,Integer> resources){
        return withdraw(Accountant.convert(resources));
    }
    public List<ItemStack> withdraw(List<ItemStack> items){
        return withdraw(items.toArray(new ItemStack[0]));
    }
    public List<ItemStack> withdraw(ItemStack[] items){
        if(RandomEncounters.getInstance().getLogLevel()>5){
            RandomEncounters.getInstance().logMessage(getVaultName()+ " - Attempting to withdraw "+items.length+" item stacks");
        }
        List<ItemStack> leftovers   =   Arrays.asList(items);
        if(!locations.isEmpty() && items.length>0){
            Iterator<Location> litr =   locations.iterator();
            while(litr.hasNext()){
                Location location   =   litr.next();
                if(validInventory(location,litr)){
                    Inventory inventory =   ((Chest) location.getBlock().getState()).getBlockInventory();
                    leftovers           =   new ArrayList(inventory.removeItem(items).values());
                    if(leftovers.isEmpty()){
                        if(RandomEncounters.getInstance().getLogLevel()>9){
                            RandomEncounters.getInstance().logMessage(getVaultName()+" - Successfully withdrew all items");
                        }
                        break;
                    }
                }
            }
        }
        ledgerTransaction(Accountant.subtract(Accountant.convert(items),Accountant.convert(leftovers)),-1);
        full    =   false;
        return leftovers;
    }
    
    private boolean validInventory(Location location,Iterator<Location> itr){
        if(location==null || !(location.getBlock().getState() instanceof Chest)){
           if(RandomEncounters.getInstance().getLogLevel()>4){
                RandomEncounters.getInstance().logMessage(getVaultName()+" detected missing inventory, removing it");
           }
           owners.remove(location);
           itr.remove(); 
           return false;
        }
        return true;
    }
    
    public HashMap<Material,Integer> contains(HashMap<Material,Integer> amounts){
        return  Accountant.complement(amounts, ledger);
    }
    
    private void ledgerTransaction(HashMap<Material,Integer> amounts,int type){
       if(amounts.isEmpty()){
           return;
       }
       type =   type<0 ? -1 : 1;
       for(Material material : amounts.keySet()){
            int amount  =   ledger.containsKey(material) ? ledger.get(material)+(amounts.get(material)*type) : amounts.get(material);
            if(amount==0){
                ledger.remove(material);
            }else{
                ledger.put(material, amount);
            }
        }
    }
    
    private void initializeLedger(){
        if(!locations.isEmpty()){
            Iterator<Location> itr            =   locations.iterator();
            List<ItemStack> items             =   new ArrayList();
            while(itr.hasNext()){
                Location location =   itr.next();
                if(validInventory(location,itr)){
                   Inventory inventory                  =   ((Chest) location.getBlock().getState()).getBlockInventory();
                   
                   for(ItemStack item : inventory.getContents()){
                       if(item==null){
                           continue;
                       }
                       items.add(item);
                   }
                }
            }
            ledgerTransaction(Accountant.convert(items),1);
        }
    }
    
    private String getVaultName(){
        return placedEncounter.getName()+"(Vault)";
    }
}
