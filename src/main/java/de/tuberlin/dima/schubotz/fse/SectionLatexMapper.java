package de.tuberlin.dima.schubotz.fse;

import java.io.Serializable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.util.Collector;

public class SectionLatexMapper extends FlatMapFunction<String, Tuple2<String,String>> implements Serializable{
	final static String FILENAME_INDICATOR = "Filename";
	final static Pattern filnamePattern = Pattern
	         .compile( "<ARXIVFILESPLIT\\\\n" + FILENAME_INDICATOR + "=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">" );
	
	String TEX_SPLIT = MainProgram.TEX_SPLIT;
	
	//static int counter = 0; //DEBUG

	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param value The input value.
	 * @return The value produced by the map function from the input value.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap (String value, Collector<Tuple2<String,String>> out) throws Exception {
		//Given document input, return <Filename, Latex>
		//Split into lines 0: ARXIVFILENAME, 1: HTML
		String[] lines = value.trim().split( "\\n", 2 );
		if ( lines.length < 2 ) { 
			System.out.println(value);
			return;
		}
		Matcher matcher = filnamePattern.matcher( lines[0] );
		String docID = null;
		if ( matcher.find() ) {
			docID = matcher.group(1) + "_" + matcher.group(2) + "_" + matcher.group(3) + ".xhtml";
		} else {
			System.out.println("null docID!");
			return; 
		}
		//counter++;
		//System.out.println(docID + String.valueOf(counter)); //DEBUG
		String latex = "";
		String curLatex = "";
		StringTokenizer tok;
		String nextTok = "";
		Node node;
		//Parse string as XML
		Document doc = XMLHelper.String2Doc(lines[1], false); //string, not namespace aware
		NodeList LatexElements = XMLHelper.getElementsB(doc, "//annotation"); //get all annotation tags
		//Extract latex
		for (int i = 0; i < LatexElements.getLength(); i++ ) {
			node = LatexElements.item(i); 
			if (node.getAttributes().getNamedItem("encoding").getNodeValue().equals(new String("application/x-tex"))){ //check if latex
				//tokenize latex
				//from https://github.com/TU-Berlin/mathosphere/blob/TFIDF/math-tests/src/main/java/de/tuberlin/dima/schubotz/fse/MathFormula.java.normalizeTex
				try {
					curLatex = node.getFirstChild().getNodeValue();
				} catch (NullPointerException e) {
					continue;
				}
				curLatex = StringEscapeUtils.unescapeHtml(curLatex);
				curLatex = curLatex.replaceAll("\\\\qvar\\{(.*?)\\}", "");
				curLatex= curLatex.replace("{", " ");
				curLatex = curLatex.replace("}", " ");
				tok = new StringTokenizer(curLatex,"\\()[]+-*:1234567890,; |\t=_^*/.~!<>&\"", true);
				while (tok.hasMoreTokens()) {
					nextTok = tok.nextToken();
					if (!(nextTok.equals(" "))) {
						latex=latex.concat(TEX_SPLIT + nextTok);//TODO ArrayLists non serializable so make do with this... 
					}
				}
			}
		}

		out.collect(new Tuple2<String,String>(docID,latex));
		
	}

}
