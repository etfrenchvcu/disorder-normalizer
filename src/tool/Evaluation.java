/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tool.util.Document;
import tool.util.Mention;
import tool.util.Util;

/**
 *
 * @author
 */
public class Evaluation {
    public File output_data_dir;

    private static int totalNames = 0;
    private static int tp = 0;
    private static int fp = 0;
    private static double accuracy = 0.0;

    /**
     * Constructor.
     * @param output_data_dir
     */
    public Evaluation(File output_data_dir) {
        this.output_data_dir = output_data_dir;
    }
    
    public void incrementTotal() {
        totalNames++;
    }
    
    public void incrementTP() {
        tp++;
    }
        
    public void incrementFP() {
        fp++;
    }
    
    public void evaluateClassification(Mention mention, Document concepts) throws IOException {
        // Don't evaluate CUI-less 
        if(mention.cui.equals("CUI-less"))
            return;

        incrementTotal();
        if ((!mention.getGoldMeSHorSNOMEDCui().equals("") && mention.getGoldMeSHorSNOMEDCui().equals(mention.cui)) ||
                (!mention.getGoldOMIMCuis().isEmpty() && mention.getGoldOMIMCuis().contains(mention.cui)))
            incrementTP();
        else if (mention.getGoldMeSHorSNOMEDCui().contains("|") && mention.cui.contains("|")) {
            List<String> gold = new ArrayList<>(Arrays.asList(mention.getGoldMeSHorSNOMEDCui().split("\\|")));
            List<String> predicted = new ArrayList<>(Arrays.asList(mention.cui.split("\\|")));
            gold.removeAll(predicted);
            if (gold.isEmpty()) {
                incrementTP();
            }
            else {
                incrementFP();
                printPred(mention, concepts.filename);
            }
        }
        else if (mention.alternateCuis != null && !mention.alternateCuis.isEmpty()) {
            if (!mention.getGoldMeSHorSNOMEDCui().equals("") && mention.alternateCuis.contains(mention.getGoldMeSHorSNOMEDCui())) {
                incrementTP();
                mention.cui = mention.getGoldMeSHorSNOMEDCui();
            }
            else if (!mention.getGoldOMIMCuis().isEmpty() && Util.containsAny(mention.alternateCuis, mention.getGoldOMIMCuis())) {
                incrementTP();
                if (mention.getGoldOMIMCuis().size() == 1)
                    mention.cui = mention.getGoldOMIMCuis().get(0);
            }
            else {
                incrementFP();
                printPred(mention, concepts.filename);
            }
        }
        else {
            incrementFP();
            printPred(mention, concepts.filename);
        }
        
        //write output
        FileOutputStream output = new FileOutputStream(output_data_dir.toPath()+"/"+concepts.filename.replace(".txt", ".concept"), true);
        output.write((concepts.filename.replace(".txt", "")+"||"+mention.getIndexes()+"||"+mention.name+"||"+mention.cui+"\n").getBytes());

        //logger output
        //Logger.writeLogFile((concepts.filename+"\t"+concept.getIndexes()+"\t"+concept.getName()+"\t"+concept.getCui()+"\t"+concept.getGoldCui()));
    }

    private void printPred(Mention concept, String file) {
        String str = String.format("%s: %s #  %s (%s) ", file, concept.getGoldMeSHorSNOMEDCui(), concept.name, concept.cui);
        // System.out.println(str);
    }
    
    public void computeAccuracy() {
        accuracy = (double)tp/(double)totalNames;
    }
    
    public void printResults() {
        System.out.println("*********************");
        System.out.println("Total Names: "+totalNames);
        System.out.println("True Normalizations: "+tp);
        System.out.println("False Normalizations: "+fp);
        System.out.println("Accuracy: "+accuracy);
        System.out.println("*********************");
    }
    
}
