package com.archmageinc.RandomEncounters;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author ArchmageInc
 */
public class Structure {
    
    protected String name;
    protected String fileName;
    protected Long minY;
    protected Long maxY;
    protected Boolean loaded                        =   false;
    protected HashSet<Material> trump               =   new HashSet();
    protected HashSet<Material> invalid             =   new HashSet();
    protected static HashSet<Structure> instances   =   new HashSet();
    /**
     * The WorldEdit session which keeps track of changes
     */
    protected EditSession session;
    
    /**
     * The WorldEdit cuboid
     */
    protected CuboidClipboard cuboid;
    
    public static Structure getInstance(String name){
        for(Structure instance : instances){
            if(instance.getName().equals(name)){
                return instance;
            }
        }
        return null;
    }
    
    public static Structure getInstance(JSONObject jsonConfiguration){
        try{
            for(Structure instance : instances){
                if(instance.getName().equals((String) jsonConfiguration.get("name"))){
                    return instance;
                }
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Structure configuration: "+e.getMessage());
        }
        return new Structure(jsonConfiguration);
    }
    
    protected Structure(JSONObject jsonConfiguration){
        try{
            name                    =   (String) jsonConfiguration.get("name");
            fileName                =   (String) jsonConfiguration.get("file");
            minY                    =   (Long) jsonConfiguration.get("minY");
            maxY                    =   (Long) jsonConfiguration.get("maxY");
            JSONArray jsonTrump     =   (JSONArray) jsonConfiguration.get("trump");
            JSONArray jsonInvalid   =   (JSONArray) jsonConfiguration.get("invalid");
            if(jsonTrump!=null){
                for(int i=0;i<jsonTrump.size();i++){
                    trump.add(Material.getMaterial((String) jsonTrump.get(i)));
                }
            }
            if(jsonInvalid!=null){
                for(int i=0;i<jsonInvalid.size();i++){
                    invalid.add(Material.getMaterial((String) jsonInvalid.get(i)));
                }
            }
            loaded  =   load();
            if(loaded){
                instances.add(this);
            }
        }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid Structure configuration: "+e.getMessage());
        }
    }
    
    protected final boolean load(){
        try{
            File file           =   new File(RandomEncounters.getInstance().getDataFolder()+"/"+fileName);
            SchematicFormat sf  =   SchematicFormat.getFormat(file);
            if(sf==null){
                RandomEncounters.getInstance().logError("Unable to detect schematic format for file: "+fileName);
                return false;
            }
            cuboid         =    sf.load(file);
            
        }catch(IOException e){
            RandomEncounters.getInstance().logError("Unable to load structure "+name+": "+e.getMessage());
            return false;
        } catch (DataException e) {
            RandomEncounters.getInstance().logError("Invalid structure schematic "+fileName+": "+e.getMessage());
            return false;
        }
        return true;
    }
    
    private void newSession(World world){
        if(RandomEncounters.getInstance().getLogLevel()>8){
            RandomEncounters.getInstance().logMessage("Generating new WorldEdit session for structure "+name);
        }
        session        =    new EditSession((new BukkitWorld(world)),cuboid.getWidth()*cuboid.getLength()*cuboid.getHeight());
        //session.enableQueue();
        flipRandom();
    }
    private void flipRandom(){
        int angle   =  (int) Math.round(Math.random()*4)*90;
        if(RandomEncounters.getInstance().getLogLevel()>7){
            RandomEncounters.getInstance().logMessage("Flipping structure "+name+" "+angle+" degrees");
        }
        cuboid.rotate2D(angle);
    }
    private void placeTreasures(Encounter encounter,Location location){
        int x    =   location.getBlockX();
        int y    =   location.getBlockY();
        int z    =   location.getBlockZ();
        for(int cx=x-cuboid.getWidth();cx<x+cuboid.getWidth();cx++){
            for(int cy=y-cuboid.getHeight();cy<y+cuboid.getHeight();cy++){
                for(int cz=z-cuboid.getLength();cz<z+cuboid.getLength();cz++){
                    BlockState state        =   location.getWorld().getBlockAt(cx, cy, cz).getState();
                    if(state instanceof Chest){
                        List<ItemStack> items   =   encounter.getTreasure();
                        Chest chest             =   (Chest) state;
                        for(ItemStack item : items){
                            chest.getInventory().addItem(item);
                        }
                    }
                }
            }
        }
    }
    public void place(Encounter encounter,Location location){
        if(!loaded){
            RandomEncounters.getInstance().logWarning("Attempted to place a non-loaded structure: "+name);
            return;
        }
        newSession(location.getWorld());
        try{
            Vector v    =   new Vector(location.getX(),location.getY(),location.getZ());
            cuboid.setOffset(new Vector(-Math.ceil(cuboid.getWidth()/2),0,-Math.ceil(cuboid.getLength()/2)));
            cuboid.paste(session, v, false);
            if(RandomEncounters.getInstance().getLogLevel()>5){
                RandomEncounters.getInstance().logMessage("Placed structure "+name+": "+session.size());
            }
            placeTreasures(encounter,location);
        }catch(MaxChangedBlocksException e){
            RandomEncounters.getInstance().logWarning("Unable to place structure: Maximum number of blocks changed: "+e.getMessage());
        }
    }
    
    public int getWidth(){
        return loaded ? cuboid.getWidth() : 0;
    }
    public int getHeight(){
        return loaded ? cuboid.getHeight() : 0;
    }
    public int getLength(){
        return loaded ? cuboid.getLength() : 0;
    }
    public Set<Material> getTrump(){
        return trump;
    }
    public Set<Material> getInvalid(){
        return invalid;
    }
    public Long getMinY(){
        return minY;
    }
    public Long getMaxY(){
        return maxY;
    }
    public String getName(){
        return name;
    }
}
