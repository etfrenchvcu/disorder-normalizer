/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Object representing a single document. Contains name and text of the doc and a list of annotated mentions.
 * @author
 */
public class Document {
    
    public String filename;
    public String text;
    public List<Mention> mentions;
    
    public Document(String filename, String text) {
        this.filename = filename;
        this.text = text;
        mentions = new ArrayList<>();
    }

    /**
     * Adds a new mention to the list for this document.
     * @param tokens
     * @param abbreviationMap
     */
    public void addMention(String[] tokens, Map<String,String> abbreviationMap) {
        String[] cuis = tokens[4].contains("+") ? tokens[4].split("\\+") : tokens[4].split("\\|");
        String MeSHorSNOMEDcuis = Terminology.getMeSHorSNOMEDCuis(cuis);
        List<String> OMIMcuis = Terminology.getOMIMCuis(cuis);
        Mention mention = new Mention(tokens[1], tokens[3], MeSHorSNOMEDcuis, OMIMcuis);
        mention.setNameExpansion(text, abbreviationMap);
        mentions.add(mention);
    }
    
}
