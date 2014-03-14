package com.archmageinc.RandomEncounters.Encounters;

import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Tasks.ChunkLocatorTask;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
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
    
    private boolean checking                                    =   false;
    
    private boolean vault                                       =   false;
    
    /**
     * The last time this expansion was checked to expand.
     */
    private Calendar lastCheck                                  =   (Calendar) Calendar.getInstance().clone();
    
    private final HashMap<String,Long> proximities              =   new HashMap();
    
    private boolean canExpand                                   =   true;
    
    private HashMap<Material,Integer> rootResources             =   new HashMap();
    
    private HashMap<Material,Integer> parentResources           =   new HashMap();
    
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
            vault                         =   jsonConfiguration.get("vault")==null ? false : (boolean) jsonConfiguration.get("vault");
            JSONObject jsonRequirements   =   (JSONObject) jsonConfiguration.get("requirements");
            JSONArray jsonProximities     =   (JSONArray) jsonConfiguration.get("proximities");
            if(jsonProximities!=null){
                for(int i=0;i<jsonProximities.size();i++){
                    JSONObject jsonProx     =   (JSONObject) jsonProximities.get(i);
                    String proxName         =   (String) jsonProx.get("encounter");
                    Long proxMinDistance    =   jsonProx.get("minDistance")==null ? 1 : ((Number) jsonProx.get("minDistance")).longValue();
                    proximities.put(proxName, proxMinDistance);
                }
            }
            if(jsonRequirements!=null){
                rootResources               =   getResourceAmounts((JSONObject) jsonRequirements.get("root"));
                parentResources             =   getResourceAmounts((JSONObject) jsonRequirements.get("parent"));
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid expansion configuration: "+e.getMessage());
        }
    }
    
    private HashMap<Material,Integer> getResourceAmounts(JSONObject requirements){
        HashMap<Material,Integer> resourceRequirements  =   new HashMap();
        if(requirements!=null){
            JSONObject jsonResources   =   (JSONObject) requirements.get("resources");
            if(jsonResources!=null){
                for(String materialName : (Set<String>) jsonResources.keySet()){
                    Material material   =   Material.getMaterial(materialName);
                    if(material!=null){
                        Integer amount  =   ((Number) jsonResources.get(materialName)).intValue();
                        resourceRequirements.put(material, amount);
                    }else{
                        RandomEncounters.getInstance().logWarning("Invalid material in resource requirements: "+materialName);
                    }
                }
            }
        }
        return resourceRequirements;
    }
    
    /**
     * Checks the expansion for placement. 
     * If successful, the encounter will be placed in the world.
     */
    public void checkExpansion(){
        if(checking){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("    * Expansion for "+expandingEncounter.getName()+"->"+getEncounter().getName()+" is still running checks");
            }
            return;
        }
        Calendar nextRun    =   (Calendar) getLastCheck().clone();
        nextRun.add(Calendar.MINUTE, getDuration().intValue());
        if(nextRun.after(Calendar.getInstance())){
            if(RandomEncounters.getInstance().getLogLevel()>11){
                int difference      =   Math.round(nextRun.compareTo(Calendar.getInstance())/(1000*60));
                RandomEncounters.getInstance().logMessage("    * Expansion for "+expandingEncounter.getName()+"->"+getEncounter().getName()+" waiting "+difference+" minutes");
            }
            return;
        }
        updateLastCheck();
        if(!canExpand){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("    * Expansion for "+expandingEncounter.getName()+"->"+getEncounter().getName()+" reportedly has nowhere to expand");
            }
            return;
        }
        if(expandingEncounter.isSacked()){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("    * "+expandingEncounter.getName()+" is sacked and cannot expand");
            }
            return;
        }
        Double random   =   Math.random();
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("    * Checking expansion for "+expandingEncounter.getName()+"->"+getEncounter().getName()+" : ("+random+","+probability+") ");
        }
        if(random<probability){
            if(expandingEncounter.getChildren(encounter).size()>=max){
                if(RandomEncounters.getInstance().getLogLevel()>7){
                    RandomEncounters.getInstance().logMessage("      # Expansion "+expandingEncounter.getName()+"->"+getEncounter().getName()+" has reached the maximum number of expansions");
                }
                return;
            }
            if(isVault() && expandingEncounter.getAccountant().hasVaultSpace()){
                if(RandomEncounters.getInstance().getLogLevel()>7){
                    RandomEncounters.getInstance().logMessage("      # Expansion "+expandingEncounter.getName()+"->"+getEncounter().getName()+" still has vault space");
                }
                return;
            }
            if(!expandingEncounter.getRoot().getAccountant().hasResources((HashMap<Material,Integer>) rootResources.clone())){
                if(RandomEncounters.getInstance().getLogLevel()>7){
                    RandomEncounters.getInstance().logMessage("      # Expansion "+expandingEncounter.getName()+"->"+getEncounter().getName()+" does not have the required root resources");
                }
                return;
            }
            if(!expandingEncounter.getAccountant().hasResources((HashMap<Material,Integer>) parentResources.clone())){
                if(RandomEncounters.getInstance().getLogLevel()>7){
                    RandomEncounters.getInstance().logMessage("      # Expansion "+expandingEncounter.getName()+"->"+getEncounter().getName()+" does not have the required local resources");
                }
                return;
            }
            deductResources();
            checking    =   true;
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("      # Expansion "+expandingEncounter.getName()+"->"+getEncounter().getName()+" has started looking for a suitable location");
            }
           (new ChunkLocatorTask(this,expandingEncounter.getLocation().getChunk(),maxDistance.intValue(),minDistance.intValue())).runTaskTimer(RandomEncounters.getInstance(), 1, 1);               
            
        }
        
    }
    
    private void deductResources(){
        expandingEncounter.getRoot().getAccountant().withdrawResources((HashMap<Material,Integer>) rootResources.clone());
        expandingEncounter.getAccountant().withdrawResources((HashMap<Material,Integer>) parentResources.clone());
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
    
    public boolean isVault(){
        return vault;
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
    public String getLineageName(){
        return expandingEncounter.getName()+"->"+encounter.getName();
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
           double adjust   =   expandingEncounter.getChildren(encounter).size()*(Math.PI/8)+(Math.PI/2);
           return Math.atan2(expandingEncounter.getLocation().getChunk().getX()-expandingEncounter.getRoot().getLocation().getChunk().getX(),expandingEncounter.getLocation().getChunk().getZ()-expandingEncounter.getRoot().getLocation().getChunk().getZ())-adjust;
        }else{
            return Math.random()*(Math.PI*2);
        }
    }

    @Override
    public void addPlacedEncounter(PlacedEncounter newEncounter) {
       
        checking    =   false;
        if(newEncounter!=null){
            if(vault){
                expandingEncounter.addVault(newEncounter);
            }
            expandingEncounter.addChild(newEncounter);
        }else{
            expandingEncounter.getRoot().getAccountant().depositResources((HashMap<Material,Integer>) rootResources.clone());
            expandingEncounter.getAccountant().depositResources((HashMap<Material,Integer>) parentResources.clone());
            canExpand   =   false;
        }
    }
}
