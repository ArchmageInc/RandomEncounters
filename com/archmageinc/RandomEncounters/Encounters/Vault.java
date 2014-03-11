package com.archmageinc.RandomEncounters.Encounters;

import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final Set<Location> locations       =   new HashSet();
    private boolean full                        =   false;
    
    public Vault(PlacedEncounter encounter){
        placedEncounter =   encounter;
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
                        locations.add(block.getLocation());
                    }
                    z++;
                }
                y++;
            }
            x++;
        }
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage(placedEncounter.getName()+"(Vault) located "+locations.size()+" storage inventories");
        }
    }
    
    public PlacedEncounter getEncounter(){
        return placedEncounter;
    }
    
    public boolean isFull(){
        return full;
    }
    
    public List<ItemStack> deposit(HashMap<Material,Integer> resources){
        List<ItemStack> leftovers   =   Arrays.asList(convertResources(resources));
        return deposit(leftovers);
    }
    
    public List<ItemStack> deposit(List<ItemStack> items){
        if(RandomEncounters.getInstance().getLogLevel()>5){
            RandomEncounters.getInstance().logMessage("Depositing "+items.size()+" item stacks into "+placedEncounter.getName()+"(Vault)");
        }
        List<ItemStack> leftovers   =   items;
        Iterator<Location> itr     =   locations.iterator();
        if(!items.isEmpty()){
            int i   =   1;
            while(itr.hasNext()){
                Location location =   itr.next();
                if(validInventory(location,itr)){
                    Inventory inventory                 =   ((Chest) location.getBlock().getState()).getBlockInventory();
                    ItemStack[] itemArray               =   leftovers.toArray(new ItemStack[0]);
                    HashMap<Integer,ItemStack> bounce   =   inventory.addItem(itemArray);
                    leftovers   =   new ArrayList(bounce.values());
                    if(leftovers.isEmpty()){
                        if(RandomEncounters.getInstance().getLogLevel()>9){
                            RandomEncounters.getInstance().logMessage("All stacks fit into this vault");
                        }
                        break;
                    }
                }
                i++;
            }
        }
        if(!leftovers.isEmpty()){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage(placedEncounter.getName()+"(Vault) is full");
            }
            full = true;
        }else{
            full = false;
        }
        return leftovers;
    }
    
    public HashMap<Material,Integer> withdraw(HashMap<Material,Integer> resources){
        if(RandomEncounters.getInstance().getLogLevel()>5){
            RandomEncounters.getInstance().logMessage("widthdrawing "+resources.size()+" types of resources from "+placedEncounter.getName()+"(Vault)");
        }
        if(resources.isEmpty()){
            return resources;
        }
        HashMap<Material,Integer> leftover  =   resources;
        Iterator<Location> itr              =   locations.iterator();
        while(itr.hasNext()){
            Location location   =   itr.next();
            if(validInventory(location,itr)){
                Inventory inventory =   ((Chest) location.getBlock().getState()).getBlockInventory();
                leftover            =   convertStacks(inventory.removeItem(convertResources(leftover)));
            }
            if(leftover.isEmpty()){
                break;
            }
        }
        full = false;
        return leftover;
    }
    
    private boolean validInventory(Location location,Iterator<Location> itr){
        if(location==null || !(location.getBlock().getState() instanceof Chest)){
           if(RandomEncounters.getInstance().getLogLevel()>4){
                RandomEncounters.getInstance().logMessage("Vault detected missing inventory, removing it");
           }
           itr.remove(); 
           return false;
        }
        return true;
    }
    
    public HashMap<Material,Integer> contains(HashMap<Material,Integer> amounts){
        HashMap<Material,Integer> leftover    =   amounts;
        if(!amounts.isEmpty()){
            Iterator<Location> itr            =   locations.iterator();
            int i   =   1;
            while(itr.hasNext()){
                Location location =   itr.next();
                if(validInventory(location,itr)){
                   Inventory inventory      =   ((Chest) location.getBlock().getState()).getBlockInventory();
                   Iterator<Material> iitr  =   leftover.keySet().iterator();
                   while(iitr.hasNext()){
                       Material material        =   iitr.next();
                       Integer amount           =   leftover.get(material);
                       List<ItemStack> contents =   new ArrayList(((HashMap<Integer,ItemStack>) inventory.all(material)).values());
                       if(!contents.isEmpty()){
                           for(ItemStack item : contents){
                               amount   -=   item.getAmount();
                               if(amount<=0){
                                   iitr.remove();
                                   break;
                               }else{
                                   leftover.put(material, amount);
                               }
                           }
                       }
                   }
                   if(leftover.isEmpty()){
                       break;
                   }
                }
                i++;
            }
        }
        return leftover;
    }
    
    private ItemStack[] convertResources(HashMap<Material,Integer> resources){
        List<ItemStack> items   =   new ArrayList();
        for(Material material : resources.keySet()){
            if(material!=null){
                ItemStack item  =   new ItemStack(material,resources.get(material));
                items.add(item);
            }
        }
        
        return items.toArray(new ItemStack[0]);
    }
    
    private HashMap<Material,Integer> convertStacks(HashMap<Integer,ItemStack> items){
        HashMap<Material,Integer> resources =   new HashMap();
        for(Integer i : items.keySet()){
            ItemStack item  =   items.get(i);
            Integer amount  =   item.getAmount();
            if(resources.containsKey(item.getType())){
                amount  +=  resources.get(item.getType());
            }
            resources.put(item.getType(), amount);
        }
        return resources;
    }
}
