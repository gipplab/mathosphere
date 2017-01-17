package com.formulasearchengine.mathosphere.mathpd.contracts;

import com.formulasearchengine.mathosphere.mathpd.Distances;
import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
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

public class TextExtractorMapper implements FlatMapFunction<String, Tuple2<String, ExtractedMathPDDocument>> {


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

        // tailored to the input format, you might need to change this if you have another format
        switch (titleComponents.length) {
            case 5:
                page = titleComponents[4];
            case 4:
                name = titleComponents[3];
        }

        return new Tuple4<>(title, name, page, xhtml);
    }

    public static ExtractedMathPDDocument convertArxivToExtractedMathPDDocument(ArxivDocument document) throws ParserConfigurationException, IOException, XPathExpressionException, TransformerException {
        if (document == null)
            return null;

        try {
            final ExtractedMathPDDocument extractedMathPDDocument = new ExtractedMathPDDocument(document.title, document.text);
            extractedMathPDDocument.setName(document.getName());
            extractedMathPDDocument.setPage(document.getPage());

            // discard this document if no math tag is contained
            if (document.getMathTags().getLength() == 0) {
                return null;
            }

            // extract all features we are or might be interested in later
            extractedMathPDDocument.setHistogramCn(Distances.getDocumentHistogram(document, "cn"));
            extractedMathPDDocument.setHistogramCsymbol(Distances.getDocumentHistogram(document, "csymbol"));
            extractedMathPDDocument.setHistogramCi(Distances.getDocumentHistogram(document, "ci"));
            extractedMathPDDocument.setHistogramBvar(Distances.getDocumentHistogram(document, "bvar"));

            return extractedMathPDDocument;
        } catch (Exception e) {
            LOGGER.error(e.toString());
            return null;
        }
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
    public void flatMap(String content, Collector<Tuple2<String, ExtractedMathPDDocument>> out) throws Exception {
        final ArxivDocument document = arxivTextToDocument(content);
        if (document == null) {
            LOGGER.warn("could not convert raw string to ArxivDocuemt: {}", content.substring(0, 100));
            return;
        }

        LOGGER.info("processing document '{}'...", document.title);
        final ExtractedMathPDDocument extractedMathPDDocument = convertArxivToExtractedMathPDDocument(document);
        if (extractedMathPDDocument == null) {
            LOGGER.warn("could not convert ArxivDocument to ExtractedMathPDDocument: {}", document.title);
            return;
        }

        // store the doc in the collector
        LOGGER.info("finished processing document '{}'...", document.title);
        out.collect(new Tuple2<>(extractedMathPDDocument.getName(), extractedMathPDDocument));
    }
}
