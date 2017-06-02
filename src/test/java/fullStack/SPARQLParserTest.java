/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.qald.SPARQLParser;
import de.citec.sc.utils.DBpediaEndpoint;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class SPARQLParserTest {

    @Test
    public void test() {
        
        String query = "SELECT DISTINCT ?v10 WHERE {  <http://dbpedia.org/resource/Wikipedia> <http://dbpedia.org/ontology/author> ?v10 . ?v10 <http://dbpedia.org/ontology/author2> ?v3. }";
        
        String query2 = "SELECT DISTINCT ?v1 WHERE {  <http://dbpedia.org/resource/Wikipedia> <http://dbpedia.org/ontology/author> ?v1 . ?v1 <http://dbpedia.org/ontology/author2> ?v3. }";
        
        
        long start2 = System.currentTimeMillis();
        boolean q1 = DBpediaEndpoint.isValidQuery(query, true);
        long end2 = System.currentTimeMillis();
        
        long start = System.currentTimeMillis();
        String c1 = DBpediaEndpoint.getCanonicalForm(query);
        long end = System.currentTimeMillis();
        
        
        boolean q2 = DBpediaEndpoint.isValidQuery(query, true);
        
        String c2 = DBpediaEndpoint.getCanonicalForm(query2);
        
        System.out.println(c1);
        System.out.println(c2);
        System.out.println(c1.equals(c2));
        
        System.out.println((end-start));
        System.out.println((end2-start2));
        
//        List<String> uris = SPARQLParser.extractURIsFromQuery("SELECT DISTINCT ?uri WHERE {  <http://dbpedia.org/resource/Wikipedia> <http://dbpedia.org/ontology/author> ?uri . } ");
//
//        System.out.println("Extracted URIs: " + uris);
//        Assert.assertEquals(true, uris.contains("http://dbpedia.org/resource/Wikipedia"));
//        Assert.assertEquals(true, uris.contains("http://dbpedia.org/ontology/author"));

    }
}
