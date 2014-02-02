package com.archmageinc.RandomEncounters.Treasures;

import com.archmageinc.RandomEncounters.RandomEncounters;
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
    private Enchantment enchantment;
    
    /**
     * The probability of having the enchantment.
     */
    private Double probability;
    
    /**
     * The enchantment level.
     */
    private Integer level;
    
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
            probability =   jsonConfiguration.get("probability")==null ? 0 : ((Number) jsonConfiguration.get("probability")).doubleValue();
            level       =   jsonConfiguration.get("level")==null ? 0 : ((Number) jsonConfiguration.get("level")).intValue();
            if(level==null){
                level   =   enchantment.getStartLevel();
            }
            if(enchantment==null){
                RandomEncounters.getInstance().logError("Invalid enchantment "+(String) jsonConfiguration.get("enchantment"));
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
