package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.DiseaseTermSynonymsSieve;
import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

public class DiseaseTermSynonymsSieveTest {
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	DiseaseTermSynonymsSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		normalizedNameToCuiListMap = new HashListMap();
		sieve = new DiseaseTermSynonymsSieve(terminology, terminology, normalizedNameToCuiListMap);
	}

	@Test
	public void replaceTermTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("cushing's disease", cui);
		var mention = new Mention("cushing's syndrome", null, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNotNull(normalizedNameToCuiListMap.get("cushing's syndrome"));
	}

	@Test
	public void removeTermTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("opiod use", cui);
		var mention = new Mention("opiod use disorder", null, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNotNull(normalizedNameToCuiListMap.get("opiod use disorder"));
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
