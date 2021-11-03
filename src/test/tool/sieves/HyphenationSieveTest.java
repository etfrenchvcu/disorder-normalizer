package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.HyphenationSieve;
import tool.util.Mention;
import tool.util.Terminology;

public class HyphenationSieveTest {
	Terminology terminology;
	HyphenationSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		sieve = new HyphenationSieve(terminology, terminology);
	}

	@Test
	public void addHyphenTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("a-fib", cui);
		var mention = new Mention("a fib", null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	@Test
	public void removeHyphenTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("diabetes mellitus", cui);
		var mention = new Mention("diabetes-mellitus", null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	@Test
	public void normalization() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("fake-disease name", cui);
		var mention = new Mention("fake disease name", null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
	}

	@Test
	public void failToNormalize() {
		var mention = new Mention("xkcd", null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
		assertEquals("", mention.cui);
	}
}
