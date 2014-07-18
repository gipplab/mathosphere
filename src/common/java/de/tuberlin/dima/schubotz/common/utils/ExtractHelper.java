package de.tuberlin.dima.schubotz.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.muni.fi.mir.mathmlcanonicalization.MathMLCanonicalizer;


public class ExtractHelper {
	public static StringTokenizer tokenize (String latex) {
		//tokenize latex
		//from https://github.com/TU-Berlin/mathosphere/blob/TFIDF/math-tests/src/main/java/de/tuberlin/dima/schubotz/fse/MathFormula.java.normalizeTex
		latex = StringEscapeUtils.unescapeHtml(latex);
		latex = latex.replaceAll("\\\\qvar\\{(.*?)\\}", ""); //TODO these are not working correctly
		latex = latex.replaceAll("\\\\displaystyle", "");  
		latex = latex.replace("{", " ");
		latex = latex.replace("}", " ");
		latex = latex.replace("\n"," "); 
		latex = latex.replace("\r"," ");
		latex = latex.trim();
		StringTokenizer tok = new StringTokenizer(latex,"\\()[]+-*:1234567890,; |\t=_^*/.~!<>&\"", true);
		return tok;
	}
	
	public static String constructOutput (String in, String TEX_SPLIT) {
		Log LOG = LogFactory.getLog(ExtractHelper.class);
		StringTokenizer tok;
		String nextTok;
		StringBuilder out = new StringBuilder();
		tok = ExtractHelper.tokenize(in);
		while (tok.hasMoreTokens()) {
			nextTok = tok.nextToken();
			if (!(nextTok.equals(" ")) && !(nextTok.equals(""))) {
				if (!out.toString().equals("")) {
					out.append(TEX_SPLIT + nextTok.trim());//TODO ArrayLists non serializable so make do with this...
				} else {
					out.append(nextTok);
				}
			}
		}
		return out.toString();
	}
	
	/**
	 * TODO kept for compatibilty with main (switch main over to Jsoup)
	 * @param LatexElements (XMLHelper Nodelist)
	 * @return out String of latex tokens
	 */
	public static String extractLatexXMLHelper(NodeList LatexElements, String TEX_SPLIT) {
		Log LOG = LogFactory.getLog(ExtractHelper.class);
		String curLatex;
		Node node;
		StringBuilder out = new StringBuilder();
		if (LatexElements == null) {
			return "";
		}
		for (int i = 0; i < LatexElements.getLength(); i++ ) {
			node = LatexElements.item(i); 
			if (node.getAttributes().getNamedItem("encoding").getNodeValue().equals(new String("application/x-tex"))){ //check if latex
				try {
					curLatex = node.getFirstChild().getNodeValue();
				} catch (NullPointerException e) {
					continue;
				}
				out.append(constructOutput(curLatex, TEX_SPLIT));
			}
		}
		return out.toString();
	}
	/**
	 * TODO move math extraction to extracthelper?
	 * @param MathElements annotation tags with latex in them
	 * @param STR_SPLIT delimiter
	 * @return out String[] of {latex,content mathml,presentation mathml}
	 */
	public static String[] extractWikiMath(Elements MathElements, String STR_SPLIT) {
		Log LOG = LogFactory.getLog(ExtractHelper.class);
		String curLatex;
		StringBuilder latex = new StringBuilder();
		if (MathElements == null) {
			return null;
		}
		for (Element curElement : MathElements) {
			Elements LatexElements = curElement.select("[encoding=application/x-tex]");
			if (LatexElements != null) {
				for (Element node : LatexElements) {
					try {
						curLatex = node.text();
					} catch (NullPointerException e) {
						continue;
					}
					latex.append(constructOutput(curLatex, STR_SPLIT));
				}
			}
			
			//Either a) first-child is CMML, or b) first-child is PMML
			//Find out by checking xref of semantics tag
			Elements semantics = curElement.select("");
		}
		
		return new String[] {"","",latex.toString()};
	}
	/**
	 * @param LatexElements (Jsoup Elements)
	 * @param TEX_SPLIT
	 * @return
	 */
	public static String extractLatex(Elements LatexElements, String TEX_SPLIT) {
		String curLatex = "";
		StringBuilder out = new StringBuilder();
		if (LatexElements == null) {
			return "";
		}
		for (Element element : LatexElements) {
			try {
				curLatex = element.text();
			} catch (NullPointerException e) {
				continue;
			}
			out.append(constructOutput(curLatex, TEX_SPLIT));
		}
		return out.toString();
		
	}
	
	/**
	 * @param elements Elements to canonicalize. Temporarily redirects System.out.
	 * @return
	 */
	public static Document extractCanonicalizedDoc(Elements elements) {
		InputStream input = new ByteArrayInputStream(elements.toString().getBytes(StandardCharsets.UTF_8));
		OutputStream output = new ByteArrayOutputStream();
		MathMLCanonicalizer engine = MathMLCanonicalizer.getDefaultCanonicalizer(); //init with default settings
		try {
			engine.canonicalize(input,output);
		} catch (Exception e) {
			Log LOG = LogFactory.getLog(ExtractHelper.class);
			if (LOG.isWarnEnabled()) {
				LOG.warn("Canonicalizer failed: " + elements.toString());
			}
		}
		try {
			Document out = Jsoup.parse(output.toString());
			out.child(0).remove(); //child 0 is <?xml..>
			return out; 
		} catch (Exception e) {
			Log LOG = LogFactory.getLog(ExtractHelper.class);
			if (LOG.isWarnEnabled()) {
				LOG.warn("Jsoup unable to parse or Canonicalizer failed: " + output.toString());
			}
			return null;
		}
	}
}
