package com.formulasearchengine.mathosphere.mathpd.pojos;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents a document with only those features and data we are interested in our later analysis pipeline.
 * <p>
 * Created by felix on 07.12.16.
 */
public class ExtractedMathPDDocument implements Comparable<ExtractedMathPDDocument>, Serializable {
    public String title;
    public String text;
    private HashMap<String, Integer> histogramCn;
    private HashMap<String, Integer> histogramCo;
    private HashMap<String, Integer> histogramCi;

    public ExtractedMathPDDocument() {
    }

    public ExtractedMathPDDocument(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public HashMap<String, Integer> getHistogramCn() {
        return histogramCn;
    }

    public void setHistogramCn(HashMap<String, Integer> histogramCn) {
        this.histogramCn = histogramCn;
    }

    public HashMap<String, Integer> getHistogramCo() {
        return histogramCo;
    }

    public void setHistogramCo(HashMap<String, Integer> histogramCo) {
        this.histogramCo = histogramCo;
    }

    public HashMap<String, Integer> getHistogramCi() {
        return histogramCi;
    }

    public void setHistogramCi(HashMap<String, Integer> histogramCi) {
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
}
