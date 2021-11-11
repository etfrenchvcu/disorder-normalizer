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
 * UmlsEndingSieve Sieve.
 * 
 * @author
 */
public class UmlsEndingSieve extends Sieve {

    private List<String> beginnings;
    private List<String> endings;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @throws IOException
     */
    public UmlsEndingSieve(Terminology standardTerminology, Terminology trainTerminology) throws IOException {
        super(standardTerminology, trainTerminology);

        beginnings = Arrays.asList("finding of");
        endings = Arrays.asList(", nos", " (procedure)", " test", " measurement", " (body structure)", " (finding)");
    }

    /**
     * Checks for an exact match in one of the dictionaries after adding common
     * endings in UMLS names.
     * 
     * @param mention
     */
    public void apply(Mention mention) {
        List<String> allPermutations = new ArrayList<>();

        for (String name : mention.namePermutations) {
            for (String ending : endings) {
                allPermutations.add(name + ending);
            }
            for (String beginning : beginnings) {
                allPermutations.add(beginning + " " + name);
            }
        }

        // Append unique permutations to the mention object.
        mention.addPermutationList(allPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        normalize(mention);
    }
}
