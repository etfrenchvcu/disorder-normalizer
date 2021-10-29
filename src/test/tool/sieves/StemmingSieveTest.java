package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.StemmingSieve;
import tool.util.Mention;
import tool.util.Stemmer;
import tool.util.Terminology;

public class StemmingSieveTest {
	Terminology terminology;
	StemmingSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		var stemmer = new Stemmer(new ArrayList<String>());
		sieve = new StemmingSieve(terminology, terminology, stemmer);
	}

	@Test
	public void addHyphenTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("system", cui);
		var mention = new Mention("systemic", null, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	@Test
	public void failToNormalize() {
		var mention = new Mention("xkcd", null, null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
		assertEquals("", mention.cui);
	}
}
