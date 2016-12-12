package com.formulasearchengine.mathosphere.mathpd.contracts;

import com.formulasearchengine.mathosphere.mathpd.Distances;
import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
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

    private static final Pattern TITLE_PATTERN = Pattern.compile("<ARXIVFILESPLIT(?:\\\\n?|[\\s\\r\\n]+)" +
            "Filename=\"(.*?).xhtml\">(?:\\s*)(.*)", Pattern.DOTALL);

    public static Tuple2<String, String> getTitleAndTextualContent(String content) {
        Matcher titleMatcher = TITLE_PATTERN.matcher(content);
        if (!titleMatcher.find()) {
            return null;
        }
        final String title = titleMatcher.group(1);
        final String xhtml = titleMatcher.group(2);

        return new Tuple2<>(title, xhtml);
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
        final Tuple2<String, String> titleAndContent = getTitleAndTextualContent(content);
        if (titleAndContent == null) {
            return null;
        }

        return new ArxivDocument(titleAndContent.f0, titleAndContent.f1);
    }

    @Override
    public void flatMap(String content, Collector<ExtractedMathPDDocument> out) throws Exception {
        System.out.println(content);
        System.out.println();
        System.out.println();
        final ArxivDocument document = arxivTextToDocument(content);
        if (document == null) {
            return;
        }

        LOGGER.info("processing document '{}'...", document.title);
        final ExtractedMathPDDocument extractedMathPDDocument = convertArxivToExtractedMathPDDocument(document);


        // write to storage for test later
        //ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("test_title")));
        //oos.writeObject(extractedMathPDDocument);
        //oos.close();

        // store the doc in the collector
        LOGGER.info("finished processing document '{}'...", document.title);
        out.collect(extractedMathPDDocument);
    }
}
