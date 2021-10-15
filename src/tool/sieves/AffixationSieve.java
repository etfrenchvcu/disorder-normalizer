/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

/**
 * Affixation Sieve.
 * 
 * @author
 */
public class AffixationSieve extends Sieve {

    private Map<String, String> affixMap;
    private Map<String, String> prefixMap;
    private HashListMap suffixMap;
    private int maxSuffixLength;
    private int maxPrefixLength;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     * @throws IOException
     */
    public AffixationSieve(Terminology standardTerminology, Terminology trainTerminology,
            HashListMap normalizedNameToCuiListMap) throws IOException {
        super(standardTerminology, trainTerminology, normalizedNameToCuiListMap);

        // Initialize max lengths.
        maxSuffixLength = 0;
        maxPrefixLength = 0;

        // Initialize maps.
        loadSuffixMap();
        loadPrefixMap();
        loadAffixMap();
    }

    /**
     * Checks for an exact match in one of the dictionaries after permuting the
     * mention text by using alternative prefixes, suffixes, and affixes.
     * 
     * @param mention
     */
    public String apply(Mention mention) {
        List<String> allPermutations = new ArrayList<>();

        for (String name : mention.namePermutations) {
            String[] nameTokens = name.split("\\s");
            allPermutations.addAll(suffixation(nameTokens, name));
            allPermutations.add(prefixation(nameTokens, name));
            allPermutations.add(affixation(nameTokens, name));
        }

        // Append unique permutations to the mention object.
        mention.addPermutationList(allPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        return normalize(mention.namePermutations);
    }

    /**
     * Create permutations of the given name from suffix alternatives.
     * 
     * @param nameTokens
     * @param name
     * @return
     */
    private List<String> suffixation(String[] nameTokens, String name) {
        List<String> suffixPermutations = new ArrayList<>();

        // All combinations of suffix replacements.
        suffixPermutations.addAll(getSuffixPermutations(nameTokens));

        // For every suffix, replace just that suffix everywhere in the name.
        suffixPermutations.addAll(globalSuffixReplacement(nameTokens, name));

        return suffixPermutations;
    }

    /**
     * Build a list of all possible name permutations using all permutations of each
     * token with a suffix
     * 
     * @param nameTokens
     * @return
     */
    private List<String> getSuffixPermutations(String[] nameTokens) {
        List<String> suffixatedNamePermutations = new ArrayList<>();

        // Initialize with a single empty string to build permutations from.
        suffixatedNamePermutations.add("");

        // Build a list of all possible name permutations, appending one token at a
        // time, using all permutations of each token with a suffix.
        for (String token : nameTokens) {
            // Finds the longest suffix on the token in the suffixMap (returns NULL if none
            // found)
            String suffix = getSuffix(token);

            // Create list of alternative suffixes for the current token
            List<String> alternativeSuffixes = suffixMap.get(suffix);
            alternativeSuffixes = alternativeSuffixes != null ? alternativeSuffixes : new ArrayList<>();

            // Create list of permutations of the token with alternative suffixes
            List<String> tokenPermutations = new ArrayList<>();
            tokenPermutations.add(token); // Add original token
            for (var alternativeSuffix : alternativeSuffixes) {
                tokenPermutations.add(token.replace(suffix, alternativeSuffix));
            }

            // Add each permutation of current token to each suffixatedNamePermutations
            List<String> suffixatedPermutationsTemp = new ArrayList<>();
            for (String suffixatedPermutation : suffixatedNamePermutations) {
                for (var tokenPermutation : tokenPermutations) {
                    // Add permutation of current token to a name permutation we're building.
                    suffixatedPermutationsTemp.add(suffixatedPermutation + " " + tokenPermutation);
                }
            }
            suffixatedNamePermutations = suffixatedPermutationsTemp;
        }
        return suffixatedNamePermutations;
    }

    /**
     * Replace any suffix found everywhere in the name.
     * 
     * @param stringTokens
     * @param name
     * @return
     */
    private List<String> globalSuffixReplacement(String[] stringTokens, String name) {
        List<String> suffixatedPhrases = new ArrayList<>();
        for (String token : stringTokens) {
            String suffix = getSuffix(token);
            List<String> alternativeSuffixes = suffixMap.get(suffix);
            alternativeSuffixes = alternativeSuffixes != null ? alternativeSuffixes : new ArrayList<>();

            // Replace the suffix everywhere in the name (even if not used as a suffix)
            for (String alternativeSuffix : alternativeSuffixes) {
                suffixatedPhrases.add(name.replaceAll(suffix, alternativeSuffix).trim());
            }
        }
        return suffixatedPhrases;
    }

    /**
     * Checks the token for suffixes in the suffixMap, starting with longest
     * possible.
     * 
     * @param token
     * @return
     */
    private String getSuffix(String token) {
        String suffix;
        var length = Math.min(maxSuffixLength, token.length());

        for (int i = length; i >= 2; i--) {
            suffix = token.substring(token.length() - i);
            if (suffixMap.containsKey(suffix))
                return suffix;
        }
        return null;
    }

    /**
     * Checks the token for prefixes in the prefixMap, starting with longest
     * possible.
     * 
     * @param token
     * @return
     */
    public String getPrefix(String token) {
        String prefix;
        var length = Math.min(maxPrefixLength, token.length());

        for (int i = length; i >= 2; i--) {
            prefix = token.substring(0, i);
            if (prefixMap.containsKey(prefix))
                return prefix;
        }
        return null;
    }

    /**
     * Replace all prefixes in name.
     * 
     * @param nameTokens
     * @param name
     * @return
     */
    private String prefixation(String[] nameTokens, String name) {
        String prefixedName = "";
        for (String token : nameTokens) {
            String prefix = getPrefix(token);
            String alternativePrefix = prefixMap.get(prefix);

            // Add next token, replace prefix if replacement is available.
            prefixedName += " ";
            prefixedName += alternativePrefix == null ? token : token.replace(prefix, alternativePrefix);
        }
        return prefixedName.trim();
    }

    /**
     * Replace an affix in name.
     * @param nameTokens
     * @param name
     * @return
     */
    private String affixation(String[] nameTokens, String name) {
        String affixatedName = "";
        for (String token : nameTokens) {
            String nextToken = token;

            // Check the token for an affix that can be replaced with an alternative.
            for (var affix : affixMap.keySet()) {
                if (token.contains(affix)) {
                    var alternativeAffix = affixMap.get(affix);
                    nextToken = token.replace(affix, alternativeAffix);
                    break;
                }
            }

            // Add next token, replace prefix if replacement is available.
            affixatedName += " " + nextToken;
        }
        return affixatedName.trim();
    }

    // region Initialize maps
    /**
     * Loads the suffixMap from file.
     * 
     * @throws IOException
     */
    private void loadSuffixMap() throws IOException {
        suffixMap = new HashListMap();
        var file = new File("resources/suffix.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        while (in.ready()) {
            String s = in.readLine().trim();
            String[] tokens = s.split("\\|\\|");
            String value = tokens.length == 1 ? "" : tokens[1];

            // Suffixes without pair are removed it instead of replacing.
            suffixMap.addKeyPair(tokens[0], value);

            // Update maxSuffixLength if necessary.
            maxSuffixLength = Math.max(maxSuffixLength, tokens[0].length());
        }
        in.close();
    }

    /**
     * Loads the prefix map from file.
     * 
     * @throws IOException
     */
    private void loadPrefixMap() throws IOException {
        prefixMap = new HashMap<>();
        var file = new File("resources/prefix.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        while (in.ready()) {
            String s = in.readLine().trim();
            String[] tokens = s.split("\\|\\|");
            String value = tokens.length == 1 ? "" : tokens[1];
            prefixMap.put(tokens[0], value);

            // Update maxPrefixLength if necessary.
            maxPrefixLength = Math.max(maxPrefixLength, tokens[0].length());
        }
        in.close();
    }

    /**
     * Loads the affix map from file.
     * 
     * @throws IOException
     */
    private void loadAffixMap() throws IOException {
        affixMap = new HashMap<>();
        var file = new File("resources/affix.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        while (in.ready()) {
            String s = in.readLine().trim();
            String[] tokens = s.split("\\|\\|");
            String value = tokens.length == 1 ? "" : tokens[1];
            affixMap.put(tokens[0], value);
        }
        in.close();
    }
    // endregion
}
