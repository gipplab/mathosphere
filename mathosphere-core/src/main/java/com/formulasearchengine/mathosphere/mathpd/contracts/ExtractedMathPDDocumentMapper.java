package com.formulasearchengine.mathosphere.mathpd.contracts;

import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import com.google.gson.Gson;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

import java.util.Base64;

/**
 * Created by felix on 13.01.17.
 */
public class ExtractedMathPDDocumentMapper implements FlatMapFunction<String, Tuple2<String, ExtractedMathPDDocument>> {
    public static String getFormattedWritableText(ExtractedMathPDDocument doc) {
        return Base64.getEncoder().encodeToString(new Gson().toJson(doc).getBytes());
    }

    @Override
    public void flatMap(String s, Collector<Tuple2<String, ExtractedMathPDDocument>> collector) throws Exception {
        ExtractedMathPDDocument doc = new Gson().fromJson(s, ExtractedMathPDDocument.class);
        collector.collect(new Tuple2<>(doc.getName(), doc));
    }
}
