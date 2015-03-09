package com.formulasearchengine.mathosphere.utils;

import cz.muni.fi.mir.mathmlcanonicalization.ConfigException;
import cz.muni.fi.mir.mathmlcanonicalization.MathMLCanonicalizer;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


public class ExtractHelper {
	//XML configuration file for canonicalizer
	private static final MathMLCanonicalizer canonicalizer;
    public static final String NAMESPACE_NAME = "xmlns:m";
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


}


