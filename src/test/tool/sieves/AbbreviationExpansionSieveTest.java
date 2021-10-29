package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.AbbreviationExpansionSieve;
import tool.util.Document;
import tool.util.Mention;
import tool.util.Terminology;

public class AbbreviationExpansionSieveTest {
	Terminology terminology;
	AbbreviationExpansionSieve sieve;

	@Before
	public void setUp() throws Exception {
		var empty = new ArrayList<String>();
		terminology = new Terminology(empty);
		sieve = new AbbreviationExpansionSieve(terminology, new Terminology(empty), empty);
	}

	// Tests using abbreviation from document.
	@Test
	public void fromDocument() throws Exception {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		var doc = new Document(new File("src/test/sample.concept"));
		terminology.nameToCuiListMap.addKeyPair("made up disease", cui);
		doc.abbreviationMap.put("made up disease", cui);
		var mention = new Mention("MUD", null, null, null);
		sieve.apply(mention, doc);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	// Tests using abbreviation from abbreviation file.
	@Test
	public void fromAbbreviationFile() throws Exception {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		terminology.nameToCuiListMap.addKeyPair("altered auditory feedback", cui);
		var mention = new Mention("aaf", null, null, null);
		sieve.apply(mention, new Document());
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
	}

	@Test
	public void ambiguousAbbreviation() throws Exception {
		terminology.nameToCuiListMap.addKeyPair("cardiac arrest", "cui1");
		terminology.nameToCuiListMap.addKeyPair("cholic acid", "cui2");
		var mention = new Mention("ca", null, null, null);
		sieve.apply(mention, new Document());
		assertFalse(mention.normalized);
	}

	@Test
	public void failToNormalize() throws Exception {
		var mention = new Mention("xkcd", null, null, null);
		sieve.apply(mention, new Document());
		assertFalse(mention.normalized);
		assertEquals("", mention.cui);
	}
}
