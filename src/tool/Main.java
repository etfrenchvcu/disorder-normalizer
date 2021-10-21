/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author
 */
public class Main {
    /**
     * Performs rule-based entity linking on given test set.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting run...");
        File training_data_dir = null;
        File test_data_dir = null;
        File output_data_dir = null;
        int maxSieveLevel = 0;
        File standardTerminologyFile = null;
        MultiPassSieveNormalizer multiPassSieve;

        // Parse input args
        if (args.length == 4) {
            // Parse training directory
            if (new File(args[0]).isDirectory())
                training_data_dir = new File(args[0]);
            else
                throwIllegalDirectoryException(args[0]);

            // Parse test directory.
            if (new File(args[1]).isDirectory()) {
                test_data_dir = new File(args[1]);
                output_data_dir = new File(test_data_dir.toString().replace(test_data_dir.getName(), "output"));
                output_data_dir.mkdirs();
            } else
                throwIllegalDirectoryException(args[1]);

            // Parse terminology file location.
            if (new File(args[2]).isFile()) {
                standardTerminologyFile = new File(args[2]);
            } else
                throwIllegalFileException(args[2]);

            maxSieveLevel = Integer.parseInt(args[3]);
        } else {
            System.out.println(
                    "Usage: java tool.Main <training-data-dir> <test-data-dir> <terminology/ontology-file> max-sieve-level");
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

        Evaluation eval = new Evaluation(output_data_dir);
        multiPassSieve = new MultiPassSieveNormalizer(training_data_dir, test_data_dir, eval, maxSieveLevel,
                standardTerminologyFile);
        multiPassSieve.run();
        eval.computeAccuracy();
        eval.printResults();
    }

    /**
     * Exception logic for invalid directory argument.
     * @param name
     * @throws IOException
     */
    private static void throwIllegalDirectoryException(String name) throws IOException {
        System.out.println("Input Parameter Exception --> " + name + " is not a directory");
        var directory = new File(name);

        // Validate files in directory
        for (File file : directory.listFiles()) {
            if (file.toString().endsWith(".txt"))
                continue;
            BufferedReader in = new BufferedReader(new FileReader(file));
            while (in.ready()) {
                String s = in.readLine().trim();
                if (s.split("\\|\\|").length != 5) {
                    System.out.println("Input Data Exception --> Check concept data file: " + file.toString());
                    System.out.println("Every line in file must have the following five main fields");
                    System.out.println("textfilename||concept_name_indexes||type||concept_name||normalized_identifier");
                    System.out.println("Multiple values within a field are delimited by \"|\"");
                    System.out.println(
                            "Only two fields \"concept_name_indexes\" and \"normalized_indentifier\" can have multiple values");
                    System.exit(1);
                }
            }
            in.close();

            if (!new File(file.toString().replace(".concept", ".txt")).exists()) {
                System.out.println(
                        "Input Data Exception --> Text data file for concept file: " + file.toString() + " is absent");
                System.exit(1);
            }
        }

        System.exit(1);
    }

    /**
     * Exception logic for invalid file argument.
     * @param name
     */
    private static void throwIllegalFileException(String name) {
        System.out.println("Input Parameter Exception --> " + name + " is not a file");
        System.exit(1);
    }
}
