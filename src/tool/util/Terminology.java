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

import tool.Main;
import tool.sieves.CompoundPhraseSieve;
import tool.sieves.SimpleNameSieve;

/**
 *
 * @author
 */
public class Terminology {

    private HashListMap tokenToNameListMap;
    private HashListMap nameToCuiListMap;
    private HashListMap simpleNameToCuiListMap;
    private HashListMap compoundNameToCuiListMap;
    private HashListMap cuiToNameListMap;
    private HashListMap stemmedNameToCuiListMap;
    private HashListMap cuiToStemmedNameListMap;
    private HashListMap cuiAlternateCuiMap;
    public static Map<String, HashListMap> cuiNameFileListMap;

    /**
     * Initializes Terminology object from a file. Maps each CUI to multiple names/synonyms.
     * @param file
     * @throws IOException
     */
    public Terminology(File file) throws IOException {
        // Initialize maps
        tokenToNameListMap = new HashListMap();
        nameToCuiListMap = new HashListMap();
        simpleNameToCuiListMap = new HashListMap();
        compoundNameToCuiListMap = new HashListMap();
        cuiToNameListMap = new HashListMap();
        stemmedNameToCuiListMap = new HashListMap();
        cuiToStemmedNameListMap = new HashListMap();
        cuiAlternateCuiMap = new HashListMap();
        cuiNameFileListMap = new HashMap<>();

        loadTerminologyFile(file);
    }

