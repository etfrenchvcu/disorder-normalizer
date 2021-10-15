package tool.util;

import java.util.List;

public class Stemmer {

    private List<String> stopwords;

    /**
     * Constructor.
     * 
     * @param stopwords
     */
    public Stemmer(List<String> stopwords) {
        this.stopwords = stopwords;
    }

    /**
     * Stems each token (except stopwords) in the given name.
     * 
     * @param name
     * @return
     */
    public String stem(String name) {
        // Iteratively append stemmed tokens to build stemmedName.
        String stemmedName = "";
        for (var token : name.split("\\s")) {
            var nextToken = token;

            // Don't stem stopword tokens.
            if (!stopwords.contains(token)) {
                // Attempt to stem the token.
                var stemmedToken = PorterStemmer.get_stem(token).trim();
                nextToken = stemmedToken.equals("") ? token : stemmedToken;
            }

            // Append nextToken to stemmedName.
            stemmedName += nextToken + " ";
        }
        return stemmedName.trim();
    }
}