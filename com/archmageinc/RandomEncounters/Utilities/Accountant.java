package com.archmageinc.RandomEncounters.Utilities;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.Encounters.Vault;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author ArchmageInc
 */
public class Accountant {
    private final PlacedEncounter owner;
    private final List<Vault> vaults;
    private final HashMap<Material,Integer> ledger  =   new HashMap();
    
    public Accountant(PlacedEncounter placedEncounter,List<Vault> vaults){
        owner       =   placedEncounter;
        this.vaults =   vaults;
    }
    
    public void depositResources(List<ItemStack> items){
        depositResources(items,0);
    }
    public void depositResources(HashMap<Material,Integer> resources){
        depositResources(resources,0);
    }
    public void depositResources(List<ItemStack> items, int n){
        depositResources(convert(items),n);
    }
    public void depositResources(HashMap<Material,Integer> resources,int n){
        if(resources.isEmpty()){
            return;
        }
        if(RandomEncounters.getInstance().getLogLevel()>10){
            logRequest("DEPOSIT",resources);
        }
        if(n<vaults.size()){
            vaults.get(n).deposit(this, resources, n);
            if(RandomEncounters.getInstance().getLogLevel()>11){
                vaults.get(n).logBalance();
            }
            return;
        }
        if(owner.getParent()!=null){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("Leftover deposit from "+owner.getName()+", sending to parent.");
            }
            owner.getParent().getAccountant().depositResources(resources);
            return;
        }
        
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("Leftover deposit from "+owner.getName()+", no space, no parent, it's lost.");
        }
        
    }
    
    public void withdrawResources(List<ItemStack> items){
        withdrawResources(items,0);
    }
    public void withdrawResources(HashMap<Material,Integer> resources){
        withdrawResources(resources,0);
    }
    public void withdrawResources(List<ItemStack> items,int n){
        withdrawResources(Accountant.convert(items),n);
    }
    public void withdrawResources(HashMap<Material,Integer> resources, int n){
        if(resources.isEmpty()){
            return;
        }
        if(RandomEncounters.getInstance().getLogLevel()>10){
            logRequest("WITHDRAW",resources);
        }
        if(n<vaults.size()){
            vaults.get(n).withdraw(this, resources, n);
            if(RandomEncounters.getInstance().getLogLevel()>11){
                vaults.get(n).logBalance();
            }
            return;
        }
        if(owner.getParent()!=null){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("Leftover withdraw from "+owner.getName()+", taking from parent.");
            }
            owner.getParent().getAccountant().withdrawResources(resources);
            return;
        }
        
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("Leftover withdraw from "+owner.getName()+", no space, no parent, it's free.");
        }
        
    }
    
    private void inventory(){
        ledger.clear();
        for(Vault vault : vaults){
            ledger.putAll(add(ledger,vault.inventory()));
        }
        if(RandomEncounters.getInstance().getLogLevel()>10){
            logBalance();
        }
    }
    public boolean hasResources(HashMap<Material,Integer> resources){
        if(resources.isEmpty()){
            return true;
        }
        if(RandomEncounters.getInstance().getLogLevel()>10){
            logRequest("CHECK",resources);
        }
        inventory();
        
        return contains(ledger,resources);
    }
    public boolean hasVaultSpace(){
        for(Vault vault : vaults){
            if(!vault.getEncounter().equals(owner) && !vault.isFull()){
                return true;
            }
        }
        return false;
    }
    
    public void logRequest(String type,HashMap<Material,Integer> resources){
        RandomEncounters.getInstance().logMessage("*********"+type+"*********");
        RandomEncounters.getInstance().logMessage("  **"+getName()+"**");
        for(Material m : resources.keySet()){
            RandomEncounters.getInstance().logMessage("  "+m.name()+": "+resources.get(m));
        }
        RandomEncounters.getInstance().logMessage("*******END "+type+"*******");
    }
    
    public void logBalance(){
        RandomEncounters.getInstance().logMessage("======LEDGER BALANCE=======");
        RandomEncounters.getInstance().logMessage("======"+getName()+"=======");
        for(Material material : ledger.keySet()){
            RandomEncounters.getInstance().logMessage("  "+material.name()+": "+ledger.get(material));
        }
        RandomEncounters.getInstance().logMessage("======END BALANCE=======");
    }
    
    private String getName(){
        return owner.getName()+"(ACCOUNTANT)";
    }
    
    public static List<ItemStack> convert(HashMap<Material,Integer> resources){
        List<ItemStack> items   =   new ArrayList();
        for(Material material : resources.keySet()){
            if(material!=null && resources.get(material)>0){
                ItemStack item  =   new ItemStack(material,resources.get(material));
                items.add(item);
            }
        }
        return items;
    }
    
    public static HashMap<Material,Integer> convert(ItemStack[] items){
        return convert(Arrays.asList(items));
    }
    
    public static HashMap<Material,Integer> convert(List<ItemStack> items){
        HashMap<Material,Integer> resources =   new HashMap();
        for(ItemStack item : items){
            int amount  =   resources.containsKey(item.getType()) ? resources.get(item.getType())+item.getAmount() : item.getAmount();
            if(amount>0){
                resources.put(item.getType(), amount);
            }
        }
        return resources;
    }
    
    public static HashMap<Material,Integer> subtract(HashMap<Material,Integer> input, HashMap<Material,Integer> output){
        HashMap<Material,Integer> result    =   new HashMap();
        for(Material material : input.keySet()){
            int amount  =   output.containsKey(material) ? input.get(material)-output.get(material) : input.get(material);
            if(amount!=0){
                result.put(material,amount);
            }
        }
        for(Material material : output.keySet()){
            if(!input.containsKey(material)){
                result.put(material, -output.get(material));
            }
        }
        return result;
    }
    
    public static boolean contains(HashMap<Material,Integer> source,HashMap<Material,Integer> check){
        for(Material m : check.keySet()){
            if(!source.containsKey(m) || source.get(m)<check.get(m)){
                return false;
            }
        }
        return true;
    }
    
    public static HashMap<Material,Integer> add(HashMap<Material,Integer> l1,HashMap<Material,Integer> l2){
        HashMap<Material,Integer> combined  =   (HashMap<Material,Integer>) l1.clone();
        for(Material m : l2.keySet()){
            int v   =   combined.containsKey(m) ? combined.get(m)+l2.get(m) : l2.get(m);
            combined.put(m, v);
        }
        return combined;
    }
    
}
