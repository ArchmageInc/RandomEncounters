package com.archmageinc.RandomEncounters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONObject;



/**
 *
 * @author ArchmageInc
 */
public class MobGroup {
    private String mobName;
    private Integer min;
    private Integer max;
    private Double probability;
    private Mob mob;
    
    public MobGroup(JSONObject jsonConfiguration){
        try{
            mobName                     =   (String) jsonConfiguration.get("name");
            min                         =   ((Number) jsonConfiguration.get("min")).intValue();
            max                         =   ((Number) jsonConfiguration.get("max")).intValue();
            probability                 =   ((Number) jsonConfiguration.get("probability")).doubleValue();
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Mob Group Configuration for "+mobName+": "+e.getMessage());
        }
    }
    
    public Set<PlacedMob> placeGroup(PlacedEncounter encounter){
        Set<PlacedMob> placements   =   new HashSet();
        Integer count               =   getCount();
        for(int i=0;i<count;i++){
            placements.addAll(getMob().placeMob(encounter));
        }
        return placements;
    }
    
    public List<Mob> getPlacements(){
        List<Mob> toPlace    =   new ArrayList();
        Integer count        =   getCount();
        for(int i=0;i<count;i++){
            toPlace.addAll(getMob().getPlacements());
        }
        return toPlace;
    }
    
    private Integer getCount(){
        Integer count   =   min;
        for(int i=min.intValue();i<max;i++){
            if(Math.random()<probability)
                count++;
        }
        return count;
    }
    
    public Mob getMob(){
        if(mob==null){
            mob =   Mob.getInstance(mobName);
            if(mob==null){
                RandomEncounters.getInstance().logError("Invalid mob group configuration: "+mobName+" not found!");
            }
        }
        return mob;
    }
}
