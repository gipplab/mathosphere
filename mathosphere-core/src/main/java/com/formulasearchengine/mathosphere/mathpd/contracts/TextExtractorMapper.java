package com.formulasearchengine.mathosphere.mathpd.contracts;

import com.formulasearchengine.mathmltools.xmlhelper.NonWhitespaceNodeList;
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

    private static final Pattern FILENAME_PATTERN_20PD = Pattern.compile("<ARXIVFILESPLIT(?:\\\\n?|[\\s\\r\\n]+)" +
            "Filename=\"(.*?).xhtml\">(?:\\s*)(.*)", Pattern.DOTALL);
    private static final Pattern FILENAME_PATTERN_NTCIR = Pattern.compile("<ARXIVFILESPLIT(?:\\\\n?|[\\s\\r\\n]+)" +
            "Filename=\"\\./.+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">(?:\\s*)(.*)", Pattern.DOTALL);
    // private static final Pattern FILENAME_PATTERN_NTCIR = Pattern.compile("<ARXIVFILESPLIT(?:\\\\n?|[\\s\\r\\n]+)" +
    //        "Filename=\"\\./\\d+/(.*?)/\\1_(\\d+)_(\\d+)\\.xhtml\">(?:\\s*)(.*)", Pattern.DOTALL);


    private final Pattern filenamePattern;
    private final boolean isNtcir;

    public TextExtractorMapper(boolean isNtcir) {
        if (isNtcir) {
            filenamePattern = FILENAME_PATTERN_NTCIR;
        } else {
            filenamePattern = FILENAME_PATTERN_20PD;
        }
        this.isNtcir = isNtcir;
    }

    public static ExtractedMathPDDocument convertArxivToExtractedMathPDDocument(ArxivDocument document) throws ParserConfigurationException, IOException, XPathExpressionException, TransformerException {
        if (document == null) {
            LOGGER.warn("ArxivDocument = null");
            return null;
        }

        // try {
        final ExtractedMathPDDocument extractedMathPDDocument = new ExtractedMathPDDocument(document.title, document.text);
        extractedMathPDDocument.setName(document.getName());
        extractedMathPDDocument.setPage(document.getPage());

        // discard this document if no math tag is contained
        NonWhitespaceNodeList mathTags = null;
        try {
            mathTags = document.getMathTags();
            if (mathTags.getLength() == 0) {
                LOGGER.info("{} contains no math tags", document.getName());
                return null;
            }
        } catch (XPathExpressionException xPathExpressionException) {
            LOGGER.error("following string could not be converted to xpath: {}", document.text);
            return null;
        }

        // extract all features we are or might be interested in later

        extractedMathPDDocument.setHistogramCn(Distances.getDocumentHistogram(document, "cn", mathTags));
        extractedMathPDDocument.setHistogramCsymbol(Distances.getDocumentHistogram(document, "csymbol", mathTags));
        extractedMathPDDocument.setHistogramCi(Distances.getDocumentHistogram(document, "ci", mathTags));
        extractedMathPDDocument.setHistogramBvar(Distances.getDocumentHistogram(document, "bvar", mathTags));

        return extractedMathPDDocument;
        // } catch (Exception e) {
        //    LOGGER.error(e.getClass().toString());
        //    LOGGER.error(e.toString());
        //    return null;
        //}
    }

    public static void main(String[] args) {
        TextExtractorMapper t = new TextExtractorMapper(true);
        t.getTitleAndTextualContent("<ARXIVFILESPLIT Filename=\"./xhtml5/1/0704.0097/0704.0097_1_11.xhtml\">\n" +
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>");
    }

    public Tuple4<String, String, String, String> getTitleAndTextualContent(String content) {
        if (content.startsWith("\n"))
            content = content.substring(1);

        Matcher titleMatcher = filenamePattern.matcher(content);
        if (!titleMatcher.find()) {
            LOGGER.error("found no title");
            return null;
        }
        String title;
        String xhtml;
        String name = "no-name";
        String page = "-1";

        if (isNtcir) {
            name = titleMatcher.group(1);
            page = titleMatcher.group(3);
            xhtml = titleMatcher.group(4);
        } else {
            title = titleMatcher.group(1);
            xhtml = titleMatcher.group(2);
            final String[] titleComponents = title.split("/");

            // tailored to the input format, you might need to change this if you have another format
            switch (titleComponents.length) {
                case 5:
                    page = titleComponents[4];
                case 4:
                    name = titleComponents[3];
                    break;
                default:
                    throw new RuntimeException("title does not contain all components: " + title);
            }
        }

        title = name + "/" + page;

        //LOGGER.warn(name);
        //LOGGER.warn(page);

        return new Tuple4<>(title, name, page, xhtml);
    }

    public ArxivDocument arxivTextToDocument(String content) {
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
    public void flatMap(String content, Collector<Tuple2<String, ExtractedMathPDDocument>> out) throws ParserConfigurationException, TransformerException, XPathExpressionException, IOException {
        final ArxivDocument document = arxivTextToDocument(content);
        if (document == null) {
            LOGGER.trace("could not convert raw string to ArxivDocuemt: {}", content.substring(0, content.length() > 100 ? 100 : content.length() - 1));
            return;
        }

        LOGGER.info("processing document '{}'...", document.title);
        final ExtractedMathPDDocument extractedMathPDDocument = convertArxivToExtractedMathPDDocument(document);
        if (extractedMathPDDocument == null) {
            LOGGER.info("could not convert ArxivDocument to ExtractedMathPDDocument: {}", document.title);
            return;
        }

        // store the doc in the collector
        LOGGER.info("finished processing document '{}'...", document.title);
        out.collect(new Tuple2<>(extractedMathPDDocument.getName(), extractedMathPDDocument));
    }
}
