package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.PartialMatchSieve;
import tool.util.Mention;
import tool.util.Terminology;

public class PartialMatchSieveTest {
	Terminology terminology;
	PartialMatchSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		sieve = new PartialMatchSieve(terminology, terminology, new ArrayList<String>());
	}

	@Test
	public void uniqueTokenTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("unique term", cui);
		terminology.loadConceptMaps("term name", "foo");
		var mention = new Mention("unique", null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	@Test
	public void nonUniqueTokenTest() {
		terminology.loadConceptMaps("nonunique term", "bar");
		terminology.loadConceptMaps("nonunique name", "baz");
		var mention = new Mention("nonunique", null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
		assertEquals(2, mention.cui.split(",").length);
	}

	@Test
	public void failToNormalize() {
		var mention = new Mention("xkcd", null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
		assertEquals("", mention.cui);
	}
}
