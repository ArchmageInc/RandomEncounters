package com.archmageinc.RandomEncounters.Encounters;

import com.archmageinc.RandomEncounters.Mobs.PlacedMob;
import com.archmageinc.RandomEncounters.RandomEncounters;
import com.archmageinc.RandomEncounters.ResourceCollection;
import com.archmageinc.RandomEncounters.Tasks.SpawnLocatorTask;
import com.archmageinc.RandomEncounters.Tasks.TreasurePlacementTask;
import com.archmageinc.RandomEncounters.Utilities.Accountant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Represents an Encounter that has been placed in the world.
 * 
 * @author ArchmageInc
 */
public class PlacedEncounter {
    
    /**
     * The generated Unique ID.
     */
    private UUID uuid;
    
    /**
     * The location of this placed encounter in the world.
     */
    private Location location;
    
    /**
     * The Encounter on which this is based.
     */
    private Encounter encounter;
    
    /**
     * The set of PlacedMobs that were spawned with this encounter.
     */
    private final Set<PlacedMob> mobs                       =   new HashSet();
    
    /**
     * Has this encounter been sacked.
     */
    private Boolean sacked                                  =   false;
    
    /**
     * The set of placed encounter unique IDs that have expanded from this placed encounter.
     * 
     */
    private final Set<UUID> children                        =   new HashSet();
    
    private PlacedEncounter parent                          =   null;
    
    /**
     * The set of valid expansion configurations for this Placed Encounter.
     * 
     * This is a clone of the Encounter configuration expansions
     * @see Encounter#expansions
     */
    private final HashSet<Expansion> expansions                 =   new HashSet();
    
    /**
     * An internal list of safe creature spawn locations.
     */
    private List<Location> spawnLocations                   =   new ArrayList();
    
    private PlacedEncounter root                            =   null;
    
    private final Set<Vault> vaults                         =   new HashSet();
    
    private Vault selfVault;
    
    private static Map<UUID,PlacedEncounter> uuidInstances  =   new HashMap();
    
    private final Set<ResourceCollection> collections       =   new HashSet();
    
    /**
     * Get an instance of the placed encounter based on the Unique ID.
     * 
     * @param uuid The unique ID of the PlacedEncounter
     * @return Returns the PlacedEncounter if found, null otherwise.
     */
    public static PlacedEncounter getInstance(UUID uuid){
        return uuidInstances.get(uuid);
    }
    
