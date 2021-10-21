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
 *
 * @author
 */
public class Terminology {

    public Stemmer stemmer;
    List<String> stopwords;
    public HashListMap tokenToNameListMap;
    public HashListMap nameToCuiListMap;
    public HashListMap simpleNameToCuiListMap;
    public HashListMap compoundNameToCuiListMap;
    public HashListMap cuiToNameListMap;
    public HashListMap stemmedNameToCuiListMap;
    public HashListMap cuiToStemmedNameListMap;
    public HashListMap cuiAlternateCuiMap;
    public static Map<String, HashListMap> cuiNameFileListMap;

    /**
     * Initializes Terminology object from either a single terminology.txt file or a
     * training data directory. Maps each CUI to multiple names/synonyms.
     * 
     * @param path
     * @throws Exception
     */
    public Terminology(File path, List<String> stopwords) throws Exception {
        this.stopwords = stopwords;
        stemmer = new Stemmer(stopwords);
        initializeMaps();

        if (path.isFile())
            loadTerminologyFile(path);
        else if (path.isDirectory())
            loadTrainingDataTerminology(path);
        else
            throw new Exception("Given path is neither file nor directory: " + path.toString());
    }

    /**
     * Dummy constructor for unit testing.
     */
    public Terminology(List<String> stopwords) {
        this.stopwords = stopwords;
        stemmer = new Stemmer(stopwords);
        initializeMaps();
    }

    /**
     * Initialize maps in constructors.
     */
    private void initializeMaps() {
        tokenToNameListMap = new HashListMap();
        nameToCuiListMap = new HashListMap();
        simpleNameToCuiListMap = new HashListMap();
        compoundNameToCuiListMap = new HashListMap();
        cuiToNameListMap = new HashListMap();
        stemmedNameToCuiListMap = new HashListMap();
        cuiToStemmedNameListMap = new HashListMap();
        cuiAlternateCuiMap = new HashListMap();
        cuiNameFileListMap = new HashMap<>();
    }

    /**
     * Process terminology file and load maps.
     * 
     * @param file
     * @throws IOException
     */
    private void loadTerminologyFile(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals(""))
                    continue;
                String[] token = line.split("\\|\\|");

                // Records with multiple CUIs separate them with a pipe.
                String cui = token[0].contains("|") ? get_preferredID_set_altID(token[0].split("\\|")) : token[0];

                // List of concept names/synonyms
                String[] names = token[1].toLowerCase().split("\\|");

