/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author
 */
public class Mention {

    public String name;
    public String nameExpansion;
    public String cui;
    public boolean normalized = false;
    public int normalizingSieveLevel;
    public String normalizingSieveName;
    public List<String> alternateCuis;
    public List<String> namePermutations;
    public String indexes;

    // private String nameExpansion;
    private String goldMeSHorSNOMEDCui;
    private List<String> goldOMIMCuis;

    public Mention(String name, String indexes, String goldMeSHorSNOMEDCui, List<String> goldOMIMCuis) {
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
     * 
     * @param name
     */
    public void addPermutation(String name) {
        name = name.trim();
        if (name != null && !name.isEmpty() && !namePermutations.contains(name)) {
            namePermutations.add(name);
        }
    }

    /**
     * Adds a list of name permutations to namePermutations.
     * 
     * @param names
     */
    public void addPermutationList(List<String> names) {
        for (var name : names) {
            addPermutation(name);
        }
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
}
