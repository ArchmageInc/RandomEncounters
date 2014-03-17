package com.archmageinc.RandomEncounters.Encounters;

import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Tasks.LocationLoadingTask;
import com.archmageinc.RandomEncounters.Utilities.Accountant;
import com.archmageinc.RandomEncounters.Utilities.LoadListener;
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
public class Vault implements LoadListener {
    
    private final PlacedEncounter placedEncounter;
    private final Set<Location> locations                       =   new HashSet();
    private boolean full                                        =   false;
    private final HashMap<Material,Integer> ledger              =   new HashMap();
    private static final HashMap<Location,Vault> owners         =   new HashMap();
    private final HashMap<ItemStack[],Accountant> depositQueue  =   new HashMap();
    private final HashMap<ItemStack[],Accountant> withdrawQueue =   new HashMap();
    
    public Vault(PlacedEncounter placedEncounter){
        this.placedEncounter =   placedEncounter;
        if(placedEncounter.isLoaded()){
            findChests();
        }else{
            placedEncounter.getLoadingTask().addListener(this);
        }
    }
    
    @Override
    public void processLoad(LocationLoadingTask loadTask){
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("  "+getVaultName()+" has been notified of loading completion.");
        }
        findChests();
        processQueues();
    }
    
    private void findChests(){
        int[] blockLocations    =   placedEncounter.getBlockLocations();
        if(blockLocations==null){
            return;
        }
        if(RandomEncounters.getInstance().getLogLevel()>8){
            RandomEncounters.getInstance().logMessage(getVaultName()+": locating chests from "+(blockLocations.length/3)+" locations");
        }
        for(int i=0;i<blockLocations.length;i+=3){
            int x               =   blockLocations[i];
            int y               =   blockLocations[i+1];
            int z               =   blockLocations[i+2];
            
            if(x+y+z!=0){
                Block block         =   placedEncounter.getLocation().getWorld().getBlockAt(x, y, z);
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
            }
        }
        if(locations.isEmpty()){
            full    =   true;
        }
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage(getVaultName()+": located "+locations.size()+" storage inventories");
        }
        inventory();
    }
    
    private void processQueues(){
        if(RandomEncounters.getInstance().getLogLevel()>8){
            RandomEncounters.getInstance().logMessage("  "+getVaultName()+": Processing queues [D:"+depositQueue.size()+" W:"+withdrawQueue.size()+"]");
        }
        for(ItemStack[] items : depositQueue.keySet()){
            deposit(depositQueue.get(items),items,0);
        }
        for(ItemStack[] items : withdrawQueue.keySet()){
            withdraw(withdrawQueue.get(items),items,0);
        }
    }
    
    public PlacedEncounter getEncounter(){
        return placedEncounter;
    }
    
    public boolean isFull(){
        return full;
    }
    
    public void deposit(Accountant accountant,HashMap<Material,Integer> resources,int n){
        deposit(accountant,Accountant.convert(resources),n);
    }
    public void deposit(Accountant accountant,List<ItemStack> items,int n){
        deposit(accountant,items.toArray(new ItemStack[0]),n);
    }
    public void deposit(Accountant accountant,ItemStack[] items,int n){
        if(!placedEncounter.isLoaded()){
            depositQueue.put(items,accountant);
            if(RandomEncounters.getInstance().getLogLevel()>3){
                RandomEncounters.getInstance().logMessage(getVaultName()+ ": Not yet loaded adding "+items.length+" item stacks to the deposit queue");
            }
            return;
        }
        if(RandomEncounters.getInstance().getLogLevel()>5){
            RandomEncounters.getInstance().logMessage(getVaultName()+ ": Attempting to deposit "+items.length+" item stacks");
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
                            RandomEncounters.getInstance().logMessage(getVaultName()+": Successfully deposited all items");
                        }
                        break;
                    }
                }
            }
        }
        ledgerTransaction(Accountant.subtract(Accountant.convert(items), Accountant.convert(leftovers)),1);
        full    =   leftovers.isEmpty();
        accountant.depositResources(leftovers,n+1);
    }
    
    public void withdraw(Accountant accountant,HashMap<Material,Integer> resources,int n){
        withdraw(accountant,Accountant.convert(resources),n);
    }
    public void withdraw(Accountant accountant,List<ItemStack> items,int n){
        withdraw(accountant,items.toArray(new ItemStack[0]),n);
    }
    public void withdraw(Accountant accountant,ItemStack[] items, int n){
        if(!placedEncounter.isLoaded()){
            withdrawQueue.put(items,accountant);
            if(RandomEncounters.getInstance().getLogLevel()>3){
                RandomEncounters.getInstance().logMessage(getVaultName()+ ": Not yet loaded adding "+items.length+" item stacks to the deposit queue");
            }
            return;
        }
        if(RandomEncounters.getInstance().getLogLevel()>5){
            RandomEncounters.getInstance().logMessage(getVaultName()+ ": Attempting to withdraw "+items.length+" item stacks");
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
                            RandomEncounters.getInstance().logMessage(getVaultName()+": Successfully withdrew all items");
                        }
                        break;
                    }
                }
            }
        }
        ledgerTransaction(Accountant.subtract(Accountant.convert(items),Accountant.convert(leftovers)),-1);
        full    =   false;
        accountant.withdrawResources(leftovers,n+1);
    }
    
    private boolean validInventory(Location location,Iterator<Location> itr){
        if(location==null || !(location.getBlock().getState() instanceof Chest)){
           if(RandomEncounters.getInstance().getLogLevel()>4){
                RandomEncounters.getInstance().logMessage(getVaultName()+": detected missing inventory, removing it");
           }
           owners.remove(location);
           itr.remove(); 
           return false;
        }
        return true;
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
    
    public HashMap<Material,Integer> inventory(){
        ledger.clear();
        if(!locations.isEmpty()){
            Iterator<Location> itr  =   locations.iterator();
            while(itr.hasNext()){
                Location location   =   itr.next();
                if(validInventory(location,itr)){
                    Inventory i                  =   ((Chest) location.getBlock().getState()).getBlockInventory();
                    for(ItemStack item : i.getContents()){
                       if(item!=null){
                           int v    =   ledger.containsKey(item.getType()) ? ledger.get(item.getType())+item.getAmount() : item.getAmount();
                           ledger.put(item.getType(), v);
                       }
                   }
                }
            }
        }
        if(RandomEncounters.getInstance().getLogLevel()>11){
            logBalance();
        }
        return (HashMap<Material,Integer>) ledger.clone();
    }
    
    public void logBalance(){
        RandomEncounters.getInstance().logMessage("======LEDGER STATEMENT=======");
        RandomEncounters.getInstance().logMessage("======"+getVaultName()+"=======");
        for(Material material : ledger.keySet()){
            RandomEncounters.getInstance().logMessage("  "+material.name()+": "+ledger.get(material));
        }
        RandomEncounters.getInstance().logMessage("======END STATEMENT=======");
    }
    private String getVaultName(){
        return placedEncounter.getName()+"(Vault)";
    }
}
