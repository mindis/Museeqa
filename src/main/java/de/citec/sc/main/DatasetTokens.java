/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.qald.QALDCorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class DatasetTokens {

    public static void main(String[] args) {
        CandidateRetriever retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);
        Search.useMatoll(true);
        ManualLexicon.useManualLexicon(true);

        boolean includeYAGO = false;
        boolean includeAggregation = false;
        boolean includeUNION = false;
        boolean onlyDBO = true;
        boolean isHybrid = false;

        List<Language> languages = new ArrayList<>();
        languages.add(Language.EN);
        languages.add(Language.DE);
        languages.add(Language.ES);

        for (Language lang : languages) {

            Main.lang = lang;

            QALDCorpus trainCorpus = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Train, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);
            QALDCorpus testCorpus = QALDCorpusLoader.load(QALDCorpusLoader.Dataset.qald6Test, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

            List<AnnotatedDocument> trainDocuments = trainCorpus.getDocuments();
            List<AnnotatedDocument> testDocuments = testCorpus.getDocuments();

            Set<String> trainTokens = new HashSet<>();
            Set<String> testTokens = new HashSet<>();

            for (AnnotatedDocument doc : trainDocuments) {
                
                if (doc.getParse() == null) {
                    continue;
                }
                
                doc.getParse().mergeEdges();
                doc.getParse().removePunctuations();
                doc.getParse().removeLoops();

                

                for (Integer tokenID : doc.getParse().getNodes().keySet()) {
                    String token = doc.getParse().getNodes().get(tokenID);
                    if (token.length() > 2) {
                        trainTokens.add(token);
                    }
                }

            }
            for (AnnotatedDocument doc : testDocuments) {

                if (doc.getParse() == null) {
                    continue;
                }
                
                doc.getParse().mergeEdges();
                doc.getParse().removePunctuations();
                doc.getParse().removeLoops();
//                
                

                for (Integer tokenID : doc.getParse().getNodes().keySet()) {
                    String token = doc.getParse().getNodes().get(tokenID);
                    if (token.length() > 2) {
                        testTokens.add(token);
                    }
                }
                
                

            }

            FileFactory.writeListToFile(lang.name() + "_train_tokens.txt", trainTokens, false);
            FileFactory.writeListToFile(lang.name() + "_test_tokens.txt", testTokens, false);
        }
    }
}
