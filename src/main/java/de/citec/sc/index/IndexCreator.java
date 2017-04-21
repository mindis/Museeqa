/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.index;

import de.citec.sc.utils.FileFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author sherzod
 */
public class IndexCreator {

    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            args = new String[1];
            args[0] = "indexFiles";

        }

        File outerFolder = new File(args[0]);
        File[] listOfLanguageFiles = outerFolder.listFiles();

        for (File langFile : listOfLanguageFiles) {

            if (langFile.isDirectory()) {

                File[] listOfFolders = langFile.listFiles();

                System.out.println("Loading " + langFile.getName() + " with " + listOfFolders.length + " files ... ");

                Map<String, Map<String, Integer>> indexMap = new HashMap<>();
                
                int c = 0;

                for (File folder : listOfFolders) {
                    c++;
                    if(c %10 == 0){
                        System.out.println(c+ " files are done.");
                    }
                    if (folder.isDirectory()) {
                        File[] listOfFiles = folder.listFiles();
                        for (File file : listOfFiles) {
                            if (file.isFile()) {
                                Map<String, Map<String, Integer>> partialIndexMap = getSurfaceForms(file);

                                //add each surface form
                                for (String surfaceForm : partialIndexMap.keySet()) {
                                    
                                    if (indexMap.containsKey(surfaceForm)) {
                                        Map<String, Integer> pair = indexMap.get(surfaceForm);
                                        Map<String, Integer> pairPartial = partialIndexMap.get(surfaceForm);

                                        //merge two pair maps by adding the frequency values
                                        for(String uri : pairPartial.keySet()){
                                            if(pair.containsKey(uri)){
                                                pair.put(uri, pair.get(uri) + pairPartial.get(uri));
                                            }
                                            else{
                                                pair.put(uri, pairPartial.get(uri));
                                            }
                                        }

                                        indexMap.put(surfaceForm, pair);
                                    } else {
                                        indexMap.put(surfaceForm, partialIndexMap.get(surfaceForm));
                                    }
                                }
                            }
                        }
                    }
                }
                
                //write out the map
                String content  = "";
                for(String s : indexMap.keySet()){
                    for(String uri : indexMap.get(s).keySet()){
                        content += s+"\t"+uri+"\t"+indexMap.get(s).get(uri)+"\n";
                    }
                }
                
                FileFactory.writeListToFile(langFile.getName().replace(" ", "").trim()+"_anchorFile.txt", content, false);
                System.out.println("\nFile saved.");
            }
        }
    }

    private static Map<String, Map<String, Integer>> getSurfaceForms(File file) {

        Map<String, Map<String, Integer>> indexMap = new HashMap<>();

        JSONParser parser = new JSONParser();

        try {

            Set<String> content = FileFactory.readFile(file);

            for (String c : content) {
                Object obj = parser.parse(c);

                JSONObject jsonObject = (JSONObject) obj;

                // loop array
                JSONArray annotations = (JSONArray) jsonObject.get("annotations");

                for (Object o : annotations) {
                    JSONObject surfaceFormObject = (JSONObject) o;

                    String uri = (String) surfaceFormObject.get("uri");
                    String surfaceForm = (String) surfaceFormObject.get("surface_form");
                    surfaceForm = surfaceForm.toLowerCase().trim();

                    if (indexMap.containsKey(surfaceForm)) {
                        Map<String, Integer> pair = indexMap.get(surfaceForm);

                        pair.put(uri, pair.getOrDefault(uri, 1) + 1);

                        indexMap.put(surfaceForm, pair);
                    } else {
                        Map<String, Integer> pair = new HashMap<>();

                        pair.put(uri, pair.getOrDefault(uri, 1) + 1);

                        indexMap.put(surfaceForm, pair);
                    }
                }
            }

        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(IndexCreator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return indexMap;
    }
}
