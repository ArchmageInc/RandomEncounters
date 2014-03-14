package com.archmageinc.RandomEncounters.Tasks;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.Utilities.LoadListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author ArchmageInc
 */
public class LocationLoadingTask extends BukkitRunnable {
    private final PlacedEncounter placedEncounter;
    private final String filename;
    private final Set<LoadListener> listeners   =   new HashSet();
    private int[] locations;
    
    public LocationLoadingTask(PlacedEncounter placedEncounter,String filename){
        this.placedEncounter    =   placedEncounter;
        this.filename           =   filename;
        addListener(placedEncounter);
        if(RandomEncounters.getInstance().getLogLevel()>6){
            RandomEncounters.getInstance().logMessage("Location loading started for "+placedEncounter.getName());
        }
        runTaskAsynchronously(RandomEncounters.getInstance());
    }
    
    public final void addListener(LoadListener listener){
        listeners.add(listener);
    }
    @Override
    public void run() {
        try {
            FileInputStream in          =   new FileInputStream(filename);
            ObjectInputStream stream    =   new ObjectInputStream(in);
            locations                   =   (int[]) stream.readObject();
            notifyListeners();
        } catch (IOException | ClassNotFoundException e) {
            RandomEncounters.getInstance().logError("Error while loading locations: "+e.getMessage());
        }
    }
    
    private void notifyListeners(){
        if(RandomEncounters.getInstance().getLogLevel()>6){
            RandomEncounters.getInstance().logMessage("Location loading finished for "+placedEncounter.getName()+": found "+(locations.length/3)+" locations, notifying:");
        }
        for(LoadListener listener : listeners){
            listener.processLoad(this);
        } 
    }
    
    public int[] getLocations(){
        return locations;
    }
    
}
