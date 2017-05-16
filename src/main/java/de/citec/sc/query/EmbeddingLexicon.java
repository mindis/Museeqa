/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.query;

import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.utils.FileFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class EmbeddingLexicon {

    private static HashMap<String, HashMap<String, Double>> lexiconPropertiesEN;
//    private static HashMap<String, HashMap<String, Double>> lexiconClassesEN;
    private static HashMap<String, HashMap<String, Double>> lexiconPropertiesES;
//    private static HashMap<String, HashMap<String, Double>> lexiconClassesES;
    private static HashMap<String, HashMap<String, Double>> lexiconPropertiesDE;
//    private static HashMap<String, HashMap<String, Double>> lexiconClassesDE;
    
    
    

    public static boolean useEmbedding = false;

    private static boolean loaded = false;

    public static void useEmbedding(boolean b) {
        useEmbedding = b;
        if (b) {
            load();
        }
    }
    
    public static void main(String[] args) {
        System.out.println(getProperties("creador", Language.ES));
    }

    private static void load() {
        
//        lexiconClassesEN = new HashMap<>();
//        lexiconClassesES = new HashMap<>();
//        lexiconClassesDE = new HashMap<>();
        lexiconPropertiesEN = new HashMap<>();
        lexiconPropertiesES = new HashMap<>();
        lexiconPropertiesDE = new HashMap<>();
        
        Set<String> contentDE = FileFactory.readFile("w2v_output/w2v-de-100-plain-exclude-stopwords=True-ranking.txt");
        Set<String> contentEN = FileFactory.readFile("w2v_output/w2v-en-100-plain-exclude-stopwords=True-ranking.txt");
        Set<String> contentES = FileFactory.readFile("w2v_output/w2v-es-100-plain-exclude-stopwords=True-ranking.txt");
        
        for(String c : contentDE){
            //soccer players	http://dbpedia.org/ontology/SoccerLeague	0.988897	class_original, datatype_original, object_original
            String[] data = c.split("\t");
            
            String label = data[0].toLowerCase();
            String uri = data[1];
            Double score = Double.parseDouble(data[2]);
            String type = data[3];
            
            if(type.startsWith("class")){
                continue;
            }
            
            if(lexiconPropertiesDE.containsKey(label)){
                HashMap<String, Double> addedMap = lexiconPropertiesDE.get(label);
                addedMap.put(uri, score);
                
                lexiconPropertiesDE.put(label, addedMap);
            }
            else{
                HashMap<String, Double> addedMap = new LinkedHashMap<>();
                addedMap.put(uri, score);
                
                lexiconPropertiesDE.put(label, addedMap);
            }
        }
        
        
        for(String c : contentES){
            //soccer players	http://dbpedia.org/ontology/SoccerLeague	0.988897	class_original, datatype_original, object_original
            String[] data = c.split("\t");
            
            String label = data[0].toLowerCase();
            String uri = data[1];
            Double score = Double.parseDouble(data[2]);
            String type = data[3];
            
            if(type.startsWith("class")){
                continue;
            }
            
            if(lexiconPropertiesES.containsKey(label)){
                HashMap<String, Double> addedMap = lexiconPropertiesES.get(label);
                addedMap.put(uri, score);
                
                lexiconPropertiesES.put(label, addedMap);
            }
            else{
                HashMap<String, Double> addedMap = new LinkedHashMap<>();
                addedMap.put(uri, score);
                
                lexiconPropertiesES.put(label, addedMap);
            }
        }
        
        
        for(String c : contentEN){
            //soccer players	http://dbpedia.org/ontology/SoccerLeague	0.988897	class_original, datatype_original, object_original
            String[] data = c.split("\t");
            
            String label = data[0].toLowerCase();
            String uri = data[1];
            Double score = Double.parseDouble(data[2]);
            String type = data[3];
            
            if(type.startsWith("class")){
                continue;
            }
            
            if(lexiconPropertiesEN.containsKey(label)){
                HashMap<String, Double> addedMap = lexiconPropertiesEN.get(label);
                addedMap.put(uri, score);
                
                lexiconPropertiesEN.put(label, addedMap);
            }
            else{
                HashMap<String, Double> addedMap = new LinkedHashMap<>();
                addedMap.put(uri, score);
                
                lexiconPropertiesEN.put(label, addedMap);
            }
        }

        loaded = true;

    }

    private static void addLexicon(String key, String value, HashMap<String, Set<String>> map) {

        key = key.toLowerCase().trim();
        value = value.trim();

        if (map.containsKey(key)) {
            Set<String> set = map.get(key);
            set.add(value);
            map.put(key, set);
        } else {
            Set<String> set = new HashSet<>();
            set.add(value);
            map.put(key, set);
        }
    }

    public static HashMap<String, Double> getProperties(String term, Language lang) {

        term = term.toLowerCase();

        if (!loaded) {
            load();
        }

        switch (lang) {
            case EN:
                if (lexiconPropertiesEN.containsKey(term)) {
                    return lexiconPropertiesEN.get(term);
                }
                break;
            case DE:
                if (lexiconPropertiesDE.containsKey(term)) {
                    return lexiconPropertiesDE.get(term);
                }
                break;
            case ES:
                if (lexiconPropertiesES.containsKey(term)) {
                    return lexiconPropertiesES.get(term);
                }
                break;
            
        }
        return new HashMap<>();
    }


    
}
