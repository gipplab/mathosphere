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
    private static final String ID_SEPARATOR = "/";
    public String title;
    public String text;
    private String name;
    private String page;
    private HashMap<String, Integer> histogramCn;
    private HashMap<String, Integer> histogramCsymbol;
    private HashMap<String, Integer> histogramCi;
    private HashMap<String, Integer> histogramBvar;

    public ExtractedMathPDDocument() {
    }

    public ExtractedMathPDDocument(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public static String getNameFromId(String id) {
        return id.split("/")[0];
    }

    public static String getPageFromId(String id) {
        return id.split("/")[1];
    }

    public HashMap<String, Integer> getHistogramBvar() {
        return histogramBvar;
    }

    public void setHistogramBvar(HashMap<String, Integer> histogramBvar) {
        this.histogramBvar = histogramBvar;
    }

    public HashMap<String, Integer> getHistogramCn() {
        return histogramCn;
    }

    public void setHistogramCn(HashMap<String, Integer> histogramCn) {
        this.histogramCn = histogramCn;
    }

    public HashMap<String, Integer> getHistogramCsymbol() {
        return histogramCsymbol;
    }

    public void setHistogramCsymbol(HashMap<String, Integer> histogramCsymbol) {
        this.histogramCsymbol = histogramCsymbol;
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

    public String getId() {
        return this.getName() + "/" + this.getPage();
    }

    @Override
    public String toString() {
        return "[title=" + title + ", name=" + name + ", page=" + page + ", text=" + StringUtils.abbreviate(text, 100) + "]";
    }

    @Override
    public int compareTo(ExtractedMathPDDocument o) {
        return this.getText().compareTo(o.getText());
    }

    @Override
    public int hashCode() {
        return this.getTitle().hashCode() + this.getText().hashCode();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }
}
