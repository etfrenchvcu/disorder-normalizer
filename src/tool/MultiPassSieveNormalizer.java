/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tool.sieves.*;
import tool.util.*;

/**
 *
 * @author
 */
public class MultiPassSieveNormalizer {

    boolean ncbi;
    int max_level;
    Evaluation eval;
    File test_data_dir;
    File train_data_dir;
    HashListMap normalizedNameToCuiListMap;
    HashListMap stemmedNormalizedNameToCuiListMap;
    Terminology standardTerminology;
    Terminology trainTerminology;
    ArrayList<Sieve> sieves;
    List<String> stopwords;

    /***
     * Initialize MultiPassSieveNormalizer and associated resources.
     * 
     * @param train_data_dir
     * @param test_data_dir
     * @param eval
     * @param max_level
     * @param standardTerminology
     * @throws Exception
     */
    public MultiPassSieveNormalizer(File train_data_dir, File test_data_dir, Evaluation eval, int max_level,
            File standardTerminologyFile) throws Exception {
        this.max_level = max_level;
        this.eval = eval;
        this.test_data_dir = test_data_dir;
        this.train_data_dir = train_data_dir;

        normalizedNameToCuiListMap = new HashListMap();

        // set stopwords, correct spellings, and abbreviations data
        // TODO: Revisit this to make more general
        ncbi = test_data_dir.toString().contains("ncbi") ? true : false;
        Ling.setSpellingCorrectionMap(new File("resources/spell-check.txt"));

        // Load training data terminology
        stopwords = loadStopwords();
        standardTerminology = new Terminology(standardTerminologyFile, ncbi, stopwords);
        trainTerminology = new Terminology(train_data_dir, ncbi, stopwords);
    }

