package com.archmageinc.RandomEncounters;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.json.simple.JSONObject;



/**
 *
 * @author ArchmageInc
 */
public class MobGroup {
    protected String mobName;
    protected Integer min;
    protected Integer max;
    protected Double probability;
    protected Mob mob;
    
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
    
    public Set<PlacedMob> placeGroup(PlacedEncounter encounter,Location location){
        Set<PlacedMob> placements   =   new HashSet();
        Integer count               =   getCount();
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("   = Prepairing to place "+count+" sets of "+getMob().getName());
        }
        for(int i=0;i<count;i++){
            placements.addAll(getMob().placeMob(encounter, location));
        }
        return placements;
    }
    
    protected Integer getCount(){
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
