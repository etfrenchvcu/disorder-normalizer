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

import tool.util.*;

/**
 * Number Replacement Sieve
 * @author
 */
public class NumberReplacementSieve extends Sieve {

    private HashListMap digitToWordMap;
    private Map<String, String> wordToDigitMap;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     * @throws IOException
     */
    public NumberReplacementSieve(Terminology standardTerminology, Terminology trainTerminology,
            HashListMap normalizedNameToCuiListMap) throws IOException {
        super(standardTerminology, trainTerminology, normalizedNameToCuiListMap);

        // Initialize word=>digit and digit=>word mappings.
        digitToWordMap = new HashListMap();
        wordToDigitMap = new HashMap<>();
        loadWordDigitMappings();
    }

    /**
     * Checks for an exact match in one of the dictionaries after permuting the
     * mention text by replacing digits with words and vice versa.
     * 
     * @param mention
     */
    public String apply(Mention mention) {

        List<String> allPermutations = new ArrayList<>();

        for (String name : mention.namePermutations) {
            // Replace digits with words.
            allPermutations.addAll(replaceDigitsWithWords(name));

            // Replace words with digits.
            allPermutations.addAll(replaceWordsWithDigits(name));
        }

        // Append unique permutations to the mention object.
        mention.addPermutationList(allPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        return normalize(mention.namePermutations);
    }

    /**
     * Creates a list of permutations for the given name by replacing digits with
     * their word form.
     * 
     * @param name
     * @return
     */
    private List<String> replaceDigitsWithWords(String name) {
        List<String> permutations = new ArrayList<>();
        for (String digit : digitToWordMap.keySet()) {
            if (name.contains(digit)) {
                List<String> digitWords = digitToWordMap.get(digit);
                for (String word : digitWords) {
                    String digitReplaced = name.replaceAll(digit, word);

                    // Add just digits replaced.
                    permutations.add(digitReplaced);

                    // Add simplified version of digits replaced.
                    permutations.add(simplifyName(digitReplaced));
                }
            }
        }
        return permutations;
    }

    /**
     * Creates a list of permutations for the given name by replacing numeric words with
     * their digit form.
     * 
     * @param name
     * @return
     */
    private List<String> replaceWordsWithDigits(String name) {
        List<String> permutations = new ArrayList<>();
        for (String word : wordToDigitMap.keySet()) {
            if (name.contains(word)) {
                String digit = wordToDigitMap.get(word);
                String wordReplaced = name.replaceAll(word, digit);

                // Add just words replaced.
                permutations.add(wordReplaced);

                // Add simplified version of words replaced.
                permutations.add(simplifyName(wordReplaced));
            }
        }
        return permutations;
    }

    /**
     * Simplifies a name by removing parentheses and replacing "and/or" and "/".
     * 
     * @param name
     * @return
     */
    private String simplifyName(String name) {
        if (name.contains("and/or"))
            name = name.replaceAll("and/or", "and");
        if (name.contains("/"))
            name = name.replaceAll("/", " and ");
        if (name.contains(" (") && name.contains(")"))
            name = name.replace(" (", "").replace(")", "");
        else if (name.contains("(") && name.contains(")"))
            name = name.replace("(", "").replace(")", "");
        return name;
    }

    /**
     * Loads mappings from words=>digits and digits=>words.
     * 
     * @throws IOException
     */
    private void loadWordDigitMappings() throws IOException {
        // TODO: load from config???
        var file = new File("resources/number.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        while (in.ready()) {
            String s = in.readLine().trim();
            String[] tokens = s.split("\\|\\|");
            digitToWordMap.addKeyPair(tokens[0], tokens[1]);
            wordToDigitMap.put(tokens[1], tokens[0]);
        }
        in.close();
    }
}