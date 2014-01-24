package com.archmageinc.RandomEncounters;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class Expansion implements Cloneable{
    protected Encounter encounter;
    protected String encounterName;
    protected Double probability;
    protected Long duration;
    protected Long max;
    protected Long distance;
    protected Calendar lastCheck                    =   (Calendar) Calendar.getInstance().clone();
    
    public Expansion(JSONObject jsonConfiguration){
        try{
            encounter       =   Encounter.getInstance((String) jsonConfiguration.get("encounter"));
            encounterName   =   (String) jsonConfiguration.get("encounter");
            probability     =   (Double) jsonConfiguration.get("probability");
            duration        =   (Long) jsonConfiguration.get("duration");
            max             =   (Long) jsonConfiguration.get("max");
            distance        =   (Long) jsonConfiguration.get("distance");
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid expansion configuration: "+e.getMessage());
        }
    }
    
    public void checkExpansion(PlacedEncounter placedEncounter){
        Double random   =   Math.random();
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("    * Checking expansion for "+placedEncounter.getEncounter().getName()+" -> "+getEncounter().getName()+" : ("+random+","+probability+") ");
        }
        if(random<probability){
            Set<PlacedEncounter> validExpansions    =   new HashSet();
            for(UUID expansionUUID : placedEncounter.getPlacedExpansions()){
                PlacedEncounter placedExpansion =   PlacedEncounter.getInstance(expansionUUID);
                if(placedExpansion.getEncounter().equals(getEncounter())){
                    validExpansions.add(placedExpansion);
                }
            }
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("      # Expansion probability hit for encounter "+placedEncounter.getEncounter().getName()+" -> "+getEncounter().getName()+". There are "+validExpansions.size()+" existing expansions, "+max+" are allowed.");
            }
            if(validExpansions.size()<max){
                Chunk encounterChunk    =   placedEncounter.getLocation().getChunk();
                World world             =   placedEncounter.getLocation().getWorld();
                int x                   =   encounterChunk.getX();
                int z                   =   encounterChunk.getZ();
                int n   =   1;
                int l   =   0;
                int m   =   2*distance.intValue()+1;
                int t   =   ((Double) Math.pow(m,2)).intValue();
                
                while(n<=t){
                    if(n>sumTn(m,l))
                        l++;
                    
                    Chunk currentChunk  =   world.getChunkAt(x+xn(l), z+zn(l));
                    Location location   =   Locator.getInstance().checkChunk(currentChunk, getEncounter());
                    if(location!=null){
                        PlacedEncounter newExpansion    =   PlacedEncounter.create(getEncounter(), location);
                        placedEncounter.addExpansion(newExpansion);
                        RandomEncounters.getInstance().addPlacedEncounter(newExpansion);
                        break;
                    }
                    
                    n++;
                }
            }
        }
    }
    
    protected int sumTn(int m,int l){
        int i   =   0;
        int n   =   0;
        while(i<=l){
            n   =   n+(m-tn(i));
            i++;
        }
        return n;
    }
    
    protected int tn(int l){
        int p    =   ((Double) Math.pow(-1,l)).intValue();
        return (1/4)*(2*l+p+3);
    }
    
    protected int xn(int l){
        return ((Double) Math.sin((l*Math.PI)/2)).intValue();
    }
    
    protected int zn(int l){
        return ((Double) Math.cos((l*Math.PI)/2)).intValue();
    }
    
    @Override
    public Expansion clone() throws CloneNotSupportedException{
       return (Expansion) super.clone();
    }
    
    public Encounter getEncounter(){
        if(encounter==null){
            encounter   =   Encounter.getInstance(encounterName);
        }
        if(encounter==null){
            RandomEncounters.getInstance().logError("Unable to locate encounter "+encounterName+" for expansion!!");
        }
        return encounter;
    }
    
    public Long getDuration(){
        return duration;
    }
    public Calendar getLastCheck(){
        return lastCheck;
    }
    
    public void updateLastCheck(){
        lastCheck   =   (Calendar) Calendar.getInstance().clone();
    }
}
