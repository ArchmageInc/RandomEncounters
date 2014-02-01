package com.archmageinc.RandomEncounters.Treasures;

import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Represents a Treasure configuration, an Item to be generated.
 * 
 * @author ArchmageInc
 */
public class Treasure {
    
    private static final HashSet<Treasure> instances    =   new HashSet();
    
    private String name;
    
    private final Set<TreasureGroup> treasureGroups     =   new HashSet();
    
    /**
     * The material of the item.
     */
    private Material material     =   Material.AIR;
    
    /**
     * The minimum number of items to generate.
     */
    private Long min;
    
    /**
     * The maximum number of items to generate.
     */
    private Long max;
    
    /**
     * The probability of additional items.
     */
    private Double probability    =   0.0;
    
    /**
     * The set of TreasureEnchantments to place on the item.
     */
    private final Set<TreasureEnchantment> enchantments =   new HashSet();
    
    private String tagName;
    
    
    public static Treasure getInstance(String name){
        for(Treasure instance : instances){
            if(instance.getName().equalsIgnoreCase(name)){
                return instance;
            }
        }
        return null;
    }
    
    public static Treasure getInstance(JSONObject jsonConfiguration){
        return getInstance(jsonConfiguration,false);
    }
    
    public static Treasure getInstance(JSONObject jsonConfiguration,Boolean force){
        String treasureName =   (String) jsonConfiguration.get("name");
        Treasure treasure   =   getInstance(treasureName);
        if(treasure==null){
            return new Treasure(jsonConfiguration);
        }
        if(force){
            treasure.reConfigure(jsonConfiguration);
        }
        return treasure;
    }
    
    private void reConfigure(JSONObject jsonConfiguration){
        try{
            instances.remove(this);
            treasureGroups.clear();
            enchantments.clear();
            name                            =   (String) jsonConfiguration.get("name");
            material                        =   Material.getMaterial((String) jsonConfiguration.get("material"));
            min                             =   ((Number) jsonConfiguration.get("min")).longValue();
            max                             =   ((Number) jsonConfiguration.get("max")).longValue();
            probability                     =   ((Number) jsonConfiguration.get("probability")).doubleValue();
            tagName                         =   (String) jsonConfiguration.get("tagName");
            JSONArray jsonEnchantments      =   (JSONArray) jsonConfiguration.get("enchantments");
            JSONArray jsonTreasures         =   (JSONArray) jsonConfiguration.get("treasureGroups");
            if(jsonEnchantments!=null){
                for(int i=0;i<jsonEnchantments.size();i++){
                    enchantments.add(new TreasureEnchantment((JSONObject) jsonEnchantments.get(i)));
                }
            }
            if(jsonTreasures!=null){
                for(int i=0;i<jsonTreasures.size();i++){
                    JSONObject treasureGroup =   (JSONObject) jsonTreasures.get(i);
                    if(name.equals((String) treasureGroup.get("name"))){
                        RandomEncounters.getInstance().logWarning("Ignoring recursive treasure group configuration for "+name);
                    }else{
                        treasureGroups.add(new TreasureGroup(treasureGroup));
                    }
                }
            }
            if(jsonTreasures==null && material==null){
                RandomEncounters.getInstance().logError("Invalid material type: "+(String) jsonConfiguration.get("material")+" for "+name);
            }
            instances.add(this);
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Treasure configuration: "+e.getMessage());
        }
    }
    
    /**
     * Constructor based on the JSON configuration.
     * 
     * @param jsonConfiguration 
     */
    private Treasure(JSONObject jsonConfiguration){
        reConfigure(jsonConfiguration);
    }
    
    /**
     * Set the Enchantments on the given item.
     * 
     * @param item The item to receive the enchantments
     */
    private void setEnchantments(ItemStack item){
        if(enchantments.size()>0){
            for(TreasureEnchantment tEnchantment : enchantments){
                Enchantment enchantment =   tEnchantment.get();
                if(enchantment!=null){
                    try{
                        item.addUnsafeEnchantment(enchantment, tEnchantment.getLevel());
                    }catch(IllegalArgumentException e){
                        RandomEncounters.getInstance().logWarning("Invalid enchantment for Treasure item: "+e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Get the list of items based on this configuration.
     * 
     * @return 
     */
    public List<ItemStack> get(){
        List<ItemStack> list    =   new ArrayList();
        if(!treasureGroups.isEmpty()){
            for(TreasureGroup treasureGroup : treasureGroups){
                list.addAll(treasureGroup.get());
            }
            return list;
        }
        if(material==null){
            return list;
        }
        ItemStack stack         =   new ItemStack(material,0);
        if(tagName!=null){
            ItemMeta meta   =   stack.getItemMeta();
            meta.setDisplayName(tagName);
            stack.setItemMeta(meta);
        }
        for(int i=0;i<max;i++){
            if(i<min || Math.random()<probability){
                if(stack.getAmount()<stack.getMaxStackSize()){
                    stack.setAmount(stack.getAmount()+1);
                }else{
                    list.add(stack.clone());
                    stack   =   new ItemStack(material,1);
                    if(tagName!=null){
                        ItemMeta meta   =   stack.getItemMeta();
                        meta.setDisplayName(tagName);
                        stack.setItemMeta(meta);
                    }
                }
                setEnchantments(stack);
            }
        }
        if(stack.getAmount()>0){
            list.add(stack);
        }
        return list;
    }
    
    public String getName(){
        return name;
    }
    
    /**
     * Get one item from the treasure.
     * 
     * @return Returns the item based on probability or null
     */
    public ItemStack getOne(){
        ItemStack item  =   null;
        if(material!=null){
            if(Math.random()<probability){
                item    =   new ItemStack(material,1);
                setEnchantments(item);
            }
        }
        return item;
    }
}
