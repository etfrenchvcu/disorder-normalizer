package tool.sieves;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

public class PartialMatchSieveTest {
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	PartialMatchSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		normalizedNameToCuiListMap = new HashListMap();
		sieve = new PartialMatchSieve(terminology, terminology, normalizedNameToCuiListMap, new ArrayList<String>());
	}

	@Test
	public void uniqueTokenTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("unique term", cui);
		terminology.loadConceptMaps("term name", "foo");
		var mention = new Mention("unique",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}

	@Test
	public void nonUniqueTokenTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("nonunique term", "bar");
		terminology.loadConceptMaps("nonunique name", "baz");
		var mention = new Mention("nonunique",null,null,null);
		assertEquals("", sieve.apply(mention));
	}
}
