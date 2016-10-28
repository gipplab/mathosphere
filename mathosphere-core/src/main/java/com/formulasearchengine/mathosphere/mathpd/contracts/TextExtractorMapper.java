package com.formulasearchengine.mathosphere.mathpd.contracts;

import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExtractorMapper implements FlatMapFunction<String, ArxivDocument> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextExtractorMapper.class);

    private static final Pattern TITLE_PATTERN = Pattern.compile("<ARXIVFILESPLIT(?:\\\\n?|[\\s\\r\\n]+)" +
            "Filename=\"(.*?).xhtml\">(?:\\s*)(.*)", Pattern.DOTALL);


    @Override
    public void flatMap(String content, Collector<ArxivDocument> out) throws Exception {
        Matcher titleMatcher = TITLE_PATTERN.matcher(content);
        if (!titleMatcher.find()) {
            return;
        }

        final String title = titleMatcher.group(1);
        final String xhtml = titleMatcher.group(2);
        final ArxivDocument document = new ArxivDocument(title, xhtml);

        LOGGER.info("processing document '{}'...", title);

        out.collect(document);

    }

}
