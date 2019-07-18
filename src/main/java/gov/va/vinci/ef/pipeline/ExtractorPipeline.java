package gov.va.vinci.ef.pipeline;

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

import gov.va.vinci.ef.ae.*;
import gov.va.vinci.leo.annotationpattern.ae.AnnotationPatternAnnotator;
import gov.va.vinci.leo.conceptlink.ae.ConceptLinkAnnotator;
import gov.va.vinci.leo.conceptlink.ae.MatchMakerAnnotator;
import gov.va.vinci.leo.context.ae.ContextAnnotator;
import gov.va.vinci.leo.descriptors.LeoAEDescriptor;
import gov.va.vinci.leo.descriptors.LeoTypeSystemDescription;
import gov.va.vinci.leo.filter.ae.FilterAnnotator;
import gov.va.vinci.leo.regex.ae.RegexAnnotator;
import gov.va.vinci.leo.sentence.ae.AnchoredSentenceAnnotator;
import gov.va.vinci.leo.tools.LeoUtils;
import gov.va.vinci.leo.types.TypeLibrarian;
import gov.va.vinci.leo.window.ae.WindowAnnotator;

import org.apache.log4j.Logger;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.impl.TypeDescription_impl;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Based on the Pipeline class this pipeline is a scalable, optimized echo extraction pipeline that attempts to zero in on concepts and
 * measurements as fast as possible.
 * <p>
 * Created by thomasginter on 07/28/2015. Updated by olga patterson on 06/14/2016 Updated by prakash adekkanattu on 08/25/2016
 */
public class ExtractorPipeline implements PipelineInterface {

	String TYPE_REGEX = "gov.va.vinci.leo.regex.types.RegularExpressionType";
	String TYPE_PATTERN = "gov.va.vinci.leo.annotationpattern.types.AnnotationPatternType";
	String SENTENCE_TYPE = "gov.va.vinci.ef.types.AnchoredSentence";
	String RESOURCE_TERM = "term.regex";
	String BX_BLAST_TERM = "bxBlast.regex";
	String RESOURCE_PATTERN = "context.pattern";
	String RESOURCE_CONTEXT = "contextRules.txt";
	String TYPE_TERM = "gov.va.vinci.elite.types.Term";
	String TYPE_CONTEXT_PATTERN = "gov.va.vinci.elite.types.TermPattern";
	String TYPE_WINDOW_FEATURE = "Anchor";
	String TYPE_CONTEXT = "gov.va.vinci.elite.types.TermContext";

	public static final Logger log = Logger.getLogger(LeoUtils.getRuntimeClass().toString());

	LeoAEDescriptor pipeline = null;
	LeoTypeSystemDescription description = null;

	protected static String RESOURCE_PATH = "resources/";

	public ExtractorPipeline() {
		this.getLeoTypeSystemDescription();
	}

	@Override
	public LeoAEDescriptor getPipeline() throws Exception {
		if (pipeline != null)
			return pipeline;
		
		// Build the pipeline
		LeoTypeSystemDescription types = getLeoTypeSystemDescription();
		pipeline = new LeoAEDescriptor();
		
		/*******************************
		 * Segment: Pipeline for Document segment used for extracting various concepts:
		 ********************************/
		
		// Module: Identify region of report to look for sections
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "textSegment.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setOutputType("gov.va.vinci.ef.types.TextSegment").setName("TextSegmentRegex")
				.addLeoTypeSystemDescription(types).getDescriptor());

		/*******************************
		 * Sections: Pipeline for identifying various sections:
		 ********************************/
		
		// Module: Section Biopsy
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "secBiopsy.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.TextSegment" })
				.setOutputType("gov.va.vinci.ef.types.SecBiopsy").setName("SectionBiopsyRegex").addLeoTypeSystemDescription(types)
				.getDescriptor());

		// Module: Section Aspirate
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "secAspirate.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.TextSegment" })
				.setOutputType("gov.va.vinci.ef.types.SecAspirate").setName("SectionAspirateRegex").addLeoTypeSystemDescription(types)
				.getDescriptor());

		// Module: Section Cytometry
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "secCytometry.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.TextSegment" })
				.setOutputType("gov.va.vinci.ef.types.SecCytometry").setName("SectionCytometryRegex").addLeoTypeSystemDescription(types)
				.getDescriptor());

		// Filter sections that are invalid or shares
		pipeline.addDelegate(new SectionMeasureFilter().setRegexFilePath(RESOURCE_PATH + "sectionInvalidMeasure.regex")
				.setName("SectionMeasureFilter").addLeoTypeSystemDescription(types).getDescriptor());

		/*******************************
		 * Biopsy: Pipeline for Biopsy blast:
		 ********************************/
		
