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
 * Synonym Sieve.
 * 
 * @author
 */
public class SynonymSieve extends Sieve {

    private Map<String, String> synonymMap;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @throws IOException
     */
    public SynonymSieve(Terminology standardTerminology, Terminology trainTerminology) throws IOException {
        super(standardTerminology, trainTerminology);

        loadSynonyms();
    }

    /**
     * Checks for an exact match in one of the dictionaries after replacing
     * synonyms.
     * 
     * @param mention
     */
    public void apply(Mention mention) {

        List<String> permutations = new ArrayList<>();

        // Attempt to replace synonyms for each name permutation.
        for (var synonym : synonymMap.keySet()) {
            var regex = "(\\s|^)" + synonym + "(\\s|$)";
            var replacement = " " + synonymMap.get(synonym) + " ";
            for (String name : mention.namePermutations) {
                var permutation = name.replaceAll(regex, replacement).trim();
                if (!permutation.equals(name)) {
                    permutations.add(permutation);
                    mention.keyPhrase = synonym + replacement;
                }
            }
        }

        mention.addPermutationList(permutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        normalize(mention);
    }

    /**
     * Loads the synonymMap from file.
     * 
     * @throws IOException
     */
    private void loadSynonyms() throws IOException {
        synonymMap = new HashMap<>();
        var file = new File("resources/synonyms.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        while (in.ready()) {
            String s = in.readLine().trim();
            String[] tokens = s.split("\\|\\|");
            synonymMap.put(tokens[0], tokens[1]);
            synonymMap.put(tokens[1], tokens[0]);
        }
        in.close();
    }
}
