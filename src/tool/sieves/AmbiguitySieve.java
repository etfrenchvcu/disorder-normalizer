/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.sieves;

import java.util.ArrayList;
import java.util.Arrays;

import tool.util.Document;
import tool.util.Mention;
import tool.util.Terminology;
import tool.util.Util;

/**
 * Exact Match Sieve
 * 
 * @author
 */
public class AmbiguitySieve extends Sieve {

    /**
     * Constructor. Calls abstract constructor.
     * 
     * @param standardTerminology
     * @param trainTerminology
     */
    public AmbiguitySieve(Terminology standardTerminology, Terminology trainTerminology) {
        super(standardTerminology, trainTerminology);
    }

    /**
     * Checks for an exact match in one of the dictionaries for the raw (spelling
     * corrected) mention text.
     */
    public void apply(Mention mention, Document doc) {
        if (mention.cui.contains(",")) {
            var candidates = new ArrayList<String>(Arrays.asList(mention.cui.split(",")));

            var documentCuis = new ArrayList<String>();
            for (var m : doc.mentions) {
                if (!m.cui.contains(",")) {
                    Util.addUnique(documentCuis, m.cui);
                }
            }

            candidates.retainAll(documentCuis);
            if (candidates.size() == 1) {
                mention.cui = candidates.get(0);
                mention.normalizingSieveName = "AmbiguitySieve";
                mention.normalized = true;
            }
        }
    }

    /**
     * Only here to implement the abstract.
     */
    public void apply(Mention mention) throws Exception {
        throw new Exception("Use apply(Mention mention, Document doc).");
    }
}
