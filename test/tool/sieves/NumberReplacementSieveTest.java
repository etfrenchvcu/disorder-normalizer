package tool.sieves;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

public class NumberReplacementSieveTest {
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	NumberReplacementSieve sieve;

	@Before
	public void setUp() throws Exception {
		terminology = new Terminology(new ArrayList<String>());
		normalizedNameToCuiListMap = new HashListMap();
		sieve = new NumberReplacementSieve(terminology, terminology, normalizedNameToCuiListMap);
	}

	@Test
	public void numberToWordTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("type 2 diabetes", cui);
		var mention = new Mention("Type II diabetes",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}

	@Test
	public void wordToNumberTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("type two diabetes", cui);
		var mention = new Mention("Type 2 diabetes",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}
}
