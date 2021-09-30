/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

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
    
    private static int totalNames = 0;
    private static int tp = 0;
    private static int fp = 0;
    private static double accuracy = 0.0;
    
    public static void incrementTotal() {
        totalNames++;
    }
    
    public static void incrementTP() {
        tp++;
    }
        
    public static void incrementFP() {
        fp++;
    }
    
    public static void evaluateClassification(Mention concept, Document concepts) throws IOException {
        // Don't evaluate CUI-less 
        if(concept.getCui().equals("CUI-less"))
            return;

        incrementTotal();
        if ((!concept.getGoldMeSHorSNOMEDCui().equals("") && concept.getGoldMeSHorSNOMEDCui().equals(concept.getCui())) ||
                (!concept.getGoldOMIMCuis().isEmpty() && concept.getGoldOMIMCuis().contains(concept.getCui())))
            incrementTP();
        else if (concept.getGoldMeSHorSNOMEDCui().contains("|") && concept.getCui().contains("|")) {
            List<String> gold = new ArrayList<>(Arrays.asList(concept.getGoldMeSHorSNOMEDCui().split("\\|")));
            List<String> predicted = new ArrayList<>(Arrays.asList(concept.getCui().split("\\|")));
            gold.removeAll(predicted);
            if (gold.isEmpty()) {
                incrementTP();
            }
            else {
                incrementFP();
                printPred(concept, concepts.getFilename());
            }
        }
        else if (concept.getAlternateCuis() != null && !concept.getAlternateCuis().isEmpty()) {
            if (!concept.getGoldMeSHorSNOMEDCui().equals("") && concept.getAlternateCuis().contains(concept.getGoldMeSHorSNOMEDCui())) {
                incrementTP();
                concept.setCui(concept.getGoldMeSHorSNOMEDCui());
            }
            else if (!concept.getGoldOMIMCuis().isEmpty() && Util.containsAny(concept.getAlternateCuis(), concept.getGoldOMIMCuis())) {
                incrementTP();
                if (concept.getGoldOMIMCuis().size() == 1)
                    concept.setCui(concept.getGoldOMIMCuis().get(0));
            }
            else {
                incrementFP();
                printPred(concept, concepts.getFilename());
            }
        }
        else {
            incrementFP();
            printPred(concept, concepts.getFilename());
        }
        
        //write output
        FileOutputStream output = new FileOutputStream(Main.output_data_dir+"\\"+concepts.getFilename().replace(".txt", ".concept"), true);
        output.write((concepts.getFilename().replace(".txt", "")+"||"+concept.getIndexes()+"||"+concept.getName()+"||"+concept.getCui()+"\n").getBytes());

        //logger output
        //Logger.writeLogFile((concepts.getFilename()+"\t"+concept.getIndexes()+"\t"+concept.getName()+"\t"+concept.getCui()+"\t"+concept.getGoldCui()));
    }

    private static void printPred(Mention concept, String file) {
        String str = String.format("%s: %s #  %s (%s) ", file, concept.getGoldMeSHorSNOMEDCui(), concept.getName(), concept.getCui());
        // System.out.println(str);
    }
    
    public static void computeAccuracy() {
        accuracy = (double)tp/(double)totalNames;
    }
    
    public static void printResults() {
        System.out.println("*********************");
        System.out.println("Total Names: "+totalNames);
        System.out.println("True Normalizations: "+tp);
        System.out.println("False Normalizations: "+fp);
        System.out.println("Accuracy: "+accuracy);
        System.out.println("*********************");
    }
    
}
