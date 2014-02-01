package com.archmageinc.RandomEncounters.Listeners;

import com.archmageinc.RandomEncounters.Encounters.Encounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

/**
 * A Listener for chunk population to check encounter placements.
 * 
 * @author ArchmageInc
 */
public class WorldListener implements Listener {
    /**
     * Is the system already processing a chunk populate event.
     * 
     * Placing a structure has the potential to cause new chunks to generate, and therefore more checks for placement.
     * This is to reduce overall system overhead. 
     */
    private boolean processing =   false;
    
    
    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event){
        if(processing){
            return;
        }
        if(RandomEncounters.getInstance().getLogLevel()>8){
            RandomEncounters.getInstance().logMessage("New chunk detected, prepairing to run checks");
        }
        processing                      =   true;
        Set<Encounter> encounters       =   RandomEncounters.getInstance().getEncounters();
        List<Encounter> encounterList   =   new ArrayList();
        encounterList.addAll(encounters);
        Collections.shuffle(encounterList,new Random(System.nanoTime()));
        for(Encounter encounter : encounterList){
            encounter.checkPlace(event.getChunk());
        }
        processing  =   false;
    }
}
