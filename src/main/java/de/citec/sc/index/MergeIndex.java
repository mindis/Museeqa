/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.index;

import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.query.Instance;
import de.citec.sc.utils.SortUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 *
 * @author sherzod
 */
public class MergeIndex {

    public static void main(String[] args) {
        
        merge(Language.EN);
        merge(Language.DE);
        merge(Language.ES);
    }

    private static void merge(Language lang) {
        
        System.out.println("Merging index files for "+lang);
        
        System.out.println("Loading anchor files");
        
        Map<String, Map<String, Integer>> mergedIndexMap = new ConcurrentHashMap<>(5000000);
        
        int count = 0;

        try (Stream<String> stream = Files.lines(Paths.get("indexData/"+lang.name().toLowerCase()+"_resource_anchorFile.txt"))) {
            stream.parallel().forEach(item -> {

                String[] c = item.toString().split("\t");


                if (c.length == 3) {

                    String label = c[0].toLowerCase();
                    String uri = c[1];
                    int freq = Integer.parseInt(c[2]);

                    String key = label + "\t" + uri;
                    
                    if(mergedIndexMap.containsKey(label)){
                        Map<String, Integer> uri2Freq = mergedIndexMap.get(label);
                        
                        if(uri2Freq.containsKey(uri)){
                            freq = uri2Freq.get(uri) + freq;
                        }
                        
                        uri2Freq.put(uri, freq);
                        
                        mergedIndexMap.put(label, uri2Freq);
                    }
                    else{
                        Map<String, Integer> uri2Freq = new HashMap<>();
                        
                        uri2Freq.put(uri, freq);
                        
                        mergedIndexMap.put(label, uri2Freq);
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Merging dbpedia files");
        //load dbpedia files and merge keys by adding frequency values
        try (Stream<String> stream = Files.lines(Paths.get("indexData/"+lang.name().toLowerCase()+"_resource_dbpediaFile.txt"))) {
            stream.parallel().forEach(item -> {

                String[] c = item.toString().split("\t");

                String label = "";

                if (c.length == 3) {

                    label = c[0].toLowerCase();
                    String uri = c[1];
                    int freq = Integer.parseInt(c[2]);

                    String key = label + "\t" + uri;

                    if(mergedIndexMap.containsKey(label)){
                        Map<String, Integer> uri2Freq = mergedIndexMap.get(label);
                        
                        if(uri2Freq.containsKey(uri)){
                            freq = uri2Freq.get(uri) + freq;
                        }
                        
                        uri2Freq.put(uri, freq);
                        
                        mergedIndexMap.put(label, uri2Freq);
                    }
                    else{
                        Map<String, Integer> uri2Freq = new HashMap<>();
                        
                        uri2Freq.put(uri, freq);
                        
                        mergedIndexMap.put(label, uri2Freq);
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        writeIndex(mergedIndexMap, "rawFiles/"+lang.name().toLowerCase()+"/resourceFiles/merged.ttl");
    }
    
    private static void writeIndex(Map<String, Map<String, Integer>> indexMap, String filePath) {
        System.out.println("Saving the file size: " + indexMap.size());

        try {
            PrintStream p = new PrintStream(new File(filePath));
            int counter = 0;
            for (String s : indexMap.keySet()) {

                Map<String, Integer> uri2Freq = SortUtils.sortByValue(indexMap.get(s));
                
                int c = 0;
                for(String uri : uri2Freq.keySet()){
                    
                    String k = s +"\t"+uri+"\t"+uri2Freq.get(uri);
                    p.println(k);
                    c++;
                    
                    if(c == 10){
                        break;
                    }
                }
//                String k = s + "\t" + indexMap.get(s);
//                if (k.contains("hat is one small step 	Apollo_11	2")) {
//                    int z = 1;
//                }
//                p.println(k);

                counter++;

                if (counter % 700000 == 0) {
                    System.out.println((counter / (double) indexMap.size()) + " are saved.");
                }
            }

            System.out.println("\nFile saved.\n");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

}
