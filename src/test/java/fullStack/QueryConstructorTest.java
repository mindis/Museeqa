/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.evaluator.QueryEvaluator;
import de.citec.sc.learning.QueryConstructor;
import de.citec.sc.main.Main;
import de.citec.sc.parser.DependencyParse;
import de.citec.sc.qald.QALDCorpusLoader;
import de.citec.sc.qald.Question;
import de.citec.sc.query.Candidate;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.variable.State;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class QueryConstructorTest {

//    public static void main(String[] args){
//    @Ignore
    @Test
    public void test1() {

        //semantic types to sample from
        Map<Integer, String> semanticTypes = new LinkedHashMap<>();
        semanticTypes.put(1, "Property");
        semanticTypes.put(2, "Individual");
        semanticTypes.put(3, "Class");
        semanticTypes.put(4, "RestrictionClass");

        //semantic types with special meaning
        Map<Integer, String> specialSemanticTypes = new LinkedHashMap<>();
        specialSemanticTypes.put(5, "What");//it should be higher than semantic type size

        Set<String> validPOSTags = new HashSet<>();
        validPOSTags.add("NN");
        validPOSTags.add("NNP");
        validPOSTags.add("NNS");
        validPOSTags.add("NNPS");
        validPOSTags.add("VBZ");
        validPOSTags.add("VBN");
        validPOSTags.add("VBD");
        validPOSTags.add("VBG");
        validPOSTags.add("VBP");
        validPOSTags.add("VB");
        validPOSTags.add("JJ");

        Set<String> frequentWordsToExclude = new HashSet<>();
        //all of this words have a valid POSTAG , so they shouldn't be assigned any URI to these tokens
        frequentWordsToExclude.add("is");
        frequentWordsToExclude.add("was");
        frequentWordsToExclude.add("were");
        frequentWordsToExclude.add("are");
        frequentWordsToExclude.add("do");
        frequentWordsToExclude.add("does");
        frequentWordsToExclude.add("did");
        frequentWordsToExclude.add("give");
        frequentWordsToExclude.add("list");
        frequentWordsToExclude.add("show");
        frequentWordsToExclude.add("me");
        frequentWordsToExclude.add("many");
        frequentWordsToExclude.add("have");
        frequentWordsToExclude.add("belong");

        //these words can have special semantic type
        Set<String> wordsWithSpecialSemanticTypes = new HashSet<>();
        wordsWithSpecialSemanticTypes.add("which");
        wordsWithSpecialSemanticTypes.add("what");
        wordsWithSpecialSemanticTypes.add("who");
        wordsWithSpecialSemanticTypes.add("whom");
        wordsWithSpecialSemanticTypes.add("how");
        wordsWithSpecialSemanticTypes.add("when");
        wordsWithSpecialSemanticTypes.add("where");
        
        Set<String> edges = new HashSet<>();
        edges.add("obj");
        edges.add("obl");
        edges.add("flat");
        edges.add("compound");
        edges.add("nummod");
        edges.add("appos");
        edges.add("subj");
        edges.add("nsubj");
        edges.add("dobj");
        edges.add("iobj");
        edges.add("nsubjpass");
        edges.add("csubj");
        edges.add("csubjpass");
        edges.add("nmod:poss");
        edges.add("ccomp");
        edges.add("nmod");
        edges.add("amod");
        edges.add("xcomp");
        edges.add("vocative");
        
        Main.lang = Language.EN;

        QueryConstructor.initialize(specialSemanticTypes, semanticTypes, validPOSTags, edges);

        /**
         * Text: Who created Family_Guy Nodes : 1 2 3 Edges: (1,2 = subj) (3,2 =
         * dobj)
         */
        DependencyParse parseTree = new DependencyParse();
        parseTree.addNode(1, "Who", "WDT", -1, -1);
        parseTree.addNode(2, "created", "VBP", -1, -1);
        parseTree.addNode(3, "Family_Guy", "NNP", -1, -1);

        parseTree.addEdge(1, 2, "subj");
        parseTree.addEdge(3, 2, "obj");

        parseTree.setHeadNode(2);

        Map<Language, String> qMap = new HashMap<Language, String>();
        qMap.put(Language.EN, "Who created Family_Guy");
        
        Question qaldInstance = new Question(qMap, "SELECT DISTINCT ?uri WHERE { <http://dbpedia.org/resource/Family_Guy> <http://dbpedia.org/ontology/creator> ?uri . }  ");
        qaldInstance.setId("1");

        AnnotatedDocument doc = new AnnotatedDocument(parseTree, qaldInstance);

        State state = new State(doc);

        Candidate c3 = new Candidate("http://dbpedia.org/resource/Family_Guy", 0, 0, 0, null, null, null, null, null);
        Candidate c2 = new Candidate("http://dbpedia.org/ontology/creator", 0, 0, 0, null, null, null, null, null);
        Candidate c1 = new Candidate("EMPTY_STRING", 0, 0, 0, null, null, null, null, null);

        state.addHiddenVariable(1, 5, c1);
        state.addHiddenVariable(2, 1, c2);
        state.addHiddenVariable(3, 2, c3);

        state.addSlotVariable(3, 2, 1);
        state.addSlotVariable(1, 2, 2);

        System.out.println(state.toString());

        String query = QueryConstructor.getSPARQLQuery(state);

        System.out.println("Constructed Query : \n" + query);

        String expectedQuery = "SELECT DISTINCT ?uri WHERE { <http://dbpedia.org/resource/Family_Guy> <http://dbpedia.org/ontology/creator> ?uri . }  ";

        double simScore = QueryEvaluator.evaluate(query, expectedQuery);

        System.out.println("Similarity score to expected query: " + simScore);

        Assert.assertEquals(1.0, simScore);

    }

    
    @Test
    public void test2() {

        //semantic types to sample from
        Map<Integer, String> semanticTypes = new LinkedHashMap<>();
        semanticTypes.put(1, "Property");
        semanticTypes.put(2, "Individual");
        semanticTypes.put(3, "Class");
        semanticTypes.put(4, "RestrictionClass");

        //semantic types with special meaning
        Map<Integer, String> specialSemanticTypes = new LinkedHashMap<>();
        specialSemanticTypes.put(5, "What");//it should be higher than semantic type size

        Set<String> validPOSTags = new HashSet<>();
        validPOSTags.add("NN");
        validPOSTags.add("NNP");
        validPOSTags.add("NNS");
        validPOSTags.add("NNPS");
        validPOSTags.add("VBZ");
        validPOSTags.add("VBN");
        validPOSTags.add("VBD");
        validPOSTags.add("VBG");
        validPOSTags.add("VBP");
        validPOSTags.add("VB");
        validPOSTags.add("JJ");

        Set<String> frequentWordsToExclude = new HashSet<>();
        //all of this words have a valid POSTAG , so they shouldn't be assigned any URI to these tokens
        frequentWordsToExclude.add("is");
        frequentWordsToExclude.add("was");
        frequentWordsToExclude.add("were");
        frequentWordsToExclude.add("are");
        frequentWordsToExclude.add("do");
        frequentWordsToExclude.add("does");
        frequentWordsToExclude.add("did");
        frequentWordsToExclude.add("give");
        frequentWordsToExclude.add("list");
        frequentWordsToExclude.add("show");
        frequentWordsToExclude.add("me");
        frequentWordsToExclude.add("many");
        frequentWordsToExclude.add("have");
        frequentWordsToExclude.add("belong");

        //these words can have special semantic type
        Set<String> wordsWithSpecialSemanticTypes = new HashSet<>();
        wordsWithSpecialSemanticTypes.add("which");
        wordsWithSpecialSemanticTypes.add("what");
        wordsWithSpecialSemanticTypes.add("who");
        wordsWithSpecialSemanticTypes.add("whom");
        wordsWithSpecialSemanticTypes.add("how");
        wordsWithSpecialSemanticTypes.add("when");
        wordsWithSpecialSemanticTypes.add("where");
        
        Set<String> edges = new HashSet<>();
        edges.add("obj");
        edges.add("obl");
        edges.add("flat");
        edges.add("compound");
        edges.add("nummod");
        edges.add("appos");
        edges.add("subj");
        edges.add("nsubj");
        edges.add("dobj");
        edges.add("iobj");
        edges.add("nsubjpass");
        edges.add("csubj");
        edges.add("csubjpass");
        edges.add("nmod:poss");
        edges.add("ccomp");
        edges.add("nmod");
        edges.add("amod");
        edges.add("xcomp");
        edges.add("vocative");

        QueryConstructor.initialize(specialSemanticTypes, semanticTypes, validPOSTags, edges);

        /**
         * Text: Who created Family_Guy Nodes : 1 2 3 Edges: (1,2 = subj) (3,2 =
         * dobj)
         */
        DependencyParse parseTree = new DependencyParse();
        parseTree.addNode(1, "How", "WRB", -1, -1);
        parseTree.addNode(2, "tall", "JJ", -1, -1);
        parseTree.addNode(3, "is", "VBZ", -1, -1);
        parseTree.addNode(5, "Michael Jordan", "NNP", -1, -1);

        parseTree.addEdge(1, 2, "advmod");
        parseTree.addEdge(2, 3, "dep");
        parseTree.addEdge(5, 3, "nsubj");

        parseTree.setHeadNode(3);
        
        Main.lang = Language.EN;

        Map<Language, String> qMap = new HashMap<Language, String>();
        qMap.put(Language.EN, "How tall is Michael Jordan?");
        
        Question qaldInstance = new Question(qMap, "SELECT DISTINCT ?num WHERE {  <http://dbpedia.org/resource/Michael_Jordan> <http://dbpedia.org/ontology/height> ?num . } ");
        qaldInstance.setId("1");

        AnnotatedDocument doc = new AnnotatedDocument(parseTree, qaldInstance);

        State state = new State(doc);

        Candidate c3 = new Candidate("http://dbpedia.org/resource/Michael_Jordan", 0, 0, 0, null, null, null, null, null);
        Candidate c2 = new Candidate("http://dbpedia.org/ontology/height", 0, 0, 0, null, null, null, null, null);
        Candidate c1 = new Candidate("EMPTY_STRING", 0, 0, 0, null, null, null, null, null);

        state.addHiddenVariable(1, 5, c1);
        state.addHiddenVariable(2, 1, c2);
        state.addHiddenVariable(3, -1, c1);
        state.addHiddenVariable(5, 2, c3);

        state.addSlotVariable(1, 2, 2);
        state.addSlotVariable(5, 2, 1);

        System.out.println(state.toString());

        String query = QueryConstructor.getSPARQLQuery(state);

        System.out.println("Constructed Query : \n" + query);

        String expectedQuery = "SELECT DISTINCT ?num WHERE {  <http://dbpedia.org/resource/Michael_Jordan> <http://dbpedia.org/ontology/height> ?num . }";

        double simScore = QueryEvaluator.evaluate(query, expectedQuery);

        System.out.println("Similarity score to expected query: " + simScore);

        Assert.assertEquals(1.0, simScore);

    }

    
    @Test
    public void test3() {

        //semantic types to sample from
        Map<Integer, String> semanticTypes = new LinkedHashMap<>();
        semanticTypes.put(1, "Property");
        semanticTypes.put(2, "Individual");
        semanticTypes.put(3, "Class");
        semanticTypes.put(4, "RestrictionClass");

        //semantic types with special meaning
        Map<Integer, String> specialSemanticTypes = new LinkedHashMap<>();
        specialSemanticTypes.put(5, "HowMany");//it should be higher than semantic type size

        Set<String> linkingValidPOSTags = new HashSet<>();
        linkingValidPOSTags.add("PROPN");
        linkingValidPOSTags.add("VERB");
        linkingValidPOSTags.add("NOUN");
        linkingValidPOSTags.add("ADJ");
        linkingValidPOSTags.add("ADV");
        linkingValidPOSTags.add("ADP");
        
        Set<String> qaValidPOSTags = new HashSet<>();
        qaValidPOSTags.add("PRON");
        qaValidPOSTags.add("DET");
        qaValidPOSTags.add("VERB");
        qaValidPOSTags.add("NOUN");
        qaValidPOSTags.add("ADJ");

        Set<String> edges = new HashSet<>();
        edges.add("obj");
        edges.add("obl");
        edges.add("flat");
        edges.add("compound");
        edges.add("nummod");
        edges.add("appos");
        edges.add("subj");
        edges.add("nsubj");
        edges.add("dobj");
        edges.add("iobj");
        edges.add("nsubjpass");
        edges.add("csubj");
        edges.add("csubjpass");
        edges.add("nmod:poss");
        edges.add("ccomp");
        edges.add("nmod");
        edges.add("amod");
        edges.add("xcomp");
        edges.add("vocative");

        QueryConstructor.initialize(specialSemanticTypes, semanticTypes, linkingValidPOSTags, edges);

        /**
         * Text: Who created Family_Guy Nodes : 1 2 3 Edges: (1,2 = subj) (3,2 =
         * dobj)
         */
        DependencyParse parseTree = new DependencyParse();
        parseTree.addNode(1, "How", "ADV", -1, -1);
        parseTree.addNode(2, "many", "ADJ", -1, -1);
        parseTree.addNode(3, "movies", "NOUN", -1, -1);
        parseTree.addNode(4, "did", "AUX", -1, -1);
        parseTree.addNode(5, "Michael Jordan", "NNP", -1, -1);
        parseTree.addNode(6, "direct", "VERB", -1, -1);

        parseTree.addEdge(1, 2, "advmod");
        parseTree.addEdge(2, 3, "amod");
        parseTree.addEdge(3, 6, "obj");
        parseTree.addEdge(4, 6, "aux");
        parseTree.addEdge(5, 6, "subj");

        parseTree.setHeadNode(6);

        
        
        Main.lang = Language.EN;

        QueryConstructor.initialize(specialSemanticTypes, semanticTypes, linkingValidPOSTags, edges);

        Map<Language, String> qMap = new HashMap<Language, String>();
        qMap.put(Language.EN, "How many movies did Michael Jordan direct?");
        
        Question qaldInstance = new Question(qMap, "SELECT COUNT(DISTINCT ?uri) WHERE { ?uri <http://dbpedia.org/ontology/director> <http://dbpedia.org/resource/Michael_Jordan> . }");
        qaldInstance.setId("1");

        AnnotatedDocument doc = new AnnotatedDocument(parseTree, qaldInstance);

        State state = new State(doc);

        Candidate c3 = new Candidate("http://dbpedia.org/resource/Michael_Jordan", 0, 0, 0, null, null, null, null, null);
        Candidate c2 = new Candidate("http://dbpedia.org/ontology/director", 0, 0, 0, null, null, null, null, null);
        Candidate c1 = new Candidate("EMPTY_STRING", 0, 0, 0, null, null, null, null, null);

        state.addHiddenVariable(5, 2, c3);
        state.addHiddenVariable(6, 1, c2);
        state.addHiddenVariable(2, 5, c1);
        state.addHiddenVariable(1, -1, c1);
        state.addHiddenVariable(4, -1, c1);
        state.addHiddenVariable(3, -1, c1);

        state.addSlotVariable(5, 6, 2);
        state.addSlotVariable(2, 6, 1);

        System.out.println(state.toString());

        String query = QueryConstructor.getSPARQLQuery(state);

        System.out.println("Constructed Query : \n" + query);

        String expectedQuery = "SELECT COUNT(DISTINCT ?uri) WHERE { ?uri <http://dbpedia.org/ontology/director> <http://dbpedia.org/resource/Michael_Jordan> . }";

        double simScore = QueryEvaluator.evaluate(query, expectedQuery);

        System.out.println("Similarity score to expected query: " + simScore);

        Assert.assertEquals(1.0, simScore);

    }
}
