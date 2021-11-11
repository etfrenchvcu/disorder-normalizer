/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Object representing a single document. Contains name and text of the doc and
 * a list of annotated mentions.
 * 
 * @author
 */
public class Document {

	public String filename;
	public String text;
	public List<Mention> mentions;
	public Map<String, String> abbreviationMap;

	/**
	 * Constructor.
	 * 
	 * @param annotationFile
	 * @throws Exception
	 */
	public Document(File annotationFile) throws Exception {
		this.filename = annotationFile.getName();
		mentions = new ArrayList<>();

		// Read corresponding .txt file.
		// TODO: remove this if we're not going to check preceding/succeeding text
		var correspondingTxtFile = new File(annotationFile.toString().replace(".concept", ".txt"));
		this.text = read(correspondingTxtFile);

		loadMentions(annotationFile);
	}

	/**
	 * Dummy constructor for unit testing.
	 */
	public Document() {
	}

	/**
	 * Loads mentions from annotation file.
	 * 
	 * @param annotationFile
	 * @throws Exception
	 */
	private void loadMentions(File annotationFile) throws Exception {
		try (BufferedReader br = new BufferedReader(new FileReader(annotationFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				String[] tokens = line.split("\\|\\|");
				addMention(tokens);
			}
		} catch (Exception e) {
			System.out.println("ERROR: Failed to read " + annotationFile.toString());
			throw e;
		}
	}

	/**
	 * Adds a new mention to the list for this document.
	 * 
	 * @param tokens
	 */
	public void addMention(String[] tokens) {
		String[] cuis = tokens[4].contains("+") ? tokens[4].split("\\+") : tokens[4].split("\\|");
		String MeSHorSNOMEDcuis = Terminology.getMeSHorSNOMEDCuis(cuis);
		Mention mention = new Mention(tokens[3], tokens[1], MeSHorSNOMEDcuis);
		mentions.add(mention);
	}

	/**
	 * Reads file contents into a string object.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String read(File file) throws IOException {
		byte[] data;
		try (FileInputStream fis = new FileInputStream(file)) {
			data = new byte[(int) file.length()];
			fis.read(data);
		}
		return new String(data, "UTF-8");
	}
}
