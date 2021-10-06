/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tool.util.HashListMap;
import tool.util.Terminology;

/**
 *
 * @author
 */
public abstract class Sieve {

    Terminology standardTerminology; 
    Terminology trainTerminology;
    HashListMap normalizedNameToCuiListMap;

    /**
     * Abstract constructor.
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     */
    public Sieve(Terminology standardTerminology, Terminology trainTerminology, HashListMap normalizedNameToCuiListMap) {
        this.standardTerminology = standardTerminology;
        this.trainTerminology = trainTerminology;
        this.normalizedNameToCuiListMap = normalizedNameToCuiListMap;
    }

    public static List<String> getTerminologyNameCuis(Map<String, List<String>> nameToCuiListMap, String name) {
        return nameToCuiListMap.containsKey(name) ? nameToCuiListMap.get(name) : null;
    }
    
    public static String getTerminologyNameCui(HashListMap nameToCuiListMap, String name) {
        return nameToCuiListMap.containsKey(name) && nameToCuiListMap.get(name).size() == 1 ? nameToCuiListMap.get(name).get(0) : "";        
    }
    
    public String exactMatch(String name) {
        String cui = "";
        //checks against names normalized by multi-pass sieve
        cui = getTerminologyNameCui(normalizedNameToCuiListMap , name);
        if (!cui.equals("")) {
            return cui;
        }
        
        //checks against names in training data
        cui = getTerminologyNameCui(trainTerminology.nameToCuiListMap, name);
        if (!cui.equals("")) {
            return cui;       
        }
        
        //checks against names in dictionary
        return getTerminologyNameCui(standardTerminology.nameToCuiListMap, name);               
    }

    public List<String> getAlternateCuis(String cui) {
        List<String> alternateCuis = new ArrayList<>();
        if (trainTerminology.cuiAlternateCuiMap.containsKey(cui)) {
            alternateCuis.addAll(trainTerminology.cuiAlternateCuiMap.get(cui));
        }
        if (standardTerminology.cuiAlternateCuiMap.containsKey(cui)) {
            alternateCuis.addAll(standardTerminology.cuiAlternateCuiMap.get(cui));
        }
        return alternateCuis;
    } 
 
    // Original implementation
    // public static String normalize(List<String> namesKnowledgeBase) {
    //     for (String name : namesKnowledgeBase) {
    //         String cui = exactMatchSieve(name);            
    //         if (!cui.equals(""))
    //             return cui;
    //     }
    //     return "";
    // }

    // TODO: Revisit to make sure this is actually what I want to do
    // Updated: Only return unambiguous CUIs
    public static String normalize(List<String> namesKnowledgeBase) {
        int matches = 0;
        String cui = "";
        for (String name : namesKnowledgeBase) {
            cui = exactMatch(name);            
            if (!cui.equals(""))
                // return cui;
                matches++;
        }
        if (matches==1)
            return cui;
        return "";
    }
    
}
