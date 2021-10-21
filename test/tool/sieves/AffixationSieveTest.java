package tool.sieves;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

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
		var mention = new Mention("septic",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}

	@Test
	public void replacePrefixTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("opiod use disorder", cui);
		var mention = new Mention("opiod abuse disorder",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}

	@Test
	public void replaceAffixTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("basal cell carcinoma", cui);
		var mention = new Mention("basal cell cancer",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}
}
