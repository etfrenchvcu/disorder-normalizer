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
    public List<String> alternateCuis;

    private String indexes;    
    private String nameExpansion;
    private String goldMeSHorSNOMEDCui;  
    private List<String> goldOMIMCuis;
    private int normalizingSieveLevel = 0;
    private List<String> namesKnowledgeBase = new ArrayList<>();
    private List<String> stemmedNamesKnowledgeBase = new ArrayList<>();
    
    // Don't think this is ever called...
    // public Mention(String name) {
    //     this.name = Ling.correctSpelling(name.toLowerCase().trim());
    // }
    
    public Mention(String indexes, String name, String goldMeSHorSNOMEDCui, List<String> goldOMIMCuis) {
        this.indexes = indexes;
        this.name = Ling.correctSpelling(name.toLowerCase().trim());
        this.goldMeSHorSNOMEDCui = goldMeSHorSNOMEDCui;
        this.goldOMIMCuis = goldOMIMCuis;
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
    }
    
    public String getNameExpansion() {
        return nameExpansion;
    }
    
    public String getStemmedName() {
        return Ling.getStemmedPhrase(name);
    }
    
    public void setNormalizingSieveLevel(int sieveLevel) {
        this.normalizingSieveLevel = sieveLevel;
    }
    
    public int getNormalizingSieve() {
        return normalizingSieveLevel;
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
 
    public void reinitializeNamesKnowledgeBase() {
        this.namesKnowledgeBase = new ArrayList<>();
    }
    
    public void setNamesKnowledgeBase(String name) {
        this.namesKnowledgeBase = Util.setList(this.namesKnowledgeBase, name);
    }
    
    public void setNamesKnowledgeBase(List<String> namesList) {
        this.namesKnowledgeBase = Util.addUnique(this.namesKnowledgeBase, namesList);
    }
    
    public List<String> getNamesKnowledgeBase() {
        return namesKnowledgeBase;
    }

    public void setStemmedNamesKnowledgeBase(List<String> namesList) {
        this.stemmedNamesKnowledgeBase = Util.addUnique(this.stemmedNamesKnowledgeBase, namesList);
    }
    
    public List<String> getStemmedNamesKnowledgeBase() {
        return stemmedNamesKnowledgeBase;
    }    
    
}
