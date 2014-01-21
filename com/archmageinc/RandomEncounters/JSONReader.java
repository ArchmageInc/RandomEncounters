package com.archmageinc.RandomEncounters;

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
 *
 * @author ArchmageInc
 */
public class JSONReader {
    protected static JSONReader instance = new JSONReader();
    
    public static JSONReader getInstance(){
        return instance;
    }
    
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
    
    public void write(String fileName, JSONObject data, Boolean append){
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName,append))) {
            writer.write(data.toJSONString());
        } catch(IOException ex) {
            RandomEncounters.getInstance().logWarning("Error while writing to file "+fileName);
        }
    }
    
    public void write(String fileName,JSONObject data){
        write(fileName,data,false);
    }
    
}