//		// Extract blast qualitative assessment		
//		// Module: find mentions of blast in biopsy section
//		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + BX_BLAST_TERM).setCaseSensitive(false)
//				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecBiopsy" }).setName("BxBlastTermAnnotator")
//				.setOutputType(gov.va.vinci.ef.types.BxBlastTerm.class.getCanonicalName()).getLeoAEDescriptor().setTypeSystemDescription(types));
//		
//		// Module: filter overlapping annotations
//		pipeline.addDelegate(new FilterAnnotator().setTypesToKeep(new String[] { gov.va.vinci.ef.types.BxBlastTerm.class.getCanonicalName() })
//				.getLeoAEDescriptor().setTypeSystemDescription(types));
//		
//		// Module: find sentences covering blast terms
//		pipeline.addDelegate(new AnchoredSentenceAnnotator().setSpanSize(140)
//				.setInputTypes(gov.va.vinci.ef.types.BxBlastTerm.class.getCanonicalName())
//				.setOutputType(gov.va.vinci.ef.types.BxBlastSentence.class.getCanonicalName()).setName("BxBlastTermSentencenAnnotator")
//				.getLeoAEDescriptor().setTypeSystemDescription(types));
		
		// Module: filter overlapping annotations
		pipeline.addDelegate(
				new FilterAnnotator().setTypesToKeep(new String[] { gov.va.vinci.ef.types.BxBlastSentence.class.getCanonicalName() })
						.getLeoAEDescriptor().setTypeSystemDescription(types));
		
		// Extract quantitative measure of blast in biopsy section		
		// Module: MeasurementRegex -- find mentions of biopsy blasts
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "bxBlast.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecBiopsy" })
				.setOutputType("gov.va.vinci.ef.types.BxBlast").setName("BxBlastRegex").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: WindowAnnotator -- make a context window of -7...+7 tokens around the measurement phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(7)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxBlast" }).setOutputType("gov.va.vinci.ef.types.BxBlastWindow")
				.setName("BxBlastWindowAnnotator").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: NumericValueAnnotator -- find numeric value in the context window
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "blastNumericValue.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.BxBlastWindow" })
				.setOutputType("gov.va.vinci.ef.types.BxBlastNumericValue").setName("BxBlastNumericValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MergeRangeValueAnnotator - create a range annotation when the value is expressed as a range
		pipeline.addDelegate(new ConceptLinkAnnotator().setIncludeChildren(true).setMaxCollectionSize(1).setMaxDifference(0)
				.setMaxDistance(10).setPatternFile(RESOURCE_PATH + "blastRange.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxBlastNumericValue" })
				.setOutputType("gov.va.vinci.ef.types.BxBlastNumericValue").setName("BxBlastMergeRangeValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BlastwNumericMeasureFilter -- Filter out all numeric values that do not meet context criteria
		pipeline.addDelegate(new BxBlastNumericMeasureFilter().setRegexFilePath(RESOURCE_PATH + "blastInvalidNumeric.regex")
				.setName("BxBlastNumericMeasureFilter").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BMRValidatorAnnotator -- Filter out all numeric values that do not meet valid range criteria
		pipeline.addDelegate(new BxBlastValidatorAnnotator().setRemoveIfAnyInvalid(false)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxBlastNumericValue" }).setName("BxBlastValidatorAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MeasureToValueRelationAnnotator -- Create Aspirate Blast Differential concept-value
		// pair based on the relational patterns between measurement and value
		pipeline.addDelegate(new MatchMakerAnnotator().setConceptTypeName("gov.va.vinci.ef.types.BxBlast")
				.setValueTypeName("gov.va.vinci.ef.types.BxBlastNumericValue").setPeekRightFirst(true).setMaxCollectionSize(2)
				.setMaxDifference(2).setMaxDistance(50).setPatternFile(RESOURCE_PATH + "blastMiddleStuff.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxBlast", "gov.va.vinci.ef.types.BxBlastNumericValue" })
				.setOutputType("gov.va.vinci.ef.types.BxBlastRelation").setName("BxBlastMeasureToValueRelationAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());
		
		
		
		
		

		/*******************************
		 * Biopsy: Pipeline for Biopsy Cellularity:
		 ********************************/
		// Module: MeasurementRegex -- find mentions of cellularity
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "bxCellMeasure.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecBiopsy" })
				.setOutputType("gov.va.vinci.ef.types.BxCellMeasurement").setName("BxCellMeasurementRegex")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: ConceptRegex -- find mentions of marrow
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "bxCellConcept.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecBiopsy" })
				.setOutputType("gov.va.vinci.ef.types.BxCellConcept").setName("BxCellConceptRegex").addLeoTypeSystemDescription(types)
				.getDescriptor());

		// Module: WindowAnnotator -- make a window of -4...+4 tokens around the measurement phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(4)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxCellMeasurement" })
				.setOutputType("gov.va.vinci.ef.types.BxCellMeasurementWindow").setName("BxCellWindowAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BxCellContextWindowAnnotator -- make a context window of -15 ... +15 tokens around measurement phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(15)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxCellMeasurement" })
				.setOutputType("gov.va.vinci.ef.types.BxCellContextWindow").setName("BxCellContextWindowAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BxCellConceptWindowAnnotator -- create a window of -15 ... +15 tokens around marrow concept phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(15)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxCellConcept" })
				.setOutputType("gov.va.vinci.ef.types.BxCellConceptWindow").setName("BxCellConceptWindowAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: NumericValueAnnotator -- find numeric value in the context window
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "bxCellNumericValue.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.BxCellContextWindow" })
				.setOutputType("gov.va.vinci.ef.types.BxCellNumericValue").setName("BxCellNumericValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BxCellQValueAnnotator - find qualitative value in the concept window
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "bxCellQValue.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.BxCellConceptWindow" })
				.setOutputType("gov.va.vinci.ef.types.BxCellQValue").setName("BxCellQValueAnnotator").addLeoTypeSystemDescription(types)
				.getDescriptor());

		// Module: MergeRangeValueAnnotator - create a range annotation when the value is expressed as a range
		pipeline.addDelegate(new ConceptLinkAnnotator().setIncludeChildren(true).setMaxCollectionSize(1).setMaxDifference(0)
				.setMaxDistance(10).setPatternFile(RESOURCE_PATH + "bxCellRange.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxCellNumericValue" })
				.setOutputType("gov.va.vinci.ef.types.BxCellNumericValue").setName("BxCellMergeRangeValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module 9: CellNumericMeasureFilter -- Filter out all numeric values that do not meet context criteria
		pipeline.addDelegate(new BxCellNumericMeasureFilter().setRegexFilePath(RESOURCE_PATH + "bxCellInvalidNumeric.regex")
				.setName("BxCellNumericMeasureFilter").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BxCellValidatorAnnotator -- Filter out all numeric values that do not meet valid range criteria
		pipeline.addDelegate(new BxCellValidatorAnnotator().setRemoveIfAnyInvalid(false)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxCellNumericValue" }).setName("BxCellValidatorAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MeasureToValueRelationAnnotator -- Create cellularity concept-value pair based on the relational patterns between
		// measurement and value
		pipeline.addDelegate(
				new MatchMakerAnnotator().setConceptTypeName("gov.va.vinci.ef.types.BxCellMeasurement")
						.setValueTypeName("gov.va.vinci.ef.types.BxCellNumericValue").setPeekRightFirst(true).setMaxCollectionSize(2)
						.setMaxDifference(2).setMaxDistance(50).setPatternFile(RESOURCE_PATH + "bxCellValueMiddleStuff.regex")
						.setRemoveCovered(true)
						.setInputTypes(
								new String[] { "gov.va.vinci.ef.types.BxCellMeasurement", "gov.va.vinci.ef.types.BxCellNumericValue" })
						.setOutputType("gov.va.vinci.ef.types.BxCellValueRelation").setName("BxCellMeasureToValueRelationAnnotator")
						.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: ConceptToAssessmentRelationAnnotator -- Create bone marrow concept-assessment pair based on the relational patterns
		// between concept and assessment
		pipeline.addDelegate(new MatchMakerAnnotator().setConceptTypeName("gov.va.vinci.ef.types.BxCellConcept")
				.setValueTypeName("gov.va.vinci.ef.types.BxCellQValue").setPeekRightFirst(true).setMaxCollectionSize(2).setMaxDifference(2)
				.setMaxDistance(50).setPatternFile(RESOURCE_PATH + "bxCellAssessMiddleStuff.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxCellConcept", "gov.va.vinci.ef.types.BxCellQValue" })
				.setOutputType("gov.va.vinci.ef.types.BxCellAssessmentRelation").setName("BxCellConceptToAssessementRelationAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		/*******************************
		 * Biopsy: Pipeline for Biopsy Fibrosis:
		 ********************************/
		// *****Context algorithm begins:*****
		// Testing negation for fibrosis. Adopted from the Elite package developed by Olga Patterson
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + RESOURCE_TERM).setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecBiopsy" }).setName("TermAnnotator")
				.setOutputType(gov.va.vinci.ef.types.Term.class.getCanonicalName()).getLeoAEDescriptor().setTypeSystemDescription(types));

		pipeline.addDelegate(new FilterAnnotator().setTypesToKeep(new String[] { gov.va.vinci.ef.types.Term.class.getCanonicalName() })
				.getLeoAEDescriptor().setTypeSystemDescription(types));

		log.info("Resource files will be used  : \r\n" + (new File(RESOURCE_PATH + RESOURCE_TERM)).getAbsolutePath() + "\r\n"
				+ (new File(RESOURCE_PATH + RESOURCE_CONTEXT)).getAbsolutePath());

		/* Pattern detection AnnotationPatternAnnotation -- context.pattern */
		pipeline.addDelegate(new AnnotationPatternAnnotator().setResource(RESOURCE_PATH + RESOURCE_PATTERN).setCaseSensitive(false)
				.setOutputType(gov.va.vinci.ef.types.TermPattern.class.getCanonicalName()).setName("ContextPatternAnnotator")
				.getLeoAEDescriptor().setTypeSystemDescription(types));

		/* SentenceAnnotation */
		pipeline.addDelegate(new AnchoredSentenceAnnotator().setSpanSize(200).setAnchorFeature("Anchor")
				.setInputTypes(gov.va.vinci.ef.types.Term.class.getCanonicalName())
				.setOutputType(gov.va.vinci.ef.types.AnchoredSentence.class.getCanonicalName()).setName("TermSentencenAnnotator")
				.getLeoAEDescriptor().setTypeSystemDescription(types));

		pipeline.addDelegate(
				new FilterAnnotator().setTypesToKeep(new String[] { gov.va.vinci.ef.types.TermContext.class.getCanonicalName() })
						.getLeoAEDescriptor().setTypeSystemDescription(types));

		pipeline.addDelegate(
				new FilterAnnotator().setTypesToKeep(new String[] { gov.va.vinci.ef.types.AnchoredSentence.class.getCanonicalName() })
						.getLeoAEDescriptor().setTypeSystemDescription(types));

		// ContextAnnotator based on Chapman's ConText with VINCI improvements
		pipeline.addDelegate(new ContextAnnotator().setConceptFeatureName("Anchor").setResourceFile(RESOURCE_PATH + RESOURCE_CONTEXT)
				.setInputTypes(new String[] { gov.va.vinci.ef.types.AnchoredSentence.class.getCanonicalName() })
				.setOutputType(gov.va.vinci.ef.types.TermContext.class.getCanonicalName()).getLeoAEDescriptor()
				.setTypeSystemDescription(types));

		pipeline.addDelegate(
				new FilterAnnotator().setTypesToKeep(new String[] { gov.va.vinci.ef.types.TermContext.class.getCanonicalName() })
						.getLeoAEDescriptor().setTypeSystemDescription(types));

		// Filter out all TermContexts that do not meet context criteria
		pipeline.addDelegate(new ContextMeasureFilter().setRegexFilePath(RESOURCE_PATH + "contextInvalid.regex")
				.setName("ContextMeasureFilter").addLeoTypeSystemDescription(types).getDescriptor());

		// ****Context algorithm ends:****

		// Module: MeasurementRegex -- find mentions of reticulin
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "bxFibMeasure.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecBiopsy" })
				.setOutputType("gov.va.vinci.ef.types.BxFibMeasurement").setName("BxFibMeasurementRegex").addLeoTypeSystemDescription(types)
				.getDescriptor());

		// Module: BxFibContextWindowAnnotator -- create a window of -15 ... +15 tokens around measurement phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(24)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxFibMeasurement" })
				.setOutputType("gov.va.vinci.ef.types.BxFibContextWindow").setName("BxFibContextWindowAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: NumericValueAnnotator -- find numeric value in the context window
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "bxFibNumericValue.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.BxFibContextWindow" })
				.setOutputType("gov.va.vinci.ef.types.BxFibNumericValue").setName("BxFibNumericValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MergeRangeValueAnnotator - create a range annotation when the value is expressed as a range
		pipeline.addDelegate(new ConceptLinkAnnotator().setIncludeChildren(true).setMaxCollectionSize(1).setMaxDifference(0)
				.setMaxDistance(10).setPatternFile(RESOURCE_PATH + "bxFibRange.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxFibNumericValue" })
				.setOutputType("gov.va.vinci.ef.types.BxFibNumericValue").setName("BxFibMergeRangeValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: FibNumericMeasureFilter -- Filter out all numeric values that do not meet context criteria
		pipeline.addDelegate(new BxFibNumericMeasureFilter().setRegexFilePath(RESOURCE_PATH + "bxFibInvalidNumeric.regex")
				.setName("BxFibNumericMeasureFilter").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BxFibValidatorAnnotator -- Filter out all numeric values that do not meet valid range criteria
		pipeline.addDelegate(new BxFibValidatorAnnotator().setRemoveIfAnyInvalid(false)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxFibNumericValue" }).setName("BxFibValidatorAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MeasureToValueRelationAnnotator -- Create fibrosis concept-value pair based on the relational patterns between
		// measurement and value
		pipeline.addDelegate(new MatchMakerAnnotator().setConceptTypeName("gov.va.vinci.ef.types.BxFibMeasurement")
				.setValueTypeName("gov.va.vinci.ef.types.BxFibNumericValue").setPeekRightFirst(true).setMaxCollectionSize(2)
				.setMaxDifference(2).setMaxDistance(140).setPatternFile(RESOURCE_PATH + "bxFibValueMiddleStuff.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.BxFibMeasurement", "gov.va.vinci.ef.types.BxFibNumericValue" })
				.setOutputType("gov.va.vinci.ef.types.BxFibValueRelation").setName("BxFibMeasureToValueRelationAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		/*******************************
		 * Aspirate: Pipeline for ASP Blast Differential:
		 ********************************/

		// Module: MeasurementRegex -- find mentions of aspirate blast
		// differential
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "aspBlastDiff.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecAspirate" })
				.setOutputType("gov.va.vinci.ef.types.AspBlastDifferential").setName("AspBlastDifferentialRegex")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: WindowAnnotator -- make a context window of -7...+7 tokens
		// around the measurement phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(4)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastDifferential" })
				.setOutputType("gov.va.vinci.ef.types.AspBlastDifferentialWindow").setName("AspBlastDifferentialWindowAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: NumericValueAnnotator -- find numeric value in the context
		// window
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "blastNumericValue.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastDifferentialWindow" })
				.setOutputType("gov.va.vinci.ef.types.AspBlastDifferentialNumericValue")
				.setName("AspBlastDifferentialNumericValueAnnotator").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MergeRangeValueAnnotator - create a range annotation when the
		// value is expressed as a range
		pipeline.addDelegate(new ConceptLinkAnnotator().setIncludeChildren(true).setMaxCollectionSize(1).setMaxDifference(0)
				.setMaxDistance(10).setPatternFile(RESOURCE_PATH + "blastRange.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastDifferentialNumericValue" })
				.setOutputType("gov.va.vinci.ef.types.AspBlastDifferentialNumericValue")
				.setName("AspBlastDifferentialMergeRangeValueAnnotator").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BlastwNumericMeasureFilter -- Filter out all numeric values
		// that do not meet context criteria
		pipeline.addDelegate(new AspBlastDifferentialNumericMeasureFilter().setRegexFilePath(RESOURCE_PATH + "blastInvalidNumeric.regex")
				.setName("AspBlastDifferentialNumericMeasureFilter").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BMRValidatorAnnotator -- Filter out all numeric values that
		// do not meet valid range criteria
		pipeline.addDelegate(new AspBlastDifferentialValidatorAnnotator().setRemoveIfAnyInvalid(false)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastDifferentialNumericValue" })
				.setName("AspBlastDifferentialValidatorAnnotator").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MeasureToValueRelationAnnotator -- Create Aspirate Blast
		// Differential concept-value
		// pair based on the relational patterns between measurement and value
		pipeline.addDelegate(new MatchMakerAnnotator().setConceptTypeName("gov.va.vinci.ef.types.AspBlastDifferential")
				.setValueTypeName("gov.va.vinci.ef.types.AspBlastDifferentialNumericValue").setPeekRightFirst(true).setMaxCollectionSize(2)
				.setMaxDifference(2).setMaxDistance(50).setPatternFile(RESOURCE_PATH + "blastMiddleStuff.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastDifferential",
						"gov.va.vinci.ef.types.AspBlastDifferentialNumericValue" })
				.setOutputType("gov.va.vinci.ef.types.AspBlastDifferentialRelation")
				.setName("AspBlastDifferentialMeasureToValueRelationAnnotator").addLeoTypeSystemDescription(types).getDescriptor());

		/*******************************
		 * Aspirate: Pipeline for Aspirate Cellularity:
		 ********************************/

		// Module: MeasurementRegex -- find mentions of cellularity
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "aspCellMeasure.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecAspirate" })
				.setOutputType("gov.va.vinci.ef.types.AspCellMeasurement").setName("AspCellMeasurementRegex")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: ConceptRegex -- find mentions of marrow
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "aspCellConcept.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecAspirate" })
				.setOutputType("gov.va.vinci.ef.types.AspCellConcept").setName("AspCellConceptRegex").addLeoTypeSystemDescription(types)
				.getDescriptor());

		// Module: WindowAnnotator -- make a window of -7...+7 tokens around the measurement phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(4)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspCellMeasurement" })
				.setOutputType("gov.va.vinci.ef.types.AspCellMeasurementWindow").setName("AspCellWindowAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BxCellContextWindowAnnotator -- make a context window of -15 ... +15 tokens around measurement phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(15)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspCellMeasurement" })
				.setOutputType("gov.va.vinci.ef.types.AspCellContextWindow").setName("AspCellContextWindowAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BxCellConceptWindowAnnotator -- create a window of -15 ... +15 tokens around marrow concept phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(15)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspCellConcept" })
				.setOutputType("gov.va.vinci.ef.types.AspCellConceptWindow").setName("AspCellConceptWindowAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: NumericValueAnnotator -- find numeric value in the context window
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "aspCellNumericValue.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.AspCellContextWindow" })
				.setOutputType("gov.va.vinci.ef.types.AspCellNumericValue").setName("AspCellNumericValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BxCellQValueAnnotator - find qualitative value in the concept window
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "aspCellQValue.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.AspCellConceptWindow" })
				.setOutputType("gov.va.vinci.ef.types.AspCellQValue").setName("AspCellQValueAnnotator").addLeoTypeSystemDescription(types)
				.getDescriptor());

		// Module: MergeRangeValueAnnotator - create a range annotation when the value is expressed as a range
		pipeline.addDelegate(new ConceptLinkAnnotator().setIncludeChildren(true).setMaxCollectionSize(1).setMaxDifference(0)
				.setMaxDistance(10).setPatternFile(RESOURCE_PATH + "aspCellRange.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspCellNumericValue" })
				.setOutputType("gov.va.vinci.ef.types.AspCellNumericValue").setName("AspCellMergeRangeValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module 9: CellNumericMeasureFilter -- Filter out all numeric values that do not meet context criteria
		pipeline.addDelegate(new AspCellNumericMeasureFilter().setRegexFilePath(RESOURCE_PATH + "aspCellInvalidNumeric.regex")
				.setName("AspCellNumericMeasureFilter").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BxCellValidatorAnnotator -- Filter out all numeric values that do not meet valid range criteria
		pipeline.addDelegate(new AspCellValidatorAnnotator().setRemoveIfAnyInvalid(false)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspCellNumericValue" }).setName("AspCellValidatorAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MeasureToValueRelationAnnotator -- Create cellularity concept-value pair based on the relational patterns between
		// measurement and value
		pipeline.addDelegate(
				new MatchMakerAnnotator().setConceptTypeName("gov.va.vinci.ef.types.AspCellMeasurement")
						.setValueTypeName("gov.va.vinci.ef.types.AspCellNumericValue").setPeekRightFirst(true).setMaxCollectionSize(2)
						.setMaxDifference(2).setMaxDistance(50).setPatternFile(RESOURCE_PATH + "aspCellValueMiddleStuff.regex")
						.setRemoveCovered(true)
						.setInputTypes(
								new String[] { "gov.va.vinci.ef.types.AspCellMeasurement", "gov.va.vinci.ef.types.AspCellNumericValue" })
						.setOutputType("gov.va.vinci.ef.types.AspCellValueRelation").setName("AspCellMeasureToValueRelationAnnotator")
						.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: ConceptToAssessmentRelationAnnotator -- Create bone marrow concept-assessment pair based on the relational patterns
		// between concept and assessment
		pipeline.addDelegate(new MatchMakerAnnotator().setConceptTypeName("gov.va.vinci.ef.types.AspCellConcept")
				.setValueTypeName("gov.va.vinci.ef.types.AspCellQValue").setPeekRightFirst(true).setMaxCollectionSize(2).setMaxDifference(2)
				.setMaxDistance(50).setPatternFile(RESOURCE_PATH + "aspCellAssessMiddleStuff.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspCellConcept", "gov.va.vinci.ef.types.AspCellQValue" })
				.setOutputType("gov.va.vinci.ef.types.AspCellAssessmentRelation").setName("AspCellConceptToAssessementRelationAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		/*******************************
		 * Cytometry: Pipeline for Cytometry Blast :
		 ********************************/
		// Module: Cytometry Blast Measurement Regex -- find mentions of
		// cytometry blast
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "aspBlastCyt.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.SecCytometry" })
				.setOutputType("gov.va.vinci.ef.types.AspBlastCytometry").setName("AspBlastCytometryRegex")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: WindowAnnotator -- make a context window of -7...+7 tokens
		// around the measurement phrase
		pipeline.addDelegate(new WindowAnnotator().setAnchorFeature("Anchor").setWindowSize(7)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastCytometry" })
				.setOutputType("gov.va.vinci.ef.types.AspBlastCytometryWindow").setName("AspBlastCytometryWindowAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: NumericValueAnnotator -- find numeric value in the context
		// window
		pipeline.addDelegate(new RegexAnnotator().setResource(RESOURCE_PATH + "blastNumericValue.regex").setCaseSensitive(false)
				.setPatternFlags(Pattern.DOTALL).setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastCytometryWindow" })
				.setOutputType("gov.va.vinci.ef.types.AspBlastCytometryNumericValue").setName("AspBlastCytometryNumericValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MergeRangeValueAnnotator - create a range annotation when the
		// value is expressed as a range
		pipeline.addDelegate(new ConceptLinkAnnotator().setIncludeChildren(true).setMaxCollectionSize(1).setMaxDifference(0)
				.setMaxDistance(10).setPatternFile(RESOURCE_PATH + "blastRange.regex").setRemoveCovered(true)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastCytometryNumericValue" })
				.setOutputType("gov.va.vinci.ef.types.AspBlastCytometryNumericValue").setName("AspBlastCytometryMergeRangeValueAnnotator")
				.addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BlastwNumericMeasureFilter -- Filter out all numeric values
		// that do not meet context criteria
		pipeline.addDelegate(new AspBlastCytometryNumericMeasureFilter().setRegexFilePath(RESOURCE_PATH + "blastInvalidNumeric.regex")
				.setName("AspBlastCytometryNumericMeasureFilter").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: BMRValidatorAnnotator -- Filter out all numeric values that
		// do not meet valid range criteria
		pipeline.addDelegate(new AspBlastCytometryValidatorAnnotator().setRemoveIfAnyInvalid(false)
				.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastCytometryNumericValue" })
				.setName("AspBlastCytometryValidatorAnnotator").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: MeasureToValueRelationAnnotator -- Create Aspirate Blast
		// Differential concept-value
		// pair based on the relational patterns between measurement and value
		pipeline.addDelegate(
				new MatchMakerAnnotator().setConceptTypeName("gov.va.vinci.ef.types.AspBlastCytometry")
						.setValueTypeName("gov.va.vinci.ef.types.AspBlastCytometryNumericValue").setPeekRightFirst(true)
						.setMaxCollectionSize(2).setMaxDifference(2).setMaxDistance(50)
						.setPatternFile(RESOURCE_PATH + "blastMiddleStuff.regex").setRemoveCovered(true)
						.setInputTypes(new String[] { "gov.va.vinci.ef.types.AspBlastCytometry",
								"gov.va.vinci.ef.types.AspBlastCytometryNumericValue" })
						.setOutputType("gov.va.vinci.ef.types.AspBlastCytometryRelation")
						.setName("AspBlastCytometryMeasureToValueRelationAnnotator").addLeoTypeSystemDescription(types).getDescriptor());

		// Module: FlattenRelationAE -- Create a new type for ease of writing results to csv or database
		pipeline.addDelegate(new FlattenRelationAE().setName("FlattenRelationAE").addLeoTypeSystemDescription(types).getDescriptor());

		return pipeline;
	}

	@Override
	public LeoTypeSystemDescription getLeoTypeSystemDescription() {
		if (description != null)
			return description;
		description = new LeoTypeSystemDescription();
		String linkParent = "gov.va.vinci.leo.conceptlink.types.Link";

		// Pattern Type description
		TypeDescription regexFtsd;
		String regexParent = "gov.va.vinci.ef.types.Regex";
		regexFtsd = new TypeDescription_impl(regexParent, "", "uima.tcas.Annotation");
		regexFtsd.addFeature("pattern", "", "uima.cas.String");
		regexFtsd.addFeature("concept", "", "uima.cas.String");
		regexFtsd.addFeature("groups", "", "uima.cas.StringArray");

		// Sentence Type description
		TypeDescription sentFtsd;
		String sentParent = "gov.va.vinci.ef.types.AnchoredSentence";
		sentFtsd = new TypeDescription_impl(sentParent, "", "uima.tcas.Annotation");
		sentFtsd.addFeature("Anchor", "", "uima.tcas.Annotation");

		// Context
		TypeDescription contextFtsd;
		String contextParent = "gov.va.vinci.ef.types.TermContext";
		contextFtsd = new TypeDescription_impl(contextParent, "", "uima.tcas.Annotation");
		contextFtsd.addFeature("Experiencer", "", "uima.cas.String");
		contextFtsd.addFeature("ExperiencerPattern", "", "uima.cas.String");
		contextFtsd.addFeature("Negation", "", "uima.cas.String");
		contextFtsd.addFeature("NegationPattern", "", "uima.cas.String");
		contextFtsd.addFeature("Temporality", "", "uima.cas.String");
		contextFtsd.addFeature("TemporalityPattern", "", "uima.cas.String");
		contextFtsd.addFeature("Window", "", "uima.tcas.Annotation");

		// Pattern Type description
		TypeDescription patternFtsd;
		String patternParent = "gov.va.vinci.ef.types.AnnotationPatternType";
		patternFtsd = new TypeDescription_impl(patternParent, "", "uima.tcas.Annotation");
		patternFtsd.addFeature("anchor", "", "uima.tcas.Annotation");
		patternFtsd.addFeature("anchorPattern", "", "uima.cas.String");
		patternFtsd.addFeature("target", "", "uima.tcas.Annotation");
		patternFtsd.addFeature("targetPattern", "", "uima.cas.String");
		patternFtsd.addFeature("pattern", "", "uima.cas.String");

		// Total type definition
		try {
			description.addType(TypeLibrarian.getCSITypeSystemDescription())
					.addTypeSystemDescription(new WindowAnnotator().getLeoTypeSystemDescription())
					.addTypeSystemDescription(new ConceptLinkAnnotator().getLeoTypeSystemDescription()).addType(regexFtsd)
					.addType(patternFtsd).addType(sentFtsd).addType(contextFtsd)

					// Text segment
					.addType("gov.va.vinci.ef.types.TextSegment", "", regexParent)

					// Sections
					.addType("gov.va.vinci.ef.types.SecBiopsy", "", regexParent)
					.addType("gov.va.vinci.ef.types.SecAspirate", "", regexParent)
					.addType("gov.va.vinci.ef.types.SecCytometry", "", regexParent)

					// Biopsy
					.addType("gov.va.vinci.ef.types.BxBlast", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxBlastWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.BxBlastNumericValue", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxBlastRelation", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxBlastTerm", "", regexParent)
					.addType("gov.va.vinci.ef.types.BxBlastSentence", "", "uima.tcas.Annotation")

					.addType("gov.va.vinci.ef.types.BxCellValueRelation", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxCellAssessmentRelation", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxCellMeasurementWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.BxCellContextWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.BxCellConceptWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.BxCellMeasurement", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxCellConcept", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxCellNumericValue", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxCellQValue", "", linkParent)

					.addType("gov.va.vinci.ef.types.BxFibValueRelation", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxFibAssessmentRelation", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxFibMeasurementWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.BxFibContextWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.BxFibConceptWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.BxFibMeasurement", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxFibConcept", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxFibNumericValue", "", linkParent)
					.addType("gov.va.vinci.ef.types.BxFibQValue", "", linkParent)

					// Aspirate
					.addType("gov.va.vinci.ef.types.AspBlastDifferential", "", linkParent)
					.addType("gov.va.vinci.ef.types.AspBlastDifferentialWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.AspBlastDifferentialNumericValue", "", linkParent)
					.addType("gov.va.vinci.ef.types.AspBlastDifferentialRelation", "", linkParent)
					
					.addType("gov.va.vinci.ef.types.AspCellValueRelation", "", linkParent)
					.addType("gov.va.vinci.ef.types.AspCellAssessmentRelation", "", linkParent)
					.addType("gov.va.vinci.ef.types.AspCellMeasurementWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.AspCellContextWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.AspCellConceptWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.AspCellMeasurement", "", linkParent)
					.addType("gov.va.vinci.ef.types.AspCellConcept", "", linkParent)
					.addType("gov.va.vinci.ef.types.AspCellNumericValue", "", linkParent)
					.addType("gov.va.vinci.ef.types.AspCellQValue", "", linkParent)

					.addType("gov.va.vinci.ef.types.AspBlastCytometry", "", linkParent)
					.addType("gov.va.vinci.ef.types.AspBlastCytometryWindow", "", "gov.va.vinci.leo.window.types.Window")
					.addType("gov.va.vinci.ef.types.AspBlastCytometryNumericValue", "", linkParent)
					.addType("gov.va.vinci.ef.types.AspBlastCytometryRelation", "", linkParent)

					.addType("gov.va.vinci.ef.types.Term", "", regexParent)
					.addType("gov.va.vinci.ef.types.TermPattern", "", patternParent)
					.addType("gov.va.vinci.ef.types.AnchoredSentence", "", "uima.tcas.Annotation")
					.addType("gov.va.vinci.ef.types.TermContext", "", "uima.tcas.Annotation")
					

					.addTypeSystemDescription(new FlattenRelationAE().getLeoTypeSystemDescription());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return description;
	}
}
