package com.formulasearchengine.mathosphere.pomlp.util;

import com.formulasearchengine.mathmlconverters.canonicalize.MathMLCanUtil;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unbescape.html.HtmlEscape;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Static utility class for some typical functions we need
 */
public final class Utility {
    // Own logger
    private static final Logger LOG = LogManager.getLogger( Utility.class.getName() );

    // Surrounding underscore
    private static final String POM_BUG_AVOIDANCE_UNDERSCORE = "_(\\\\[^\\s\\n-+]+)";

    // Start/End with ,;. or \[ \] will be deleted
    private static final String ELEMINATE_ENDINGS = "[\\s,;.]*(\\\\])?[\\s,;.]*$";
    private static final String ELEMINATE_STARTS = "^[\\s,;.]*(\\\\\\[)?[\\s,;.]*";

    private static final String ELEMINATE_SIMPLE_ENDS = "\\\\+[\\s,.;]*$";
    private static final String ELEMINATE_SIMPLE_STARTS = "^\\\\+[\\s,.;]+";


    // Possible line endings
    public static final char CR = (char)0x0D;   // Mac (pre-OSX)
    public static final char LF = (char)0x0A;   // Unix, Mac (OSX)
    public static final String CRLF = ""+CR+LF; // Windows

    // Commented line breaks in Latex looks like: %\r
    private static final String LATEX_COMMENTED_LINEBREAK = "%\\s*("+CR+"|"+LF+"|"+CRLF+")";

    private static final String SPECIAL_UNESCPAE_OPEN = "&lt;";
    private static final String SPECIAL_UNESCAPE_CLOSE = "&gt;";
    private static final String PLACEHOLDER_OPEN = "xxxOxxx";
    private static final String PLACEHOLDER_CLOSED = "xxxCxxx";

    private static final String SINGLE_AND = "\\s+&\\s+";
    private static final String TMP_SINGLE_AND = "---AND---";

    /**
     * Pre processing mathematical latex expressions with
     * several methods.
     *
     * @param latex raw latex input
     * @return pre processed latex string
     */
    public static String latexPreProcessing(String latex ){
        LOG.debug(" Pre-Processing for:  " + latex);

        if ( latex.contains("subarray") ){
            latex = latex.replaceAll("subarray", "array");
            LOG.trace(" Eval replacement of subarray: " + latex);
        }

        latex = latex.replaceAll( POM_BUG_AVOIDANCE_UNDERSCORE, "_{$1}" );
        LOG.trace("Surround underscore:  " + latex);

        latex = latex.replaceAll( SINGLE_AND, TMP_SINGLE_AND );
        latex = HtmlEscape.unescapeHtml(latex);
        latex = latex.replaceAll( TMP_SINGLE_AND, " & " );
        LOG.trace("HTML Unescaped:       " + latex);

        latex = latex.replaceAll( LATEX_COMMENTED_LINEBREAK, "" );
        LOG.trace("Commented linebreaks: " + latex);

        latex = latex.replaceAll( ELEMINATE_ENDINGS, "");
        latex = latex.replaceAll( ELEMINATE_STARTS, "" );
        latex = latex.replaceAll( ELEMINATE_SIMPLE_STARTS, "" );
        latex = latex.replaceAll( ELEMINATE_SIMPLE_ENDS, "" );
        LOG.trace("Replace bad end/start:" + latex);
        LOG.debug("Finalize Pre-Processing for POM-Tagger: " + latex);

        return latex;
    }

    /**
     *
     * @param doc
     * @param indent
     * @return
     */
    public static String documentToString(Document doc, boolean indent ){
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transer = tf.newTransformer();
            transer.setOutputProperty(OutputKeys.METHOD, "xml");
            if ( indent ) transer.setOutputProperty(OutputKeys.INDENT, "yes");
            transer.transform( new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch ( Exception e ){
            LOG.error("Cannot convert document into string.", e);
            return "";
        }
    }

    public static Document getCanonicalizedDocument( Document doc ) {
        String strDoc = documentToString(doc, false);
        String canonical = getCanoicalizedString( strDoc );
        return MathMLDocumentReader.getDocumentFromXMLString(canonical);
    }

    public static String getCanoicalizedString( String doc ){
        try {
            return MathMLCanUtil.canonicalize( doc );
        } catch ( Exception e ){
            LOG.error("Kann halt alles kaputt gehen irgendwie... ", e);
            return null;
        }
    }

    public static String safeUnescape( String escaped ){
        escaped = escaped.replaceAll( SPECIAL_UNESCPAE_OPEN, PLACEHOLDER_OPEN );
        escaped = escaped.replaceAll( SPECIAL_UNESCAPE_CLOSE, PLACEHOLDER_CLOSED );
        escaped = escaped.replaceAll( SINGLE_AND, TMP_SINGLE_AND );
        String unescaped = HtmlEscape.unescapeHtml(escaped);
        unescaped = unescaped.replaceAll( TMP_SINGLE_AND, " & " );
        unescaped = unescaped.replaceAll( PLACEHOLDER_OPEN, SPECIAL_UNESCPAE_OPEN );
        unescaped = unescaped.replaceAll( PLACEHOLDER_CLOSED, SPECIAL_UNESCAPE_CLOSE );
        return unescaped;
    }

    // static class usage only
    private Utility(){}
}
