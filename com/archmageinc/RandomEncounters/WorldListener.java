package com.archmageinc.RandomEncounters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

/**
 *
 * @author ArchmageInc
 */
public class WorldListener implements Listener {
    
    protected boolean processing =   false;
    
    
    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event){
        if(processing){
            return;
        }
        processing                      =   true;
        Set<Encounter> encounters       =   RandomEncounters.getInstance().getEncounters();
        List<Encounter> encounterList   =   new ArrayList();
        encounterList.addAll(encounters);
        Collections.shuffle(encounterList,new Random(System.nanoTime()));
        for(Encounter encounter : encounterList){
            PlacedEncounter placedEncounter =   encounter.checkPlace(event.getChunk());
            if(placedEncounter!=null){
                RandomEncounters.getInstance().addPlacedEncounter(placedEncounter);
                break;
            }
        }
        processing  =   false;
    }
}
