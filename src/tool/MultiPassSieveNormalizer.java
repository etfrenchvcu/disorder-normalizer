/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tool.sieves.AbbreviationExpansionSieve;
import tool.sieves.AmbiguitySieve;
import tool.sieves.ExactMatchSieve;
import tool.sieves.HyphenationSieve;
import tool.sieves.NumberReplacementSieve;
import tool.sieves.PartialMatchSieve;
import tool.sieves.PrepositionalTransformSieve;
import tool.sieves.RemoveStopwordsSieve;
import tool.sieves.Sieve;
import tool.sieves.StemmingSieve;
import tool.sieves.UmlsEndingSieve;
import tool.util.Document;
import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Stemmer;
import tool.util.Terminology;
import tool.util.Util;

/**
 *
 * @author
 */
public class MultiPassSieveNormalizer {
    boolean usingPartialMatch = false;
    Evaluation eval;
    HashListMap normalizedNameToCuiListMap;
    HashListMap stemmedNormalizedNameToCuiListMap;
    Terminology standardTerminology;
    ArrayList<Sieve> sieves;
    List<String> stopwords;

    /***
     * Initialize MultiPassSieveNormalizer and associated resources.
     * 
     * @param eval
     * @param max_level
     * @param standardTerminology
     * @throws Exception
     */
    public MultiPassSieveNormalizer(Evaluation eval, File standardTerminologyFile) throws Exception {
        this.eval = eval;
        normalizedNameToCuiListMap = new HashListMap();

        // set stopwords, correct spellings, and abbreviations data
        Util.setSpellingCorrectionMap(new File("resources/spell-check.txt"));

        // Load training data terminology
        stopwords = loadStopwords();
        standardTerminology = new Terminology(standardTerminologyFile, stopwords, usingPartialMatch);
    }

    /**
     * Initializes the sieves. TODO: Do this from config file?
     * 
     * @param trainTerminology
     * @return
     * @throws IOException
     */
    private ArrayList<Sieve> initializeSieves(Terminology trainTerminology) throws IOException {
        ArrayList<Sieve> sieves = new ArrayList<Sieve>();

        sieves.add(new ExactMatchSieve(standardTerminology, trainTerminology));
        sieves.add(new RemoveStopwordsSieve(standardTerminology, trainTerminology));
        sieves.add(new UmlsEndingSieve(standardTerminology, trainTerminology));
        sieves.add(new AbbreviationExpansionSieve(standardTerminology, trainTerminology, stopwords));
        sieves.add(new PrepositionalTransformSieve(standardTerminology, trainTerminology));
        sieves.add(new NumberReplacementSieve(standardTerminology, trainTerminology));
        sieves.add(new HyphenationSieve(standardTerminology, trainTerminology));
        // sieves.add(new AffixationSieve(standardTerminology, trainTerminology)); //
        // This one is slow...
        // sieves.add(new DiseaseTermSynonymsSieve(standardTerminology,
        // trainTerminology)); //Slow and bad
        // sieves.add(new StemmingSieve(standardTerminology, trainTerminology, new Stemmer(stopwords)));
        // sieves.add(new CompoundPhraseSieve(standardTerminology, trainTerminology,
        // normalizedNameToCuiListMap));
        sieves.add(new PartialMatchSieve(standardTerminology, trainTerminology, stopwords));
        sieves.add(new AmbiguitySieve(standardTerminology, trainTerminology));

        return sieves;
    }

