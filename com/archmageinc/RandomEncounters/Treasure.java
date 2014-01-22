package com.archmageinc.RandomEncounters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class Treasure {
    protected Material material     =   Material.AIR;
    protected Long min;
    protected Long max;
    protected Double probability    =   0.0;
    protected Set<TreasureEnchantment> enchantments =   new HashSet();
    
    public Treasure(JSONObject jsonConfiguration){
        try{
            material    =   Material.getMaterial((String) jsonConfiguration.get("material"));
            min         =   (Long) jsonConfiguration.get("min");
            max         =   (Long) jsonConfiguration.get("max");
            probability =   (Double) jsonConfiguration.get("probability");
            JSONArray jsonEnchantments  =   (JSONArray) jsonConfiguration.get("enchantments");
            if(jsonEnchantments!=null){
                for(int i=0;i<jsonEnchantments.size();i++){
                    enchantments.add(new TreasureEnchantment((JSONObject) jsonEnchantments.get(i)));
                }
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Treasure configuration: "+e.getMessage());
        }
        
    }
    
    public List<ItemStack> get(){
        List<ItemStack> list    =   new ArrayList();
        ItemStack stack         =   new ItemStack(material,0);
        for(int i=min.intValue();i<max;i++){
            if(Math.random()<probability){
                if(stack.getAmount()<stack.getMaxStackSize()){
                    stack.setAmount(stack.getAmount()+1);
                }else{
                    list.add(stack.clone());
                    stack   =   new ItemStack(material,1);
                }
                if(enchantments.size()>0){
                    for(TreasureEnchantment tEnchantment : enchantments){
                        Enchantment enchantment =   tEnchantment.get();
                        if(enchantment!=null){
                            try{
                                stack.addEnchantment(enchantment, tEnchantment.getLevel());
                            }catch(IllegalArgumentException e){
                                RandomEncounters.getInstance().logWarning("Invalid enchantment for Treasure item: "+e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        if(stack.getAmount()>0){
            list.add(stack);
        }
        return list;
    }
}
