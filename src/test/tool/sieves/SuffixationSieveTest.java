package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.SuffixationSieve;
import tool.util.Mention;
import tool.util.Terminology;

public class SuffixationSieveTest {
	Terminology terminology;
	SuffixationSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		sieve = new SuffixationSieve(terminology, terminology);
	}

	@Test
	public void replaceSuffixTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("sepsis", cui);
		var mention = new Mention("septic", null, null);
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
