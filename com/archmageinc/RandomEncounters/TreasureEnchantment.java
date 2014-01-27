package com.archmageinc.RandomEncounters;

import org.bukkit.enchantments.Enchantment;
import org.json.simple.JSONObject;

/**
 * Represents the enchantment configuration for a treasure item.
 * 
 * @author ArchmageInc
 */
public class TreasureEnchantment {
    
    /**
     * The enchantment for the item.
     */
    protected Enchantment enchantment;
    
    /**
     * The probability of having the enchantment.
     */
    protected Double probability;
    
    /**
     * The enchantment level.
     */
    protected Integer level;
    
    /**
     * Constructor for a TreasureEnchantment based on an enchantment and probability.
     * @param enchantment
     * @param probability 
     */
    public TreasureEnchantment(Enchantment enchantment,Double probability){
        this.enchantment    =   enchantment;
        this.probability     =   probability;
    }
    
    /**
     * Constructor for a TreasureEnchantment based on JSON Configuration
     * @param jsonConfiguration 
     */
    public TreasureEnchantment(JSONObject jsonConfiguration){
        try{
            enchantment =   Enchantment.getByName((String) jsonConfiguration.get("enchantment"));
            probability =   (Double) jsonConfiguration.get("probability");
            level       =   (Integer) jsonConfiguration.get("level");
            if(level==null){
                level   =   enchantment.getStartLevel();
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid TreasureEnchantment configuration: "+e.getMessage());
        }
    }
    
    /**
     * Get the enchantment or null
     * @return 
     */
    public Enchantment get(){
        if(Math.random()<probability){
           return enchantment; 
        }
        return null;
    }
    
    /**
     * Get the level of the enchantment.
     * @return 
     */
    public Integer getLevel(){
        return level;
    }
}
