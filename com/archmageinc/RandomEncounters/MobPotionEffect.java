package com.archmageinc.RandomEncounters;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class MobPotionEffect {
    protected PotionEffect effect;
    protected Long probability;
    protected PotionEffectType type;
    protected Long duration;
    protected Long amplifier;
    
    public MobPotionEffect(JSONObject jsonConfiguration){
        try{
            probability =   (Long) jsonConfiguration.get("probability");
            type        =   PotionEffectType.getByName((String) jsonConfiguration.get("type"));
            amplifier   =   (Long) jsonConfiguration.get("amplifier");
            duration    =   (Long) jsonConfiguration.get("duration");
            if(type!=null){
                effect  =   new PotionEffect(type,duration.intValue()*50,amplifier.intValue());
            }else{
                RandomEncounters.getInstance().logWarning("Invalid potion effect configuration: Unknown potion effect "+(String) jsonConfiguration.get("type"));
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid potion effect configuration: "+e.getMessage());
        }
    }
    
    public void checkApply(LivingEntity entity){
        if(effect!=null && Math.random()<probability){
            entity.addPotionEffect(effect);
        }
    }
}