    /**
     * Runs the sieve algorithm over the input datasets.
     * 
     * @param trainDir
     * @param testDir
     * @throws Exception
     */
    public void run(File trainDir, File testDir) throws Exception {
        // Setup
        List<Document> test_data = getDataSet(testDir);
        var trainTerminology = new Terminology(trainDir, stopwords, usingPartialMatch);
        sieves = initializeSieves(trainTerminology);

        // Progressively apply sieves until the mention is linked to a CUI.
        for (int i = 0; i < sieves.size(); i++) {
            var sieveName = sieves.get(i).getClass().getName().replace("tool.sieves.", "");
            System.out.println(sieveName + ": " + new SimpleDateFormat("mm.ss").format(new Date()));

            for (Document doc : test_data) {
                HashListMap documentCuiNamesMap = new HashListMap();

                for (Mention mention : doc.mentions) {
                    // Skip already normalized and length==1 (too ambiguous)
                    if (mention.normalized || mention.name.length() == 1)
                        continue;

                    if (sieveName.equals("AbbreviationExpansionSieve")) {
                        // Special case for abbreviation expansion sieve.
                        AbbreviationExpansionSieve sieve = (AbbreviationExpansionSieve) sieves.get(i);
                        sieve.apply(mention, doc);
                    } else if (sieveName.equals("AmbiguitySieve")) {
                        // Special case for AmbiguitySieve.
                        AmbiguitySieve sieve = (AmbiguitySieve) sieves.get(i);
                        sieve.apply(mention, doc);
                    } else {
                        // Default sieve apply.
                        sieves.get(i).apply(mention);
                    }

                    if (mention.normalized) {
                        // Set the sieve level at which the mention was normalized/ambiguous.
                        mention.normalizingSieveLevel = i + 1;
                        mention.normalizingSieveName = sieveName;

                        // Add normalized CUI to documentCuiNamesMap.
                        documentCuiNamesMap.addKeyPair(mention.cui, mention.name);

                        // Set alternate CUIs on mention. (Used only in evaluation)
                        if (trainTerminology.cuiAlternateCuiMap.containsKey(mention.cui)) {
                            Util.addUnique(mention.alternateCuis, trainTerminology.cuiAlternateCuiMap.get(mention.cui));
                        }
                        if (standardTerminology.cuiAlternateCuiMap.containsKey(mention.cui)) {
                            Util.addUnique(mention.alternateCuis,
                                    standardTerminology.cuiAlternateCuiMap.get(mention.cui));
                        }

                        // Drop out once we successfully normalize the mention.
                        // break;
                    }
                }
            }
            // resolveAmbiguity(doc, documentCuiNamesMap);
        }

        // Evaluate all mentions after completely done.
        for (Document doc : test_data) {
            for (Mention mention : doc.mentions) {
                // Debug only: Find the name corresponding to the gold mention CUI.
                appendGoldName(mention, trainTerminology.cuiToNameListMap);

                // This is also called in resolveAmbiguity.
                // Remove this line if using that method.
                eval.evaluateClassification(mention, doc);
            }
        }
    }

    private void appendGoldName(Mention mention, HashListMap trainMap) {
        // Find name for gold cui if available.
        if (standardTerminology.cuiToNameListMap.containsKey(mention.goldCui)) {
            for (var name : standardTerminology.cuiToNameListMap.get(mention.goldCui))
                Util.addUnique(mention.goldNames, name);
        }

        if (trainMap.containsKey(mention.goldCui)) {
            for (var name : trainMap.get(mention.goldCui))
                Util.addUnique(mention.goldNames, name);
        }
    }

    /**
     * Attempts to resolve ambiguity within a document before scoring performance on
     * a document.
     * 
     * @param doc
     * @param documentCuiNamesMap
     * @throws IOException
     */
    private void resolveAmbiguity(Document doc, HashListMap documentCuiNamesMap) throws IOException {
        for (Mention mention : doc.mentions) {

            eval.evaluateClassification(mention, doc);
            // // If mention was normalized (not by ExactMatchSieve) or definitively
            // CUI-less.
            // if (!mention.normalizingSieveName.equals("ExactMatchSieve") ||
            // mention.cui.equals("CUI-less")) {
            // eval.evaluateClassification(mention, doc);
            // continue;
            // }

            // // TODO: Handle multiple CUIs, prioritize CUI match in document.

            // // If mention absent or unambiguous in training data.
            // List<String> trainingDataCuis =
            // trainTerminology.nameToCuiListMap.get(mention.name);
            // if (trainingDataCuis == null || trainingDataCuis.size() == 1) {
            // eval.evaluateClassification(mention, doc);
            // // storeNormalizedConcept(mention);
            // continue;
            // }

            // String[] conceptNameTokens = mention.name.split("\\s+");
            // if (conceptNameTokens.length > 1)
            // mention.cui = "CUI-less";
            // else {
            // int countCUIMatch = 0;
            // for (String cui : trainingDataCuis) {
            // List<String> names = documentCuiNamesMap.containsKey(cui) ?
            // documentCuiNamesMap.get(cui) : new ArrayList<String>();
            // for (String name : names) {
            // String[] nameTokens = name.split("\\s+");
            // if (nameTokens.length == 1)
            // continue;
            // if (name.matches(mention.name + " .*")) {
            // countCUIMatch++;
            // }
            // }
            // }
            // if (countCUIMatch > 0)
            // mention.cui = "CUI-less";
            // // else
            // // storeNormalizedConcept(mention);
            // }
        }
    }

    /**
     * Loads stopwords from file into a list.
     * 
     * @throws IOException
     */
    private static List<String> loadStopwords() throws IOException {
        List<String> stopwords = new ArrayList<>();
        var file = new File("resources/stopwords.txt");
        BufferedReader in = new BufferedReader(new FileReader(file));
        while (in.ready()) {
            String s = in.readLine().trim();
            if (!s.equals(""))
                stopwords.add(s);
        }
        in.close();
        return stopwords;
    }

    /**
     * Creates a list of Document objects corresponding 1-1 with each file in the
     * given directory
     * 
     * @throws Exception
     */
    private List<Document> getDataSet(File dir) throws Exception {
        List<Document> dataset = new ArrayList<>();
        for (File file : dir.listFiles()) {
            // Only interested in .concept files, ignore .txt note files
            if (!file.toString().contains(".concept"))
                continue;

            dataset.add(new Document(file));
        }
        return dataset;
    }
}
