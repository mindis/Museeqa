/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fullStack;

import de.citec.sc.evaluator.AnswerEvaluator;
import de.citec.sc.evaluator.QueryEvaluator;
import de.citec.sc.learning.NELObjectiveFunction;
import de.citec.sc.learning.QAObjectiveFunction;
import de.citec.sc.learning.QueryTypeObjectiveFunction;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author sherzod
 */
public class ObjectiveFunctionTest {

    @Test
    public void test() {

        NELObjectiveFunction function1 = new NELObjectiveFunction();
        QAObjectiveFunction function2 = new QAObjectiveFunction();
        QueryTypeObjectiveFunction function3 = new QueryTypeObjectiveFunction();

        String q1 = "SELECT COUNT(DISTINCT ?uri) WHERE {  <http://dbpedia.org/resource/Goofy> <http://dbpedia.org/ontology/creator> ?uri . }";
        String q2 = "\n" +
"SELECT DISTINCT  ?v8\n" +
"WHERE\n" +
"  { ?v2  <http://dbpedia.org/ontology/creator>  ?v8}";

        double score1 = function1.computeValue(q1, q2);
        double score2 = function2.computeValue(q1, q2);
        double score3 = function3.computeValue(q1, q2);

        System.out.println("Sim score NEL: " + score1);
        System.out.println("Sim score QA: " + score2);
        System.out.println("Sim score Query Type: " + score3);

        Assert.assertEquals(true, score1 >= 0.5);
        Assert.assertEquals(true, score2 > 0.5);

    }
}
