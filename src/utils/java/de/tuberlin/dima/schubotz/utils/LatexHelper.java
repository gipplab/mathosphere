package de.tuberlin.dima.schubotz.utils;

import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tuberlin.dima.schubotz.fse.MainProgram;

public class LatexHelper {
	
	private static String TEX_SPLIT; 
	
	public static StringTokenizer tokenize (String latex) {
		latex = StringEscapeUtils.unescapeHtml(latex);
		latex = latex.replaceAll("\\\\qvar\\{(.*?)\\}", ""); //TODO check if this is what we want to do
		latex = latex.replaceAll("\\\\displaystyle", ""); 
		latex = latex.replace("{", " ");
		latex = latex.replace("}", " ");
		latex = latex.replace("\n"," "); 
		latex = latex.replace("\r"," ");
		latex = latex.trim();
		StringTokenizer tok = new StringTokenizer(latex,"\\()[]+-*:1234567890,; |\t=_^*/.~!<>&\"", true);
		return tok;
	}
	
	/**
	 * @param LatexElements
	 * @return out String of latex tokens
	 */
	public static String extract(NodeList LatexElements, String TEX_SPLIT) {
		String curLatex;
		Node node;
		StringTokenizer tok;
		String nextTok;
		String out = "";
		if (LatexElements == null) {
			return out;
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
				tok = LatexHelper.tokenize(curLatex);
				while (tok.hasMoreTokens()) {
					nextTok = tok.nextToken();
					if (!(nextTok.equals(" ")) && !(nextTok.equals(""))) {
						if (!out.equals("")) {
							out=out.concat(TEX_SPLIT + nextTok.trim());//TODO ArrayLists non serializable so make do with this...
						} else {
							out = nextTok;
						}
					}
				}
			}
		}
		return out;
	}
}