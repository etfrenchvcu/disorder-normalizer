/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tool.util.Mention;
import tool.util.Terminology;

/**
 * Hyphenation Sieve.
 * 
 * @author
 */
public class HyphenationSieve extends Sieve {

    List<String> specialCases;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @throws IOException
     */
    public HyphenationSieve(Terminology standardTerminology, Terminology trainTerminology) {
        super(standardTerminology, trainTerminology);
        specialCases = Arrays.asList("non", "pre", "post");
    }

    /**
     * Checks for an exact match in one of the dictionaries after permuting the
     * mention text adding or removing hyphens.
     * 
     * @param mention
     */
    public void apply(Mention mention) {

        List<String> allPermutations = new ArrayList<>();

        for (String name : mention.namePermutations) {
            // Add hyphens.
            allPermutations.addAll(hyphenateName(name));

            // Remove hyphens.
            allPermutations.addAll(dehyphenateString(name));

            // Inject hyphen after non/pre/post
            for (var s : specialCases) {
                var regex = "(\\s|^)" + s + "(?=[a-z])";
                var replacement = " " + s + "-";
                var permutation = name.replaceAll(regex, replacement).trim();
                if (!permutation.equals(name)) {
                    allPermutations.add(permutation);
                    mention.keyPhrase = s;
                }
            }
        }

        // Append unique permutations to the mention object.
        mention.addPermutationList(allPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        normalize(mention);
    }

    /**
     * Try adding a hyphen between each pair of tokens in the name.
     * 
     * @param name
     * @return
     */
    public static List<String> hyphenateName(String name) {
        String[] nameTokens = name.split("\\s+");
        List<String> hyphenatedNames = new ArrayList<>();
        for (int i = 1; i < nameTokens.length; i++) {
            String hyphenatedName = "";
            for (int j = 0; j < nameTokens.length; j++) {
                if (j == i)
                    hyphenatedName += "-" + nameTokens[j];
                else
                    hyphenatedName = hyphenatedName.equals("") ? nameTokens[j] : hyphenatedName + " " + nameTokens[j];
            }
            hyphenatedNames.add(hyphenatedName);
        }
        return hyphenatedNames;
    }

    /**
     * Try replacing hyphens in names with spaces.
     * 
     * @param name
     * @return
     */
    public static List<String> dehyphenateString(String name) {
        List<String> dehyphenatedNames = new ArrayList<>();
        dehyphenatedNames.add(name.replaceAll("\\-", ""));
        dehyphenatedNames.add(name.replaceAll("\\-", " "));
    
        return dehyphenatedNames;
    }

}
