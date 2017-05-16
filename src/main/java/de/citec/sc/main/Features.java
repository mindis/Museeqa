/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sherzod
 */
public class Features {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
        
        Set<String> featureCombinations = new HashSet<>();
        
        for(Integer i1 : list){
            List<Integer> temp = new ArrayList<>();
            for(Integer i2: list){
                if(!i1.equals(i2)){
                    temp.add(i2);
                }
            }
            
            Collections.sort(temp);
            
            String s = "";
            for(Integer i2 : temp){
                if(temp.indexOf(i2) != temp.size()-1){
                    s+=i2+"=";
                }
                else{
                    s+=i2;
                }
            }
            
            featureCombinations.add(s);
        }
        
        featureCombinations.add("1=2=3=4=5=6=7");
        
        for(String s : featureCombinations){
            System.out.print(s +" ");
        }
    }
}
