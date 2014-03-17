package com.archmageinc.RandomEncounters.Utilities;

import com.archmageinc.RandomEncounters.RandomEncounters;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 *
 * @author ArchmageInc
 */
public abstract class LocationManager {
    
    
    public static void saveLocations(String filename,int[] locations){
        if(locations==null){
            return;
        }
        try {
            FileOutputStream out        =   new FileOutputStream(filename);
            ObjectOutputStream stream   =   new ObjectOutputStream(out);
            stream.writeObject(locations);
            stream.close();
            out.close();
        } catch (IOException e) {
            RandomEncounters.getInstance().logError("Error while saving locations: "+e.getMessage());
        }
    }
}
