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
public class Expansion {
    protected Encounter encounter;
    protected String encounterName;
    protected Double probability;
    protected Long duration;
    protected Long max;
    protected Long distance;
    
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
        Calendar nextRun    =   (Calendar) placedEncounter.getLastCheck().clone();
        nextRun.add(Calendar.MINUTE, duration.intValue());
        if(nextRun.before(Calendar.getInstance())){
            Double random   =   Math.random();
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("Checking expansion for "+placedEncounter.getEncounter().getName()+": ("+random+","+probability+") - "+nextRun.getTimeInMillis()+" : "+Calendar.getInstance().getTimeInMillis());
            }
            placedEncounter.updateLastCheck();
            if(random<probability){
                Set<PlacedEncounter> validExpansions    =   new HashSet();
                for(UUID expansionUUID : placedEncounter.getExpansions()){
                    PlacedEncounter placedExpansion =   PlacedEncounter.getInstance(expansionUUID);
                    if(placedExpansion.getEncounter().equals(getEncounter())){
                        validExpansions.add(placedExpansion);
                    }
                }
                if(RandomEncounters.getInstance().getLogLevel()>7){
                    RandomEncounters.getInstance().logMessage("Expansion probability hit for encounter "+placedEncounter.getEncounter().getName()+". There are "+validExpansions.size()+" existing expansions, "+max+" are allowed.");
                }
                if(validExpansions.size()<max){
                    Chunk encounterChunk    =   placedEncounter.getLocation().getChunk();
                    World world             =   placedEncounter.getLocation().getWorld();
                    int x                   =   encounterChunk.getX();
                    int z                   =   encounterChunk.getZ();
                    boolean placed          =   false;
                    for(int cx=x-distance.intValue();cx<x+distance.intValue();cx++){
                        for(int cz=z-distance.intValue();cz<z+distance.intValue();cz++){
                            Chunk currentChunk  =   world.getChunkAt(cx, cz);
                            Location location   =   Locator.getInstance().checkChunk(currentChunk, getEncounter());
                            if(location!=null){
                                PlacedEncounter newExpansion    =   PlacedEncounter.create(getEncounter(), location);
                                //Concurent Modification
                                placedEncounter.addExpansion(newExpansion);
                                RandomEncounters.getInstance().addPlacedEncounter(newExpansion);
                                placed  =   true;
                                break;
                            }
                        }
                        if(placed){
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public Encounter getEncounter(){
        if(encounter==null){
            encounter   =   Encounter.getInstance(encounterName);
        }
        return encounter;
    }
}
