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
import org.springframework.web.util.HtmlUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.muni.fi.mir.mathmlcanonicalization.ConfigException;
import cz.muni.fi.mir.mathmlcanonicalization.MathMLCanonicalizer;


public class ExtractHelper {
	//XML configuration "file" for canonicalizer TODO better way of producing xml file?
	//Properties can be found in each individual module's java file
	static String configString = 
			"<?xml version='1.0' encoding='UTF-8'?>" +
			"<config>" + 
				"<module name='OperatorNormalizer'/>" +
				"<module name='ElementMinimizer'/>" +  //TODO this produces "must consist of well-formed character data"
				"<module name='MfencedReplacer'/>" + 
				"<module name='MrowNormalizer'/>" + 
				"<module name='FunctionNormalizer'/>" + 
				"<module name='ScriptNormalizer'/>" +  //TODO this produces invalid msub and ConcurrentModificationException
			"</config>";
	static InputStream configInputStream = new ByteArrayInputStream(configString.getBytes());
	static MathMLCanonicalizer canonicalizer;

	static {
		try {
			canonicalizer = new MathMLCanonicalizer(configInputStream);
		} catch (ConfigException e) {
			throw new RuntimeException("Unable to configure canonicalizer, exiting", e);
		}
	}
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
	public static Document extractCanonicalizedDoc(Elements elements) throws Exception {
		String doc = HtmlUtils.htmlUnescape(elements.toString()); //for some reason toString escapes characters
		InputStream input = new ByteArrayInputStream(doc.getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		canonicalizer.canonicalize(input,output);
		Document out = Jsoup.parse(output.toString(StandardCharsets.UTF_8.displayName()));
		return out;
	}
}
