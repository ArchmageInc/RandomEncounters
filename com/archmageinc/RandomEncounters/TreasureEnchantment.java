package com.archmageinc.RandomEncounters;

import org.bukkit.enchantments.Enchantment;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class TreasureEnchantment {
    protected Enchantment enchantment;
    protected Double probability;
    protected Integer level;
    
    public TreasureEnchantment(Enchantment enchantment,Double probability){
        this.enchantment    =   enchantment;
        this.probability     =   probability;
    }
    
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
    
    public Enchantment get(){
        if(Math.random()<probability){
           return enchantment; 
        }
        return null;
    }
    
    public Integer getLevel(){
        return level;
    }
}
