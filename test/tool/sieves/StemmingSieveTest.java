package tool.sieves;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Stemmer;
import tool.util.Terminology;

public class StemmingSieveTest {
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	StemmingSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		normalizedNameToCuiListMap = new HashListMap();
		var stemmer = new Stemmer(new ArrayList<String>());
		sieve = new StemmingSieve(terminology, terminology, normalizedNameToCuiListMap, stemmer);
	}

	@Test
	public void addHyphenTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("system", cui);
		var mention = new Mention("systemic",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}
}
