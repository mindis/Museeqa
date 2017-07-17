/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.main.Main;
import de.citec.sc.query.CandidateRetriever;
import de.citec.sc.query.EmbeddingLexicon;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class EmbeddingLexiconTest {

    @Test
    public void test() {

        String s2 = "discovered";
        
        Main.lang = CandidateRetriever.Language.EN;
        
        EmbeddingLexicon.useEmbdeding(true);
        
        Set<String> props = EmbeddingLexicon.getProperties(s2, CandidateRetriever.Language.EN);
        
        Assert.assertEquals(true, props.contains("http://dbpedia.org/ontology/discoverer"));

    }
}
