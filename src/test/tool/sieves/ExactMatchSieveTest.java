package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.ExactMatchSieve;
import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

public class ExactMatchSieveTest {
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	ExactMatchSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		normalizedNameToCuiListMap = new HashListMap();
		sieve = new ExactMatchSieve(terminology, terminology, normalizedNameToCuiListMap);
	}

	@Test
	public void apply_Match() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("name", cui);
		var mention = new Mention("name", null, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNull(normalizedNameToCuiListMap.get("name"));
	}

	@Test
	public void failToNormalize() {
		var mention = new Mention("xkcd", null, null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
		assertEquals("", mention.cui);
		assertNull(normalizedNameToCuiListMap.get("xkcd"));
	}

}
