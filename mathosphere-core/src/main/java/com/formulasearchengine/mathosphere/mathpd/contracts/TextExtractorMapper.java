package com.formulasearchengine.mathosphere.mathpd.contracts;

import com.formulasearchengine.mathosphere.mathpd.Distances;
import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExtractorMapper implements FlatMapFunction<String, ExtractedMathPDDocument> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextExtractorMapper.class);

    private static final Pattern TITLE_PATTERN = Pattern.compile("<ARXIVFILESPLIT(?:\\\\n?|[\\s\\r\\n]+)" +
            "Filename=\"(.*?).xhtml\">(?:\\s*)(.*)", Pattern.DOTALL);


    @Override
    public void flatMap(String content, Collector<ExtractedMathPDDocument> out) throws Exception {
        Matcher titleMatcher = TITLE_PATTERN.matcher(content);
        if (!titleMatcher.find()) {
            return;
        }

        final String title = titleMatcher.group(1);
        final String xhtml = titleMatcher.group(2);
        final ArxivDocument document = new ArxivDocument(title, xhtml);
        final ExtractedMathPDDocument extractedMathPDDocument = new ExtractedMathPDDocument();

        LOGGER.info("processing document '{}'...", title);

        // extract all features we are or might be interested in later
        extractedMathPDDocument.setHistogramCn(Distances.getDocumentHistogram(document, "cn"));
        extractedMathPDDocument.setHistogramCo(Distances.getDocumentHistogram(document, "co"));
        extractedMathPDDocument.setHistogramCi(Distances.getDocumentHistogram(document, "ci"));


        // store the doc in the collector
        LOGGER.info("finished processing document '{}'...", title);
        out.collect(extractedMathPDDocument);
    }
}
