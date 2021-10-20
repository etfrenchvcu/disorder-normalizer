package tool.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class DocumentTest {
	Document doc;

	@Before
	public void setUp() throws Exception {
		doc = new Document(new File("test/sample.concept"));
	}

	@Test
	public void textTest() {
		var text = "Myotonic dystrophy (DM), the most prevalent muscular disorder in adults.";
		assertEquals(text, doc.text);
	}
	
	@Test
	public void abbreviationMapTest() {
		assertTrue(doc.abbreviationMap.size() > 0);
		assertEquals("myotonic dystrophy", doc.abbreviationMap.get("dm"));
	}
	
	@Test
	public void mentionsTest() {
		assertEquals(2, doc.mentions.size());
		var m = doc.mentions.get(0);
		assertEquals("myotonic dystrophy", m.name);
		assertEquals("0|18", m.indexes);
	}
}
