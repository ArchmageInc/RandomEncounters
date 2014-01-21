package com.archmageinc.RandomEncounters;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;


public class Locator {
    protected static Locator instance  =   new Locator();
    
    public static Locator getInstance(){
        return instance;
    }
    protected Locator(){
        
    }
    public Location checkChunk(Chunk chunk,Encounter encounter){
        Block currentBlock,aboveBlock;
        if(RandomEncounters.getInstance().getLogLevel()>=7){
            RandomEncounters.getInstance().logMessage("Checking chunk: "+chunk.getX()+","+chunk.getZ());
        }

        for(int x=0;x<16;x++){
            for(int z=0;z<16;z++){
                for(int y=encounter.getStructure().getMinY().intValue();y<encounter.getStructure().getMaxY();y++){
                    currentBlock    =   chunk.getBlock(x, y, z);
                    aboveBlock      =   currentBlock.getRelative(BlockFace.UP);
                    if(
                        (encounter.getValidBiomes().isEmpty() || encounter.getValidBiomes().contains(currentBlock.getBiome()))

                        && (!encounter.getInvalidBiomes().contains(currentBlock.getBiome()))

                        /**
                        * The current block may not be:
                        */
                        && !encounter.getStructure().getInvalid().contains(currentBlock.getType())

                        /**
                        * The block above the current block must be
                        */

                        && encounter.getStructure().getTrump().contains(aboveBlock.getType())

                        && checkSpace(currentBlock,encounter.getStructure())
                    ){
                        return currentBlock.getRelative(BlockFace.UP).getLocation();
                    }
                }
            }
        }
        return null;
    }
    
    private boolean checkSpace(Block startingBlock,Structure structure){
        int xMin    =    (int) Math.ceil(structure.getWidth()/2);
        int zMin    =    (int) Math.ceil(structure.getLength()/2);
        Block currentBlock,belowBlock,aboveBlock;
        for(int x = -xMin;x<=xMin;x++){
            for(int z = -zMin;z<=zMin;z++){
                currentBlock  =   startingBlock.getRelative(x,0,z);
                belowBlock    =   currentBlock.getRelative(BlockFace.DOWN);
                aboveBlock    =   currentBlock.getRelative(BlockFace.UP);

                if(
                    /**
                    * The current block may not be:
                    */
                    structure.getInvalid().contains(currentBlock.getType())

                    /**
                    * The block below the current block may not be:
                    */
                    || structure.getInvalid().contains(belowBlock.getType())

                    /**
                    * The block above the current block must be:
                    */
                    ||  !structure.getTrump().contains(aboveBlock.getType())
                ){
                    if(RandomEncounters.getInstance().midas())
                        currentBlock.setType(Material.GOLD_BLOCK);
                    return false;
                }
            }
        }
        return true;
    }
}
