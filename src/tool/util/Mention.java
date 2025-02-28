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
    public String snippet;
    public String keyPhrase;
    public String cui = "";
    public boolean normalized = false;
    public int normalizingSieveLevel;
    public String normalizingName;
    public String normalizingSieveName;
    public String normalizingSource;
    public List<String> alternateCuis;
    public List<String> namePermutations;
    public String indexes;
    public String goldCui;
    public List<String> goldNames;

    public Mention(String name, String indexes, String goldCui) {
        this.indexes = indexes;
        this.name = Util.correctSpelling(name.trim().toLowerCase());
        this.goldCui = goldCui;
        this.normalizingSieveLevel = 0;

        // Initialize lists
        goldNames = new ArrayList<>();
        alternateCuis = new ArrayList<>();
        namePermutations = new ArrayList<>();
        addPermutation(this.name);
    }

    /**
     * Prints tab delimited property values.
     */
    public String toString() {
        return name + "\t" + keyPhrase + "\t" + cui + "\t" + normalized + "\t" + normalizingSieveName + "\t" + 
        normalizingSource + "\t" + goldCui + "\t" + normalizingName + "\t" + normalizingSieveLevel + "\t" + String.join(",",goldNames) + "\t" + snippet;
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
}
