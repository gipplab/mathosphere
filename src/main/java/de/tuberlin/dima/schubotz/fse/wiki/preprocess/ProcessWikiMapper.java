package de.tuberlin.dima.schubotz.fse.wiki.preprocess;

import de.tuberlin.dima.schubotz.fse.common.utils.ExtractHelper;
import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 * Map de.tuberlin.dima.schubotz.fse.wiki text to {@link de.tuberlin.dima.schubotz.fse.types.DataTuple}
 */
@SuppressWarnings("serial")
public class ProcessWikiMapper extends FlatMapFunction<String, DataTuple> {
    /**
     * See {@link de.tuberlin.dima.schubotz.wiki.WikiProgram#STR_SPLIT}
     */
    private final String STR_SPLIT;
    private final String namespace = "http://www.w3.org/1998/Math/MathML";
    private final String namespaceTag = "xmlns:m";
    private final Log LOG = LogFactory.getLog(ProcessWikiMapper.class);

    /**
     * @param {@link de.tuberlin.dima.schubotz.de.tuberlin.dima.schubotz.fse.wiki.WikiProgram#STR_SPLIT} passed in to ensure serializability
     */
    @SuppressWarnings("hiding")
    public ProcessWikiMapper(String STR_SPLIT) {
        this.STR_SPLIT = STR_SPLIT;
    }

    /**
     * Takes in de.tuberlin.dima.schubotz.fse.wiki string, parses wikiID and latex
     */
    @Override
    public void flatMap (String in, Collector<DataTuple> out) {
        final Document doc;

        try {
            doc = Jsoup.parse(in, "", Parser.xmlParser()); //using jsoup b/c de.tuberlin.dima.schubotz.fse.wiki html is invalid, also handles entities
        } catch (final RuntimeException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Unable to parse XML in de.tuberlin.dima.schubotz.fse.wiki: " + in);
            }
            e.printStackTrace();
            return;
        }
        String docID;
        if (doc.select("title").first() == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not find title tag, assigning this_was_null: " + in);
            }
            docID = "this_was_null";
        } else {
            docID = doc.select("title").first().text();
            if (docID == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("docID was null, assigning this_was_null: " + in);
                }
                docID = "this_was_null";
            }
        }

        final Elements MathElements = doc.select("math");
        if (MathElements.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Unable to find math tags: " + in);
            }
            return;
        }

        final StringBuilder outputLatex = new StringBuilder();
        final StringBuilder cmml = new StringBuilder();
        final StringBuilder pmml = new StringBuilder();

        for (final Element MathElement : MathElements) {
            processMathElement(MathElement, docID, outputLatex, cmml, pmml);
        }

        final String outputLatexStr = outputLatex.toString();
        final String cmmlStr = cmml.toString();
        final String pmmlStr = pmml.toString();
        if (outputLatexStr.isEmpty() && cmmlStr.isEmpty() && pmmlStr.isEmpty()) {
            if (LOG.isInfoEnabled()) {
                LOG.info(docID + " has no math.");
            }
        } else {
            out.collect(new DataTuple(docID, outputLatex.toString(), cmml.toString(), pmml.toString()));
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(docID + " complete!");
        }
    }


    /**
     * Processes <math> elements per de.tuberlin.dima.schubotz.fse.wiki and attaches
     * info to string builders given in parameter
     * @param MathElement
     * @param docID
     * @param outputLatex
     * @param cmml
     * @param pmml
     */
    private void processMathElement(Element MathElement, String docID,
                                    StringBuilder outputLatex,
                                    StringBuilder cmml,
                                    StringBuilder pmml) {
        //Assume only one root element and that it is <semantic>
        Element SemanticElement;
        try {
            SemanticElement = MathElement.child(0);
        } catch (final RuntimeException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Unable to find semantics elements: " + docID + ": " + MathElement.text());
            }
            e.printStackTrace();
            return;
        }
        if (MathElement.children().size() > 1) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Multiple elements under math: " + docID + ": " + MathElement.text());
            }
        }
        final Elements MMLElements = SemanticElement.children();

        final Elements PmmlElements = new Elements(); //how are we handling multiple math tags per de.tuberlin.dima.schubotz.fse.wiki?
        final Elements CmmlElements = new Elements();
        final Elements LatexElements = new Elements();
        //All data is well formed: 1) Presentation MML 2) annotation-CMML 3) annotation-TEX
        //Any element not under annotation tag is Presentation MML
        for (final Element curElement : MMLElements) {
            try {
                if ("annotation-xml".equals(curElement.tagName())) {
                    //MathML-Content
                    //Again, always assuming one root element per MathML type
                    //E.g. there will never be two <mrow> elements under <annotation-xml>
                    //Add namespace information so canonicalizer can parse it
                    curElement.child(0).attr(namespaceTag, namespace);
                    CmmlElements.add(curElement.child(0));
                } else if ("annotation".equals(curElement.tagName())) {
                    //Latex
                    curElement.attr(namespaceTag, namespace);
                    LatexElements.add(curElement); //keep annotation tags b/c parsed by ExtractLatex
                } else {
                    //PMML (not wrapped in annotation)
                    curElement.attr(namespaceTag, namespace);
                    PmmlElements.add(curElement);
                }
            } catch (final RuntimeException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Badly formatted math xml: " + docID + ": " + MathElement.text());
                }
                e.printStackTrace();
                return;
            }
        }
        final String curLatex,curCmml,curPmml;
        try {
            curLatex = ExtractHelper.extractLatex(LatexElements, STR_SPLIT);
            curCmml = ExtractHelper.extractCanonicalizedDoc(CmmlElements);
            curPmml = ExtractHelper.extractCanonicalizedDoc(PmmlElements);
        } catch (final Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Extraction/canonicalization failed on math element. Moving on: " + docID + ": " + MathElement.text());
            }
            e.printStackTrace();
            return;
        }

        if (curLatex == null || curCmml == null || curPmml == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Bug in canonicalization or element has no math. Moving on: " + docID + ": " + MathElement.text());
            }
        } else{
            outputLatex.append(curLatex);
            cmml.append(curCmml);
            pmml.append(curPmml);
        }
    }
}