    /**
     * Get an instance of the PlacedEncounter based on JSON Configuration data.
     * 
     * @param jsonConfiguration The JSON Configuration
     * @return Returns the PlacedEncounter defined by the JSON Configuration.
     */
    public static PlacedEncounter getInstance(JSONObject jsonConfiguration){
        try{
            UUID jsonUUID             =   UUID.fromString((String) jsonConfiguration.get("uuid"));
            PlacedEncounter located   =   PlacedEncounter.getInstance(jsonUUID);
            if(located!=null){
                return located;
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid PlacedEncounter configuration: "+e.getMessage());
        }catch(IllegalArgumentException e){
            RandomEncounters.getInstance().logError("Invalid UUID in PlacedEncounter configuration: "+e.getMessage());
        }
        return new PlacedEncounter(jsonConfiguration);
    }
    
    /**
     * Static Method to create a new PlacedEncounter based on an Encounter at a Lcoation.
     * 
     * @param encounter
     * @param location
     * @return The newly created PlacedEncounter
     * @see PlacedEncounter#PlacedEncounter(com.archmageinc.RandomEncounters.Encounter, org.bukkit.Location) 
     */
    public static PlacedEncounter create(Encounter encounter,Location location){
        if(encounter==null){
            RandomEncounters.getInstance().logError("Missing encounter during placed encounter creation");
            return null;
        }
        if(location==null){
            RandomEncounters.getInstance().logError("An invalid location was passed to create a placed encounter: "+encounter.getName());
            return null;
        }
        return new PlacedEncounter(encounter,location);
    }
    
    /**
     * Constructor for PlacedEncounter based on JSON Configuration
     * 
     * @param jsonConfiguration The JSON configuration
     */
    private PlacedEncounter(JSONObject jsonConfiguration){
        try{
            uuid                    =   UUID.fromString((String) jsonConfiguration.get("uuid"));
            sacked                  =   (Boolean) jsonConfiguration.get("sacked");
            encounter               =   Encounter.getInstance((String) jsonConfiguration.get("encounter"));
            JSONObject jsonLocation =   (JSONObject) jsonConfiguration.get("location");
            location                =   new Location(RandomEncounters.getInstance().getServer().getWorld((String) jsonLocation.get("world")),(Long) jsonLocation.get("x"),(Long) jsonLocation.get("y"),(Long) jsonLocation.get("z"));
            if(encounter==null){
                RandomEncounters.getInstance().logError("Missing Encounter ("+(String) jsonConfiguration.get("encounter")+") from PlacedEncounter configuration");
            }
            JSONArray jsonMobs   =   (JSONArray) jsonConfiguration.get("mobs");
            if(jsonMobs!=null){
                for(int i=0;i<jsonMobs.size();i++){
                    mobs.add(PlacedMob.getInstance((JSONObject) jsonMobs.get(i),this));
                }
            }
            
            JSONArray jsonExpansions    =   (JSONArray) jsonConfiguration.get("expansions");
            if(jsonExpansions!=null){
                for(int i=0;i<jsonExpansions.size();i++){
                    JSONObject jsonExpansion    =   (JSONObject) jsonExpansions.get(i);
                    try{
                        UUID expansionUUID          =   UUID.fromString((String) jsonExpansion.get("uuid"));
                        children.add(expansionUUID);
                    }catch(IllegalArgumentException e){
                        RandomEncounters.getInstance().logError("Invalid UUID in PlacedEncounter expansion configuration: "+e.getMessage());
                    }
                }
            }
            setupEncounter(false);
            uuidInstances.put(uuid, this);
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid PlacedEncounter configuration: "+e.getMessage());
        }catch(IllegalArgumentException e){
            RandomEncounters.getInstance().logError("Invalid UUID in PlacedEncounter configuration: "+e.getMessage());
        }
    }
    
    /**
     * Constructor to create a new PlacedEncounter which will place the structure and spawn creatures.
     * 
     * @param encounter The parent Encounter
     * @param location The location to place the encounter
     */
    private PlacedEncounter(Encounter encounter,Location location){
        this.uuid       =   UUID.randomUUID();
        this.encounter  =   encounter;
        this.location   =   location;
        if(encounter.getStructure()!=null){
            encounter.getStructure().place(this);
        }
        uuidInstances.put(uuid, this);
    }
    
    public final void setupEncounter(boolean isNew){
        if(isNew){
            placeMobs();
            placeTreasures();
        }
        setupExpansions();
        setupCollections();
        //selfVault   =   new Vault(this);
    }
    
    private void setupCollections(){
        collections.clear();
        JSONArray jsonCollections   =   encounter.getCollectionConfiguration();
        if(jsonCollections!=null){
            for(int i=0;i<jsonCollections.size();i++){
                collections.add(new ResourceCollection(this,(JSONObject) jsonCollections.get(i)));
            }
        }
    }
    
    /**
     * Sets up the cloned expansion configurations for newly generated PlacedEncounters.
     */
    private void setupExpansions(){
        expansions.clear();
        for(Expansion expansion : encounter.getExpansions()){
            try {
                Expansion clonedExpansion   =   expansion.clone(this);
                expansions.add(clonedExpansion);
            } catch (CloneNotSupportedException e) {
                RandomEncounters.getInstance().logError("Clone failed for expansion: "+e.getMessage());
            }
        }
    }
    
    public void runCollectionChecks(){
        for(ResourceCollection collection : collections){
            collection.check();
        }
    }
    
    public List<ItemStack> depositResources(List<ItemStack> items){
        return depositResources(Accountant.convert(items));
    }
    public List<ItemStack> depositResources(HashMap<Material,Integer> resources){
        if(resources.isEmpty()){
            return new ArrayList<>();
        }
        List<ItemStack> leftovers   =   Accountant.convert(resources);
        for(Vault vault : getVaults()){
            leftovers   =   vault.deposit(leftovers);
            if(leftovers.isEmpty()){
                break;
            }
        }
        if(!leftovers.isEmpty() && parent!=null){
            if(RandomEncounters.getInstance().getLogLevel()>7){
                RandomEncounters.getInstance().logMessage("Leftover deposit from "+getName()+", sending to parent.");
            }
            return parent.depositResources(Accountant.convert(leftovers));
        }
        return leftovers;
    }
    
    public List<ItemStack> withdrawResources(List<ItemStack> items){
        return withdrawResources(Accountant.convert(items));
    }
    public List<ItemStack> withdrawResources(HashMap<Material,Integer> resources){
        if(resources.isEmpty()){
            return new ArrayList<>();
        }
        List<ItemStack> leftover    =   Accountant.convert(resources);
        for(Vault vault : getVaults()){
            leftover    =   vault.withdraw(leftover);
            if(leftover.isEmpty()){
                break;
            }
        }
        return leftover;
    }
    public boolean hasResources(HashMap<Material,Integer> resources){
        if(resources.isEmpty()){
            return true;
        }
        HashMap<Material,Integer> leftover  =   (HashMap<Material,Integer>) resources.clone();
        for(Vault vault : getVaults()){
            leftover    =   vault.contains(leftover);
            if(leftover.isEmpty()){
                break;
            }
        }
        if(RandomEncounters.getInstance().getLogLevel()>9){
            for(Material material : leftover.keySet()){
                RandomEncounters.getInstance().logMessage("        - "+leftover.get(material)+" more "+material.name()+" needed");
            }
        }
        return leftover.isEmpty();
    }
    public boolean hasVaultSpace(){
        for(Vault vault : getVaults()){
            if(!vault.getEncounter().equals(this)){
                if(!vault.isFull()){
                    return true;
                }
            }
        }
        return false;
    }
    
    private void placeMobs(){
        (new SpawnLocatorTask(this)).runTaskTimer(RandomEncounters.getInstance(),1,1);
    }
    
    private void placeTreasures(){
        (new TreasurePlacementTask(this)).runTaskTimer(RandomEncounters.getInstance(), 1, 1);
    }
    
    public void addMobs(Set<PlacedMob> newMobs){
        mobs.addAll(newMobs);
    }
    
    public void setSpawnLocations(List<Location> locations){
        spawnLocations =   locations;
    }
    
    public void addVault(PlacedEncounter vault){
        if(vaults.isEmpty()){
            getVaults();
        }
        vaults.add(new Vault(vault));
    }
    
    private Set<Vault> getVaults(){
        if(vaults.isEmpty()){
            for(Expansion exp : expansions){
                if(exp.isVault()){
                    for(PlacedEncounter enc : getChildren(exp.getEncounter())){
                        vaults.add(new Vault(enc));
                    }
                }
            }
            vaults.add(new Vault(this));
        }
        return vaults;
    }
    
    /**
     * Gets a random spawn location for a creature.
     * 
     * @return Returns a location to spawn a creature.
     */
    public Location findSafeSpawnLocation(){
        if(spawnLocations.isEmpty()){
            return null;
        }
        Collections.shuffle(spawnLocations);
        return spawnLocations.get(1);
    }
    
    /**
     * Adds an expansion to the list.
     * 
     * @param placedEncounter The PlacedEncounter that expanded.
     */
    public void addChild(PlacedEncounter placedEncounter){
        children.add(placedEncounter.getUUID());
        placedEncounter.setParent(this);
    }
    
    public void removeChild(PlacedEncounter placedEncounter){
        children.remove(placedEncounter.getUUID());
    }
    
    /**
     * Gets the Unique ID of this PlacedExpansion
     * @return 
     */
    public UUID getUUID(){
        return uuid;
    }
    
    /**
     * Gets the location of this PlacedExpansion.
     * @return 
     */
    public Location getLocation(){
        return location;
    }
    
    /**
     * Removes the PlacedMob from the list of Mobs in this encounter.
     * @param mob The PlacedMob to be removed
     */
    public void removeMob(PlacedMob mob){
        mobs.remove(mob);
        if(mobs.isEmpty()){
            sack();
        }
    }
    
    /**
     * Adds a mob to the encounter
     * @param mob 
     */
    public void addMob(PlacedMob mob){
        mobs.add(mob);
    }
    
    /**
     * Gets the parent Encounter configuration that generated this PlacedEncounter.
     * @return 
     */
    public Encounter getEncounter(){
        return encounter;
    }
    
    /**
     * Gets the unique name of the parent Encounter that generated this PlacedEncounter.
     * @return 
     */
    public String getName(){
        if(RandomEncounters.getInstance().getLogLevel()>11){
            if(parent!=null && !parent.isSacked()){
                return parent.getName()+"->"+encounter.getName();
            }
            return encounter.getName();
        }
        if(parent!=null && !parent.isSacked()){
            return getRoot().getName()+"->["+(getGeneration()-1)+"]->"+encounter.getName();
        }
        return encounter.getName();
    }
    
    public int getGeneration(){
        if(parent!=null && !parent.isSacked()){
            return parent.getGeneration()+1;
        }
        return 0;
    }
    
    /**
     * Gets the set of unique IDs of expansions spawned from this PlacedEncounter.
     * @return 
     */
    public Set<UUID> getChildren(){
        return children;
    }
    
    public Set<PlacedEncounter> getChildren(Encounter type){
        Set<PlacedEncounter> placements =   new HashSet();
        for(UUID id : children){
            PlacedEncounter placedEncounter =   PlacedEncounter.getInstance(id);
            if(placedEncounter!=null && placedEncounter.getEncounter().equals(type)){
                placements.add(placedEncounter);
            }
        }
        return placements;
    }
    
    /**
     * Gets the set of Expansion configurations for this PlacedEncounter.
     * @return 
     */
    public HashSet<Expansion> getExpansions(){
        return expansions;
    }
    
    public PlacedEncounter getRoot(){
        if(root==null){
            root    =   this;
            if(this.parent!=null && !this.parent.isSacked()){
                root    =   parent.getRoot();
            }
        }
        
        return root;
    }
    
    public PlacedEncounter getParent(){
        return this.parent;
    }
    
    public void setParent(PlacedEncounter parent){
        this.parent =   parent;
    }
    
    /**
     * Has this encounter been sacked.
     * @return 
     */
    public boolean isSacked(){
        return sacked;
    }
    
    private void sack(){
        sacked  =   true;
        if(RandomEncounters.getInstance().getLogLevel()>6){
            RandomEncounters.getInstance().logMessage(getName()+" has been sacked!");
        }
        if(parent!=null){
            parent.removeChild(this);
        }
        RandomEncounters.getInstance().removePlacedEncounter(this);
        uuidInstances.remove(uuid);
    }
    
    /**
     * Convert the PlacedEncounter into a JSONObject for serialization.
     * @return Returns the JSONObject
     */
    public JSONObject toJSON(){
        JSONObject jsonConfiguration    =   new JSONObject();
        if(location==null){
            RandomEncounters.getInstance().logError("Attempted to save: "+getName()+" but the location was null");
            return jsonConfiguration;
        }
        if(location.getWorld()==null){
            RandomEncounters.getInstance().logError("Attempted to save: "+getName()+" but the world in the location was null: "+location.toString());
            return jsonConfiguration;
        }
        if(location.getWorld().getName()==null){
            RandomEncounters.getInstance().logError("Attempted to save: "+getName()+" but the world name in the location was null: "+location.toString());
            return jsonConfiguration;
        }
        jsonConfiguration.put("uuid", uuid.toString());
        jsonConfiguration.put("encounter",encounter.getName());
        jsonConfiguration.put("sacked",sacked);
        
        JSONObject jsonLocation         =   new JSONObject();
        jsonLocation.put("world",location.getWorld().getName());
        jsonLocation.put("x",location.getBlockX());
        jsonLocation.put("y",location.getBlockY());
        jsonLocation.put("z",location.getBlockZ());
        jsonConfiguration.put("location", jsonLocation);
        
        JSONArray jsonMobs              =   new JSONArray();
        for(PlacedMob mob : mobs){
            jsonMobs.add(mob.toJSON());
        }
        jsonConfiguration.put("mobs",jsonMobs);
        
        JSONArray jsonExpansions        =   new JSONArray();
        for(UUID expansion : children){
            JSONObject jsonExpansion    =   new JSONObject();
            jsonExpansion.put("uuid",expansion.toString());
            jsonExpansions.add(jsonExpansion);
        }
        jsonConfiguration.put("expansions", jsonExpansions);
        
        return jsonConfiguration;
    }
}
