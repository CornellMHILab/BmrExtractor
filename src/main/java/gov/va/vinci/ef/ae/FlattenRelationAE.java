package gov.va.vinci.ef.ae;

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
import gov.va.vinci.leo.descriptors.LeoTypeSystemDescription;
import gov.va.vinci.leo.tools.LeoUtils;
import gov.va.vinci.leo.window.WindowService;
import gov.va.vinci.leo.window.types.Window;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.impl.TypeDescription_impl;
import gov.va.vinci.ef.types.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flatten relationships for easier output.
 * <p>
 * Created by vhaslcpatteo on 9/16/2015. Edited by Thomas Ginter on 09/18/2015. Added the setValueStrings method. Edited by padekkanattu
 * 10/01/17
 */
public class FlattenRelationAE extends LeoBaseAnnotator {
	protected Pattern numberPattern = Pattern.compile("\\d+");
	protected Pattern blastPattern = Pattern.compile("\\d+");
	protected Pattern cellPattern = Pattern.compile("\\d+");

	protected Pattern reticulinPattern = Pattern.compile("\\breticulin\\b", Pattern.CASE_INSENSITIVE);
	protected Pattern trichromePattern = Pattern.compile("\\b(collagen|trichrome)\\b", Pattern.CASE_INSENSITIVE);

	protected WindowService windowService = new WindowService(15, 15, Window.class.getCanonicalName());

	/**
	 * Log messages
	 */
	private static final Logger log = Logger.getLogger(LeoUtils.getRuntimeClass());

	@Override
	public void annotate(JCas aJCas) throws AnalysisEngineProcessException {

		// get today's date
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		
		// System.out.println("Date:" + timeStamp + "\n");
		
		//Get the document ID
	    String documentID = null;
	    Collection<gov.va.vinci.leo.types.CSI> infoList = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, gov.va.vinci.leo.types.CSI.type, false);
	    if (infoList.size() > 0) {
	      documentID = infoList.iterator().next().getID();
	    } else {
	      documentID = "Num:" + this.numberOfCASesProcessed;
	    }		
	    // System.out.println("Document ID:" + documentID + "\n");
	    
		
//		Collection<TextSegment> txt_values = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, TextSegment.type, false);
//		for (gov.va.vinci.ef.types.TextSegment txt_value : txt_values) {
//			System.out.println("****Text Segment:**** " + txt_value.getCoveredText());
//		}
//
//		Collection<SecBiopsy> bx_values = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, SecBiopsy.type, false);
//		for (gov.va.vinci.ef.types.SecBiopsy bx_value : bx_values) {
//			System.out.println("****Biopsy Sections:**** \n");
//			System.out.println(bx_value.getCoveredText() + "\n\n");
//		}
//
//		Collection<gov.va.vinci.ef.types.BxBlastTerm> terms = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, gov.va.vinci.ef.types.BxBlastTerm.type,
//		false);
//		if (terms.size() > 0) {
//			for (gov.va.vinci.ef.types.BxBlastTerm term : terms) {
//				System.out.println("****Biopsy Blast Term:**** " + term.getCoveredText());
//			}
//		}
		
//		Collection<gov.va.vinci.ef.types.BxBlastSentence> sents = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, gov.va.vinci.ef.types.BxBlastSentence.type,
//		false);
//		for (gov.va.vinci.ef.types.BxBlastSentence sent : sents) {
//			System.out.println("****Biopsy Blast Sentence:**** " + sent.getCoveredText());
//		}


//		Collection<SecAspirate> asp_values = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, SecAspirate.type, false);
//		for (gov.va.vinci.ef.types.SecAspirate asp_value : asp_values) {
//			System.out.println("****Aspirate Sections:**** " + asp_value.getCoveredText());
//		}
//		
//		Collection<SecCytometry> cyt_values = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, SecCytometry.type, false);
//		for (gov.va.vinci.ef.types.SecCytometry cyt_value : cyt_values) {
//			System.out.println("****Cytometry Sections:**** " + cyt_value.getCoveredText());
//		}
	    
	    
//	    Collection<BxFibNumericValue> bx_fib_grades = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, BxFibNumericValue.type, false);
//		for (gov.va.vinci.ef.types.BxFibNumericValue bx_fib_grade : bx_fib_grades) {
//			System.out.println("****Biopsy grade:**** " + bx_fib_grade.getCoveredText() + "\n");
//		}
//		
//		
//		Collection<BxFibValueRelation> bx_fib_val_rels = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, BxFibValueRelation.type, false);
//		for (gov.va.vinci.ef.types.BxFibValueRelation bx_fib_val_rel : bx_fib_val_rels) {
//			System.out.println("****Biopsy Value relation:**** " + bx_fib_val_rel.getCoveredText());
//		}
	    

