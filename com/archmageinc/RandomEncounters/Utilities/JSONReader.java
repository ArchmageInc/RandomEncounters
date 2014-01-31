package com.archmageinc.RandomEncounters.Utilities;

import com.archmageinc.RandomEncounters.RandomEncounters;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A singleton utility to Read and Write JSON data from and to the file system.
 * 
 * @author ArchmageInc
 */
public class JSONReader {
    private static final JSONReader instance = new JSONReader();
    
    /**
     * Get the instance of the JSONReader
     * @return 
     */
    public static JSONReader getInstance(){
        return instance;
    }
    
    /**
     * Read JSON data from the file system.
     * @param fileName The name of the file, with directories.
     * @return Returns the JSONObject read, or an empty object on an error.
     */
    public JSONObject read(String fileName){
        JSONParser parser   =   new JSONParser();
        JSONObject json     =   new JSONObject();
        String data         =   "";
        String line;
        try{
            BufferedReader reader   =   new BufferedReader(new FileReader(fileName));
            while((line=reader.readLine())!=null){
                data    +=  line;
            }
            json =   (JSONObject) parser.parse(data);
        }catch(FileNotFoundException e){
            RandomEncounters.getInstance().logWarning("File not found: "+fileName);
        }catch(IOException e){
            RandomEncounters.getInstance().logWarning("Error while reading from file "+fileName);
        }catch (ParseException e) {
            RandomEncounters.getInstance().logWarning("Error while parsing JSON data from file "+fileName+": "+e.getMessage());
        }
        
        return json;
    }
    
    /**
     * Write JSON data to the file system
     * @param fileName The name of the file to write, with directories. 
     * @param data The JSON data to write.
     * @param append Should the JSON data be appended to existing JSON within the file.
     */
    public void write(String fileName, JSONObject data, Boolean append){
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName,append))) {
            writer.write(data.toJSONString());
        } catch(IOException ex) {
            RandomEncounters.getInstance().logWarning("Error while writing to file "+fileName);
        }
    }
    
    /**
     * Write JSON data to the file system
     * @param fileName
     * @param data 
     * @see JSONReader#write(java.lang.String, org.json.simple.JSONObject, java.lang.Boolean) 
     */
    public void write(String fileName,JSONObject data){
        write(fileName,data,false);
    }
    
}
