/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import java.io.File;

import tool.util.Terminology;
import tool.util.Util;

/**
 *
 * @author
 */
public class Main {
    /**
     * Performs rule-based entity linking on given test set.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        File training_data_dir = null;
        File test_data_dir = null;
        File output_data_dir = null;
        int maxSieveLevel = 0;
        Terminology standardTerminology = null;
        MultiPassSieveNormalizer multiPassSieve;

        // Parse input args
        if (args.length == 4) {
            if (new File(args[0]).isDirectory())
                training_data_dir = new File(args[0]);
            else
                Util.throwIllegalDirectoryException(args[0]);
            if (new File(args[1]).isDirectory()) {
                test_data_dir = new File(args[1]);
                output_data_dir = new File(test_data_dir.toString().replace(test_data_dir.getName(), "output"));
                output_data_dir.mkdirs();   
            } else
                Util.throwIllegalDirectoryException(args[1]);
            if (new File(args[2]).isFile()) {
                // TODO: remove this later
                boolean ncbi = test_data_dir.toString().contains("ncbi") ? true : false;
                standardTerminology = new Terminology(new File(args[2]), ncbi);
            } else
                Util.throwIllegalFileException(args[2]);

            maxSieveLevel = Integer.parseInt(args[3]);
        }
        else {
            System.out.println("Usage: java tool.Main <training-data-dir> <test-data-dir> <terminology/ontology-file> max-sieve-level");
            System.out.println("---------------------");
            System.out.println("Sieve levels:");
            System.out.println("1 for exact match");
            System.out.println("2 for abbreviation expansion");
            System.out.println("3 for subject<->object conversion");
            System.out.println("4 for numbers replacement");
            System.out.println("5 for hyphenation");
            System.out.println("6 for affixation");
            System.out.println("7 for disorder synonyms replacement");
            System.out.println("8 for stemming");
            System.out.println("9 for composite disorder mentions");
            System.out.println("10 for partial match");
            System.out.println("---------------------");
            System.exit(1);
        }

        multiPassSieve = new MultiPassSieveNormalizer(training_data_dir, test_data_dir, output_data_dir, maxSieveLevel, standardTerminology);      
        multiPassSieve.run();
        Evaluation.computeAccuracy();
        Evaluation.printResults();
    }

}
