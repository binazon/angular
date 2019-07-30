package com.orange.srs.refreport.applicative.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orange.srs.statcommon.technical.csv.CSVReader;

/**
 * Paginater for input data files used to do provisioning of StatEntities in RefReport
 */
public class InputObjectsPaginater {

	/**
	 * Number of lines to read for one page
	 */
	private static final int PAGE_SIZE = 1000;

	/**
	 * Reader for csv input data file
	 */
	private CSVReader inputDatareader = null;

	/**
	 * Source id of the input data file (example: REFO)
	 */
	private String source = "";

	/**
	 * Header line defined in the input data file
	 */
	private boolean header = false;

	/**
	 * Number of read lines in the input data file
	 */
	private int countReadLines = 0;

	public InputObjectsPaginater(CSVReader csvReader, String source, boolean header) {
		this.inputDatareader = csvReader;
		this.source = source;
		this.header = header;
		this.countReadLines = 0;
	}

	public int getPageMaxSize() {
		return PAGE_SIZE;
	}

	public String getSource() {
		return source;
	}

	/**
	 * @return List of read lines from input data file. Null if no line.
	 */
	public List<String[]> getNextPage() {
		List<String[]> pageContents = new ArrayList<>();

		if (this.inputDatareader == null) {
			return null;
		}

		String[] lineContents = null;
		int i = 0;
		try {
			while (i < PAGE_SIZE) {
				lineContents = this.inputDatareader.readNext();
				if (lineContents == null) {
					break;
				}

				// If header defined, ignore the first line (release) and the second line
				// (header description)
				if (countReadLines < 2 && header) {
					++countReadLines;
					continue;
				}

				// Ignore the line if it contains no data or starts with comments char
				if (CSVReader.isValidLine(lineContents)) {
					pageContents.add(lineContents);
					++i;
				}
				++countReadLines;
			}
		} catch (IOException e) {
			return null;
		}

		return pageContents;
	}

	/**
	 * @return columns numbers according to header data defined in the file and expected attributes
	 */
	public Map<String, Integer> definedMapAttributesFromHeader(Map<String, String> mapAttributesHeaderFile) {
		Map<String, Integer> mapAttributesFile = new HashMap<>();

		if (countReadLines < 2 && header && mapAttributesHeaderFile != null && !mapAttributesHeaderFile.isEmpty()) {
			String[] lineContents;
			try {
				// First line = release -> to ignore
				lineContents = this.inputDatareader.readNext();
				if (lineContents != null) {
					++countReadLines;

					// Second line = header
					lineContents = this.inputDatareader.readNext();
					if (lineContents != null) {
						++countReadLines;
						for (int i = 0; i < lineContents.length; i++) {
							// add the column number if the column name is associated to one attribute
							if (mapAttributesHeaderFile.get(lineContents[i]) != null) {
								mapAttributesFile.put(mapAttributesHeaderFile.get(lineContents[i]), i);
							}
						}
					}
				}
			} catch (IOException e) {
			}
		}

		return mapAttributesFile;
	}
}
