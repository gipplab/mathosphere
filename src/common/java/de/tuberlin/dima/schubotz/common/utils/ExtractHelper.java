package de.tuberlin.dima.schubotz.common.utils;

import cz.muni.fi.mir.mathmlcanonicalization.ConfigException;
import cz.muni.fi.mir.mathmlcanonicalization.MathMLCanonicalizer;
import org.apache.commons.lang.StringEscapeUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;


public class ExtractHelper {
	//XML configuration file for canonicalizer
    private static InputStream configInputStream;
	static final MathMLCanonicalizer canonicalizer;
    private static final SafeLogWrapper LOG;

	static {
        LOG = new SafeLogWrapper(ExtractHelper.class);
        try (InputStream configInputStream = ExtractHelper.class.getClassLoader()
                            .getResourceAsStream("de/tuberlin/dima/schubotz/common/utils/canonicalizer-config.xml")) {
            try {
                canonicalizer = new MathMLCanonicalizer(configInputStream);
                canonicalizer.setEnforcingXHTMLPlusMathMLDTD(true); //DTD will resolve all HTML entities
            } catch (ConfigException e) {
                throw new RuntimeException("Unable to configure canonicalizer, exiting", e);
            }
        } catch(final IOException e) {
            throw new RuntimeException("Could not find config for canonicalizer, exiting", e);

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
     * This method returns strings with escaped HTML entities.
	 * @param elements Elements to canonicalize.
	 * @return
	 */
	public static String extractCanonicalizedDoc(Elements elements) throws Exception {
        String doc = elements.toString(); //toString escapes HTML entities
        int length = doc.length();
		InputStream input = new BufferedInputStream(
                new ByteArrayInputStream(doc.getBytes(StandardCharsets.UTF_8)),doc.length());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		canonicalizer.canonicalize(input,output);
        return output.toString(StandardCharsets.UTF_8.toString());
	}
}
