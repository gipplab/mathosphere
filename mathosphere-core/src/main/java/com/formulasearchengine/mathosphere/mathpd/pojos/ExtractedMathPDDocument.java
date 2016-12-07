package com.formulasearchengine.mathosphere.mathpd.pojos;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.types.Key;

import java.io.IOException;
import java.util.Map;

/**
 * Represents a document with only those features and data we are interested in our later analysis pipeline.
 * <p>
 * Created by felix on 07.12.16.
 */
public class ExtractedMathPDDocument implements Key<ExtractedMathPDDocument> {
    public String title;
    public String text;
    private Map<String, Integer> histogramCn;
    private Map<String, Integer> histogramCo;
    private Map<String, Integer> histogramCi;

    public ExtractedMathPDDocument() {
    }

    public ExtractedMathPDDocument(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public Map<String, Integer> getHistogramCn() {
        return histogramCn;
    }

    public void setHistogramCn(Map<String, Integer> histogramCn) {
        this.histogramCn = histogramCn;
    }

    public Map<String, Integer> getHistogramCo() {
        return histogramCo;
    }

    public void setHistogramCo(Map<String, Integer> histogramCo) {
        this.histogramCo = histogramCo;
    }

    public Map<String, Integer> getHistogramCi() {
        return histogramCi;
    }

    public void setHistogramCi(Map<String, Integer> histogramCi) {
        this.histogramCi = histogramCi;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "[title=" + title + ", text=" + StringUtils.abbreviate(text, 100) + "]";
    }

    @Override
    public int compareTo(ExtractedMathPDDocument o) {
        return this.getText().compareTo(o.getText());
    }

    @Override
    public int hashCode() {
        return this.getTitle().hashCode() + this.getText().hashCode();
    }

    @Override
    public void write(DataOutputView dataOutputView) throws IOException {
        throw new NotImplementedException("write");
    }

    @Override
    public void read(DataInputView dataInputView) throws IOException {
        throw new NotImplementedException("read");
    }
}
