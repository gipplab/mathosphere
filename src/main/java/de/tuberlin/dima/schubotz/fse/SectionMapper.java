package de.tuberlin.dima.schubotz.fse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.collect.HashMultiset;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

public class SectionMapper extends FlatMapFunction<String, SectionTuple> {
	final static String FILENAME_INDICATOR = "Filename";
	final static Pattern filnamePattern = Pattern
	         .compile( "<ARXIVFILESPLIT\\\\n" + FILENAME_INDICATOR + "=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">" );
	private static final Log LOG = LogFactory.getLog(SectionMapper.class);
	Pattern WORD_SPLIT;
	String STR_SPLIT;
	
	HashMultiset<String> keywords;

	public SectionMapper (Pattern WORD_SPLIT, String STR_SPLIT, HashMultiset<String> keywords) {
		this.WORD_SPLIT = WORD_SPLIT;		
		this.STR_SPLIT = STR_SPLIT;
		this.keywords = keywords;
	}


	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param value Document input
	 * @return SectionTuple
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap (String value, Collector<SectionTuple> out) throws Exception {
		//Split into lines 0: ARXIVFILENAME, 1: HTML
		String[] lines = value.trim().split( "\\n", 2 );
		if ( lines.length < 2 ) { 
			LOG.warn("Null document: " + value); 
			return;
		}
		Matcher matcher = filnamePattern.matcher( lines[0] );
		String docID = null;
		if ( matcher.find() ) {
			docID = matcher.group(1) + "_" + matcher.group(2) + "_" + matcher.group(3) + ".xhtml";
		} else {
			LOG.warn("null docID! (possible non ARXIV document input)");
			return; 
		}
		
		//Parse string as XML
		Document doc = XMLHelper.String2Doc(lines[1], false); 
		NodeList LatexElements = XMLHelper.getElementsB(doc, "//annotation"); //get all annotation tags
		
		//Extract latex
		String latex = LatexHelper.extract(LatexElements);
		
		//Extract plaintext from article
		String plainText;
		try {
			plainText = Jsoup.parse(value).text();
		} catch (Exception e) {
			LOG.warn("Jsoup could not parse the document", e);
			return;
		}
		String[] tokens = WORD_SPLIT.split(plainText.toLowerCase()); 
		SectionTuple tup = new SectionTuple(docID,latex,"",STR_SPLIT);
		for (String token : tokens) {
			if (keywords.contains(token)) {
				if (!token.equals(""))
					tup.addPlaintext(token);
			}
		}
		if (!tup.getKeywords().equals("") || !tup.getLatex().equals("")) {
			out.collect(tup);
		}
		
	}

}
