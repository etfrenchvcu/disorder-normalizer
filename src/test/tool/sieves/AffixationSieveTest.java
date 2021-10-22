package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.AffixationSieve;
import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

public class AffixationSieveTest {
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	AffixationSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		normalizedNameToCuiListMap = new HashListMap();
		sieve = new AffixationSieve(terminology, terminology, normalizedNameToCuiListMap);
	}

	@Test
	public void replaceSuffixTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("sepsis", cui);
		var mention = new Mention("septic", null, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNotNull(normalizedNameToCuiListMap.get("septic"));
	}

	@Test
	public void replacePrefixTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("opiod use disorder", cui);
		var mention = new Mention("opiod abuse disorder", null, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNotNull(normalizedNameToCuiListMap.get("opiod abuse disorder"));
	}

	@Test
	public void replaceAffixTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("basal cell carcinoma", cui);
		var mention = new Mention("basal cell cancer", null, null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNotNull(normalizedNameToCuiListMap.get("basal cell cancer"));
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
