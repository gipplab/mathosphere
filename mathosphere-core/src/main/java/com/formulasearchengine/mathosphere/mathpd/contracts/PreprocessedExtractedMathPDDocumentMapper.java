package com.formulasearchengine.mathosphere.mathpd.contracts;

import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
        LOGGER.info("text = " + text);
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(text));
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return (ExtractedMathPDDocument) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            new RuntimeException(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex2) {
                // ignore close exception
            }
        }
        return null;
    }

    public static String getFormattedWritableText(ExtractedMathPDDocument doc) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(doc);
            oos.flush();
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            try {
                if (bos != null)
                    bos.close();
                if (oos != null) {
                    oos.close();
                    ;
                }
            } catch (IOException oie2) {
                throw new RuntimeException(oie2);
            }
        }
    }

    @Override
    public void flatMap(String s, Collector<Tuple2<String, ExtractedMathPDDocument>> collector) throws Exception {
        ExtractedMathPDDocument doc = readExtractedMathPDDocumentFromText(s);
        collector.collect(new Tuple2<>(doc.getName(), doc));
    }
}
