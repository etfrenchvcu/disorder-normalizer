package tool.sieves;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

public class HyphenationSieveTest {
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	HyphenationSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		normalizedNameToCuiListMap = new HashListMap();
		sieve = new HyphenationSieve(terminology, terminology, normalizedNameToCuiListMap);
	}

	@Test
	public void addHyphenTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("a-fib", cui);
		var mention = new Mention("a fib",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}

	@Test
	public void removeHyphenTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("diabetes mellitus", cui);
		var mention = new Mention("diabetes-mellitus",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}
}