    /**
     * Process terminology file and load maps.
     * @param file
     */
    private void loadTerminologyFile(File file) {
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
                    loadMaps(name, cui);
            }
        }
    }

    /**
     * TODO: Not all of these need to be loaded depending on the max_sieve_level
     * @param name
     * @param cui
     * @param ncbi
     */
    private void loadConceptMaps(String name, String cui, boolean ncbi) {
        nameToCuiListMap.addKeyPair(name, cui);
        cuiToNameListMap.addKeyPair(cui, name);

        String stemmedName = Ling.getStemmedPhrase(name);
        stemmedNameToCuiListMap.addKeyPair(stemmedName, cui);
        cuiToStemmedNameListMap.addKeyPair(cui, stemmedName);

        // Tokenize concept name and remove stopwords
        String[] nameTokens = name.split("\\s");
        for (String token : nameTokens) {
            if (Ling.getStopwordsList().contains(token))
                continue;
            tokenToNameListMap.addKeyPair(token, name);
        }

        if (!ncbi) {
            CompoundPhraseSieve.setCompoundNameTerminology(this, name, nameTokens, cui);
        } else if (cui.contains("|")) {
            nameToCuiListMap.remove(name);
            stemmedNameToCuiListMap.remove(stemmedName);
            for (String conceptNameToken : nameTokens) {
                if (Ling.getStopwordsList().contains(conceptNameToken))
                    continue;
                tokenToNameListMap.remove(conceptNameToken, name);
            }
            compoundNameToCuiListMap.addKeyPair(name, cui);
        }
    }

    public Map<String, List<String>> getTokenToNameListMap() {
        return tokenToNameListMap;
    }

    public Map<String, List<String>> getNameToCuiListMap() {
        return nameToCuiListMap;
    }

    public Map<String, List<String>> getSimpleNameToCuiListMap() {
        return simpleNameToCuiListMap;
    }

    public Map<String, List<String>> getCompoundNameToCuiListMap() {
        return compoundNameToCuiListMap;
    }

    public void setCompoundNameToCuiListMap(String name, String cui) {
        compoundNameToCuiListMap = Util.setMap(compoundNameToCuiListMap, name, cui);
    }

    public Map<String, List<String>> getCuiToNameListMap() {
        return cuiToNameListMap;
    }

    public Map<String, List<String>> getStemmedNameToCuiListMap() {
        return stemmedNameToCuiListMap;
    }

    public Map<String, List<String>> getCuiToStemmedNameListMap() {
        return cuiToStemmedNameListMap;
    }

    public Map<String, List<String>> getCuiAlternateCuiMap() {
        return cuiAlternateCuiMap;
    }

    private static String get_preferredID_set_altID(String[] identifiers) {
        String preferredID = "";
        boolean set = false;
        List<String> altIDs = new ArrayList<>();

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
            altIDs.add(identifiers[i]);
        }

        if (!altIDs.isEmpty()) {
            cuiAlternateCuiMap.put(preferredID, altIDs);
        }

        return preferredID;
    }

    private void setOMIM(String cuis, String MeSHorSNOMEDcuis, String conceptName) {
        if (MeSHorSNOMEDcuis.equals("")) {
            cuis = cuis.replaceAll("OMIM:", "");
            loadMaps(conceptName, cuis);
        } else {
            String[] cuis_arr = cuis.split("\\|");
            for (String cui : cuis_arr) {
                if (!cui.contains("OMIM"))
                    continue;
                cui = cui.split(":")[1];
                cuiAlternateCuiMap = Util.setMap(cuiAlternateCuiMap, MeSHorSNOMEDcuis, cui);
            }
        }
    }

    public static List<String> getOMIMCuis(String[] cuis) {
        List<String> OMIMcuis = new ArrayList<>();
        for (String cui : cuis) {
            if (!cui.contains("OMIM"))
                continue;
            cui = cui.split(":")[1];
            OMIMcuis = Util.setList(OMIMcuis, cui);
        }
        return OMIMcuis;
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
        Map<String, List<String>> cuiNamesMap = new HashMap<>();
        for (File file : dir.listFiles()) {
            if (!file.toString().contains(".concept"))
                continue;
            cuiNamesMap = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    String[] tokens = line.split("\\|\\|");
                    String conceptName = Ling.correctSpelling(tokens[3].toLowerCase().trim());
                    String[] cuis = tokens[4].contains("+") ? tokens[4].split("\\+") : tokens[4].split("\\|");
                    String MeSHorSNOMEDcuis = getMeSHorSNOMEDCuis(cuis);
                    if (!MeSHorSNOMEDcuis.equals(""))
                        loadMaps(conceptName, MeSHorSNOMEDcuis);
                    setOMIM(tokens[4], MeSHorSNOMEDcuis, conceptName);
                    String cui = !MeSHorSNOMEDcuis.equals("") ? MeSHorSNOMEDcuis : tokens[4].replaceAll("OMIM:", "");

                    cuiNamesMap = Util.setMap(cuiNamesMap, cui, conceptName);

                    // -------------remove-------------------------------
                    Map<String, List<String>> nameFileListMap = cuiNameFileListMap.get(MeSHorSNOMEDcuis);
                    if (nameFileListMap == null)
                        cuiNameFileListMap.put(MeSHorSNOMEDcuis, nameFileListMap = new HashMap<>());
                    nameFileListMap = Util.setMap(nameFileListMap, conceptName, tokens[0]);
                    // -------------remove-------------------------------

                    List<String> simpleConceptNames = SimpleNameSieve
                            .getTerminologySimpleNames(conceptName.split("\\s+"));
                    for (String simpleConceptName : simpleConceptNames)
                        simpleNameToCuiListMap = Util.setMap(simpleNameToCuiListMap, simpleConceptName, cui);

                }
            }
            if (Main.training_data_dir.toString().contains("ncbi"))
                continue;
            for (String cui : cuiNamesMap.keySet()) {
                List<String> names = cuiNamesMap.get(cui);
                List<String> namesToPrune = new ArrayList<>();
                for (String name : names) {
                    String[] nameTokens = name.split("\\s+");
                    if (nameTokens.length < 3)
                        continue;
                    if (names.contains(nameTokens[0] + " " + nameTokens[1]))
                        namesToPrune = Util.setList(namesToPrune, nameTokens[0] + " " + nameTokens[1]);
                    else if (names
                            .contains(nameTokens[nameTokens.length - 2] + " " + nameTokens[nameTokens.length - 1]))
                        namesToPrune = Util.setList(namesToPrune,
                                nameTokens[nameTokens.length - 2] + " " + nameTokens[nameTokens.length - 1]);
                }
                for (String nameToPrune : namesToPrune) {
                    nameToCuiListMap.remove(nameToPrune);
                    cuiToNameListMap.get(cui).remove(nameToPrune);
                    stemmedNameToCuiListMap.remove(Ling.getStemmedPhrase(nameToPrune));
                    cuiToStemmedNameListMap.get(cui).remove(Ling.getStemmedPhrase(nameToPrune));
                    String[] nameToPruneTokens = nameToPrune.split("\\s+");
                    for (String nameToPruneToken : nameToPruneTokens) {
                        if (Ling.getStopwordsList().contains(nameToPruneToken))
                            continue;
                        tokenToNameListMap.get(nameToPruneToken).remove(nameToPrune);
                    }
                }
            }
        }
    }

    private static Map<String, List<String>> normalizedNameToCuiListMap = new HashMap<>();

    public static void setNormalizedNameToCuiListMap(String name, String cui) {
        normalizedNameToCuiListMap = Util.setMap(normalizedNameToCuiListMap, name, cui);
    }

    public static Map<String, List<String>> getNormalizedNameToCuiListMap() {
        return normalizedNameToCuiListMap;
    }

    private static Map<String, List<String>> stemmedNormalizedNameToCuiListMap = new HashMap<>();

    public static void setStemmedNormalizedNameToCuiListMap(String stemmedName, String cui) {
        stemmedNormalizedNameToCuiListMap = Util.setMap(stemmedNormalizedNameToCuiListMap, stemmedName, cui);
    }

    public static Map<String, List<String>> getStemmedNormalizedNameToCuiListMap() {
        return stemmedNormalizedNameToCuiListMap;
    }

    public static void storeNormalizedConcept(Mention concept) {
        setNormalizedNameToCuiListMap(
                concept.getNormalizingSieve() == 2 ? concept.getNameExpansion() : concept.getName(), concept.getCui());
        setStemmedNormalizedNameToCuiListMap(
                concept.getNormalizingSieve() == 2 ? Ling.getStemmedPhrase(concept.getNameExpansion())
                        : concept.getStemmedName(),
                concept.getCui());
    }

}
