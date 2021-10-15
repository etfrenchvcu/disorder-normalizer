/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object representing a single document. Contains name and text of the doc and
 * a list of annotated mentions.
 * 
 * @author
 */
public class Document {

    public String filename;
    public String text;
    public List<Mention> mentions;
    public Map<String, String> abbreviationMap;

    public Document(File file, String text) throws IOException {
        this.filename = file.getName();
        this.text = text;
        mentions = new ArrayList<>();
        abbreviationMap = getTextAbbreviationExpansionMapFromFile(file);
    }

    /**
     * Adds a new mention to the list for this document.
     * 
     * @param tokens
     */
    public void addMention(String[] tokens) {
        String[] cuis = tokens[4].contains("+") ? tokens[4].split("\\+") : tokens[4].split("\\|");
        String MeSHorSNOMEDcuis = Terminology.getMeSHorSNOMEDCuis(cuis);
        List<String> OMIMcuis = Terminology.getOMIMCuis(cuis);
        Mention mention = new Mention(tokens[1], tokens[3], MeSHorSNOMEDcuis, OMIMcuis);
        mentions.add(mention);
    }

    /**
     * Loads text abbreviation map from annotation file.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    private Map<String, String> getTextAbbreviationExpansionMapFromFile(File file) throws IOException {
        Map<String, String> abbreviationMap = new HashMap<>();
        BufferedReader input = new BufferedReader(new FileReader(file));
        while (input.ready()) {
            String s = input.readLine().trim().replaceAll("\\s+", " ");
            String[] tokens = s.split("\\s");
            int size = tokens.length;
            for (int i = 0; i < size; i++) {
                int expansionIndex = -1;

                if (tokens[i].matches("\\(\\w+(\\-\\w+)?\\)(,|\\.)?") || tokens[i].matches("\\([A-Z]+(;|,|\\.)"))
                    expansionIndex = i - 1;
                else if (tokens[i].matches("[A-Z]+\\)"))
                    expansionIndex = Util.firstIndexOf(tokens, i, "\\(");

                if (expansionIndex == -1)
                    continue;

                String abbreviation = tokens[i].replace("(", "").replace(")", "").toLowerCase();
                String reversedAbbreviation = Ling.reverse(abbreviation);

                if (abbreviation.charAt(abbreviation.length() - 1) == ','
                        || abbreviation.charAt(abbreviation.length() - 1) == '.'
                        || abbreviation.charAt(abbreviation.length() - 1) == ';')
                    abbreviation = abbreviation.substring(0, abbreviation.length() - 1);

                if (abbreviationMap.containsKey(abbreviation) || abbreviationMap.containsKey(reversedAbbreviation))
                    continue;

                int abbreviationLength = abbreviation.length();
                setTextAbbreviationExpansionMap(abbreviationMap, tokens, abbreviationLength, abbreviation,
                        expansionIndex);
                if (!abbreviationMap.containsKey(abbreviation))
                    setTextAbbreviationExpansionMap(abbreviationMap, tokens, abbreviationLength, reversedAbbreviation,
                            expansionIndex);
            }
        }
        input.close();

        return abbreviationMap;
    }

    private void setTextAbbreviationExpansionMap(Map<String, String> map, String[] tokens, int abbreviationLength,
            String abbreviation, int expansionIndex) {
        String expansion = getTentativeExpansion(tokens, expansionIndex, abbreviationLength);
        expansion = Ling.correctSpelling(getExpansionByHearstAlgorithm(abbreviation, expansion).toLowerCase()).trim();
        if (!expansion.equals(""))
            map.put(abbreviation, expansion);
    }

    public static String getTentativeExpansion(String[] tokens, int i, int abbreviationLength) {
        String expansion = "";
        while (i >= 0 && abbreviationLength > 0) {
            expansion = tokens[i] + " " + expansion;
            i--;
            abbreviationLength--;
        }
        return expansion.trim();
    }

    private String getExpansionByHearstAlgorithm(String shortForm, String longForm) {
        int sIndex;
        int lIndex;
        char currChar;

        sIndex = shortForm.length() - 1;
        lIndex = longForm.length() - 1;

        for (; sIndex >= 0; sIndex--) {
            currChar = Character.toLowerCase(shortForm.charAt(sIndex));
            if (!Character.isLetterOrDigit(currChar))
                continue;

            while (((lIndex >= 0) && (Character.toLowerCase(longForm.charAt(lIndex)) != currChar))
                    || ((sIndex == 0) && (lIndex > 0) && (Character.isLetterOrDigit(longForm.charAt(lIndex - 1)))))
                lIndex--;
            if (lIndex < 0)
                return "";
            lIndex--;
        }

        lIndex = longForm.lastIndexOf(" ", lIndex) + 1;
        longForm = longForm.substring(lIndex);

        return longForm;
    }
}
