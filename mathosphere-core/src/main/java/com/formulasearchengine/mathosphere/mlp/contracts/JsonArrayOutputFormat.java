package com.formulasearchengine.mathosphere.mlp.contracts;

import org.apache.flink.api.java.io.TextOutputFormat;
import org.apache.flink.core.fs.Path;

import java.io.IOException;

/**
 * Created by Moritz on 27.08.2017.
 */
public class JsonArrayOutputFormat extends TextOutputFormat {
    private static final char NEWLINE = '\n';
    private static final char COMMA = ',';
    private static final char CURLY_OPEN = '[';
    private static final char CURLY_CLOSE = ']';
    private final String CHARSET = "UTF-8";
    private boolean needsComma = false;

    public JsonArrayOutputFormat(Path outputPath) {
        super(outputPath);
    }

    @Override
    public void close() throws IOException {
        if (needsComma) {
            this.stream.write(NEWLINE);
        }
        this.stream.write(CURLY_CLOSE);
        this.stream.write(NEWLINE);//Files should end with new lines
        super.close();
    }

    @Override
    public void open(int taskNumber, int numTasks) throws IOException {
        super.open(taskNumber, numTasks);
        this.stream.write(CURLY_OPEN);
        this.stream.write(NEWLINE);
    }

    @Override
    public void writeRecord(Object record) throws IOException {
        byte[] bytes = record.toString().getBytes(CHARSET);
        if (bytes.length > 0) {
            if (needsComma) {
                this.stream.write(COMMA);
                this.stream.write(NEWLINE);
            }
            this.stream.write(bytes);
            needsComma = true;
        }
    }
}
