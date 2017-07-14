/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.utils;

import de.citec.sc.query.CandidateRetriever.Language;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.tagging.es.SpanishTagger;

/**
 *
 * @author sherzod
 * 
 * https://github.com/languagetool-org/languagetool
 * 
 * https://languagetool.org/
 */
public class Lemmatizer {

    private static GermanTagger germanTagger;
    private static EnglishTagger englishTagger;
    private static SpanishTagger spanishTagger;

    private static void load() {
        germanTagger = new GermanTagger();
        englishTagger = new EnglishTagger();
        spanishTagger = new SpanishTagger();
    }

    public static Set<String> lemmatize(String token, Language lang) {
        Set<String> lemmas = new HashSet<>();

        if(englishTagger == null){
            load();
        }
        
        try {

            List<String> tokens = new ArrayList<>();
            tokens.add(token);

            BaseTagger tagger = null;

            switch (lang) {
                case EN:
                    tagger = englishTagger;
                    break;
                case DE:
                    tagger = germanTagger;
                    break;
                case ES:
                    tagger = spanishTagger;
                    break;
            }

            List<AnalyzedTokenReadings> tags = tagger.tag(tokens);

            for (AnalyzedTokenReadings a : tags) {
                List<AnalyzedToken> analyzedTokens = a.getReadings();

                for (AnalyzedToken a1 : analyzedTokens) {
                    
                    if(a1.getLemma() == null){
                        continue;
                    }
                    if(a1.getLemma().equals(token)){
                        continue;
                    }
                    
                    lemmas.add(a1.getLemma());
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Lemmatizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return lemmas;
    }
}
