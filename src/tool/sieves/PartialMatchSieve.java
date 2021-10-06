/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.util.ArrayList;
import java.util.List;

import tool.util.Ling;
import tool.util.Mention;
import tool.util.Terminology;
import tool.util.Util;

/**
 *
 * @author
 */
public class PartialMatchSieve extends Sieve {    
    
    public static String apply(Mention concept) {
        //init(concept);
        String name = concept.getNameExpansion().equals("") ? concept.getNameExpansion() : concept.getName();
        String[] nameTokens = name.split("\\s+");
        List<String> cuiList = partialMatch(name, nameTokens);        
        return cuiList.size() == 1 ? cuiList.get(0) : "";
    }   
    
    private static List<String> partialMatch(String phrase, String[] phraseTokens) {
        
        List<String> cuiList = new ArrayList<>();
        List<String> prevPartialMatchedPhrases = new ArrayList<>();
                
        for (String phraseToken : phraseTokens) {
            if (Ling.getStopwordsList().contains(phraseToken))
                continue;
            List<String> candidatePhrases = null;
            int map = -1;
            
            if (Sieve.getTrainingDataTerminology().tokenToNameListMap.containsKey(phraseToken)) {
                candidatePhrases = new ArrayList<>(Sieve.getTrainingDataTerminology().tokenToNameListMap.get(phraseToken));
                map = 2;
            }
            else if (Sieve.getStandardTerminology().tokenToNameListMap.containsKey(phraseToken)) {
                candidatePhrases = new ArrayList<>(Sieve.getStandardTerminology().tokenToNameListMap.get(phraseToken));
                map = 3;
            }
            
            if (candidatePhrases == null)
                continue;
                        
            candidatePhrases.removeAll(prevPartialMatchedPhrases);
            
            if (map == 2 && candidatePhrases.isEmpty() && Sieve.getStandardTerminology().tokenToNameListMap.containsKey(phraseToken)) {
                candidatePhrases = new ArrayList<>(Sieve.getStandardTerminology().tokenToNameListMap.get(phraseToken));
                map = 3;                
            }            
            
            cuiList = exactTokenMatchCondition(phrase, candidatePhrases, map == 2 ? Sieve.getTrainingDataTerminology() : Sieve.getStandardTerminology(), cuiList);
            prevPartialMatchedPhrases = Util.addUnique(candidatePhrases, prevPartialMatchedPhrases);
        }        
        return cuiList;
    }    
    
    public static List<String> exactTokenMatchCondition(String phrase, List<String> candidatePhrases, Terminology terminology, List<String> cuiList) {
        for (String candidatePhrase : candidatePhrases) {
            if (!Ling.exactTokenMatch(candidatePhrase, phrase))
                continue;

            String cui = terminology.nameToCuiListMap.get(candidatePhrase).get(0);
            cuiList = Util.setList(cuiList, cui);
        }              
        return cuiList;
    }
            
}
