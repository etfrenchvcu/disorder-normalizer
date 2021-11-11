/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author
 */
public class Util {

    /**
     * Add a unique value to a list.
     * 
     * @param list
     * @param value
     */
    public static void addUnique(List<String> list, String value) {
        if (!list.contains(value) && !value.equals(""))
            list.add(value);
    }

    /**
     * Adds a list of values to a list with duplication.
     * 
     * @param list
     * @param value
     */
    public static void addUnique(List<String> list, List<String> values) {
        for (var value : values) {
            addUnique(list, value);
        }
    }

    private static Map<String, String> spellingCorrectionMap = new HashMap<>();

    public static void setSpellingCorrectionMap(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        while (in.ready()) {
            String s = in.readLine().trim();
            String[] tokens = s.split("\\|\\|");
            String value = tokens.length == 1 ? "" : tokens[1];
            spellingCorrectionMap.put(tokens[0], value);
        }
        in.close();
    }

    public static String correctSpelling(String phrase) {
        String[] phrase_tokens = phrase.split("\\s+");
        phrase = "";
        for (String phrase_token : phrase_tokens) {
            if (spellingCorrectionMap.containsKey(phrase_token)) {
                phrase_token = spellingCorrectionMap.get(phrase_token);
            }

            phrase += phrase_token + " ";
        }
        phrase = phrase.trim();
        return phrase;
    }

    /**
     * Reverses the given string.
     * 
     * @param string
     * @return
     */
    public static String reverse(String string) {
        String reversedString = "";
        int size = string.length() - 1;
        for (int i = size; i >= 0; i--) {
            reversedString += string.charAt(i);
        }
        return reversedString;
    }

    // public static int getMatchingTokensCount(String phrase1, String phrase2) {
    // List<String> tokens = new ArrayList<>(Arrays.asList(phrase1.split("\\s+")));
    // tokens.retainAll(new ArrayList<>(Arrays.asList(phrase2.split("\\s+"))));
    // tokens.removeAll(Ling.stopwords);
    // return tokens.isEmpty() ? 0 : tokens.size();
    // }
}
