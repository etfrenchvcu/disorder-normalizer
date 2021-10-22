package test.tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tool.sieves.AbbreviationExpansionSieve;
import tool.util.Document;
import tool.util.HashListMap;
import tool.util.Mention;
import tool.util.Terminology;

public class AbbreviationExpansionSieveTest {
	Terminology terminology;
	HashListMap normalizedNameToCuiListMap;
	AbbreviationExpansionSieve sieve;

	@Before
	public void setUp() throws Exception {
		var empty = new ArrayList<String>();
		terminology = new Terminology(empty);
		normalizedNameToCuiListMap = new HashListMap();
		sieve = new AbbreviationExpansionSieve(new Terminology(empty), new Terminology(empty),
				normalizedNameToCuiListMap, empty);
	}

	// Tests using abbreviation from document.
	@Test
	public void fromDocument() throws Exception {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		normalizedNameToCuiListMap.addKeyPair("made up disease", cui);
		var empty = new ArrayList<String>();
		var doc = new Document(new File("src/test/sample.concept"));
		var localSieve = new AbbreviationExpansionSieve(new Terminology(empty), new Terminology(empty),
				normalizedNameToCuiListMap, empty);
		var mention = new Mention("MUD", null, null, null);
		localSieve.apply(mention, doc);
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNotNull(normalizedNameToCuiListMap.get("mud"));
		assertNotNull(normalizedNameToCuiListMap.get("made up disease"));
	}

	// Tests using abbreviation from abbreviation file.
	@Test
	public void fromAbbreviationFile() throws IOException {
		var cui = new Exception().getStackTrace()[0].getMethodName();
		normalizedNameToCuiListMap.addKeyPair("attention deficit disorder", cui);
		var mention = new Mention("add", null, null, null);
		sieve.apply(mention, new Document());
		assertTrue(mention.normalized);
		assertEquals(cui, mention.cui);
		assertNotNull(normalizedNameToCuiListMap.get("add"));
		assertNotNull(normalizedNameToCuiListMap.get("attention deficit disorder"));
	}

	@Test
	public void failToNormalize() throws IOException {
		var mention = new Mention("xkcd", null, null, null);
		sieve.apply(mention, new Document());
		assertFalse(mention.normalized);
		assertEquals("", mention.cui);
		assertNull(normalizedNameToCuiListMap.get("xkcd"));
	}
}
