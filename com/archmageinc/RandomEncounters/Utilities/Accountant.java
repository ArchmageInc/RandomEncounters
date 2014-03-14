package com.archmageinc.RandomEncounters.Utilities;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.Encounters.Vault;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author ArchmageInc
 */
public class Accountant {
    private final PlacedEncounter owner;
    private final List<Vault> vaults;
    
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
        if(n<vaults.size()){
            vaults.get(n).deposit(this, resources, n);
            if(RandomEncounters.getInstance().getLogLevel()>10){
                vaults.get(n).logBalance();
            }
            return;
        }
        if(owner.getParent()!=null){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("Leftover deposit from "+owner.getName()+", sending to parent.");
            }
            owner.getParent().getAccountant().depositResources(resources); 
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("Leftover deposit from "+owner.getName()+", with no parent sending to child.");
            }
            for(UUID id : owner.getChildren()){
                PlacedEncounter child   =   PlacedEncounter.getInstance(id);
                if(child!=null){
                    child.getAccountant().depositResources(resources);
                    break;
                }
            }
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
        if(n<vaults.size()){
            vaults.get(n).withdraw(this, resources, n);
            if(RandomEncounters.getInstance().getLogLevel()>10){
                vaults.get(n).logBalance();
            }
            return;
        }
        if(owner.getParent()!=null){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("Leftover withdraw from "+owner.getName()+", taking from parent.");
            }
            owner.getParent().getAccountant().withdrawResources(resources); 
        }else{
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("Leftover withdraw from "+owner.getName()+", with no parent taking from child.");
            }
            for(UUID id : owner.getChildren()){
                PlacedEncounter child   =   PlacedEncounter.getInstance(id);
                if(child!=null){
                    child.getAccountant().withdrawResources(resources);
                    break;
                }
            }
        }
    }
    public boolean hasResources(HashMap<Material,Integer> resources){
        if(resources.isEmpty()){
            return true;
        }
        HashMap<Material,Integer> leftover  =   (HashMap<Material,Integer>) resources.clone();
        for(Vault vault : vaults){
            leftover    =   vault.contains(leftover);
            if(leftover.isEmpty()){
                break;
            }
        }
        if(RandomEncounters.getInstance().getLogLevel()>9){
            for(Material material : leftover.keySet()){
                RandomEncounters.getInstance().logMessage("        - "+leftover.get(material)+" more "+material.name()+" needed");
            }
        }
        return leftover.isEmpty();
    }
    public boolean hasVaultSpace(){
        for(Vault vault : vaults){
            if(!vault.getEncounter().equals(owner)){
                if(!vault.isFull()){
                    return true;
                }
            }
        }
        return false;
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
    
    public static HashMap<Material,Integer> complement(HashMap<Material,Integer> amount,HashMap<Material,Integer> ledger){
        HashMap<Material,Integer> remainder =   new HashMap();
        for(Material material : amount.keySet()){
            int v  =   ledger.containsKey(material) ? amount.get(material)-ledger.get(material) : amount.get(material);
            if(v>0){
                remainder.put(material, v);
            }
        }
        return remainder;
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
    
}
