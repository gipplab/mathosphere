package de.tuberlin.dima.schubotz.fse.utils;

import cz.muni.fi.mir.mathmlcanonicalization.ConfigException;
import cz.muni.fi.mir.mathmlcanonicalization.MathMLCanonicalizer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


public class ExtractHelper {
	//XML configuration file for canonicalizer
	private static final MathMLCanonicalizer canonicalizer;
    //For XML math processing
    private static final String NAMESPACE = "http://www.w3.org/1998/Math/MathML";
    private static final String NAMESPACE_NAME = "xmlns:m";
    private static final SafeLogWrapper LOG;

	static {
        LOG = new SafeLogWrapper(ExtractHelper.class);
        try (InputStream configInputStream = ExtractHelper.class.getClassLoader()
                            .getResourceAsStream("de/tuberlin/dima/schubotz/utils/canonicalizer-config.xml")) {
            canonicalizer = new MathMLCanonicalizer(configInputStream);
            canonicalizer.setEnforcingXHTMLPlusMathMLDTD(true); //DTD will resolve all HTML entities
        } catch(final IOException e) {
            throw new RuntimeException("Could not find config for canonicalizer, exiting", e);
        } catch (final ConfigException e) {
            throw new RuntimeException("Unable to configure canonicalizer, exiting", e);
        }
	}

    private static final Pattern LATEX_CLEANER = Pattern.compile("\\\\qvar\\{(.*?)\\}|\\\\displaystyle");

    private ExtractHelper() {
    }

    private static StringTokenizer tokenize(String latex) {
        String result = latex;
		//tokenize latex
		//from https://github.com/TU-Berlin/mathosphere/blob/TFIDF/math-tests/src/main/java/de/tuberlin/dima/schubotz/fse/MathFormula.java.normalizeTex
		result = StringEscapeUtils.unescapeXml( result );
		result = LATEX_CLEANER.matcher(result).replaceAll("");
		result = result.replace("{", " ");
		result = result.replace("}", " ");
		result = result.replace("\n"," ");
		result = result.replace("\r"," ");
		result = result.trim();
        return new StringTokenizer(result,"\\()[]+-*:1234567890,; |\t=_^*/.~!<>&\"", true);
	}
	
