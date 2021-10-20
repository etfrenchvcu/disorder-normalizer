package tool.sieves;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.ExactMatchSieve;
import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

public class ExactMatchSieveTest {
	String goldCui = "cui";
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	ExactMatchSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		normalizedNameToCuiListMap = new HashListMap();

		terminology.loadConceptMaps("name", goldCui);
		sieve = new ExactMatchSieve(terminology, terminology, normalizedNameToCuiListMap);
	}

	@Test
	public void apply_Match() {
		var mention = new Mention("name",null,null,null);
		assertEquals(goldCui, sieve.apply(mention));
	}

	@Test
	public void apply_Fail() {
		var mention = new Mention("other_name",null,null,null);
		assertNotEquals(goldCui, sieve.apply(mention));
	}

}
