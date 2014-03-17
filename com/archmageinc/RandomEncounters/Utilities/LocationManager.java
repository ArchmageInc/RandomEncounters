package com.archmageinc.RandomEncounters.Utilities;

import com.archmageinc.RandomEncounters.RandomEncounters;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;

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
    
    public static void removeLocations(String filename){
        try{
            File file   =   new File(filename);
            if(file.delete() && RandomEncounters.getInstance().getLogLevel()>5){
                RandomEncounters.getInstance().logMessage(filename+" has been deleted");
            }else{
                RandomEncounters.getInstance().logWarning("Unable to remove location file: "+filename);
            }
        }catch(Exception e){
            RandomEncounters.getInstance().logWarning("Unable to remove location file: "+filename+": "+e.getMessage());
        }
    }
}