		// Biopsy: BxBlast Relation
		Collection<BxBlastRelation> bx_blast_relations = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, BxBlastRelation.type, false);
		if (bx_blast_relations.size() > 0) {
			for (BxBlastRelation relation : bx_blast_relations) {
				// Create the output annotation
				Relation out = new Relation(aJCas, relation.getBegin(), relation.getEnd());				
				// set time stamp 
				out.setRunDate(timeStamp);
				// add to index
				out.addToIndexes();
				// Set the string value features
				setBxBlastValue(relation, out);
			}
		}

		// Biopsy: Cellularity Value Relation
		Collection<BxCellValueRelation> cell_relations = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, BxCellValueRelation.type,
				false);
		if (cell_relations.size() > 0) {
			for (BxCellValueRelation relation : cell_relations) {
				// Create the output annotation
				Relation out = new Relation(aJCas, relation.getBegin(), relation.getEnd());
				// set time stamp 
				out.setRunDate(timeStamp);
				// add to index
				out.addToIndexes();
				// Set the string value features
				setBxCellValue(relation, out);
			}
		}

		// Biopsy: Cellularity Assessment Relation
		Collection<BxCellAssessmentRelation> cell_concepts = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
				BxCellAssessmentRelation.type, false);
		if (cell_concepts.size() > 0) {
			for (BxCellAssessmentRelation concept : cell_concepts) {
				// Create the output annotation
				Relation out = new Relation(aJCas, concept.getBegin(), concept.getEnd());
				// set time stamp 
				out.setRunDate(timeStamp);
				// add to index
				out.addToIndexes();
				// Set the string value features
				setBxCellAssessment(concept, out);
			}
		}

		// Biopsy: Fibrosis Value Relation
		Collection<BxFibValueRelation> fib_relations = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, BxFibValueRelation.type, false);
		if (fib_relations.size() > 0) {
			for (BxFibValueRelation relation : fib_relations) {
				// Create the output annotation
				Relation out = new Relation(aJCas, relation.getBegin(), relation.getEnd());
				// set time stamp 
				out.setRunDate(timeStamp);
				// add to index
				out.addToIndexes();
				// Set the string value features
				setBxFibValue(relation, out);
			}
		}

		// Biopsy: fibrosis qualitative assessment: Reticulin and Trichrome
		Collection<AnchoredSentence> term_sentences = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, AnchoredSentence.type, false);
		if (term_sentences.size() > 0) {
			for (AnchoredSentence sentence : term_sentences) {
				Matcher rMatches = reticulinPattern.matcher(sentence.getCoveredText());
				if (rMatches.find()) {
					String assessment = "";
					String term = "";
					term = "bx_fib_ret_qual";
					assessment = sentence.getCoveredText();
					if ((StringUtils.isNotBlank(term)) && (StringUtils.isNotBlank(assessment))) {
						// Create the output annotation
						Relation out = new Relation(aJCas, sentence.getBegin(), sentence.getEnd());
						out.setTerm(term);
						out.setAssessment(assessment);
						// set time stamp 
						out.setRunDate(timeStamp);
						// add to index
						out.addToIndexes();
					}
				}

				Matcher tMatches = trichromePattern.matcher(sentence.getCoveredText());
				if (tMatches.find()) {
					String assessment = "";
					String term = "";
					term = "bx_fib_trich_qual";
					assessment = sentence.getCoveredText();
					if ((StringUtils.isNotBlank(term)) && (StringUtils.isNotBlank(assessment))) {
						// Create the output annotation
						Relation out = new Relation(aJCas, sentence.getBegin(), sentence.getEnd());
						out.setTerm(term);
						out.setAssessment(assessment);
						// set time stamp 
						out.setRunDate(timeStamp);
						// add to index
						out.addToIndexes();
					}
				}
			}
		}

		// Aspirate: AspBlastDifferential Relation
		Collection<AspBlastDifferentialRelation> asp_blast_diff_relations = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
				AspBlastDifferentialRelation.type, false);
		if (asp_blast_diff_relations.size() > 0) {
			for (AspBlastDifferentialRelation relation : asp_blast_diff_relations) {
				// Create the output annotation
				Relation out = new Relation(aJCas, relation.getBegin(), relation.getEnd());
				// set time stamp 
				out.setRunDate(timeStamp);
				// add to index
				out.addToIndexes();
				// Set the string value features
				setAspBlastValue(relation, out);
			}
		}

		// Aspirate: Cellularity Value Relation
		Collection<AspCellValueRelation> asp_cell_relations = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, AspCellValueRelation.type,
				false);
		if (asp_cell_relations.size() > 0) {
			for (AspCellValueRelation relation : asp_cell_relations) {
				// Create the output annotation
				Relation out = new Relation(aJCas, relation.getBegin(), relation.getEnd());
				// set time stamp 
				out.setRunDate(timeStamp);
				// add to index
				out.addToIndexes();
				// Set the string value features
				setAspCellValue(relation, out);
			}
		}

		// Aspirate: Cellularity Assessment Relation
		Collection<AspCellAssessmentRelation> asp_cell_concepts = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
				AspCellAssessmentRelation.type, false);
		if (asp_cell_concepts.size() > 0) {
			for (AspCellAssessmentRelation concept : asp_cell_concepts) {
				// Create the output annotation
				Relation out = new Relation(aJCas, concept.getBegin(), concept.getEnd());
				// set time stamp 
				out.setRunDate(timeStamp);
				// add to index
				out.addToIndexes();
				// Set the string value features
				setAspCellAssessment(concept, out);
			}
		}

		// Cytometry: AspBlastCytometry Relation
		Collection<AspBlastCytometryRelation> asp_blast_cyt_relations = AnnotationLibrarian.getAllAnnotationsOfType(aJCas,
				AspBlastCytometryRelation.type, false);
		if (asp_blast_cyt_relations.size() > 0) {
			for (AspBlastCytometryRelation relation : asp_blast_cyt_relations) {
				// Create the output annotation
				Relation out = new Relation(aJCas, relation.getBegin(), relation.getEnd());
				// set time stamp 
				out.setRunDate(timeStamp);
				// add to index
				out.addToIndexes();
				// Set the string value features
				setCytBlastValue(relation, out);
			}
		}

	}

	// Biopsy: Blast
	protected void setBxBlastValue(BxBlastRelation in, Relation out) {
		// Get the NumericValue annotation from the merged set
		Annotation value = null;
		Annotation measure = null;
		FSArray merged = in.getLinked();
		for (int i = 0; i < merged.size(); i++) {
			Annotation a = (Annotation) merged.get(i);
			String typeName = a.getType().getName();
			if (typeName.equals(BxBlast.class.getCanonicalName())) {
				measure = a;
				if (measure != null) {
					// out.setTerm(measure.getCoveredText());
					out.setTerm("bx_blast");
				}
			} else if (typeName.equals(BxBlastNumericValue.class.getCanonicalName())) {
				value = a;
			}
		}
		// Exit if no value found
		if (value == null)
			return;
		// Get the values
		String valueText = value.getCoveredText();
		Matcher m = blastPattern.matcher(valueText);
		ArrayList<Double> values = new ArrayList<Double>(2);
		while (m.find())
			values.add(new Double(valueText.substring(m.start(), m.end())));
		Collections.sort(values);

		// Set the values
		if (values.size() > 0)
			out.setValue(values.get(0).toString());
		if (values.size() > 1)
			out.setValue2(values.get(values.size() - 1).toString());
		if (StringUtils.isNotBlank(valueText))
			out.setValueString(valueText);

	}

	// Biopsy: Cellularity value
	protected void setBxCellValue(BxCellValueRelation in, Relation out) {
		// Get the NumericValue annotation from the merged set
		Annotation value = null;
		Annotation measure = null;

		FSArray merged = in.getLinked();
		for (int i = 0; i < merged.size(); i++) {
			Annotation a = (Annotation) merged.get(i);
			String typeName = a.getType().getName();

			// System.out.println("Linked: " + a.getCoveredText());
			// System.out.println("TypeName: " + typeName);

			if (typeName.equals(BxCellMeasurement.class.getCanonicalName())) {
				measure = a;
				if (measure != null) {
					// out.setTerm(measure.getCoveredText());
					out.setTerm("bx_cell_quant");
				}
			} else if (typeName.equals(BxCellNumericValue.class.getCanonicalName())) {
				value = a;
			}
		}

		// Exit if no value found
		if (value == null)
			return;

		// Get the values
		String valueText = value.getCoveredText();
		Matcher m = cellPattern.matcher(valueText);
		ArrayList<Double> values = new ArrayList<Double>(2);
		while (m.find())
			values.add(new Double(valueText.substring(m.start(), m.end())));
		Collections.sort(values);

		// Set the values
		if (values.size() > 0)
			out.setValue(values.get(0).toString());
		if (values.size() > 1)
			out.setValue2(values.get(values.size() - 1).toString());
		if (StringUtils.isNotBlank(valueText))
			out.setValueString(valueText);

	}

	// Biopsy: Cellularity assessment
	protected void setBxCellAssessment(BxCellAssessmentRelation in, Relation out) {
		// Get the assessment annotation from the merged set
		Annotation concept = null;
		Annotation qvalue = null;

		FSArray merged = in.getLinked();
		for (int i = 0; i < merged.size(); i++) {
			Annotation a = (Annotation) merged.get(i);
			String typeName = a.getType().getName();

			// System.out.println("TypeName: " + typeName);
			if (typeName.equals(BxCellConcept.class.getCanonicalName())) {
				concept = a;
				if (concept != null) {
					// out.setTerm(concept.getCoveredText());
					out.setTerm("bx_cell_qual");
				}
			} else if (typeName.equals(BxCellQValue.class.getCanonicalName())) {
				qvalue = a;
			}
		}

		// Set the assessment
		String qvalueText = qvalue.getCoveredText();
		if (StringUtils.isNotBlank(qvalueText))
			out.setAssessment(qvalueText);
	}

	// Biopsy: Fibrosis value
	protected void setBxFibValue(BxFibValueRelation in, Relation out) {
		// Get the NumericValue annotation from the merged set
		Annotation value = null;
		Annotation measure = null;

		FSArray merged = in.getLinked();
		for (int i = 0; i < merged.size(); i++) {
			Annotation a = (Annotation) merged.get(i);
			String typeName = a.getType().getName();

			// System.out.println("Linked: " + a.getCoveredText());
			// System.out.println("TypeName: " + typeName);

			if (typeName.equals(BxFibMeasurement.class.getCanonicalName())) {
				measure = a;
				if (measure != null) {
					// out.setTerm(measure.getCoveredText());
					out.setTerm("bx_fib_grade");
				}
			} else if (typeName.equals(BxFibNumericValue.class.getCanonicalName())) {
				value = a;
			}
		}

		// Exit if no value found
		if (value == null)
			return;

		// Get the values
		String valueText = value.getCoveredText();
		if (StringUtils.isNotBlank(valueText))
			out.setValueString(valueText);

	}

	// Aspirate: Blast differential value
	protected void setAspBlastValue(AspBlastDifferentialRelation in, Relation out) {
		// Get the NumericValue annotation from the merged set
		Annotation value = null;
		Annotation measure = null;
		FSArray merged = in.getLinked();
		for (int i = 0; i < merged.size(); i++) {
			Annotation a = (Annotation) merged.get(i);
			String typeName = a.getType().getName();
			if (typeName.equals(AspBlastDifferential.class.getCanonicalName())) {
				measure = a;
				if (measure != null) {
					// out.setTerm(measure.getCoveredText());
					out.setTerm("asp_blast_diff");
				}
			} else if (typeName.equals(AspBlastDifferentialNumericValue.class.getCanonicalName())) {
				value = a;
			}
		}
		// Exit if no value found
		if (value == null)
			return;
		// Get the values
		String valueText = value.getCoveredText();
		Matcher m = blastPattern.matcher(valueText);
		ArrayList<Double> values = new ArrayList<Double>(2);
		while (m.find())
			values.add(new Double(valueText.substring(m.start(), m.end())));
		Collections.sort(values);

		// Set the values
		if (values.size() > 0)
			out.setValue(values.get(0).toString());
		if (values.size() > 1)
			out.setValue2(values.get(values.size() - 1).toString());
		if (StringUtils.isNotBlank(valueText))
			out.setValueString(valueText);

	}

	// Aspirate: cellularity value
	protected void setAspCellValue(AspCellValueRelation in, Relation out) {
		// Get the NumericValue annotation from the merged set
		Annotation value = null;
		Annotation measure = null;

		FSArray merged = in.getLinked();
		for (int i = 0; i < merged.size(); i++) {
			Annotation a = (Annotation) merged.get(i);
			String typeName = a.getType().getName();

			// System.out.println("Linked: " + a.getCoveredText());
			// System.out.println("TypeName: " + typeName);

			if (typeName.equals(AspCellMeasurement.class.getCanonicalName())) {
				measure = a;
				if (measure != null) {
					// out.setTerm(measure.getCoveredText());
					out.setTerm("asp_cell_quant");
				}
			} else if (typeName.equals(AspCellNumericValue.class.getCanonicalName())) {
				value = a;
			}
		}

		// Exit if no value found
		if (value == null)
			return;

		// Get the values
		String valueText = value.getCoveredText();
		Matcher m = cellPattern.matcher(valueText);
		ArrayList<Double> values = new ArrayList<Double>(2);
		while (m.find())
			values.add(new Double(valueText.substring(m.start(), m.end())));
		Collections.sort(values);

		// Set the values
		if (values.size() > 0)
			out.setValue(values.get(0).toString());
		if (values.size() > 1)
			out.setValue2(values.get(values.size() - 1).toString());
		if (StringUtils.isNotBlank(valueText))
			out.setValueString(valueText);

	}

	// Aspirate: Cellularity assessment
	protected void setAspCellAssessment(AspCellAssessmentRelation in, Relation out) {
		// Get the assessment annotation from the merged set
		Annotation concept = null;
		Annotation qvalue = null;

		FSArray merged = in.getLinked();
		for (int i = 0; i < merged.size(); i++) {
			Annotation a = (Annotation) merged.get(i);
			String typeName = a.getType().getName();

			// System.out.println("TypeName: " + typeName);
			if (typeName.equals(AspCellConcept.class.getCanonicalName())) {
				concept = a;
				if (concept != null) {
					// out.setTerm(concept.getCoveredText());
					out.setTerm("asp_cell_qual");
				}
			} else if (typeName.equals(AspCellQValue.class.getCanonicalName())) {
				qvalue = a;
			}
		}

		// Set the assessment
		String qvalueText = qvalue.getCoveredText();
		if (StringUtils.isNotBlank(qvalueText))
			out.setAssessment(qvalueText);
	}

	// Cytometry: blast
	protected void setCytBlastValue(AspBlastCytometryRelation in, Relation out) {
		// Get the NumericValue annotation from the merged set
		Annotation value = null;
		Annotation measure = null;
		FSArray merged = in.getLinked();
		for (int i = 0; i < merged.size(); i++) {
			Annotation a = (Annotation) merged.get(i);
			String typeName = a.getType().getName();
			if (typeName.equals(AspBlastCytometry.class.getCanonicalName())) {
				measure = a;
				if (measure != null) {
					// out.setTerm(measure.getCoveredText());
					out.setTerm("asp_blast_cyt");
				}
			} else if (typeName.equals(AspBlastCytometryNumericValue.class.getCanonicalName())) {
				value = a;
			}
		}
		// Exit if no value found
		if (value == null)
			return;
		// Get the values
		String valueText = value.getCoveredText();
		Matcher m = blastPattern.matcher(valueText);
		ArrayList<Double> values = new ArrayList<Double>(2);
		while (m.find())
			values.add(new Double(valueText.substring(m.start(), m.end())));
		Collections.sort(values);

		// Set the values
		if (values.size() > 0)
			out.setValue(values.get(0).toString());
		if (values.size() > 1)
			out.setValue2(values.get(values.size() - 1).toString());
		if (StringUtils.isNotBlank(valueText))
			out.setValueString(valueText);

	}

	/* test if annotation is in negative context */
	private HashMap<String, String> checkContext(JCas aJCas, Annotation a) throws AnalysisEngineProcessException {
		String ctx = "";

		HashMap<String, String> ctxMap = new HashMap<String, String>();

		try {

			Window window = (Window) windowService.buildNonWhitespaceTokenWindow(a);
			int assBegin = window.getBegin();
			int assEnd = window.getEnd();

			// System.out.println("Concept covered window: " + window.getCoveredText());

			// get all termContext annotations
			Collection<Annotation> terms = AnnotationLibrarian.getAllAnnotationsOfType(aJCas, gov.va.vinci.ef.types.TermContext.type, true);
			for (Annotation t : terms) {
				int tBegin = t.getBegin();
				int tEnd = t.getEnd();
				// check if this term matches for assessment string
				if ((tBegin >= assBegin) && (tEnd <= assEnd)) {
					Feature negFeature = t.getType().getFeatureByBaseName("Negation");
					String negStr = t.getStringValue(negFeature);

					if (negStr.equalsIgnoreCase("Negated")) {
						ctx = "Negated";
					} else if (negStr.equalsIgnoreCase("Affirmed")) {
						ctx = "Affirmed";
					}
					ctxMap.put("term", t.getCoveredText());
					ctxMap.put("context", ctx);
					break;
				}
			}

		} catch (Exception e) {
			log.error("Error check negation processing ");
			throw new AnalysisEngineProcessException(e);
		}

		return ctxMap;
	}

	/***/
	/**
	 * Normalization removes [,][;][=][:][multiple -][|][*] , replaces multiple whitespaces with one and sets to lower case
	 *
	 * @param concept
	 * @return
	 */

	private String normalize(String concept, boolean removeIfNumeric) {
		if (StringUtils.isNotBlank(concept)) {
			String str = concept.toLowerCase().replaceAll(",", " ").replaceAll(";", " ").replaceAll("==+", " ")
					// .replaceAll(":", "")
					.replaceAll("--+", " ").replaceAll("\\|", "").replaceAll("\\*", " ").replaceAll("\\s+", " ").trim();
			if (removeIfNumeric) {
				try {
					Double.parseDouble(str);
					return "";
				} catch (Exception e) {
					return str;
				}
			} else {
				return str;
			}
		} else {
			return "";
		}
	}

	@Override
	public LeoTypeSystemDescription getLeoTypeSystemDescription() {
		TypeDescription relationFtsd;
		String relationParent = "gov.va.vinci.ef.types.Relation";
		relationFtsd = new TypeDescription_impl(relationParent, "", "uima.tcas.Annotation");
		
		relationFtsd.addFeature("RunDate", "", "uima.cas.String");
		
		relationFtsd.addFeature("Term", "", "uima.cas.String"); // Extracted
																// term string
		relationFtsd.addFeature("Value", "", "uima.cas.String"); // Numeric
																	// value
		relationFtsd.addFeature("Value2", "", "uima.cas.String"); // Numeric
																	// value
		relationFtsd.addFeature("Assessment", "", "uima.cas.String"); // String
																		// value
																		// for
																		// concept
																		// assessment

		relationFtsd.addFeature("ValueString", "", "uima.cas.String"); // String
																		// value
																		// with
																		// units
																		// and
																		// extra
																		// modifiers

		LeoTypeSystemDescription types = new LeoTypeSystemDescription();
		try {
			types.addType(relationFtsd); // for target concepts with single
											// mapping

		} catch (Exception e) {
			e.printStackTrace();
		}
		return types;
	}

}
