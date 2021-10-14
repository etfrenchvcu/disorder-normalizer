/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author
 */
public class Mention {
    
    public String name;
    public String cui;
    public int normalizingSieveLevel;
    public List<String> alternateCuis;
    public List<String> namePermutations;

    private String indexes;    
    private String nameExpansion;
    private String goldMeSHorSNOMEDCui;  
    private List<String> goldOMIMCuis;
    
    private List<String> stemmedNamesKnowledgeBase = new ArrayList<>();
    
    public Mention(String indexes, String name, String goldMeSHorSNOMEDCui, List<String> goldOMIMCuis) {
        this.indexes = indexes;
        this.name = Ling.correctSpelling(name.toLowerCase().trim());
        this.goldMeSHorSNOMEDCui = goldMeSHorSNOMEDCui;
        this.goldOMIMCuis = goldOMIMCuis;
        this.normalizingSieveLevel = 0;

        // Initialize lists
        namePermutations = new ArrayList<>();
        addPermutation(this.name);
    }

    /**
     * Adds a name to the list of name permutations if unique.
     * @param name
     */
    public void addPermutation(String name) {
        name = name.trim();
        if(name != null && !name.isEmpty() && !namePermutations.contains(name)){
            namePermutations.add(name);
        }
    }

    /**
     * Adds a list of name permutations to namePermutations.
     * @param names
     */
    public void addPermutationList(List<String> names) {
        for (var name : names) {
            addPermutation(name);
        }
    }
    
    public void setIndexes(String indexes) {
        this.indexes = indexes;
    }
    
    public String getIndexes() {
        return indexes;
    }
    
    public String getName() {
        return name;
    }

    public void setNameExpansion(String text, Map<String,String> abbreviationMap) {
        nameExpansion = Abbreviation.getAbbreviationExpansion(abbreviationMap, text, name, indexes);
        addPermutation(nameExpansion);
    }
    
    public String getNameExpansion() {
        return nameExpansion;
    }
    
    public String getStemmedName() {
        return Ling.getStemmedPhrase(name);
    }
    
    public String getGoldMeSHorSNOMEDCui() {
        return goldMeSHorSNOMEDCui;
    }    
    
    public List<String> getGoldOMIMCuis() {
        return goldOMIMCuis;
    }
    
    public String getGoldCui() {
        if (!goldMeSHorSNOMEDCui.equals(""))
            return goldMeSHorSNOMEDCui;
        else 
            return goldOMIMCuis.size() == 1 ? goldOMIMCuis.get(0) : goldOMIMCuis.toString();
    }

    public void setStemmedNamesKnowledgeBase(List<String> namesList) {
        this.stemmedNamesKnowledgeBase = Util.addUnique(this.stemmedNamesKnowledgeBase, namesList);
    }
    
    public List<String> getStemmedNamesKnowledgeBase() {
        return stemmedNamesKnowledgeBase;
    }    
    
}