                for (String name : names)
                    loadConceptMaps(name, cui);
            }
        }
    }

    /**
     * TODO: Not all of these need to be loaded depending on the max_sieve_level
     * 
     * @param name
     * @param cui
     */
    public void loadConceptMaps(String name, String cui) {
        nameToCuiListMap.addKeyPair(name, cui);
        cuiToNameListMap.addKeyPair(cui, name);

        String stemmedName = stemmer.stem(name);
        stemmedNameToCuiListMap.addKeyPair(stemmedName, cui);
        cuiToStemmedNameListMap.addKeyPair(cui, stemmedName);

        // Create a hash map of names keyed on non-stop tokens they contain (used in
        // PartialMatchSieve).
        String[] nameTokens = name.split("\\s");
        for (String token : nameTokens) {
            if (stopwords.contains(token))
                continue;
            tokenToNameListMap.addKeyPair(token, name);
        }

        // TODO: Remove this.
        var ncbi = true;
        if (!ncbi) {
            setCompoundNameTerminology(this, name, nameTokens, cui);
        } else if (cui.contains("|")) {
            nameToCuiListMap.remove(name);
            stemmedNameToCuiListMap.remove(stemmedName);
            for (String conceptNameToken : nameTokens) {
                if (stopwords.contains(conceptNameToken))
                    continue;
                tokenToNameListMap.remove(conceptNameToken, name);
            }
            compoundNameToCuiListMap.addKeyPair(name, cui);
        }
    }

    public void setCompoundNameToCuiListMap(String name, String cui) {
        compoundNameToCuiListMap.addKeyPair(name, cui);
    }

    private String get_preferredID_set_altID(String[] identifiers) {
        String preferredID = "";
        boolean set = false;

        for (int i = 0; i < identifiers.length; i++) {
            if (identifiers[i].contains("OMIM"))
                identifiers[i] = identifiers[i].split(":")[1];
            if (i == 0)
                preferredID = identifiers[i];
            if (Character.isLetter(identifiers[i].charAt(0)) && set == false) {
                preferredID = identifiers[i];
                set = true;
                continue;
            }
            cuiAlternateCuiMap.addKeyPair(preferredID, identifiers[i]);
        }

        return preferredID;
    }

    private void setOMIM(String cuis, String MeSHorSNOMEDcuis, String conceptName) {
        if (MeSHorSNOMEDcuis.equals("")) {
            cuis = cuis.replaceAll("OMIM:", "");
            loadConceptMaps(conceptName, cuis);
        } else {
            String[] cuis_arr = cuis.split("\\|");
            for (String cui : cuis_arr) {
                if (!cui.contains("OMIM"))
                    continue;
                cui = cui.split(":")[1];
                cuiAlternateCuiMap.addKeyPair(MeSHorSNOMEDcuis, cui);
            }
        }
    }

    public static String getMeSHorSNOMEDCuis(String[] cuis) {
        String cuiStr = "";
        for (String cui : cuis) {
            if (cui.contains("OMIM"))
                continue;
            cuiStr = cuiStr.equals("") ? cui : cuiStr + "|" + cui;
        }
        return cuiStr;
    }

    public void loadTrainingDataTerminology(File dir) throws IOException {
        HashListMap cuiNamesMap;
        for (File file : dir.listFiles()) {
            if (!file.toString().contains(".concept"))
                continue;
            cuiNamesMap = new HashListMap();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    String[] tokens = line.split("\\|\\|");
                    String name = Util.correctSpelling(tokens[3].toLowerCase().trim());
                    String[] cuis = tokens[4].contains("+") ? tokens[4].split("\\+") : tokens[4].split("\\|");
                    String MeSHorSNOMEDcuis = getMeSHorSNOMEDCuis(cuis);
                    if (!MeSHorSNOMEDcuis.equals(""))
                        loadConceptMaps(name, MeSHorSNOMEDcuis);
                    setOMIM(tokens[4], MeSHorSNOMEDcuis, name);
                    String cui = !MeSHorSNOMEDcuis.equals("") ? MeSHorSNOMEDcuis : tokens[4].replaceAll("OMIM:", "");

                    cuiNamesMap.addKeyPair(cui, name);

                    // -------------remove-------------------------------
                    HashListMap nameFileListMap = cuiNameFileListMap.get(MeSHorSNOMEDcuis);
                    if (nameFileListMap == null)
                        cuiNameFileListMap.put(MeSHorSNOMEDcuis, nameFileListMap = new HashListMap());
                    nameFileListMap.addKeyPair(name, tokens[0]);
                    // -------------remove-------------------------------

                    List<String> simpleConceptNames = getTerminologySimpleNames(name.split("\\s+"));
                    for (String simpleConceptName : simpleConceptNames)
                        simpleNameToCuiListMap.addKeyPair(simpleConceptName, cui);

                }
            }

            // TODO: This section doesn't make sense...
            var ncbi = true;
            if (ncbi)
                continue;
            for (String cui : cuiNamesMap.keySet()) {
                List<String> names = cuiNamesMap.get(cui);
                List<String> namesToPrune = new ArrayList<>();
                for (String name : names) {
                    String[] nameTokens = name.split("\\s+");
                    if (nameTokens.length < 3)
                        continue;
                    if (names.contains(nameTokens[0] + " " + nameTokens[1]))
                        Util.addUnique(namesToPrune, nameTokens[0] + " " + nameTokens[1]);
                    else if (names
                            .contains(nameTokens[nameTokens.length - 2] + " " + nameTokens[nameTokens.length - 1]))
                        Util.addUnique(namesToPrune,
                                nameTokens[nameTokens.length - 2] + " " + nameTokens[nameTokens.length - 1]);
                }
                for (String nameToPrune : namesToPrune) {
                    nameToCuiListMap.remove(nameToPrune);
                    cuiToNameListMap.get(cui).remove(nameToPrune);
                    stemmedNameToCuiListMap.remove(stemmer.stem(nameToPrune));
                    cuiToStemmedNameListMap.get(cui).remove(stemmer.stem(nameToPrune));
                    String[] nameToPruneTokens = nameToPrune.split("\\s+");
                    for (String nameToPruneToken : nameToPruneTokens) {
                        if (stopwords.contains(nameToPruneToken))
                            continue;
                        tokenToNameListMap.get(nameToPruneToken).remove(nameToPrune);
                    }
                }
            }
        }
    }

    private void setCompoundNameTerminology(Terminology terminology, String conceptName, String[] conceptNameTokens,
            String cui) {
        if (conceptName.contains("and/or")) {
            List<Integer> indexes = getTokenIndexes(conceptNameTokens, "and/or");
            if (indexes.size() == 1) {
                int index = indexes.get(0);
                if (conceptName.matches("[a-zA-Z]+, [a-zA-Z]+ and/or [a-zA-Z]+.*")) {
                    String replacement1 = conceptNameTokens[index - 2].replace(",", "");
                    String replacement2 = conceptNameTokens[index - 1];
                    String replacement3 = conceptNameTokens[index + 1];
                    String phrase = replacement1 + ", " + replacement2 + " " + conceptNameTokens[index] + " "
                            + replacement3;

                    terminology.setCompoundNameToCuiListMap(conceptName.replace(phrase, replacement1), cui);
                    terminology.setCompoundNameToCuiListMap(conceptName.replace(phrase, replacement2), cui);
                    terminology.setCompoundNameToCuiListMap(conceptName.replace(phrase, replacement3), cui);
                } else {
                    String replacement1 = conceptNameTokens[index - 1];
                    String replacement2 = conceptNameTokens.length - 1 == index + 2
                            ? conceptNameTokens[index + 1] + " " + conceptNameTokens[index + 2]
                            : conceptNameTokens[index + 1];
                    String phrase = replacement1 + " " + conceptNameTokens[index] + " " + replacement2;
                    terminology.setCompoundNameToCuiListMap(conceptName.replace(phrase, replacement1), cui);
                    terminology.setCompoundNameToCuiListMap(conceptName.replace(phrase, replacement2), cui);
                }
            }
        }
    }

    /**
     * Get a list of all indexes where the given token appears in the array.
     * 
     * @param tokens
     * @param token
     * @return
     */
    private List<Integer> getTokenIndexes(String[] tokens, String token) {
        List<Integer> indexes = new ArrayList<>();
        int i = 0;
        while (i < tokens.length) {
            if (tokens[i].equals(token))
                indexes.add(i);
            i++;
        }
        return indexes;
    }

    private List<String> getTerminologySimpleNames(String[] phraseTokens) {
        List<String> newPhrases = new ArrayList<>();
        if (phraseTokens.length == 3) {
            String newPhrase = phraseTokens[0] + " " + phraseTokens[2];
            Util.addUnique(newPhrases, newPhrase);
            newPhrase = phraseTokens[1] + " " + phraseTokens[2];
            Util.addUnique(newPhrases, newPhrase);
        }
        return newPhrases;
    }
}
