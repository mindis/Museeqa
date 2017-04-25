/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.CandidateRetrieverOnLucene;
import de.citec.sc.query.CandidateRetrieverOnMemory;
import de.citec.sc.query.ManualLexicon;
import de.citec.sc.query.Search;
import de.citec.sc.utils.FileFactory;
import de.citec.sc.utils.ProjectConfiguration;
import de.citec.sc.wordNet.WordNetAnalyzer;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author sherzod
 */
public class UDPipe {

    private static JSONParser jsonParser;

    private static String requestUDPipeServer(String text, CandidateRetriever.Language lang) {
        String address = "";
        try {
            address = "http://lindat.mff.cuni.cz/services/udpipe/api/process?tokenizer&tagger&parser&data=" + URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UDPipe.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (lang.equals(CandidateRetriever.Language.EN)) {
            address = address + "&model=english-ud-2.0-conll17-170315";
        } else if (lang.equals(CandidateRetriever.Language.DE)) {
            address = address + "&model=german-ud-1.2-160523";
        } else if (lang.equals(CandidateRetriever.Language.ES)) {
            address = address + "&model=spanish-ud-2.0-conll17-170315";
        }

        DataOutputStream wr = null;

        try {

            URL url = new URL(address);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            // Send post request
            con.setDoOutput(true);
            wr = new DataOutputStream(con.getOutputStream());
            wr.flush();
            wr.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (con.getInputStream())));

            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }

            if (jsonParser == null) {
                jsonParser = new JSONParser();
            }
            JSONObject jObject = (JSONObject) jsonParser.parse(response.toString());
            String result = (String) jObject.get("result");
            return result;

        } catch (IOException ex) {
            Logger.getLogger(UDPipe.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(UDPipe.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                wr.close();
            } catch (IOException ex) {
                Logger.getLogger(UDPipe.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return "";
    }

    public static DependencyParse parse(String text, CandidateRetriever.Language lang) {
        DependencyParse parse = new DependencyParse();

        String result = requestUDPipeServer(text, lang);

//        System.out.println(result);
        String[] lines = result.split("\n");

        for (String l : lines) {
            if (!l.startsWith("#")) {

                String[] data = l.split("\t");
                Integer tokenID = Integer.parseInt(data[0]);
                String label = data[1];
                String pos = data[3];
                int beginPosition = text.indexOf(label);
                int endPosition = beginPosition + label.length();

                int parentNode = Integer.parseInt(data[6]);
                String depRelation = data[7];

                parse.addNode(tokenID, label, pos, beginPosition, endPosition);

                if (depRelation.equals("root")) {
                    parse.setHeadNode(tokenID);
                } else {
                    parse.addEdge(tokenID, parentNode, depRelation);
                }
            }
        }

        return parse;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println("Initialization process has started ....");

        CandidateRetriever retriever = new CandidateRetrieverOnLucene(true, "luceneIndexes/resourceIndex", "luceneIndexes/classIndex", "luceneIndexes/predicateIndex", "luceneIndexes/matollIndex");

        WordNetAnalyzer wordNet = new WordNetAnalyzer("src/main/resources/WordNet-3.0/dict");

        Search.load(retriever, wordNet);
        Search.useMatoll(ProjectConfiguration.useMatoll());

        ManualLexicon.useManualLexicon(ProjectConfiguration.useManualLexicon());

        DependencyParse parse1 = UDPipe.parse("Who was vice president of Barack Obama?", CandidateRetriever.Language.EN);

        System.out.println(parse1);
        System.out.println("After \n\n");
        parse1.mergeEdges();
        System.out.println(parse1);

        DependencyParse parse2 = UDPipe.parse("Wer war der Vizepräsident unter Barack Obama?", CandidateRetriever.Language.DE);

        System.out.println(parse2);
        DependencyParse parse3 = UDPipe.parse("¿Quién fué el vicepresidente de Barack Obama?", CandidateRetriever.Language.ES);

        System.out.println(parse3);
    }
}
