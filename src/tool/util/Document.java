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
import java.util.HashMap;
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
		var correspondingTxtFile = new File(annotationFile.toString().replace(".concept", ".txt"));
		this.text = read(correspondingTxtFile);

		loadMentions(annotationFile);
		abbreviationMap = getTextAbbreviationExpansionMapFromFile(correspondingTxtFile);
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
		List<String> OMIMcuis = getOMIMCuis(cuis);
		var name = Util.correctSpelling(tokens[3]);
		Mention mention = new Mention(name, tokens[1], MeSHorSNOMEDcuis, OMIMcuis);
		mentions.add(mention);
	}

	/**
	 * Parses out OMIM CUIs. This may only be relevant for NCBI?
	 * @param cuis
	 * @return
	 */
	private List<String> getOMIMCuis(String[] cuis) {
		List<String> OMIMcuis = new ArrayList<>();
		for (String cui : cuis) {
			if (cui.contains("OMIM")) {
				cui = cui.split(":")[1];
				Util.addUnique(OMIMcuis, cui);
			}
		}
		return OMIMcuis;
	}

	/**
	 * Checks for annotations mention a term and then provide an acronym in parens
	 * i.e. "Myotonic dystrophy (DM)" and creates a dictionary for use within doc.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private Map<String, String> getTextAbbreviationExpansionMapFromFile(File file) throws IOException {
		Map<String, String> abbreviationMap = new HashMap<>();
		BufferedReader input = new BufferedReader(new FileReader(file));
		while (input.ready()) {
			String s = input.readLine().trim().replaceAll("\\s+", " ");
			String[] tokens = s.split("\\s");
			int size = tokens.length;
			for (int i = 0; i < size; i++) {
				int expansionIndex = -1;

				if (tokens[i].matches("\\(\\w+(\\-\\w+)?\\)(,|\\.)?") || tokens[i].matches("\\([A-Z]+(;|,|\\.)"))
					expansionIndex = i - 1;
				else if (tokens[i].matches("[A-Z]+\\)"))
					expansionIndex = firstIndexOf(tokens, i, "\\(");

				if (expansionIndex == -1)
					continue;

				// Remove parens and trailing punctuation.
				String abbreviation = tokens[i].replace("(", "").replace(")", "").toLowerCase();
				if (abbreviation.charAt(abbreviation.length() - 1) == ','
						|| abbreviation.charAt(abbreviation.length() - 1) == '.'
						|| abbreviation.charAt(abbreviation.length() - 1) == ';')
					abbreviation = abbreviation.substring(0, abbreviation.length() - 1);
				int abbreviationLength = abbreviation.length();

				// Expand abbreviation and add to map.
				if (!abbreviationMap.containsKey(abbreviation)) {
					setTextAbbreviationExpansionMap(abbreviationMap, tokens, abbreviationLength, abbreviation,
							expansionIndex);
				}

				// TODO: Does it really make sense to add reversed abbreviations?
				String reversedAbbreviation = Util.reverse(abbreviation);
				if (!abbreviationMap.containsKey(reversedAbbreviation)) {
					setTextAbbreviationExpansionMap(abbreviationMap, tokens, abbreviationLength, reversedAbbreviation,
							expansionIndex);
				}
			}
		}
		input.close();

		return abbreviationMap;
	}

	/**
	 * Gets the first index matching regex expression.
	 * 
	 * @param tokens
	 * @param i
	 * @param pattern
	 * @return
	 */
	private int firstIndexOf(String[] tokens, int i, String pattern) {
		while (i >= 0) {
			if (tokens[i].matches(pattern + ".*")) {
				i = i - 1;
				return i;
			}
			i--;
		}
		return -1;
	}

	/**
	 * Attempts to expand the acronym and adds it to a dictionary if successful.
	 * 
	 * @param map
	 * @param tokens
	 * @param abbreviationLength
	 * @param abbreviation
	 * @param expansionIndex
	 */
	private void setTextAbbreviationExpansionMap(Map<String, String> map, String[] tokens, int abbreviationLength,
			String abbreviation, int expansionIndex) {
		String expansion = getTentativeExpansion(tokens, expansionIndex, abbreviationLength);

		if (expansion.equals("")) {
			expansion = getExpansionByHearstAlgorithm(abbreviation, expansion);
		}

		expansion = Util.correctSpelling(expansion).trim().toLowerCase();
		if (!expansion.equals(""))
			map.put(abbreviation, expansion);
	}

	public static String getTentativeExpansion(String[] tokens, int i, int abbreviationLength) {
		String expansion = "";
		while (i >= 0 && abbreviationLength > 0) {
			expansion = tokens[i] + " " + expansion;
			i--;
			abbreviationLength--;
		}
		return expansion.trim();
	}

	private String getExpansionByHearstAlgorithm(String shortForm, String longForm) {
		int sIndex;
		int lIndex;
		char currChar;

		sIndex = shortForm.length() - 1;
		lIndex = longForm.length() - 1;

		for (; sIndex >= 0; sIndex--) {
			currChar = Character.toLowerCase(shortForm.charAt(sIndex));
			if (!Character.isLetterOrDigit(currChar))
				continue;

			while (((lIndex >= 0) && (Character.toLowerCase(longForm.charAt(lIndex)) != currChar))
					|| ((sIndex == 0) && (lIndex > 0) && (Character.isLetterOrDigit(longForm.charAt(lIndex - 1)))))
				lIndex--;
			if (lIndex < 0)
				return "";
			lIndex--;
		}

		lIndex = longForm.lastIndexOf(" ", lIndex) + 1;
		longForm = longForm.substring(lIndex);

		return longForm;
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
