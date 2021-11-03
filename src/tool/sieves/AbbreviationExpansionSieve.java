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
import java.util.List;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

/**
 * Abbreviation Expansion Sieve
 * 
 * @author
 */
public class AbbreviationExpansionSieve extends Sieve {

    private HashListMap globalAbbreviationMap;

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     * @throws IOException
     */
    public AbbreviationExpansionSieve(Terminology standardTerminology, Terminology trainTerminology)
            throws IOException {
        super(standardTerminology, trainTerminology);

        globalAbbreviationMap = loadAbbreviationMap();
    }

    /**
     * Checks for abbreviation expansions from global file.
     */
    public void apply(Mention mention) throws Exception {
        // Tries to find an acronym and expands it to include all permutations.
        var allPermutations = getAbbreviationPermutations(mention.name);

        // Append unique permutations to the mention object.
        mention.addPermutationList(allPermutations);

        // Try to link permutations to a CUI in one of the dictionaries.
        normalize(mention);
    }

    /**
     * Assumes a name contains at most one acronym and returns all permutations of
     * its expansion.
     * 
     * @param name
     * @return
     */
    private List<String> getAbbreviationPermutations(String name) {
        var nameTokens = name.split("\\s");
        for (var i = 0; i < nameTokens.length; i++) {
            if (globalAbbreviationMap.containsKey(nameTokens[i])) {
                // Get a list of candidate expansions from the global abbreviation file.
                var expansions = new ArrayList<String>();
                for (var expansion : globalAbbreviationMap.get(nameTokens[i])) {
                    nameTokens[i] = expansion;
                    expansions.add(String.join(" ", nameTokens));
                }
                return expansions;
            }
        }
        return new ArrayList<String>();
    }

    // /**
    // * Attempt to find and expand abbreviations in the given mention using first
    // an
    // * abbreviation map from the source annotation file, the checking the global
    // * annotation file.
    // *
    // * @param doc
    // * @param mention
    // * @return
    // */
    // private String getAbbreviationExpansion(Document doc, Mention mention) {
    // var name = mention.name;
    // String[] nameTokens = name.split("\\s");

    // // Edge case when mention name is a single character, assume it's part of a
    // // bigger abbreviation and search outside the indices of the annotation.
    // if (nameTokens.length == 1 && nameTokens[0].length() == 1)
    // nameTokens[0] = expandSingleCharacter(doc.text, name,
    // mention.indexes.split("\\|"));

    // // Build expanded name one token at a time.
    // String nameExpansion = "";
    // for (String token : nameTokens) {
    // String nextToken = token;

    // if (doc.abbreviationMap != null && doc.abbreviationMap.containsKey(token)) {
    // // Check the map created from the annotation file first.
    // nextToken = doc.abbreviationMap.get(token);
    // } else {
    // // Get a list of candidate expansions from the global abbreviation file.
    // List<String> candidateExpansions = globalAbbreviationMap.get(token);
    // if (candidateExpansions != null) {
    // var expansion = getBestExpansion(doc.text, candidateExpansions);
    // nextToken = expansion.equals("") ? token : expansion;
    // }
    // }

    // // Apend next token.
    // nameExpansion += " " + nextToken;
    // }

    // // If single character name was expanded, reduce the expansion back down?
    // if (nameTokens.length == 1 && !nameTokens[0].equals(name))
    // nameExpansion = getTrimmedExpansion(doc.text, name,
    // mention.indexes.split("\\|"), nameExpansion);

    // return nameExpansion.trim();
    // }

    // private String getBestExpansion(String text, List<String> expansionList) {
    // // Return first item if it's the only item.
    // if (expansionList.size() == 1)
    // return expansionList.get(0);

    // int maxNumberOfContentWords = 0;
    // int maxContainedContentWords = 0;
    // String returnExpansion = "";
    // for (String expansion : expansionList) {
    // List<String> expansionContentWordsList =
    // filterStopwords(expansion.split("\\s"));

    // int tempNumberOfContentWords = expansionContentWordsList.size();
    // int tempContainedContentWords = 0;
    // for (String expansionContentWord : expansionContentWordsList) {
    // if (text.contains(" " + expansionContentWord) ||
    // text.contains(expansionContentWord + " "))
    // tempContainedContentWords++;
    // }

    // if (tempNumberOfContentWords > maxNumberOfContentWords
    // && tempContainedContentWords == tempNumberOfContentWords) {
    // maxNumberOfContentWords = tempNumberOfContentWords;
    // maxContainedContentWords = 1000;
    // returnExpansion = expansion;
    // } else if (tempNumberOfContentWords >= maxNumberOfContentWords
    // && tempContainedContentWords > maxContainedContentWords) {
    // maxNumberOfContentWords = tempNumberOfContentWords;
    // maxContainedContentWords = tempContainedContentWords;
    // returnExpansion = expansion;
    // }

