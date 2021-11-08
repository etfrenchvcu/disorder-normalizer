/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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
        File trainDir = null;
        File test_data_dir = null;
        File outputDir = null;
        File standardTerminologyFile = null;
        MultiPassSieveNormalizer multiPassSieve;
        System.out.println(System.getProperty("user.dir"));

        // Parse input args
        if (args.length >= 2) {
            // Parse terminology file location.
            if (new File(args[0]).isFile()) {
                standardTerminologyFile = new File(args[0]);
            } else
                throwIllegalFileException(args[0]);

            // Parse training directory
            if (new File(args[1]).isDirectory()) {
                trainDir = new File(args[1]);
                outputDir = new File(trainDir.toString().replace(trainDir.getName(), "output"));
                outputDir.mkdirs();
            } else
                throwIllegalDirectoryException(args[1]);

            if (args.length >= 3) {
                // Parse test directory.
                if (new File(args[2]).isDirectory()) {
                    test_data_dir = new File(args[2]);
                } else
                    throwIllegalDirectoryException(args[2]);
            }
        } else {
            System.out.println("Usage: java tool.Main <terminology/ontology-file> <training-data-dir> <test-data-dir>");
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

        Evaluation eval = new Evaluation(outputDir);
        multiPassSieve = new MultiPassSieveNormalizer(eval, standardTerminologyFile);
        if (test_data_dir == null) {
            // Run cross validation on training data

            // Set up train/test folders
            var xvalTrainName = trainDir.toString().replace(trainDir.getName(), "xval_train");
            var xvalTrain = new File(xvalTrainName);
            xvalTrain.mkdirs();
            var xvalTestName = trainDir.toString().replace(trainDir.getName(), "xval_test");
            var xvalTest = new File(xvalTestName);
            xvalTest.mkdirs();

            // 3-fold cross validation
            var folds = 3;
            for (int i = 0; i < folds; i++) {
                System.out.println("Running fold " + i + ": " + new SimpleDateFormat("mm.ss").format(new Date()));

                // Wipe directories
                purgeDirectory(xvalTrain);
                purgeDirectory(xvalTest);

                // Get list of training .concept files.
                var trainAnnotations = Arrays.stream(trainDir.listFiles()).filter(x -> x.getName().contains(".concept"))
                        .toArray(File[]::new);

                // Partition train/test
                for (int j = 0; j < trainAnnotations.length; j++) {
                    var txtFile = new File(trainAnnotations[j].getPath().replace(".concept", ".txt")).toPath();
                    var destinationFile = (j % folds == i) ? xvalTestName : xvalTrainName;
                    destinationFile += "/" + trainAnnotations[j].getName();

                    // Copy .concept and .txt to train/test bucket
                    Files.copy(trainAnnotations[j].toPath(), new File(destinationFile).toPath());
                    Files.copy(txtFile, new File(destinationFile.replace(".concept", ".txt")).toPath());
                }
                
                // Run for current fold.
                multiPassSieve.run(xvalTrain, xvalTest);
            }

        } else {
            throw new Exception("Running on test dataset not implemented yet.");
        }

        // Evaluate on all folds together.
        eval.computeAccuracy();
        eval.printResults();
    }

    /**
     * Delete all files in a directory.
     * 
     * @param dir
     */
    private static void purgeDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }

    /**
     * Exception logic for invalid directory argument.
     * 
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
     * 
     * @param name
     */
    private static void throwIllegalFileException(String name) {
        System.out.println("Input Parameter Exception --> " + name + " is not a file");
        System.exit(1);
    }
}
