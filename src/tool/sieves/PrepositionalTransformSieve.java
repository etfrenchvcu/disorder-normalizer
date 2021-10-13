/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tool.util.*;

/**
 * Prepositional Transform Sieve (subject <=> object conversion)
 * @author Jen
 */
public class PrepositionalTransformSieve extends Sieve {
    public final List<String> PREPOSITIONS = Arrays.asList("in", "with", "on", "of");

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @param normalizedNameToCuiListMap
     */
    public PrepositionalTransformSieve(Terminology standardTerminology, Terminology trainTerminology,
            HashListMap normalizedNameToCuiListMap) {
        super(standardTerminology, trainTerminology, normalizedNameToCuiListMap);
    }

    /**
     * Checks for an exact match in one of the dictionaries after permuting the mention text using different prepositions and converting subject/object.
     * @param mention
     */
    public String apply(Mention mention) {
        // Non-unique list of permutations of all names
        List<String> allPermutations = new ArrayList<>();

        for (String name : mention.namePermutations) {

            // Check the name for a preposition.
            String preposition = getPreposition(name);            
            
            if (!preposition.equals("")) {
                // If phrase has a preposition, permutate prepositions and swap subject/objects.
                var permutations = permutatePrepositions(preposition, name);
                allPermutations.addAll(permutations);
            }
            else {
                // If the phrase does not have a preposition, generate new phrases by inserting prepositions.
                var permutations = insertPrepositionsInPhrase(name);
                allPermutations.addAll(permutations);
            }
        }   

        // Append unique permutations to the mention object.
        mention.addPermutationList(allPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        return normalize(mention.namePermutations);
    }

    /**
     * Checks for a preposition in the given string.
     * @param string
     * @return
     */
    private String getPreposition(String string) {
        for (String preposition : PREPOSITIONS) {
            if (string.contains(" "+preposition+" ")) 
                return preposition;
        }
        return "";
    }

    /**
     * Returns a list of permutations of the given phrase created by trying all prepositions in PREPOSITIONS and swapping subject/objects in each permutation.
     * @param existingPreposition
     * @param phrase
     * @return
     */
    private List<String> permutatePrepositions(String existingPreposition, String phrase) {
        var permutations = new ArrayList<String>();
        for (String preposition : PREPOSITIONS) {
            String permutation = phrase.replace(" "+existingPreposition+" ", " "+preposition+" ").trim();
            
            // Add prepositional permutation.
            permutations.add(permutation);

            // Add prepositional permutation with subject and object swapped.
            permutations.add(swapSubjectAndObject(preposition,permutation));
        }
        return permutations;
    }

    /**
     * Naively transforms the given phrase removing the preposition and swapping the tokens after it with those before.
     * @param prepositionInPhrase
     * @param phraseTokens
     * @return
     */
    private String swapSubjectAndObject(String prepositionInPhrase, String phrase) {
        var phraseTokens = phrase.split("\\s+");
        int prepositionTokenIndex = Util.getTokenIndex(phraseTokens, prepositionInPhrase);
        return prepositionTokenIndex != -1 ? (Ling.getSubstring(phraseTokens, prepositionTokenIndex+1, phraseTokens.length)+" "+
                Ling.getSubstring(phraseTokens, 0, prepositionTokenIndex)).trim() : "";
    } 
    
    /**
     * Try to insert prepositions into a phrase by moving the first token to the end preceded by a preposition OR moving the last token to the
     * beginning followed by a preposition.
     * @param phrase
     * @return
     */
    private List<String> insertPrepositionsInPhrase(String phrase) {
        var phraseTokens = phrase.split("\\s+");
        List<String> newPrepositionalPhrases = new ArrayList<>();
        for (String preposition : PREPOSITIONS) {
            //insert preposition near the end of the string
            String newPrepositionalPhrase = (Ling.getSubstring(phraseTokens, 1, phraseTokens.length)+" "+preposition+" "+phraseTokens[0]).trim();
            newPrepositionalPhrases = Util.setList(newPrepositionalPhrases, newPrepositionalPhrase);
            //insert preposition near the beginning of the string
            newPrepositionalPhrase = (phraseTokens[phraseTokens.length-1]+" "+preposition+" "+Ling.getSubstring(phraseTokens, 0, phraseTokens.length-1)).trim();
            newPrepositionalPhrases = Util.setList(newPrepositionalPhrases, newPrepositionalPhrase);
        }        
        return newPrepositionalPhrases;
    }        
    
       
    
}
