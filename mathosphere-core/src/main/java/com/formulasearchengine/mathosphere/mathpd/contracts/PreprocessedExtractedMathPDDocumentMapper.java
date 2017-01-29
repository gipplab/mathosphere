package com.formulasearchengine.mathosphere.mathpd.contracts;

import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import com.google.gson.Gson;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Created by felix on 13.01.17.
 */
public class PreprocessedExtractedMathPDDocumentMapper implements FlatMapFunction<String, Tuple2<String, ExtractedMathPDDocument>> {
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Logger LOGGER = LoggerFactory.getLogger(PreprocessedExtractedMathPDDocumentMapper.class);

    public static ExtractedMathPDDocument readExtractedMathPDDocumentFromText(String text) {
        final String json = new String(Base64.getDecoder().decode(text), CHARSET);
        LOGGER.info(json);
        return new Gson().fromJson(json, ExtractedMathPDDocument.class);
    }

    public static String getFormattedWritableText(ExtractedMathPDDocument doc) {
        return Base64.getEncoder().encodeToString(new Gson().toJson(doc).getBytes());
    }

    @Override
    public void flatMap(String s, Collector<Tuple2<String, ExtractedMathPDDocument>> collector) throws Exception {
        ExtractedMathPDDocument doc = readExtractedMathPDDocumentFromText(s);
        collector.collect(new Tuple2<>(doc.getName(), doc));
    }
}
