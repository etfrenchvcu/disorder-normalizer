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
    
    /***
     * Initialize MultiPassSieveNormalizer and associated resources.
     * @param train_data_dir
     * @param test_data_dir
     * @param eval
     * @param max_level
     * @param standardTerminology
     * @throws Exception
     */
    public MultiPassSieveNormalizer(File train_data_dir, File test_data_dir, Evaluation eval, int max_level, Terminology standardTerminology) throws Exception {
        this.max_level = max_level;
        this.eval = eval;
        this.standardTerminology = standardTerminology;
        this.test_data_dir = test_data_dir;
        this.train_data_dir = train_data_dir;

        normalizedNameToCuiListMap = new HashListMap();
        stemmedNormalizedNameToCuiListMap = new HashListMap();

        // set stopwords, correct spellings, and abbreviations data
        // TODO: Revisit this to make more general
        ncbi = test_data_dir.toString().contains("ncbi") ? true : false;
        Ling.setSpellingCorrectionMap(ncbi ? new File("resources/ncbi-spell-check.txt") : new File("resources/semeval-spell-check.txt"));
        Ling.setStopwordsList(new File("resources/stopwords.txt"));
        Abbreviation.setWikiAbbreviationExpansionMap(ncbi ? new File("resources/ncbi-wiki-abbreviations.txt") : new File("resources/semeval-wiki-abbreviations.txt"));
        Ling.setDigitToWordformMapAndReverse(new File("resources/number.txt"));
        Ling.setSuffixMap(new File("resources/suffix.txt"));
        Ling.setPrefixMap(new File("resources/prefix.txt"));
        Ling.setAffixMap(new File("resources/affix.txt")); 

        // Load training data terminology
        trainTerminology = new Terminology(train_data_dir, ncbi);
    }

    private ArrayList<Sieve> initializeSieves(Terminology trainTerminology) {
        ArrayList<Sieve> sieves = new ArrayList<Sieve>();

        sieves.add(new ExactMatchSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        sieves.add(new AbbreviationExpansionSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        sieves.add(new PrepositionalTransformSieve(standardTerminology, trainTerminology, normalizedNameToCuiListMap));
        //TODO: Add additional sieves
        
        // //Sieve 4
        // mention.setCui(SymbolReplacementSieve.apply(mention));
        
        // //Sieve 5
        // mention.setCui(HyphenationSieve.apply(mention));
        
        // //Sieve 6
        // mention.setCui(AffixationSieve.apply(mention));     
        
        // //Sieve 7
        // mention.setCui(DiseaseModifierSynonymsSieve.apply(mention));
        
        // //Sieve 8
        // mention.setCui(StemmingSieve.apply(mention));     
        
        // //Sieve 9
        // mention.setCui(this.test_data_dir.toString().contains("ncbi") ? CompoundPhraseSieve.applyNCBI(mention.getName()) : CompoundPhraseSieve.apply(mention.getName()));
                
        // //Sieve 10
        // mention.setCui(SimpleNameSieve.apply(mention));
        // pass(mention, ++currentSieveLevel);
        // --currentSieveLevel;
        // if (!mention.getCui().equals(""))
        //     return;                 
        // //Sieve 10
        // mention.setCui(this.test_data_dir.toString().contains("ncbi") ? PartialMatchNCBISieve.apply(mention) : PartialMatchSieve.apply(mention));
        // pass(mention, ++currentSieveLevel);  

        return sieves;
    }

    /**
     * Runs the sieve algorithm over the input datasets.
     * @throws Exception
     */
    public void run() throws Exception {
        // Initialize sieves
        sieves = initializeSieves(trainTerminology);

        // Apply sieve to test data.
        List<Document> test_data = getDataSet(this.test_data_dir);
        for (Document document : test_data) {
            HashListMap cuiNamesMap = new HashListMap();

            for (Mention mention : document.mentions) {
                applyMultiPassSieve(mention);
                if (mention.cui.equals(""))
                    mention.cui = "CUI-less";

                cuiNamesMap.addKeyPair(mention.cui, mention.name);
            }
            resolveAmbiguity(document, cuiNamesMap);
        }
    }

    /**
     * Creates a list of Document objects corresponding 1-1 with each file in the given directory
     */
    private List<Document> getDataSet(File dir) throws IOException {
        List<Document> dataset = new ArrayList<>();
        for (File file : dir.listFiles()) {
            // Only interested in .concept files, ignore .txt note files
            if (!file.toString().contains(".concept"))
                continue;

            File textFile = new File(file.toString().replace(".concept", ".txt"));
            var abbreviationMap = Abbreviation.getTextAbbreviationExpansionMapFromFile(textFile); 
            Document doc = new Document(textFile.getName(), Util.read(textFile));
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    String[] tokens = line.split("\\|\\|");
                    doc.addMention(tokens, abbreviationMap);
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
     * If the CUI was annotated on a compound mention (multiple CUIs on a single annotation), add all the CUIs on the shared annotation.
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
     * @param mention
     * @param level
     * @return
     */
    private boolean checkNormalized(Mention mention, int level) {
        boolean normalized = false;
        if (!mention.cui.equals("")) {
            // TODO: Don't always return the list of alternate CUIs if there's another option

            // Find alternate CUIs (from compound annotations).
            List<String> alternateCuis = new ArrayList<>();
            if (trainTerminology.cuiAlternateCuiMap.containsKey(mention.cui)) {
                alternateCuis.addAll(trainTerminology.cuiAlternateCuiMap.get(mention.cui));
            }
            if (standardTerminology.cuiAlternateCuiMap.containsKey(mention.cui)) {
                alternateCuis.addAll(standardTerminology.cuiAlternateCuiMap.get(mention.cui));
            }

            // Set alternate CUIs on the mention
            //TODO: make sure these are distinct and !="" upstream
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
     * Progressively apply sieve levels to a given Mention object.
     * @param mention
     */
    private void applyMultiPassSieve(Mention mention) {
        for (int i=0; i<max_level; i++) {
            mention.cui = sieves.get(i).apply(mention);

            // Drop out if we successfully normalize the mention
            // TODO: We can probably drop out if a CUI is ambiguous in normalize step.
            if(checkNormalized(mention, i+1))
                return;
        }

        
        // //match with names in training data
        // //Sieve 1        
        // mention.cui = Sieve.exactMatch(mention.name);
        // if (!pass(mention, ++currentSieveLevel))
        //     return;
        
        // //Sieve 2
        // mention.setCui(Sieve.exactMatchSieve(mention.getNameExpansion()));
        // if (!pass(mention, ++currentSieveLevel))
        //     return;

        // //Sieve 3
        // mention.setCui(PrepositionalTransformSieve.apply(mention));
        // if (!pass(mention, ++currentSieveLevel))
        //     return;
        
        // //Sieve 4
        // mention.setCui(SymbolReplacementSieve.apply(mention));
        // if (!pass(mention, ++currentSieveLevel))
        //     return;
        
        // //Sieve 5
        // mention.setCui(HyphenationSieve.apply(mention));
        // if (!pass(mention, ++currentSieveLevel)) {            
        //     return;  
        // }
        
        // //Sieve 6
        // mention.setCui(AffixationSieve.apply(mention));
        // if (!pass(mention, ++currentSieveLevel))
        //     return;        
        
        // //Sieve 7
        // mention.setCui(DiseaseModifierSynonymsSieve.apply(mention));
        // if (!pass(mention, ++currentSieveLevel)) {            
        //     return;                  
        // }
        
        // //Sieve 8
        // mention.setCui(StemmingSieve.apply(mention));
        // if (!pass(mention, ++currentSieveLevel))
        //     return;       
        
        // //Sieve 9
        // mention.setCui(this.test_data_dir.toString().contains("ncbi") ? CompoundPhraseSieve.applyNCBI(mention.getName()) : CompoundPhraseSieve.apply(mention.getName()));
        // if (!pass(mention, ++currentSieveLevel)) {            
        //     return;         
        // }
                
        // //Sieve 10
        // mention.setCui(SimpleNameSieve.apply(mention));
        // pass(mention, ++currentSieveLevel);
        // --currentSieveLevel;
        // if (!mention.getCui().equals(""))
        //     return;                 
        // //Sieve 10
        // mention.setCui(this.test_data_dir.toString().contains("ncbi") ? PartialMatchNCBISieve.apply(mention) : PartialMatchSieve.apply(mention));
        // pass(mention, ++currentSieveLevel);       
    }

    /**
     * Add normalized mention to a dictionary for reference normalizing other mentions
     * @param mention
     */
    public void storeNormalizedConcept(Mention mention) {
        //TODO: what is this???
        String normalizedName = mention.normalizingSieveLevel == 2 ? mention.getNameExpansion() : mention.name;
        String stemmedNormalizedName = mention.normalizingSieveLevel == 2 ? Ling.getStemmedPhrase(mention.getNameExpansion()) : mention.getStemmedName();

        normalizedNameToCuiListMap.addKeyPair(normalizedName, mention.cui);
        stemmedNormalizedNameToCuiListMap.addKeyPair(stemmedNormalizedName, mention.cui);
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
                        if (name.matches(mention.name+" .*")) {
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
}
