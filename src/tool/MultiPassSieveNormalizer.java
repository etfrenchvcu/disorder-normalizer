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

import tool.sieves.AffixationSieve;
import tool.sieves.CompoundPhraseSieve;
import tool.sieves.DiseaseModifierSynonymsSieve;
import tool.sieves.HyphenationSieve;
import tool.sieves.PartialMatchNCBISieve;
import tool.sieves.PartialMatchSieve;
import tool.sieves.PrepositionalTransformSieve;
import tool.sieves.Sieve;
import tool.sieves.SimpleNameSieve;
import tool.sieves.StemmingSieve;
import tool.sieves.SymbolReplacementSieve;
import tool.util.Abbreviation;
import tool.util.Document;
import tool.util.HashListMap;
import tool.util.Ling;
import tool.util.Mention;
import tool.util.Terminology;
import tool.util.Util;

/**
 *
 * @author
 */
public class MultiPassSieveNormalizer {
     
    int max_level;
    File output_data_dir;
    File test_data_dir;
    File train_data_dir;
    Terminology terminology;
    
    /***
     * Initialize MultiPassSieveNormalizer and associated resources.
     * @param train_data_dir
     * @param test_data_dir
     * @param output_data_dir
     * @param max_level
     * @param terminology
     * @throws IOException
     */
    public MultiPassSieveNormalizer(File train_data_dir, File test_data_dir, File output_data_dir, int max_level, Terminology terminology) throws IOException {
        this.max_level = max_level;
        this.output_data_dir = output_data_dir;
        this.terminology = terminology;
        this.test_data_dir = test_data_dir;
        this.train_data_dir = train_data_dir;

        // set stopwords, correct spellings, and abbreviations data
        // TODO: Revisit this to make more general
        boolean ncbi = test_data_dir.toString().contains("ncbi") ? true : false;
        Ling.setSpellingCorrectionMap(ncbi ? new File("resources/ncbi-spell-check.txt") : new File("resources/semeval-spell-check.txt"));
        Ling.setStopwordsList(new File("resources/stopwords.txt"));
        Abbreviation.setWikiAbbreviationExpansionMap(ncbi ? new File("resources/ncbi-wiki-abbreviations.txt") : new File("resources/semeval-wiki-abbreviations.txt"));
        Ling.setDigitToWordformMapAndReverse(new File("resources/number.txt"));
        Ling.setSuffixMap(new File("resources/suffix.txt"));
        Ling.setPrefixMap(new File("resources/prefix.txt"));
        Ling.setAffixMap(new File("resources/affix.txt")); 
    }

    /**
     * Runs the sieve algorithm over the input datasets.
     * @throws IOException
     */
    public void run() throws IOException {
        // What's this???
        Sieve.setStandardTerminology();
        Sieve.setTrainingDataTerminology(this.train_data_dir);

        // Apply sieve to test data.
        List<Document> test_data = getDataSet(this.test_data_dir);
        for (Document concepts : test_data) {
            HashListMap cuiNamesMap = new HashListMap();

            for (Mention concept : concepts.getMentions()) {
                applyMultiPassSieve(concept);
                if (concept.getCui().equals(""))
                    concept.setCui("CUI-less");

                cuiNamesMap.addKeyPair(concept.getCui(), concept.getName());
            }
            //TODO
            // AmbiguityResolution.start(concepts, cuiNamesMap);
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

    
    
    private boolean pass(Mention concept, int currentSieveLevel) {
        if (!concept.getCui().equals("")) {
            concept.setAlternateCuis(Sieve.getAlternateCuis(concept.getCui()));
            concept.setNormalizingSieveLevel(currentSieveLevel-1);
            //Terminology.storeNormalizedConcept(concept);
            return false;
        }
        
        if (currentSieveLevel > this.max_level)
            return false;
        
        return true;
    }
        
    /**
     * Progressively apply sieve levels to a given Mention object.
     * @param mention
     */
    private void applyMultiPassSieve(Mention mention) {
        int currentSieveLevel = 1;
        //match with names in training data
        //Sieve 1        
        mention.setCui(Sieve.exactMatchSieve(mention.getName()));        
        if (!pass(mention, ++currentSieveLevel))
            return;
        
        //Sieve 2
        mention.setCui(Sieve.exactMatchSieve(mention.getNameExpansion()));
        if (!pass(mention, ++currentSieveLevel))
            return;

        //Sieve 3
        mention.setCui(PrepositionalTransformSieve.apply(mention));
        if (!pass(mention, ++currentSieveLevel))
            return;
        
        //Sieve 4
        mention.setCui(SymbolReplacementSieve.apply(mention));
        if (!pass(mention, ++currentSieveLevel))
            return;
        
        //Sieve 5
        mention.setCui(HyphenationSieve.apply(mention));
        if (!pass(mention, ++currentSieveLevel)) {            
            return;  
        }
        
        //Sieve 6
        mention.setCui(AffixationSieve.apply(mention));
        if (!pass(mention, ++currentSieveLevel))
            return;        
        
        //Sieve 7
        mention.setCui(DiseaseModifierSynonymsSieve.apply(mention));
        if (!pass(mention, ++currentSieveLevel)) {            
            return;                  
        }
        
        //Sieve 8
        mention.setCui(StemmingSieve.apply(mention));
        if (!pass(mention, ++currentSieveLevel))
            return;       
        
        //Sieve 9
        mention.setCui(this.test_data_dir.toString().contains("ncbi") ? CompoundPhraseSieve.applyNCBI(mention.getName()) : CompoundPhraseSieve.apply(mention.getName()));
        if (!pass(mention, ++currentSieveLevel)) {            
            return;         
        }
                
        //Sieve 10
        mention.setCui(SimpleNameSieve.apply(mention));
        pass(mention, ++currentSieveLevel);
        --currentSieveLevel;
        if (!mention.getCui().equals(""))
            return;                 
        //Sieve 10
        mention.setCui(this.test_data_dir.toString().contains("ncbi") ? PartialMatchNCBISieve.apply(mention) : PartialMatchSieve.apply(mention));
        pass(mention, ++currentSieveLevel);       
    }
                    
}
