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

import tool.util.Mention;
import tool.util.Terminology;

/**
 * Suffixation Sieve.
 * 
 * @author
 */
public class SuffixationSieve extends Sieve {

    private Map<String, String> suffixMap;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @throws IOException
     */
    public SuffixationSieve(Terminology standardTerminology, Terminology trainTerminology) throws IOException {
        super(standardTerminology, trainTerminology);

        loadSuffixes();
    }

    /**
     * Checks for an exact match in one of the dictionaries after removing/replacing
     * suffixes.
     * 
     * @param mention
     */
    public void apply(Mention mention) {

        List<String> stemmedPermutations = new ArrayList<>();

        // Attempt to stem each name permutation.
        for (var suffix : suffixMap.keySet()) {
            var regex = suffix + "(\\s|$)";
            var replacement = suffixMap.get(suffix);
            for (String name : mention.namePermutations) {
                var stemmedName = name.replaceAll(regex, replacement).trim();
                if (!stemmedName.equals(name)) {
                    stemmedPermutations.add(stemmedName);
                    mention.keyPhrase = suffix;
                }
            }
        }

        mention.addPermutationList(stemmedPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        normalize(mention);
    }

    /**
     * Loads the suffixMap from file.
     * 
     * @throws IOException
     */
    private void loadSuffixes() throws IOException {
        suffixMap = new HashMap<>();
        var file = new File("resources/suffix.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        while (in.ready()) {
            String s = in.readLine().trim();
            String[] tokens = s.split("\\|\\|");
            String value = tokens.length == 1 ? " " : tokens[1];
            suffixMap.put(tokens[0], value);
        }
        in.close();
    }
}
