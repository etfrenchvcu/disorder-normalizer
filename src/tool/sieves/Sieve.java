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
    
    //state of this class
    private static Terminology standardTerminology; 
    private static Terminology trainingDataTerminology;

    public static void setStandardTerminology() throws IOException {
        standardTerminology.loadTerminology();
    }

    public static Terminology getStandardTerminology() {
        return standardTerminology;
    }
    
    public static void setTrainingDataTerminology(File train_dir) throws IOException {
        trainingDataTerminology.loadTrainingDataTerminology(train_dir);
    }

    public static Terminology getTrainingDataTerminology() {
        return trainingDataTerminology;
    }        
    
    public static List<String> getTerminologyNameCuis(Map<String, List<String>> nameToCuiListMap, String name) {
        return nameToCuiListMap.containsKey(name) ? nameToCuiListMap.get(name) : null;
    }
    
    public static String getTerminologyNameCui(HashListMap nameToCuiListMap, String name) {
        return nameToCuiListMap.containsKey(name) && nameToCuiListMap.get(name).size() == 1 ? nameToCuiListMap.get(name).get(0) : "";        
    }
    
    public static String exactMatchSieve(String name) {
        String cui = "";
        //checks against names normalized by multi-pass sieve
        cui = getTerminologyNameCui(Terminology.getNormalizedNameToCuiListMap(), name);
        if (!cui.equals("")) {
            return cui;
        }
        
        //checks against names in training data
        cui = getTerminologyNameCui(trainingDataTerminology..nameToCuiListMap, name);
        if (!cui.equals("")) {
            return cui;       
        }
        
        //checks against names in dictionary
        return getTerminologyNameCui(standardTerminology..nameToCuiListMap, name);               
    }

    public static List<String> getAlternateCuis(String cui) {
        List<String> alternateCuis = new ArrayList<>();
        if (trainingDataTerminology.cuiAlternateCuiMap.containsKey(cui)) {
            alternateCuis.addAll(trainingDataTerminology.cuiAlternateCuiMap.get(cui));
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
            cui = exactMatchSieve(name);            
            if (!cui.equals(""))
                // return cui;
                matches++;
        }
        if (matches==1)
            return cui;
        return "";
    }
    
}
