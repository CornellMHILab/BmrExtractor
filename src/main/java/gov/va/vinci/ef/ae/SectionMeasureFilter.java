package gov.va.vinci.ef.ae;

import gov.va.vinci.ef.types.TermContext;

/*
 * #%L
 * Echo concept exctractor
 * %%
 * Copyright (C) 2010 - 2016 Department of Veterans Affairs
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import gov.va.vinci.leo.AnnotationLibrarian;
import gov.va.vinci.leo.ae.LeoBaseAnnotator;
import gov.va.vinci.leo.descriptors.LeoConfigurationParameter;
import gov.va.vinci.leo.descriptors.LeoTypeSystemDescription;
import gov.va.vinci.leo.tools.LeoUtils;
import gov.va.vinci.leo.window.WindowService;
import gov.va.vinci.leo.window.types.Window;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilizes sets of regular expressions to filter out invalid numeric values.
 * <p>
 * Created by prakash on 11/30/17.
 */
public class SectionMeasureFilter extends LeoBaseAnnotator {

	protected Pattern bxSectionHeaderPattern = Pattern.compile("bone\\s*marrow.{0,30}biopsy.{0,30}:", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	protected Pattern aspSectionHeaderPattern = Pattern.compile("bone\\s*marrow.{0,30}aspirate\\s*smears?.{0,30}:", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	protected Pattern cytSectionHeaderPattern = Pattern.compile("bone\\s*marrow.{0,30}flow\\s*cytometry.{0,30}:", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE);

	/**
	 * Path to the regex file to parse.
	 */
	@LeoConfigurationParameter

	protected String regexFilePath = null;

	/**
	 * Patterns for which if there is a match in the window before the anchor
	 * then the relationship is invalid.
	 */
	protected Pattern[] bxPatterns = null;

	/**
	 * Patterns for which if there is a match in the window after the anchor
	 * then the relationship is invalid.
	 */
	protected Pattern[] aspPatterns = null;

	/**
	 * Patterns matched on the covered text of the anchor itself.
	 */
	protected Pattern[] cytPatterns = null;

	/**
	 * Window service class.
	 */
	protected WindowService windowService = new WindowService(2, 2, Window.class.getCanonicalName());

	/**
	 * Pattern flags for each regex.
	 */
	public static int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;

	/**
	 * Log messages
	 */
	private static final Logger log = Logger.getLogger(LeoUtils.getRuntimeClass());

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		if (StringUtils.isBlank(regexFilePath))
			throw new ResourceInitializationException("regexFilePath cannot be blank or null", null);

		try {
			parseRegexFile(new File(regexFilePath));
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void annotate(JCas aJCas) throws AnalysisEngineProcessException {
		// Get the document ID
		String documentID = null;
		Collection<gov.va.vinci.leo.types.CSI> infoList = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
				gov.va.vinci.leo.types.CSI.type, false);
		if (infoList.size() > 0) {
			documentID = infoList.iterator().next().getID();
		} else {
			documentID = "Num:" + this.numberOfCASesProcessed;
		}

		// filter biopsy sections where section header contains aspirate smears or cytometry terms
		Collection<gov.va.vinci.ef.types.SecBiopsy> bx_sections = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
				gov.va.vinci.ef.types.SecBiopsy.type, false);
		for (gov.va.vinci.ef.types.SecBiopsy bx_section : bx_sections) {
			try {

				if (hasBxHeaderPatternMatch(bx_section)) {
					bx_section.removeFromIndexes();
				}

			} catch (Exception e) {
				log.error("Error processing " + documentID);
				throw new AnalysisEngineProcessException(e);
			}
		}
		
		// aspirate smear sections where section header contains biopsy or cytometry terms
		Collection<gov.va.vinci.ef.types.SecAspirate> asp_sections = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
				gov.va.vinci.ef.types.SecAspirate.type, false);
		for (gov.va.vinci.ef.types.SecAspirate asp_section : asp_sections) {
			try {

				if (hasAspHeaderPatternMatch(asp_section)) {
					asp_section.removeFromIndexes();
				}

			} catch (Exception e) {
				log.error("Error processing " + documentID);
				throw new AnalysisEngineProcessException(e);
			}
		}
		
		// cytometry sections where section header contains biopsy or aspirate smears 
		Collection<gov.va.vinci.ef.types.SecCytometry> cyt_sections = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
				gov.va.vinci.ef.types.SecCytometry.type, false);
		for (gov.va.vinci.ef.types.SecCytometry cyt_section : cyt_sections) {
			try {

				if (hasCytHeaderPatternMatch(cyt_section)) {
					cyt_section.removeFromIndexes();
				}

			} catch (Exception e) {
				log.error("Error processing " + documentID);
				throw new AnalysisEngineProcessException(e);
			}
		}
		
		
		
	}

	/**
	 * Return true if one of the section header patterns match.
	 *
	 * @param a Annotation
	 *          
	 * @return true if there is a match, false otherwise
	 */
	public boolean hasBxHeaderPatternMatch(Annotation a) {
		Boolean found = false;

		String hdrString = a.getCoveredText();
		Matcher headerMatches = bxSectionHeaderPattern.matcher(hdrString);

		while (headerMatches.find()) {
			
			// System.out.println("found: " + headerMatches.start() + " - " + headerMatches.end());
			
			String hdr = hdrString.substring(headerMatches.start(), headerMatches.end());
			found = hasMatch(bxPatterns, hdr);
		}

		return found;
	}
	
	/**
	 * Return true if one of the section header patterns match.
	 *
	 * @param a Annotation
	 *          
	 * @return true if there is a match, false otherwise
	 */
	public boolean hasAspHeaderPatternMatch(Annotation a) {
		Boolean found = false;

		String hdrString = a.getCoveredText();
		Matcher headerMatches = aspSectionHeaderPattern.matcher(hdrString);

		while (headerMatches.find()) {
			String hdr = hdrString.substring(headerMatches.start(), headerMatches.end());
			found = hasMatch(aspPatterns, hdr);
		}

		return found;
	}
	
	
	/**
	 * Return true if one of the section header patterns match.
	 *
	 * @param a Annotation
	 *          
	 * @return true if there is a match, false otherwise
	 */
	public boolean hasCytHeaderPatternMatch(Annotation a) {
		Boolean found = false;

		String hdrString = a.getCoveredText();
		Matcher headerMatches = cytSectionHeaderPattern.matcher(hdrString);

		while (headerMatches.find()) {
			String hdr = hdrString.substring(headerMatches.start(), headerMatches.end());
			found = hasMatch(cytPatterns, hdr);
		}

		return found;
	}


	/**
	 * Returns true if the text provided has a match in one of the patterns.
	 *
	 * @param patterns
	 * @param text
	 * @return
	 */
	protected boolean hasMatch(Pattern[] patterns, String text) {
		boolean hasMatch = false;
		for (Pattern p : patterns) {
			// System.out.println("Match: pattern: " + p + " text: " + text);

			if (p.matcher(text).find()) {
				hasMatch = true;
				log.debug("Removing Value:\n\tpattern: " + p.pattern() + "\n\ttext: " + text);
				break;
			}
		}
		return hasMatch;
	}

	/**
	 * Get the patterns from the regex file and stash them in the appropriate
	 * lists.
	 *
	 * @param regexFile
	 *            File from which to retrieve the patterns
	 * @throws IOException
	 *             If there is an error reading the file.
	 */
	protected void parseRegexFile(File regexFile) throws IOException {
		if (regexFile == null)
			throw new IllegalArgumentException("regexFile cannot be null");
		// List of Patterns to compile
		ArrayList<Pattern> bxList = new ArrayList<Pattern>();
		ArrayList<Pattern> aspList = new ArrayList<Pattern>();
		ArrayList<Pattern> cytList = new ArrayList<Pattern>();
		int patternType = 3;
		// Read in the lines of the regex file
		List<String> lines = IOUtils.readLines(FileUtils.openInputStream(regexFile));
		for (String line : lines) {
			if (line.startsWith("#")) {
				if (line.startsWith("#BX"))
					patternType = 1;
				else if (line.startsWith("#ASP"))
					patternType = 2;
				else if (line.startsWith("#CYT"))
					patternType = 3;
			} else if (StringUtils.isNotBlank(line)) {
				if (patternType == 1)
					bxList.add(Pattern.compile(line, PATTERN_FLAGS));
				else if (patternType == 2)
					aspList.add(Pattern.compile(line, PATTERN_FLAGS));
				else if (patternType == 3)
					cytList.add(Pattern.compile(line, PATTERN_FLAGS));
			}
		}
		// Stash each collection
		bxPatterns = bxList.toArray(new Pattern[bxList.size()]);
		aspPatterns = aspList.toArray(new Pattern[aspList.size()]);
		cytPatterns = cytList.toArray(new Pattern[cytList.size()]);
	}

	@Override
	public LeoTypeSystemDescription getLeoTypeSystemDescription() {
		return new LeoTypeSystemDescription();
	}

	public String getRegexFilePath() {
		return regexFilePath;
	}

	public SectionMeasureFilter setRegexFilePath(String regexFilePath) {
		this.regexFilePath = regexFilePath;
		return this;
	}

}
