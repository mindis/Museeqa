package de.citec.sc.main;

import de.citec.sc.corpus.AnnotatedDocument;
import de.citec.sc.corpus.QALDCorpus;
import de.citec.sc.dudes.rdf.ExpressionFactory;
import de.citec.sc.dudes.rdf.RDFDUDES;
import de.citec.sc.index.DBpediaIndex;
import de.citec.sc.learning.QueryConstructor;
import static de.citec.sc.main.Pipeline.nelTemplates;
import static de.citec.sc.main.Pipeline.scorer;
import de.citec.sc.qald.QALDCorpusLoader;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetriever.Language;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import de.citec.sc.query.DBpediaLabelRetriever;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.template.QATemplateFactory;
import de.citec.sc.utils.DBpediaEndpoint;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.variable.State;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import learning.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger log = LogManager.getFormatterLogger();

    public static Language lang = Language.DE;
    
    public static void main(String[] args) {

        if (args.length > 0) {

        } else {

            args = new String[32];
            args[0] = "-d1";//query dataset
            args[1] = "qaldSubset";//qald6Train  qald6Test   qaldSubset
            args[2] = "-d2";  //test dataset
            args[3] = "qaldSubset";//qald6Train  qald6Test   qaldSubset
            args[4] = "-m1";//manual lexicon
            args[5] = "true";//true, false
            args[6] = "-m2";//matoll
            args[7] = "true";//true, false
            args[8] = "-e";//epochs
            args[9] = "" + 10;
            args[10] = "-s";//sampling steps
            args[11] = "" + 15;
            args[12] = "-k1";//top k samples to select from during training NEL
            args[13] = "" + 10;
            args[14] = "-k2";//top k samples to select from during training for QA
            args[15] = "" + 10;
            args[16] = "-l1";//top k samples to select from during testing for NEL
            args[17] = "" + 10;
            args[18] = "-l2";//top k samples to select from during testing for QA
            args[19] = "" + 10;
            args[20] = "-w1";//max word count - train
            args[21] = "" + 3;
            args[22] = "-w2";//max word count - test
            args[23] = "" + 3;
            args[24] = "-i";//index
            args[25] = "lucene";//lucene, memory
            args[26] = "-l";//language
            args[27] = "EN";//EN,DE,ES
            args[28] = "-f";//language
            args[29] = "1,2,3,4,5";//1,2,3,4,5,6,7
            args[30] = "-b";// use embedding
            args[31] = "true"; // true, false
        }
        
//        int cores = Runtime.getRuntime().availableProcessors();
//        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", (cores - 5) + "");

        ProjectConfiguration.loadConfigurations(args);

        lang = Language.valueOf(ProjectConfiguration.getLanguage());

        log.info(ProjectConfiguration.getAllParameters());

        System.out.println(ProjectConfiguration.getAllParameters());
        
//        DBpediaEndpoint.loadCachedQueries();

        //load index, initialize postag lists etc.        
        initialize();

        //load training and testing corpus
        List<AnnotatedDocument> trainDocuments = getDocuments(QALDCorpusLoader.Dataset.valueOf(ProjectConfiguration.getTrainingDatasetName()), ProjectConfiguration.getTrainMaxWordCount());
        List<AnnotatedDocument> testDocuments = getDocuments(QALDCorpusLoader.Dataset.valueOf(ProjectConfiguration.getTestDatasetName()), ProjectConfiguration.getTestMaxWordCount());

        System.out.println("Training on " + ProjectConfiguration.getTrainingDatasetName() + " with " + trainDocuments.size());
        System.out.println("Testing on " + ProjectConfiguration.getTestDatasetName() + " with " + testDocuments.size());

        boolean trainOnly = true;
        if (trainOnly) {
            //train and test model
            try {
                List<Model<AnnotatedDocument, State>> trainedModels = Pipeline.train(trainDocuments);

                for (Model<AnnotatedDocument, State> m1 : trainedModels) {
                    if (trainedModels.indexOf(m1) == 0) {
                        m1.saveModelToFile("models", "model_nel_" + ProjectConfiguration.getLanguage() + "_" + ProjectConfiguration.getTrainMaxWordCount() + "_" + ProjectConfiguration.getNumberOfEpochs());
                    }
                    if (trainedModels.indexOf(m1) == 1) {
                        m1.saveModelToFile("models", "model_qa_" + ProjectConfiguration.getLanguage() + "_" + ProjectConfiguration.getTrainMaxWordCount() + "_" + ProjectConfiguration.getNumberOfEpochs());
                    }
                }

                Pipeline.test(trainedModels, testDocuments);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //train and test model
            try {
                List<Model<AnnotatedDocument, State>> trainedModels = new ArrayList<>();

                QATemplateFactory factory = new QATemplateFactory();

                Model<AnnotatedDocument, State> modelNEL = new Model<>(Pipeline.scorer, Pipeline.nelTemplates);
                Model<AnnotatedDocument, State> modelQA = new Model<>(Pipeline.scorer, Pipeline.qaTemplates);

                modelNEL.loadModelFromDir("models/model_nel_" + ProjectConfiguration.getLanguage() + "_" + ProjectConfiguration.getTrainMaxWordCount() + "_" + ProjectConfiguration.getNumberOfEpochs(), factory);
                modelQA.loadModelFromDir("models/model_qa_" + ProjectConfiguration.getLanguage() + "_" + ProjectConfiguration.getTrainMaxWordCount() + "_" + ProjectConfiguration.getNumberOfEpochs(), factory);

                trainedModels.add(modelNEL);
                trainedModels.add(modelQA);

                Pipeline.test(trainedModels, testDocuments);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
//        DBpediaEndpoint.saveCachedQueries();

    }

    private static void initialize() {

        System.out.println("Initialization process has started ....");

        CandidateRetriever retriever = null;

        if (ProjectConfiguration.getIndex().equals("lucene")) {
            retriever = new CandidateRetrieverOnLucene(false, "luceneIndex");
        } else {
            retriever = new CandidateRetrieverOnMemory("rawFiles");
        }

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);
        Search.useMatoll(ProjectConfiguration.useMatoll());

        ManualLexicon.useManualLexicon(ProjectConfiguration.useManualLexicon());

        System.out.println("Testing index: " + retriever.getAllResources("john f. kennedy", 10, CandidateRetriever.Language.EN));
        System.out.println("Testing index: " + retriever.getAllResources("goofy", 10, CandidateRetriever.Language.DE));
        System.out.println("Testing index: " + retriever.getAllPredicates("erfunden", 10, CandidateRetriever.Language.DE));
        System.out.println("Testing manual: " + ManualLexicon.getProperties("erfunden", lang));

        //semantic types to sample from
        Map<Integer, String> semanticTypes = new LinkedHashMap<>();
        semanticTypes.put(1, "Property");
        semanticTypes.put(2, "Individual");
        semanticTypes.put(3, "Class");
        semanticTypes.put(4, "RestrictionClass");
//        semanticTypes.put(5, "UnderSpecifiedClass");

        //semantic types with special meaning
        Map<Integer, String> specialSemanticTypes = new LinkedHashMap<>();
        specialSemanticTypes.put(semanticTypes.size() + 1, "What");//it should be higher than semantic type size
        specialSemanticTypes.put(semanticTypes.size() + 2, "Which");//it should be higher than semantic type size
//        specialSemanticTypes.put(semanticTypes.size() + 3, "When");//it should be higher than semantic type size
        specialSemanticTypes.put(semanticTypes.size() + 4, "Who");//it should be higher than semantic type size
//        specialSemanticTypes.put(semanticTypes.size() + 5, "HowMany");//it should be higher than semantic type size

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
//        qaValidPOSTags.add("PROPN");
        qaValidPOSTags.add("VERB");
        qaValidPOSTags.add("NOUN");
        qaValidPOSTags.add("ADJ");
//        qaValidPOSTags.add("ADV");
//        qaValidPOSTags.add("ADP");
        
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
        edges.add("nsubj:pass");
        edges.add("acl:relcl");
        edges.add("csubj");
        edges.add("csubjpass");
        edges.add("csubj:pass");
        edges.add("nmod:poss");
        edges.add("ccomp");
        edges.add("nmod");
        edges.add("amod");
        edges.add("xcomp");
        edges.add("vocative");
        edges.add("discourse");
        edges.add("parataxis");
        edges.add("advmod");
        edges.add("flat");
        edges.add("name");
        edges.add("discourse");

        DBpediaLabelRetriever.load(Main.lang);
        
        Pipeline.initialize(linkingValidPOSTags, qaValidPOSTags, semanticTypes, specialSemanticTypes, edges);

        QueryConstructor.initialize(specialSemanticTypes, semanticTypes, linkingValidPOSTags, edges);

        System.out.println("Initialization process has ended ....");
    }

    private static List<AnnotatedDocument> getDocuments(QALDCorpusLoader.Dataset dataset, int maxWordCount) {

        boolean includeYAGO = false;
        boolean includeAggregation = false;
        boolean includeUNION = false;
        boolean onlyDBO = true;    
        boolean isHybrid = false;

        QALDCorpus corpus = QALDCorpusLoader.load(dataset, includeYAGO, includeAggregation, includeUNION, onlyDBO, isHybrid);

        List<AnnotatedDocument> documents = new ArrayList<>();

        for (AnnotatedDocument d1 : corpus.getDocuments()) {

            if (DBpediaEndpoint.isValidQuery(d1.getGoldQueryString(), false)) {

                if (d1.getParse() != null) {
                    String before = d1.getParse().toString();
                    
                    d1.getParse().mergeEdges();
                    d1.getParse().removeLoops();
                    d1.getParse().removePunctuations();
                    
                    String after = d1.getParse().toString();

//                    if (!before.equals(after)) {
//                        System.out.println("Before:\n" + before);
//                        System.out.println("\nAfter:\n" + d1.getParse());
//                        System.out.println("\n=============================================================================\n");
//                    }

                    if (d1.getParse().getNodes().size() <= maxWordCount) {
                        documents.add(d1);
                    }
                }

            } else {
                System.out.println("Invalid query: " + d1.getQuestionString() + " Query: " + d1.getGoldQueryString().replace("\n", " "));
            }
        }
        

        System.out.print("Loaded dataset : " + dataset + " with " + documents.size() + " instances.");

        return documents;
    }
}
