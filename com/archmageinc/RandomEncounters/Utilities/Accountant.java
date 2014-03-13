package com.archmageinc.RandomEncounters.Utilities;

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
