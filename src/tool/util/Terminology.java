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
import java.util.List;

/**
 *
 * @author
 */
public class Terminology {
    boolean usingPartialMatch = true;
    List<String> stopwords;
    public HashListMap tokenToNameListMap;
    public HashListMap nameToCuiListMap;
    public HashListMap cuiToNameListMap;
    public HashListMap cuiAlternateCuiMap;

    /**
     * Initializes Terminology object from either a single terminology.txt file or a
     * training data directory. Maps each CUI to multiple names/synonyms.
     * @param path
     * @param stopwords
     * @param usingPartialMatch
     * @throws Exception
     */
    public Terminology(File path, List<String> stopwords, boolean usingPartialMatch) throws Exception {
        this.stopwords = stopwords;
        this.usingPartialMatch = usingPartialMatch;
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
        initializeMaps();
    }

    /**
     * Initialize maps in constructors.
     */
    private void initializeMaps() {
        tokenToNameListMap = new HashListMap();
        nameToCuiListMap = new HashListMap();
        cuiToNameListMap = new HashListMap();
        cuiAlternateCuiMap = new HashListMap();
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

        // Create a hash map of names keyed on non-stop tokens they contain.
        // Only necessary if using PartialMatchSieve.
        if(usingPartialMatch) {
            String[] nameTokens = name.split("\\s");
            for (String token : nameTokens) {
                if (!stopwords.contains(token))
                    tokenToNameListMap.addKeyPair(token, name);
            }
    
            if (cui.contains("|")) {
                nameToCuiListMap.remove(name);
                for (String conceptNameToken : nameTokens) {
                    if (stopwords.contains(conceptNameToken))
                        tokenToNameListMap.remove(conceptNameToken, name);
                }
            }
        }        
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

    /**
     * Used for OMIM annotations in NCBI.
     * 
     * @param cuis
     * @param MeSHorSNOMEDcuis
     * @param conceptName
     */
    private void setOMIM(String cuis, String MeSHorSNOMEDcuis, String conceptName) {
        if (MeSHorSNOMEDcuis.equals("")) {
            cuis = cuis.replaceAll("OMIM:", "");
            loadConceptMaps(conceptName, cuis);
        } else {
            String[] cuis_arr = cuis.split("\\|");
            for (String cui : cuis_arr) {
                if (cui.contains("OMIM")) {
                    cui = cui.split(":")[1];
                    cuiAlternateCuiMap.addKeyPair(MeSHorSNOMEDcuis, cui);
                }
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
}
