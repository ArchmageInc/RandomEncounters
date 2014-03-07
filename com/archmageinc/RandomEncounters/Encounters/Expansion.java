package com.archmageinc.RandomEncounters.Encounters;

import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Tasks.ChunkLocatorTask;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Represents a configured expansion for an encounter
 * 
 * @author ArchmageInc
 */
public class Expansion implements Cloneable,EncounterPlacer{
    
    /**
     * The encounter that may spawn for this expansion.
     */
    private Encounter encounter;
    
    /**
     * The unique name of the encounter for lazy loading.
     */
    private String encounterName;
    
    /**
     * The probability of expansion.
     */
    private Double probability;
    
    /**
     * The duration in minutes to check for expansion.
     */
    private Long duration;
    
    /**
     * The maximum number of times the encounter can be placed for expansion.
     */
    private Long max;
    
    /**
     * The maximum distance in chunks from the parent encounter this expansion can be placed.
     */
    private Long maxDistance;
    
    /**
     * The minimum distance in chunks from the parent encounter this expansion can be placed.
     */
    private Long minDistance;
    
    private Long pattern;
    
    /**
     * The last time this expansion was checked to expand.
     */
    private Calendar lastCheck                    =   (Calendar) Calendar.getInstance().clone();
    
    private HashMap<String,Long> proximities   =   new HashMap();
    
    /**
     * The encounter that will expand.
     */
    private PlacedEncounter expandingEncounter;
    
