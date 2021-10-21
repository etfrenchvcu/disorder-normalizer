/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tool.util.HashListMap;
import tool.util.Mention;
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

    /**
     * Applies the sieve to the given string. Method must be implemented by classes extending Sieve.
     * Returns a CUI.
     * @param mention
     * @return cui
     * @throws Exception
     */
    public abstract String apply(Mention mention) throws Exception;

    public static List<String> getTerminologyNameCuis(Map<String, List<String>> nameToCuiListMap, String name) {
        var cui = nameToCuiListMap.containsKey(name) ? nameToCuiListMap.get(name) : null;
        return cui;
    }
    
    public static String getTerminologyNameCui(HashListMap nameToCuiListMap, String name) {
        return nameToCuiListMap.containsKey(name) && nameToCuiListMap.get(name).size() == 1 ? nameToCuiListMap.get(name).get(0) : "";        
    }
    
    /**
     * Successively checks for exact matches in the 1) set of already normalized CUIs, 2) train Terminology, and 3) standard Terminology.
     * @param name
     * @return
     */
    public String exactMatch(String name) {
        String cui = "";
        // Checks against names already normalized by multi-pass sieve
        cui = getTerminologyNameCui(normalizedNameToCuiListMap , name);
        if (!cui.equals("")) {
            return cui;
        }
        
        // Checks against names in training data
        cui = getTerminologyNameCui(trainTerminology.nameToCuiListMap, name);
        if (!cui.equals("")) {
            return cui;       
        }
        
        // Checks against names in standard terminology dictionary
        return getTerminologyNameCui(standardTerminology.nameToCuiListMap, name);               
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

    /**
     * Check the list of name permutations for a match in one of the dictionaries.
     * Return a unique list of matches.
     * @param namePermutations
     * @return cui
     */
    public String normalize(List<String> namePermutations) {
        List<String> cuis = new ArrayList<>();

        // Check each name permutation for a CUI match in the dictionary.
        for (String name : namePermutations) {
            var match = exactMatch(name);            
            if (!match.equals("") && !cuis.contains(match)) {
                cuis.add(match);
            }
        }

        // Return unique CUIs.
        var result = String.join(",", cuis);
        return result;
    }
    
}
