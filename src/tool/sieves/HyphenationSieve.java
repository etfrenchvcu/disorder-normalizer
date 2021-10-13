/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.util.ArrayList;
import java.util.List;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;
import tool.util.Util;

/**
 *
 * @author
 */
public class HyphenationSieve extends Sieve {

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     * @throws IOException
     */
    public HyphenationSieve(Terminology standardTerminology, Terminology trainTerminology,
            HashListMap normalizedNameToCuiListMap) {
        super(standardTerminology, trainTerminology, normalizedNameToCuiListMap);
    }

    /**
     * Checks for an exact match in one of the dictionaries after permuting the
     * mention text adding or removing hyphens.
     * 
     * @param mention
     */
    public String apply(Mention mention) {

        List<String> allPermutations = new ArrayList<>();

        for (String name : mention.namePermutations) {
            // Replace digits with words.
            allPermutations.addAll(hyphenateName(name));

            // Replace words with digits.
            allPermutations.addAll(dehyphenateString(name));
        }

        // Append unique permutations to the mention object.
        mention.addPermutationList(allPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        return normalize(mention.namePermutations);
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
        // Split the name into sections separated by hyphens.
        String[] nameSections = name.split("\\-");
        List<String> dehyphenatedNames = new ArrayList<>();
        for (int i = 1; i < nameSections.length; i++) {
            String dehyphenatedString = "";
            for (int j = 0; j < nameSections.length; j++) {
                if (j == i)
                    dehyphenatedString += " " + nameSections[j];
                else
                    dehyphenatedString = dehyphenatedString.equals("") ? nameSections[j]
                            : dehyphenatedString + "-" + nameSections[j];
            }
            dehyphenatedNames.add(dehyphenatedString);
        }
        return dehyphenatedNames;
    }

}
