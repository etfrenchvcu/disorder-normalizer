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
import java.util.Map;

/**
 *
 * @author
 */
public class Ling {

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

    public static Map<String, String> getSpellingCorrectionMap() {
        return spellingCorrectionMap;
    }

    public static String correctSpelling(String phrase) {
        String[] phrase_tokens = phrase.split("\\s+");
        phrase = "";
        for (String phrase_token : phrase_tokens) {
            phrase_token = spellingCorrectionMap.containsKey(phrase_token) ? spellingCorrectionMap.get(phrase_token)
                    : phrase_token;
            phrase += phrase_token + " ";
        }
        phrase = phrase.trim();
        return phrase;
    }

    public static String reverse(String string) {
        String reversedString = "";
        int size = string.length() - 1;
        for (int i = size; i >= 0; i--) {
            reversedString += string.charAt(i);
        }
        return reversedString;
    }

    // TODO: Use String.join(" ", nameTokens.subList(0, nameTokens.size() - 1))
    // instead
    public static String getSubstring(String[] tokens, int begin, int end) {
        String substring = "";
        for (int i = begin; i < end; i++) {
            substring += tokens[i] + " ";
        }
        substring = substring.trim();
        return substring;
    }

    // public static int getMatchingTokensCount(String phrase1, String phrase2) {
    // List<String> tokens = new ArrayList<>(Arrays.asList(phrase1.split("\\s+")));
    // tokens.retainAll(new ArrayList<>(Arrays.asList(phrase2.split("\\s+"))));
    // tokens.removeAll(Ling.stopwords);
    // return tokens.isEmpty() ? 0 : tokens.size();
    // }
}
