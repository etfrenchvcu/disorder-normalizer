package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.PrepositionalTransformSieve;
import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

public class PrepositionalTransformSieveTest {
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	PrepositionalTransformSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		normalizedNameToCuiListMap = new HashListMap();
		sieve = new PrepositionalTransformSieve(terminology, terminology, normalizedNameToCuiListMap);
	}

	@Test
	public void inversionTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("breast cancer", cui);
		var mention = new Mention("cancer of breast", null, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNotNull(normalizedNameToCuiListMap.get("cancer of breast"));
	}

	@Test
	public void insertionTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("complications with diabetes", cui);
		var mention = new Mention("diabetes complications", null, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNotNull(normalizedNameToCuiListMap.get("diabetes complications"));
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
