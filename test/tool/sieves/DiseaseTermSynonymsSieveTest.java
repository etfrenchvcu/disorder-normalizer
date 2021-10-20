package tool.sieves;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

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
		var mention = new Mention("cushing's syndrome",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}

	@Test
	public void removeTermTest() {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.loadConceptMaps("opiod use", cui);
		var mention = new Mention("opiod use disorder",null,null,null);
		assertEquals(cui, sieve.apply(mention));
	}
}
