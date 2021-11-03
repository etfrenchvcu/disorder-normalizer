package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.NumberReplacementSieve;
import tool.util.Mention;
import tool.util.Terminology;

public class NumberReplacementSieveTest {
	Terminology terminology;
	NumberReplacementSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		sieve = new NumberReplacementSieve(terminology, terminology);
	}

	@Test
	public void numberToWordTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("type 2 diabetes", cui);
		var mention = new Mention("Type II diabetes", null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	@Test
	public void wordToNumberTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("type two diabetes", cui);
		var mention = new Mention("Type 2 diabetes", null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	@Test
	public void failToNormalize() {
		var mention = new Mention("xkcd", null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
		assertEquals("", mention.cui);
	}
}
