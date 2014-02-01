package com.archmageinc.RandomEncounters.Treasures;

import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class TreasureGroup {
    private String treasureName;
    private int min;
    private int max;
    private Double probability;
    private Treasure treasure;
    
    public TreasureGroup(JSONObject jsonConfiguration){
        try{
            treasureName                =   (String) jsonConfiguration.get("name");
            min                         =   ((Number) jsonConfiguration.get("min")).intValue();
            max                         =   ((Number) jsonConfiguration.get("max")).intValue();
            probability                 =   ((Number) jsonConfiguration.get("probability")).doubleValue();
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Treasure Group Configuration for "+treasureName+": "+e.getMessage());
        }
    }
    
    public List<ItemStack> get(){
        List<ItemStack> treasures   =   new ArrayList();
        Integer count               =   getCount();
        for(int i=0;i<count;i++){
            if(getTreasure()!=null){
                treasures.addAll(getTreasure().get());
            }
        }
        
        return treasures;
    }
    
    private Integer getCount(){
        Integer count   =   min;
        for(int i=min;i<max;i++){
            if(Math.random()<probability)
                count++;
        }
        return count;
    }
    
    private Treasure getTreasure(){
        if(treasure==null){
            treasure    =   Treasure.getInstance(treasureName);
            if(treasure==null){
                RandomEncounters.getInstance().logError("Invalid treasure group configuration: "+treasureName+" not found!");
            }
        }
        return treasure;
    }
}
