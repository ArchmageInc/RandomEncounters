package com.archmageinc.RandomEncounters;

import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
import com.archmageinc.RandomEncounters.Tasks.ResourceCollectorTask;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class ResourceCollection {
    private int distance;
    private int duration;
    private int total;
    private String destination;
    private int maxHeight;
    private int minHeight;
    private Calendar lastCheck;
    private final Set<Material> resources  =    new HashSet();
    private boolean checking               =    false;
    private final PlacedEncounter placedEncounter;
    
    public ResourceCollection(PlacedEncounter placed,JSONObject jsonConfiguration){
        placedEncounter =   placed;
        lastCheck       =   (Calendar) Calendar.getInstance().clone();
        try{
            distance                    =   jsonConfiguration.get("distance")==null ? 0 : ((Number) jsonConfiguration.get("distance")).intValue();
            total                       =   jsonConfiguration.get("total")==null ? 0 : ((Number) jsonConfiguration.get("total")).intValue();
            duration                    =   jsonConfiguration.get("duration")==null ? 60 : ((Number) jsonConfiguration.get("duration")).intValue();
            maxHeight                   =   jsonConfiguration.get("maxHeight")==null ? placed.getLocation().getWorld().getMaxHeight() : ((Number) jsonConfiguration.get("maxHeight")).intValue();
            minHeight                   =   jsonConfiguration.get("minHeight")==null ? 0 : ((Number) jsonConfiguration.get("minHeight")).intValue();
            destination                 =   jsonConfiguration.get("destination")==null ? "self" : (String) jsonConfiguration.get("destination");
            JSONArray jsonMaterials     =   (JSONArray) jsonConfiguration.get("resources");
            if(jsonMaterials!=null){
                for(int i=0;i<jsonMaterials.size();i++){
                    String materialName =   (String) jsonMaterials.get(i);
                    Material material   =   Material.getMaterial(materialName);
                    if(material!=null){
                        resources.add(material);
                    }else{
                        RandomEncounters.getInstance().logWarning("Invalid material name in collection configuration: "+materialName);
                    }
                }
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Collection Configuration "+e.getMessage());
        }
    }
    
    public int getTotal(){
        return total;
    }
    
    public int getDistance(){
        return distance;
    }
    
    public int getMaxHeight(){
        return maxHeight;
    }
    
    public int getMinHeight(){
        return minHeight;
    }
    
    public boolean validResource(Material check){
        return resources.contains(check);
    }
    
    public PlacedEncounter getDestination(){
        if(destination.equalsIgnoreCase("root")){
            return placedEncounter.getRoot();
        }
        if(destination.equalsIgnoreCase("parent")){
            return placedEncounter.getParent()!=null ? placedEncounter.getParent() : placedEncounter;
        }
        return placedEncounter;
    }
    public Location getLocation(){
        return placedEncounter.getLocation();
    }
    public void check(){
        Calendar nextRun    =   (Calendar) lastCheck.clone();
        nextRun.add(Calendar.MINUTE, duration);
        if(!checking && nextRun.before(Calendar.getInstance())){
            lastCheck   =   (Calendar) Calendar.getInstance().clone();
            checking    =   true;
            if(RandomEncounters.getInstance().getLogLevel()>8){
                RandomEncounters.getInstance().logMessage("  * "+placedEncounter.getName()+": resource collection rule started");
            }
            (new ResourceCollectorTask(this)).runTaskTimer(RandomEncounters.getInstance(), 1, 1);
        }
    }
    
    public void collectResources(List<ItemStack> items){
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("  * "+placedEncounter.getName()+": resource collection rule finished: "+items.size());
        }
        getDestination().depositResources(items);
        checking    =   false;
    }
    
}
