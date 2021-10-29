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
import tool.util.Util;

/**
 *
 * @author
 */
public abstract class Sieve {

    Terminology standardTerminology;
    Terminology trainTerminology;

    /**
     * Abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     */
    public Sieve(Terminology standardTerminology, Terminology trainTerminology) {
        this.standardTerminology = standardTerminology;
        this.trainTerminology = trainTerminology;
    }

    /**
     * Applies the sieve to the given string. Method must be implemented by classes
     * extending Sieve. Returns a CUI.
     * 
     * @param mention
     * @return cui
     * @throws Exception
     */
    public abstract void apply(Mention mention) throws Exception;

    public static List<String> getTerminologyNameCuis(Map<String, List<String>> nameToCuiListMap, String name) {
        var cui = nameToCuiListMap.containsKey(name) ? nameToCuiListMap.get(name) : null;
        return cui;
    }

    /**
     * Returns a list of CUIs that matched or an empty string;
     * 
     * @param nameToCuiListMap
     * @param name
     * @return
     */
    public static String getTerminologyNameCui(HashListMap nameToCuiListMap, String name) {
        var matches = nameToCuiListMap.get(name);
        return matches == null ? "" : String.join(",", matches);
    }

    /**
     * Successively checks for exact matches in the 1) set of already normalized
     * CUIs, 2) train Terminology, and 3) standard Terminology.
     * 
     * @param mention
     * @param name
     * @return comma delimited list of CUIs.
     */
    public String exactMatch(Mention mention, String name) {
        String cui = "";

        // Checks against names in training data
        cui = getTerminologyNameCui(trainTerminology.nameToCuiListMap, name);
        if (!cui.equals("")) {
            mention.normalizingSource = "trainTerminology";
            return cui;
        }

        // Checks against names in standard terminology dictionary
        cui = getTerminologyNameCui(standardTerminology.nameToCuiListMap, name);
        if (!cui.equals("")) {
            mention.normalizingSource = "standardTerminology";
            return cui;
        }

        return "";
    }

    /**
     * Check the list of name permutations for a match in one of the dictionaries.
     * Return a unique list of matches.
     * 
     * @param mention
     * @return cui
     */
    public void normalize(Mention mention) {
        List<String> cuis = new ArrayList<>();
        List<String> names = new ArrayList<>();

        // Check each name permutation for a CUI match in the dictionary.
        for (String name : mention.namePermutations) {
            var match = exactMatch(mention, name);

            if (match.contains(",")) {
                // exactMatch returned a list of CUIs.
                Util.addUnique(names, name);
                for (var cui : match.split(",")) {
                    Util.addUnique(cuis, cui);
                }
            } else if (!match.equals("")) {
                // exactMatch returned exactly one CUI.
                Util.addUnique(cuis, match);
                Util.addUnique(names, name);
            }
        }

        if (cuis.size() == 1) {
            mention.normalized = true;
        }

        // Return unique CUIs.
        mention.cui = String.join(",", cuis);
    }

}
