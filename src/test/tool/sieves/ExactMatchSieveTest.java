package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.ExactMatchSieve;
import tool.util.Mention;
import tool.util.Terminology;

public class ExactMatchSieveTest {
	Terminology standard;
	Terminology train;
	ExactMatchSieve sieve;

	@Before
	public void setUp() throws Exception {
		standard = new Terminology(new ArrayList<String>());
		train = new Terminology(new ArrayList<String>());
		sieve = new ExactMatchSieve(standard, train);
	}

	@Test
	public void applyMatch() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		standard.loadConceptMaps("name", cui);
		var mention = new Mention("name", null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	@Test
	public void ambiguousBetweenTerminologies() {
		var name = new Exception().getStackTrace()[0].getMethodName().toLowerCase();
		standard.loadConceptMaps(name, "standard_cui");
		train.loadConceptMaps(name, "train_cui");
		var mention = new Mention(name, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals("train_cui", mention.cui);
	}

	@Test
	public void ambiguousWithinTerminology() {
		var name = new Exception().getStackTrace()[0].getMethodName().toLowerCase();
		standard.loadConceptMaps(name, "standard_cui1");
		standard.loadConceptMaps(name, "standard_cui2");
		var mention = new Mention(name, null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
	}

	@Test
	public void failToNormalize() {
		var mention = new Mention("xkcd", null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
		assertEquals("", mention.cui);
	}

}