    /**
     * Constructor for an expansion based on JSON Configuration.
     * 
     * @param jsonConfiguration The configuration
     */
    public Expansion(JSONObject jsonConfiguration){
        try{
            encounter                     =   Encounter.getInstance((String) jsonConfiguration.get("encounter"));
            encounterName                 =   (String) jsonConfiguration.get("encounter");
            probability                   =   jsonConfiguration.get("probability")==null ? 0 : ((Number) jsonConfiguration.get("probability")).doubleValue();
            duration                      =   jsonConfiguration.get("duration")==null ? Long.MAX_VALUE : ((Number) jsonConfiguration.get("duration")).longValue();
            max                           =   jsonConfiguration.get("max")==null ? 0 : ((Number) jsonConfiguration.get("max")).longValue();
            maxDistance                   =   jsonConfiguration.get("maxDistance")==null ? 1 : ((Number) jsonConfiguration.get("maxDistance")).longValue();
            minDistance                   =   jsonConfiguration.get("minDistance")==null ? 1 : ((Number) jsonConfiguration.get("minDistance")).longValue();
            pattern                       =   jsonConfiguration.get("pattern")==null ? -1 : ((Number) jsonConfiguration.get("pattern")).longValue();
            JSONArray jsonProximities     =   (JSONArray) jsonConfiguration.get("proximities");
            if(jsonProximities!=null){
                for(int i=0;i<jsonProximities.size();i++){
                    JSONObject jsonProx     =   (JSONObject) jsonProximities.get(i);
                    String proxName         =   (String) jsonProx.get("encounter");
                    Long proxMinDistance    =   jsonProx.get("minDistance")==null ? 1 : ((Number) jsonProx.get("minDistance")).longValue();
                    proximities.put(proxName, proxMinDistance);
                }
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid expansion configuration: "+e.getMessage());
        }
    }
    
    /**
     * Checks the expansion for placement. 
     * If successful, the encounter will be placed in the world.
     */
    public void checkExpansion(){
        updateLastCheck();
        Double random   =   Math.random();
        if(RandomEncounters.getInstance().getLogLevel()>6){
            RandomEncounters.getInstance().logMessage("    * Checking expansion for "+expandingEncounter.getEncounter().getName()+" -> "+getEncounter().getName()+" : ("+random+","+probability+") ");
        }
        if(random<probability){
            Set<PlacedEncounter> validExpansions    =   new HashSet();
            for(UUID expansionUUID : expandingEncounter.getPlacedExpansions()){
                PlacedEncounter placedExpansion =   PlacedEncounter.getInstance(expansionUUID);
                if(placedExpansion!=null){
                    if(placedExpansion.getEncounter().equals(getEncounter())){
                        validExpansions.add(placedExpansion);
                    }
                }
            }
            if(RandomEncounters.getInstance().getLogLevel()>6){
                RandomEncounters.getInstance().logMessage("      # Expansion probability hit for encounter "+expandingEncounter.getEncounter().getName()+" -> "+getEncounter().getName()+". There are "+validExpansions.size()+" existing expansions, "+max+" are allowed.");
            }
            if(validExpansions.size()<max){
               (new ChunkLocatorTask(this,expandingEncounter.getLocation().getChunk(),maxDistance.intValue(),minDistance.intValue())).runTaskTimer(RandomEncounters.getInstance(), 1, 1);               
            }
        }
    }
    
    /**
     * This clones the expansion configuration for individual PlacedEncounters.
     * Each PlacedEncounter stores their own Expansions because of specifics
     * @param expandingEncounter
     * @return Returns a copy of the Expansion
     * @throws CloneNotSupportedException 
     */
     public Expansion clone(PlacedEncounter expandingEncounter) throws CloneNotSupportedException{
       Expansion newExpansion   =   (Expansion) super.clone();
       newExpansion.setExpandingEncounter(expandingEncounter);
       return newExpansion;
    }
    
    /**
     * Get the encounter of the expansion.
     * If the encounter has not been previously retrieved, it will attempt to do so.
     * This allows for circular referencing expansions. (i.e. an encounter can spawn itself as an expansion)
     * @return 
     */
    @Override
    public Encounter getEncounter(){
        if(encounter==null){
            encounter   =   Encounter.getInstance(encounterName);
        }
        if(encounter==null){
            RandomEncounters.getInstance().logError("Unable to locate encounter "+encounterName+" for expansion!!");
        }
        return encounter;
    }
    
    /**
     * Get the wait time in minutes for this expansion.
     * 
     * @return 
     * @see Expansion#duration
     */
    public Long getDuration(){
        return duration;
    }
    
    /**
     * Get the last time this expansion was checked
     * 
     * @return 
     * @see Expansion#lastCheck
     */
    public Calendar getLastCheck(){
        return lastCheck;
    }
    
    public void setExpandingEncounter(PlacedEncounter expandingEncounter){
        this.expandingEncounter     =   expandingEncounter;
    }
    
    /**
     * Update the last time this expansion was checked.
     * 
     * @see Expansion#lastCheck
     */
    public void updateLastCheck(){
        lastCheck   =   (Calendar) Calendar.getInstance().clone();
    }
    
    @Override
    public Map<String,Long> getProximities(){
        return proximities;
    }
    
    @Override
    public long getPattern(){
        return pattern;
    }
    
    @Override
    public double getInitialAngle(){
        if(expandingEncounter.getParent()!=null){
            //(n-1) Tangent
            double adjust   =   expandingEncounter.getPlacedExpansions(encounter).size()*(Math.PI/8)+(Math.PI/2);
           return Math.atan2(expandingEncounter.getLocation().getChunk().getX()-expandingEncounter.getRoot().getLocation().getChunk().getX(),expandingEncounter.getLocation().getChunk().getZ()-expandingEncounter.getRoot().getLocation().getChunk().getZ())-adjust;
        }else{
            return Math.random()*(Math.PI*2);
            /*
            Set<PlacedEncounter> children   =   expandingEncounter.getPlacedExpansions(encounter);
            if(children.isEmpty()){
                return Math.random()*(Math.PI*2);
            }
            if(children.size()==1){
                PlacedEncounter child   =   children.iterator().next();
                return Math.atan2(expandingEncounter.getLocation().getChunk().getX()-child.getLocation().getChunk().getX(),expandingEncounter.getLocation().getChunk().getZ()-child.getLocation().getChunk().getZ())-(Math.PI/2);  
            }
            double distance     =   0;
            Location midpoint   =   null;
            for(PlacedEncounter child1 : children){
                for(PlacedEncounter child2 : children){
                    if(child1.getLocation().distance(child2.getLocation()) > distance){
                        distance    =   child1.getLocation().distance(child2.getLocation());
                        midpoint    =   child1.getLocation().toVector().getMidpoint(child2.getLocation().toVector()).toLocation(child1.getLocation().getWorld());
                    }
                }
            }
            if(midpoint==null){
                RandomEncounters.getInstance().logWarning("An expansion's attempt to retrieve a seed's initial angle for "+encounter.getName()+" resulted in no midpoint between "+children.size()+" children");
                return Math.random()*(Math.PI*2);
            }
            return Math.atan2(expandingEncounter.getLocation().getChunk().getX()-midpoint.getChunk().getX(),expandingEncounter.getLocation().getChunk().getZ()-midpoint.getChunk().getZ())-(Math.PI/2);  
            */
        }
    }

    @Override
    public void addPlacedEncounter(PlacedEncounter newEncounter) {
        if(newEncounter!=null){
            expandingEncounter.addExpansion(newEncounter);
            RandomEncounters.getInstance().addPlacedEncounter(newEncounter);
        }
    }
}
