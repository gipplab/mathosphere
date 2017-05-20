package com.formulasearchengine.mathosphere.mathpd.pojos;

import com.formulasearchengine.mathosphere.mathpd.Distances;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a document with only those features and data we are interested in our later analysis pipeline.
 * <p>
 * Created by felix on 07.12.16.
 */
public class ExtractedMathPDDocument implements Comparable<ExtractedMathPDDocument>, Serializable {
    private static final String ID_SEPARATOR = "/";
    public String title;
    public String text;
    public String name;
    private String page;

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    private String plainText = null;
    private Map<String, Double> histogramCn = new HashMap<>();
    private Map<String, Double> histogramCsymbol = new HashMap<>();
    private Map<String, Double> histogramCi = new HashMap<>();
    private Map<String, Double> histogramBvar = new HashMap<>();

    public ExtractedMathPDDocument() {
    }

    public ExtractedMathPDDocument(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public static String getNameFromId(String id) {
        return id.split(ID_SEPARATOR)[0];
    }

    public static String getPageFromId(String id) {
        return id.split(ID_SEPARATOR)[1];
    }

    public Map<String, Double> getHistogramBvar() {
        return histogramBvar;
    }

    public String getPlainText() {
        if ( plainText == null ){
            plainText = WikiTextUtils.MATH_TAG_PATTERN.matcher(text).replaceAll("");
        }
        return plainText;
    }

    public void setHistogramBvar(Map<String, Double> histogramBvar) {
        this.histogramBvar = histogramBvar;
    }

    public Map<String, Double> getHistogramCn() {
        return histogramCn;
    }

    public void setHistogramCn(Map<String, Double> histogramCn) {
        this.histogramCn = histogramCn;
    }

    public Map<String, Double> getHistogramCsymbol() {
        return histogramCsymbol;
    }

    public void setHistogramCsymbol(Map<String, Double> histogramCsymbol) {
        this.histogramCsymbol = histogramCsymbol;
    }

    public Map<String, Double> getHistogramCi() {
        return histogramCi;
    }

    public void setHistogramCi(Map<String, Double> histogramCi) {
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
        return this.getName() + ID_SEPARATOR + this.getPage();
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
        //return "none";
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void mergeOtherIntoThis(ExtractedMathPDDocument other) {
        if (!this.name.equals(other.name)) {
            throw new RuntimeException("name is not equal : " + name + " vs " + other.name);
        }
        this.histogramBvar = Distances.histogramsPlus(this.histogramBvar, other.histogramBvar);
        this.histogramCi = Distances.histogramsPlus(this.histogramCi, other.histogramCi);
        this.histogramCn = Distances.histogramsPlus(this.histogramCn, other.histogramCn);
        this.histogramCsymbol = Distances.histogramsPlus(this.histogramCsymbol, other.histogramCsymbol);
        this.text += other.text;
        this.plainText = null;
    }
}