	private static void constructOutput(StringBuilder out, String in, String TEX_SPLIT) {
        final StringTokenizer tok = tokenize(in);
        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            //TODO fix tokenizer so this check isn't necessary
            nextTok = nextTok.trim();
            appendSeparator(out, nextTok, TEX_SPLIT);
		}
	}

    /**
	 * @param LatexElements (Jsoup Elements)
	 * @param TEX_SPLIT
	 * @return
	 */
	private static String extractLatex(Elements LatexElements, String TEX_SPLIT) {
        final StringBuilder out = new StringBuilder();
		if (LatexElements == null) {
			return "";
		}
        String curLatex = "";
        for (final Element element : LatexElements) {
			try {
				curLatex = element.text();
                constructOutput(out, curLatex, TEX_SPLIT);
			} catch (final NullPointerException e) {
                LOG.debug("Element does not have text: ", curLatex, " in ", LatexElements);
			}
		}
		return out.toString();
	}

	/**
     * This method returns strings with escaped HTML entities.
	 * @param elements Elements to canonicalize.
	 * @return
	 */
	private static String extractCanonicalizedDoc(Elements elements) throws Exception {
        final String doc = elements.toString(); //toString escapes HTML entities
		final InputStream input = new BufferedInputStream(
                new ByteArrayInputStream(doc.getBytes(StandardCharsets.UTF_8)),doc.length());
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		canonicalizer.canonicalize(input,output);
        return output.toString(StandardCharsets.UTF_8.toString());
	}

    /**
     * Processes <math> elements and attaches
     * info to string builders given in parameter
     * @param MathElement
     * @param docID
     * @param outputLatex
     * @param cmml
     * @param pmml
     */
    public static void processMathElement(Element MathElement, String docID,
                                    StringBuilder outputLatex,
                                    StringBuilder cmml,
                                    StringBuilder pmml,
                                    String STR_SPLIT) {
        if (MathElement.children().size() > 1) {
            LOG.warn("Multiple elements under math tag, assuming first: ", docID, ": ", MathElement);
        }

        //Assume only one root element and that it is <semantic>
        final Element SemanticElement;
        try {
            SemanticElement = MathElement.child(0);
            if (!"semantics".equals(SemanticElement.tagName()) && !"m:semantics".equals(SemanticElement.tagName())) {
                LOG.warn("Non semantics tag: ", docID, ": ", SemanticElement);
                return;
            }
        } catch (final RuntimeException e) {
            LOG.warn("Unable to find semantics elements: ", docID, ": ", MathElement, e);
            return;
        }
        final Elements MMLElements = SemanticElement.children();
        final Elements PmmlElements = new Elements();
        final Elements CmmlElements = new Elements();
        final Elements LatexElements = new Elements();

        parseElements(MMLElements, PmmlElements, CmmlElements, LatexElements);

        // Canonicalize and stringify everything. At this stage it is ok
        // to have empty elements sent to ExtractHelper - methods
        // immediately return empty strings.
        final String curLatex, curCmml, curPmml;
        try {
            curLatex = extractLatex(LatexElements, STR_SPLIT);
            curCmml = extractCanonicalizedDoc(CmmlElements);
            curPmml = extractCanonicalizedDoc(PmmlElements);
        } catch (final Exception e) {
            LOG.warn("Extraction/canonicalization failed on math element. Moving on: ",
                    docID, ": ", MathElement.text(), e);
            return;
        }

        if (curLatex == null || curCmml == null || curPmml == null) {
            LOG.warn("Bug in canonicalization or element has no math. Moving on: ", docID, ": ", MathElement);
        } else {
            appendSeparator(outputLatex, curLatex, STR_SPLIT);
            appendSeparator(cmml, curCmml, STR_SPLIT);
            appendSeparator(pmml, curPmml, STR_SPLIT);
       }
    }

    /**
     * Appends separators plus a token to a StringBuilder correctly.
     * Handles empty tokens by discarding them.
     * @param output
     * @param tok
     * @param STR_SPLIT
     */
    public static void appendSeparator(StringBuilder output, String tok, String STR_SPLIT) {
        if (output.length() == 0) {
            if (!tok.isEmpty()) {
                output.append(tok);
            }
        } else {
            if (!tok.isEmpty()) {
                output.append(STR_SPLIT);
                output.append(tok);
            }
        }
    }

    private static void parseElements(Elements mmlElements, Elements pmmlElements, Elements cmmlElements, Elements latexElements) {
        //Two methods of writing tags - with namespace or without
        final Elements annotationXMLElements = mmlElements.select("annotation-xml, m|annotation-xml");
        Element annotationXML = null;
        if (annotationXMLElements.isEmpty()) {
            LOG.warn("Unable to find annotation tags in element: ", mmlElements);
        } else if (annotationXMLElements.size() > 1) {
            LOG.warn("Multiple annotation tags in element, assuming first: ", mmlElements);
            annotationXML = annotationXMLElements.first();
        } else {
            annotationXML = annotationXMLElements.first();
        }

        //Any element not under annotation tag is opposite of what's under annotation-xml
        //Always assuming one root element per MathMLType
        if(annotationXML != null) {
            final String encoding = annotationXML.attr("encoding");
            //Add namespace information so canonicalizer can parse it
            annotationXML.child(0).attr(NAMESPACE_NAME, NAMESPACE); //ignore root annotation tag
            switch (encoding) {
                case "MathML-Presentation":
                    pmmlElements.add(annotationXML.child(0));
                    mmlElements.remove(annotationXML); //remove elements so that following method does not pick up on them
                    //Non annotated elements are CMML
                    extractNonAnnotatedXMLElements(mmlElements, latexElements, cmmlElements);
                    break;
                case "MathML-Content":
                    cmmlElements.add(annotationXML.child(0));
                    mmlElements.remove(annotationXML);
                    //Non annotated elements are PMML
                    extractNonAnnotatedXMLElements(mmlElements, latexElements, pmmlElements);
                    break;
                default:
                    LOG.warn("Annotation tag is malformed in element: ", mmlElements);
                    break;
            }
        }
    }

    /**
     * Extracts non annotated elements and latex elements
     * from <math>. Assumes that annotation-xml has been
     * taken out.
     * @param MMLElements mmlelements with annotation-xml removed
     * @param LatexElements latexelements to add to
     * @param NonAnnotatedElements elements to add nonannotated elements to
     */
    private static void extractNonAnnotatedXMLElements(
            Elements MMLElements, List<Element> LatexElements, List<Element> NonAnnotatedElements) {
        for (final Element curElement : MMLElements) {
            try {
                final String encoding = curElement.attr("encoding");
                if (curElement.tagName().contains("annotation")) {
                    if ("application/x-tex".equals(encoding)) { //Latex annotation tag
                        //Add namespace information so canonicalizer can parse it
                        curElement.attr(NAMESPACE_NAME, NAMESPACE);
                        LatexElements.add(curElement); //keep root annotation tag b/c will be parsed by ExtractLatex
                    } else {
                        LOG.warn("Odd annotation tag: ", curElement);
                    }
                } else { //Non annotated
                    //Add namespace information so canonicalizer can parse it
                    curElement.attr(NAMESPACE_NAME, NAMESPACE);
                    NonAnnotatedElements.add(curElement);
                }
            } catch (final RuntimeException e) {
                LOG.warn("Badly formatted math xml or bug in jsoup: ", MMLElements.text(), e);
                return;
            }
        }
    }
}


