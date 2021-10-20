package tool.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

	@Before
	public void setUp() throws Exception {
		var empty = new ArrayList<String>();
		terminology = new Terminology(empty);
		normalizedNameToCuiListMap = new HashListMap();

		normalizedNameToCuiListMap.addKeyPair("made up disease", "c001");
		normalizedNameToCuiListMap.addKeyPair("attention deficit disorder", "c002");

	}

	// Tests using abbreviation from document.
	@Test
	public void fromDocument() throws Exception {
		var empty = new ArrayList<String>();
		var doc = new Document(new File("test/sample.concept"));
		var sieve = new AbbreviationExpansionSieve(new Terminology(empty), new Terminology(empty),
				normalizedNameToCuiListMap, empty);
		var mention = new Mention("MUD", null, null, null);
		assertEquals("c001", sieve.apply(mention, doc));
	}

	// Tests using abbreviation from abbreviation file.
	@Test
	public void fromAbbreviationFile() throws IOException {
		var empty = new ArrayList<String>();
		var doc = new Document();
		var sieve = new AbbreviationExpansionSieve(new Terminology(empty), new Terminology(empty),
				normalizedNameToCuiListMap, empty);
		var mention = new Mention("add", null, null, null);
		assertEquals("c002", sieve.apply(mention, doc));
	}

}
