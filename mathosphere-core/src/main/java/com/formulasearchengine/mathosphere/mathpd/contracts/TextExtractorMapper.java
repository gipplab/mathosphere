package com.formulasearchengine.mathosphere.mathpd.contracts;

import com.formulasearchengine.mathosphere.mathpd.Distances;
import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExtractorMapper implements FlatMapFunction<String, ExtractedMathPDDocument> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextExtractorMapper.class);

    private static final Pattern FILENAME_PATTERN = Pattern.compile("<ARXIVFILESPLIT(?:\\\\n?|[\\s\\r\\n]+)" +
            "Filename=\"(.*?).xhtml\">(?:\\s*)(.*)", Pattern.DOTALL);
    private static final Pattern TITLE_COMPONENTS_PATTERN = Pattern.compile("", Pattern.DOTALL);

    //private static final String FILENAME_INDICATOR = "Filename";
    //private static final Pattern FILENAME_PATTERN = Pattern
    //        .compile( "<ARXIVFILESPLIT\\\\n" + FILENAME_INDICATOR + "=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">" );

    public static Tuple4<String, String, String, String> getTitleAndTextualContent(String content) {
        Matcher titleMatcher = FILENAME_PATTERN.matcher(content);
        if (!titleMatcher.find()) {
            return null;
        }
        final String title = titleMatcher.group(1);
        final String xhtml = titleMatcher.group(2);
        final String[] titleComponents = title.split("/");

        String name = "no-name";
        String page = "-1";

        switch (titleComponents.length) {
            case 4:
                page = titleComponents[3];
            case 3:
                name = titleComponents[2];
        }

        return new Tuple4<>(title, name, page, xhtml);
    }

    public static ExtractedMathPDDocument convertArxivToExtractedMathPDDocument(ArxivDocument document) throws ParserConfigurationException, IOException, XPathExpressionException, TransformerException {
        if (document == null)
            return null;

        final ExtractedMathPDDocument extractedMathPDDocument = new ExtractedMathPDDocument(document.title, document.text);

        // extract all features we are or might be interested in later
        extractedMathPDDocument.setHistogramCn(Distances.getDocumentHistogram(document, "cn"));
        extractedMathPDDocument.setHistogramCsymbol(Distances.getDocumentHistogram(document, "csymbol"));
        extractedMathPDDocument.setHistogramCi(Distances.getDocumentHistogram(document, "ci"));
        extractedMathPDDocument.setHistogramBvar(Distances.getDocumentHistogram(document, "bvar"));

        return extractedMathPDDocument;
    }

    public static ArxivDocument arxivTextToDocument(String content) {
        final Tuple4<String, String, String, String> titleAndContent = getTitleAndTextualContent(content);
        if (titleAndContent == null) {
            return null;
        }

        final ArxivDocument arxivDocument = new ArxivDocument(titleAndContent.f0, titleAndContent.f3);
        arxivDocument.setName(titleAndContent.f1);
        arxivDocument.setPage(titleAndContent.f2);

        return arxivDocument;
    }

    @Override
    public void flatMap(String content, Collector<ExtractedMathPDDocument> out) throws Exception {
        final ArxivDocument document = arxivTextToDocument(content);
        if (document == null) {
            return;
        }

        LOGGER.info("processing document '{}'...", document.title);
        final ExtractedMathPDDocument extractedMathPDDocument = convertArxivToExtractedMathPDDocument(document);


        // store the doc in the collector
        LOGGER.info("finished processing document '{}'...", document.title);
        out.collect(extractedMathPDDocument);
    }
}
