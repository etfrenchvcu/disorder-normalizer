/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.util.regex.Pattern;

import tool.util.Mention;
import tool.util.Terminology;

/**
 * Exact Match Sieve
 * 
 * @author
 */
public class MeasurementSieve extends Sieve {

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     */
    public MeasurementSieve(Terminology standardTerminology, Terminology trainTerminology) {
        super(standardTerminology, trainTerminology);
    }

    /**
     * Checks if the succeeding text is formatted to indicate the name is a
     * measurement and checks if appending "measurement" to the name gets a match.
     */
    public void apply(Mention mention) {

        // Check for measurement pattern
        Pattern measureRegex = Pattern.compile("^((was )|(of ))?\\d");
        if (measureRegex.matcher(mention.snippet).find()) {
            // Exclude medication mentions
            Pattern drugRegex = Pattern.compile("(meq)|(mg)|(iv)|(po)|(bid)|(qd)|(tid)|(gram)");
            if (!drugRegex.matcher(mention.snippet.replace(".","")).find()) {
                var measurementName = mention.name + " measurement";
                mention.cui = exactMatch(mention, measurementName);
                mention.addPermutation(measurementName);
            }
        }

        // Normalized if exactly one CUI was returned, not a list.
        if (!mention.cui.equals("") && !mention.cui.contains(",")) {
            mention.normalized = true;
        }
    }
}
