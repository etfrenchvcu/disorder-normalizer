package tool.sieves;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

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
		var cui = "inversionTest";
		terminology.loadConceptMaps("breast cancer", cui);
		var mention = new Mention("cancer of breast",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}

	@Test
	public void insertionTest() {
		var cui = "insertionTest";
		terminology.loadConceptMaps("complications with diabetes", cui);
		var mention = new Mention("diabetes complications",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}
}
