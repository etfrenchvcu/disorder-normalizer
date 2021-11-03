package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.AbbreviationExpansionSieve;
import tool.util.Mention;
import tool.util.Terminology;

public class AbbreviationExpansionSieveTest {
	Terminology terminology;
	AbbreviationExpansionSieve sieve;

	@Before
	public void setUp() throws Exception {
		var empty = new ArrayList<String>();
		terminology = new Terminology(empty);
		sieve = new AbbreviationExpansionSieve(terminology, new Terminology(empty));
	}

	// Tests using abbreviation from abbreviation file.
	@Test
	public void fromAbbreviationFile() throws Exception {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.nameToCuiListMap.addKeyPair("arterial blood gas", cui);
		var mention = new Mention("abg", null, null);
		sieve.apply(mention);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	@Test
	public void ambiguousAbbreviation() throws Exception {
		terminology.nameToCuiListMap.addKeyPair("cancer", "cui1");
		terminology.nameToCuiListMap.addKeyPair("coronary artery", "cui2");
		var mention = new Mention("ca", null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
	}

	@Test
	public void failToNormalize() throws Exception {
		var mention = new Mention("xkcd", null, null);
		sieve.apply(mention);
		assertFalse(mention.normalized);
		assertEquals("", mention.cui);
	}
}
