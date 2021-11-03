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
     * 
     * @param output_data_dir
     * @throws IOException
     */
    public Evaluation(File output_data_dir) throws IOException {
        this.output_data_dir = output_data_dir;

        // Wipe old .concept output files
        for (File file : output_data_dir.listFiles())
            if (file.getName().contains(".concept"))
                file.delete();

        // Set up global results file
        FileOutputStream results = new FileOutputStream(output_data_dir.toPath() + "/results.txt", false);
        var header = ("filename\tname\tkeyPhrase\tprediction\tnormalized\tnormalizingSieveName\tnormalizingSource\tgoldCui\tnormalizingName\tnormalizingSieveLevel\tgoldNames\n")
                .getBytes();
        results.write(header);
        results.close();
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

    public void evaluateClassification(Mention mention, Document doc) throws IOException {
        // Don't evaluate CUI-less?
        if (mention.cui.equals("CUI-less"))
            return;

        incrementTotal();
        if (!mention.goldCui.equals("") && mention.goldCui.equals(mention.cui))
            incrementTP();
        else if (mention.goldCui.contains("|") && mention.cui.contains("|")) {
            List<String> gold = new ArrayList<>(Arrays.asList(mention.goldCui.split("\\|")));
            List<String> predicted = new ArrayList<>(Arrays.asList(mention.cui.split("\\|")));
            gold.removeAll(predicted);
            if (gold.isEmpty()) {
                incrementTP();
            } else {
                incrementFP();
            }
        } else if (mention.alternateCuis != null && !mention.alternateCuis.isEmpty()) {
            if (!mention.goldCui.equals("") && mention.alternateCuis.contains(mention.goldCui)) {
                incrementTP();
                mention.cui = mention.goldCui;
            } else {
                incrementFP();
            }
        } else {
            incrementFP();
        }

        // Write to output annotation file.
        FileOutputStream output = new FileOutputStream(
                output_data_dir.toPath() + "/" + doc.filename.replace(".txt", ".concept"), true);
        output.write((doc.filename.replace(".txt", "") + "||" + mention.indexes + "||" + mention.name + "||"
                + mention.cui + "\n").getBytes());
        output.close();

        // Write to global results file.
        FileOutputStream results = new FileOutputStream(output_data_dir.toPath() + "/results.txt", true);
        results.write((doc.filename.replace(".concept", "") + "\t" + mention.toString() + "\n").getBytes());
        results.close();
    }

    public void computeAccuracy() {
        accuracy = (double) tp / (double) totalNames;
    }

    public void printResults() {
        System.out.println("*********************");
        System.out.println("Total Names: " + totalNames);
        System.out.println("True Normalizations: " + tp);
        System.out.println("False Normalizations: " + fp);
        System.out.println("Accuracy: " + accuracy);
        System.out.println("*********************");
    }

}
