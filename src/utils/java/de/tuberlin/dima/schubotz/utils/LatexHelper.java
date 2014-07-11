package de.tuberlin.dima.schubotz.utils;

import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tuberlin.dima.schubotz.fse.MainProgram;

public class LatexHelper {
	public static StringTokenizer tokenize (String latex) {
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
		Log LOG = LogFactory.getLog(LatexHelper.class);
		StringTokenizer tok;
		String nextTok;
		StringBuilder out = new StringBuilder();
		tok = LatexHelper.tokenize(in);
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
	 * @param LatexElements (XMLHelper Nodelist)
	 * @return out String of latex tokens
	 */
	public static String extract(NodeList LatexElements, String TEX_SPLIT) {
		Log LOG = LogFactory.getLog(LatexHelper.class);
		String curLatex;
		Node node;
		StringBuilder out = new StringBuilder();
		if (LatexElements == null) {
			return "";
		}
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
				LOG.info(curLatex);
				out.append(constructOutput(curLatex, TEX_SPLIT));
			}
		}
		return out.toString();
	}
	/**
	 * @param LatexElements (Jsoup Elements)
	 * @param TEX_SPLIT
	 * @return
	 */
	public static String extract(Elements LatexElements, String TEX_SPLIT) {
		String curLatex = "";
		StringBuilder out = new StringBuilder();
		if (LatexElements == null) {
			return "";
		}
		for (Element element : LatexElements) {
			if (element.hasAttr("encoding")) {
				if (element.attr("encoding").equals("application/x-tex")) {
					try {
						curLatex = element.text();
					} catch (NullPointerException e) {
						continue;
					}
					out.append(constructOutput(curLatex, TEX_SPLIT));
				}
			}
		}
		return out.toString();
		
	}
}