    // }
    // return returnExpansion;
    // }

    // /**
    // * Checking something only in cases where nameExpansion contains a "/"?
    // *
    // * @param text
    // * @param name
    // * @param indexes
    // * @param nameExpansionSeparated
    // * @return
    // */
    // private String getTrimmedExpansion(String text, String name, String[]
    // indexes, String nameExpansion) {
    // var nameExpansionSeparated = nameExpansion.split("/");
    // if (indexes.length != 2)
    // return name;
    // int begin = Integer.parseInt(indexes[0]);
    // int end = Integer.parseInt(indexes[1]);
    // if (text.substring(begin - 3, end + 3).toLowerCase()
    // .matches("(^|\\s|\\W)[a-zA-Z]/" + name + "/[a-zA-Z](\\s|$|\\W)"))
    // return nameExpansionSeparated[1].toLowerCase();
    // else if (text.substring(begin - 1, end + 5).toLowerCase()
    // .matches("(^|\\s|\\W)" + name + "/[a-zA-Z]/[a-zA-Z](\\s|$|\\W)"))
    // return nameExpansionSeparated[0].toLowerCase();
    // else if (text.substring(begin - 5, end + 1).toLowerCase()
    // .matches("(^|\\s|\\W)[a-zA-Z]/[a-zA-Z]/" + name + "(\\s|$|\\W)"))
    // return nameExpansionSeparated[2].toLowerCase();
    // return name;
    // }

    // /**
    // * Called when a mention name is only a single character. Attempts to expand
    // the
    // * search around the mention to find a larger abbreviation it's part of.
    // *
    // * @param text
    // * @param name
    // * @param indexes
    // * @return
    // */
    // private String expandSingleCharacter(String text, String name, String[]
    // indexes) {
    // if (indexes.length != 2)
    // return name;
    // int begin = Integer.parseInt(indexes[0]);
    // int end = Integer.parseInt(indexes[1]);
    // if (text.substring(begin - 3, end + 3).toLowerCase()
    // .matches("(^|\\s|\\W)[a-zA-Z]/" + name + "/[a-zA-Z](\\s|$|\\W)"))
    // return text.substring(begin - 2, end + 2).toLowerCase();
    // else if (text.substring(begin - 1, end + 5).toLowerCase()
    // .matches("(^|\\s|\\W)" + name + "/[a-zA-Z]/[a-zA-Z](\\s|$|\\W)"))
    // return text.substring(begin, end + 4).toLowerCase();
    // else if (text.substring(begin - 5, end + 1).toLowerCase()
    // .matches("(^|\\s|\\W)[a-zA-Z]/[a-zA-Z]/" + name + "(\\s|$|\\W)"))
    // return text.substring(begin - 4, end).toLowerCase();
    // return name;
    // }

    // /**
    // * Filters stopwords out of the given token list.
    // *
    // * @param tokens
    // * @return
    // */
    // private List<String> filterStopwords(String[] tokens) {
    // List<String> contentWords = new ArrayList<>();
    // for (String token : tokens) {
    // if (!stopwords.contains(token))
    // contentWords.add(token);
    // }
    // return contentWords;
    // }

    /**
     * Loads the abbreviation map from a file.
     * 
     * @return
     * @throws IOException
     */
    private HashListMap loadAbbreviationMap() throws IOException {
        var map = new HashListMap();
        var file = new File("resources/abbreviations.txt");
        BufferedReader input = new BufferedReader(new FileReader(file));
        while (input.ready()) {
            String s = input.readLine().trim();
            String[] token = s.split("\\|\\|");
            token[0] = token[0].toLowerCase();
            map.addKeyPair(token[0], token[1].toLowerCase());
        }
        input.close();
        return map;
    }

    /**
     * Checks for an exact match in one of the dictionaries after expanding
     * abbreviations in the mention text.
     * 
     * @param mention
     */
    // public void apply(Mention mention, Document doc) {
    // // This uses an expansion created from the corresponding annotation file and
    // the
    // // global abbreviation file.
    // mention.nameExpansion = getAbbreviationExpansion(doc, mention);

    // // Add abbreviation expansion to name permutation list.
    // mention.addPermutation(mention.nameExpansion);

    // // Set the nameExpansion
    // mention.cui = exactMatch(mention, mention.nameExpansion);

    // if (!mention.cui.equals("") && !mention.cui.contains(",")) {
    // mention.normalized = true;
    // }
    // }
}
