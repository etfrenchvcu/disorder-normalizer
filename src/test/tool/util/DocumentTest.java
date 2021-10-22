package test.tool.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import tool.util.Document;

public class DocumentTest {
	Document doc;

	@Before
	public void setUp() throws Exception {
		doc = new Document(new File("src/test/sample.concept"));
	}

	@Test
	public void textTest() {
		var text = "Made Up Disease (MUD) is bad";
		assertEquals(text, doc.text);
	}
	
	@Test
	public void abbreviationMapTest() {
		assertTrue(doc.abbreviationMap.size() > 0);
		assertEquals("made up disease", doc.abbreviationMap.get("mud"));
	}
	
	@Test
	public void mentionsTest() {
		assertEquals(1, doc.mentions.size());
		var m = doc.mentions.get(0);
		assertEquals("made up disease", m.name);
		assertEquals("0|1", m.indexes);
		assertEquals("cui", m.goldCui);
	}
}
