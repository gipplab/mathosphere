package de.tuberlin.dima.schubotz.fse.mappers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.collect.HashMultiset;

import de.tuberlin.dima.schubotz.fse.types.SectionTuple;
import de.tuberlin.dima.schubotz.utils.LatexHelper;
import de.tuberlin.dima.schubotz.utils.XMLHelper;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

@SuppressWarnings("serial")
public class SectionMapper extends FlatMapFunction<String, SectionTuple> {
	/**
	 * Pattern for extracting the filename from the ARXIV tag.
	 */
	final static Pattern filnamePattern = Pattern
	         .compile( "<ARXIVFILESPLIT\\\\nFilename=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">" );
	private static final Log LOG = LogFactory.getLog(SectionMapper.class);
	/**
	 * {@link de.tuberlin.dima.schubotz.fse.MainProgram#WORD_SPLIT}
	 */
	Pattern WORD_SPLIT;
	/**
	 * {@link de.tuberlin.dima.schubotz.fse.MainProgram#STR_SPLIT}
	 */
	String STR_SPLIT;
	
	HashMultiset<String> keywords;

	/**
	 * @param WORD_SPLIT {@link de.tuberlin.dima.schubotz.fse.MainProgram#WORD_SPLIT} sent as parameter to ensure serializability. 
	 * @param STR_SPLIT {@link de.tuberlin.dima.schubotz.fse.MainProgram#STR_SPLIT} sent as parameter to ensure serializability.
	 * @param keywords set of keywords in queries. used to determine whether to include a keyword or not.
	 */
	@SuppressWarnings("hiding")
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
		String[] lines = value.trim().split("\\n", 2);
		if ( lines.length < 2) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Null document: " + value);
			}
			return;
		}
		Matcher matcher = filnamePattern.matcher(lines[0]);
		String docID = null;
		if ( matcher.find() ) {
			docID = matcher.group(1) + "_" + matcher.group(2) + "_" + matcher.group(3) + ".xhtml";
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn("null docID! (possible non ARXIV document input)");
			}
			return; 
		}
		
		//Parse string as XML
		Document doc = XMLHelper.String2Doc(lines[1], false); 
		NodeList LatexElements = XMLHelper.getElementsB(doc, "//annotation"); //get all annotation tags
		
		//Extract latex
		String latex = LatexHelper.extract(LatexElements, STR_SPLIT);
		
		//Extract plaintext from article
		String plainText;
		try {
			plainText = Jsoup.parse(value).text();
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Jsoup could not parse the document: ", e);
			}
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