    private ArrayList<Sieve> initializeSieves() throws IOException {
        ArrayList<Sieve> sieves = new ArrayList<Sieve>();

        sieves.add(new ExactMatchSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        sieves.add(new AbbreviationExpansionSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap,
                stopwords));
        sieves.add(new PrepositionalTransformSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        sieves.add(new NumberReplacementSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        sieves.add(new HyphenationSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        sieves.add(new AffixationSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        sieves.add(new DiseaseTermSynonymsSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        sieves.add(new StemmingSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap,
                new Stemmer(stopwords)));
        sieves.add(new CompoundPhraseSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        sieves.add(new PartialMatchSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap, stopwords));

        // Compound phrase sieve
        // mention.setCui(this.test_data_dir.toString().contains("ncbi") ?
        // CompoundPhraseSieve.applyNCBI(mention.getName())
        // : CompoundPhraseSieve.apply(mention.getName()));

        // // Sieve 10
        // mention.setCui(SimpleNameSieve.apply(mention));
        // pass(mention, ++currentSieveLevel);
        // --currentSieveLevel;
        // if (!mention.getCui().equals(""))
        //     return;
        // // Sieve 10
        // mention.setCui(this.test_data_dir.toString().contains("ncbi") ? PartialMatchNCBISieve.apply(mention)
        //         : PartialMatchSieve.apply(mention));
        // pass(mention, ++currentSieveLevel);

        return sieves;
    }

    /**
     * Runs the sieve algorithm over the input datasets.
     * 
     * @throws Exception
     */
    public void run() throws Exception {
        // Initialize sieves
        sieves = initializeSieves();

        // Apply sieve to test data.
        List<Document> test_data = getDataSet(this.test_data_dir);
        for (Document doc : test_data) {
            HashListMap cuiNamesMap = new HashListMap();

            for (Mention mention : doc.mentions) {

                // Progressively apply sieves until the mention is linked to a CUI.
                for (int i = 0; i < max_level; i++) {

                    if (sieves.get(i) instanceof AbbreviationExpansionSieve) {
                        // Special case for abbreviation expansion sieve.
                        AbbreviationExpansionSieve sieve = (AbbreviationExpansionSieve) sieves.get(i);
                        mention.cui = sieve.apply(mention, doc);
                    } else {
                        // Default sieve apply.
                        mention.cui = sieves.get(i).apply(mention);
                    }

                    // TODO: If abbreviation sieve, pass doc

                    // Drop out if we successfully normalize the mention
                    // TODO: We can probably drop out if a CUI is ambiguous in normalize step.
                    if (checkNormalized(mention, i + 1))
                        break;
                }

                if (mention.cui.equals(""))
                    mention.cui = "CUI-less";

                cuiNamesMap.addKeyPair(mention.cui, mention.name);
            }
            resolveAmbiguity(doc, cuiNamesMap);
        }
    }

    /**
     * Creates a list of Document objects corresponding 1-1 with each file in the
     * given directory
     */
    private List<Document> getDataSet(File dir) throws IOException {
        List<Document> dataset = new ArrayList<>();
        for (File file : dir.listFiles()) {
            // Only interested in .concept files, ignore .txt note files
            if (!file.toString().contains(".concept"))
                continue;

            File conceptFileAsTxt = new File(file.toString().replace(".concept", ".txt"));
            Document doc = new Document(conceptFileAsTxt, Util.read(conceptFileAsTxt));

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    String[] tokens = line.split("\\|\\|");
                    doc.addMention(tokens);
                }
            } catch (Exception e) {
                System.out.println("ERROR: Failed to read " + file.toString());
                throw e;
            }
            dataset.add(doc);
        }
        return dataset;
    }

    /**
     * If the CUI was annotated on a compound mention (multiple CUIs on a single
     * annotation), add all the CUIs on the shared annotation.
     * 
     * @param cui
     * @return
     */
    public List<String> getAlternateCuis(String cui) {
        List<String> alternateCuis = new ArrayList<>();
        if (trainTerminology.cuiAlternateCuiMap.containsKey(cui)) {
            alternateCuis.addAll(trainTerminology.cuiAlternateCuiMap.get(cui));
        }
        if (standardTerminology.cuiAlternateCuiMap.containsKey(cui)) {
            alternateCuis.addAll(standardTerminology.cuiAlternateCuiMap.get(cui));
        }
        return alternateCuis;
    }

    /**
     * Checks if we should continue (pass) to the next sieve.
     * 
     * @param mention
     * @param level
     * @return
     */
    private boolean checkNormalized(Mention mention, int level) {
        boolean normalized = false;
        if (!mention.cui.equals("")) {
            // TODO: Don't always return the list of alternate CUIs if there's another
            // option

            // Find alternate CUIs (from compound annotations).
            List<String> alternateCuis = new ArrayList<>();
            if (trainTerminology.cuiAlternateCuiMap.containsKey(mention.cui)) {
                alternateCuis.addAll(trainTerminology.cuiAlternateCuiMap.get(mention.cui));
            }
            if (standardTerminology.cuiAlternateCuiMap.containsKey(mention.cui)) {
                alternateCuis.addAll(standardTerminology.cuiAlternateCuiMap.get(mention.cui));
            }

            // Set alternate CUIs on the mention
            // TODO: make sure these are distinct and !="" upstream
            mention.alternateCuis = alternateCuis;

            // Set the sieve level at which the mention was normalized.
            mention.normalizingSieveLevel = level;

            // Add the normalized mention to the running dictionary.
            storeNormalizedConcept(mention);

            normalized = true;
        }

        return normalized;
    }

    /**
     * Add normalized mention to a dictionary for reference normalizing other
     * mentions
     * 
     * @param mention
     */
    public void storeNormalizedConcept(Mention mention) {
        // Store the normalized name and expansion.
        normalizedNameToCuiListMap.addKeyPair(mention.name, mention.cui);
        normalizedNameToCuiListMap.addKeyPair(mention.nameExpansion, mention.cui);
    }

    private void resolveAmbiguity(Document document, HashListMap cuiNamesMap) throws IOException {
        for (Mention mention : document.mentions) {
            if (mention.normalizingSieveLevel != 1 || mention.cui.equals("CUI-less")) {
                eval.evaluateClassification(mention, document);
                // storeNormalizedConcept(mention);
                continue;
            }

            String[] conceptNameTokens = mention.name.split("\\s+");

            // Get matches from train data
            List<String> trainingDataCuis = trainTerminology.nameToCuiListMap.get(mention.name);
            if (trainingDataCuis == null || trainingDataCuis.size() == 1) {
                eval.evaluateClassification(mention, document);
                // storeNormalizedConcept(mention);
                continue;
            }

            if (conceptNameTokens.length > 1)
                mention.cui = "CUI-less";
            else {
                int countCUIMatch = 0;
                for (String cui : trainingDataCuis) {
                    List<String> names = cuiNamesMap.containsKey(cui) ? cuiNamesMap.get(cui) : new ArrayList<String>();
                    for (String name : names) {
                        String[] nameTokens = name.split("\\s+");
                        if (nameTokens.length == 1)
                            continue;
                        if (name.matches(mention.name + " .*")) {
                            countCUIMatch++;
                        }
                    }
                }
                if (countCUIMatch > 0)
                    mention.cui = "CUI-less";
                // else
                // storeNormalizedConcept(mention);
            }
            eval.evaluateClassification(mention, document);
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
}
