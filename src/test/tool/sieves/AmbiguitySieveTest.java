package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.AmbiguitySieve;
import tool.util.Document;
import tool.util.Mention;
import tool.util.Terminology;

public class AmbiguitySieveTest {
	Terminology standard;
	Terminology train;
	AmbiguitySieve sieve;

	@Before
	public void setUp() throws Exception {
		standard = new Terminology(new ArrayList<String>());
		train = new Terminology(new ArrayList<String>());
		sieve = new AmbiguitySieve(standard, train);
	}

	@Test
	public void applyMatch() throws Exception {
		standard.loadConceptMaps("name", "cui");
		var mention = new Mention("name", null, null, null);
		mention.cui = "bad_cui,cui";
		var doc = new Document(new File("src/test/sample.concept"));
		doc.mentions.get(0).cui = "cui";
		sieve.apply(mention, doc);
		assertTrue(mention.normalized);
		assertEquals("cui", mention.cui);
	}

	@Test
	public void failToNormalize() throws Exception {
		var mention = new Mention("xkcd", null, null, null);
		mention.cui = "bad_cui,other_cui";
		var doc = new Document(new File("src/test/sample.concept"));
		doc.mentions.get(0).cui = "cui";
		sieve.apply(mention, doc);
		assertFalse(mention.normalized);
	}

}
