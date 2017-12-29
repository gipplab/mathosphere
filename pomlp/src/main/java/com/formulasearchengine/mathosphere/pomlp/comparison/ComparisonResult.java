package com.formulasearchengine.mathosphere.pomlp.comparison;

import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;

/**
 * @author Andre Greiner-Petter
 */
public class ComparisonResult {

    private final int index;
    private Double contDist, presDist;
    private final Converters converter;

    public ComparisonResult(int index, Converters conv ){
        this.index = index;
        this.converter = conv;
    }

    public void setContentDistance( double distance ){
        this.contDist = distance;
    }

    public Double getContentDistance(){
        return contDist;
    }

    public void setPresentationDistance( double distance ){
        this.presDist = distance;
    }

    public Double getPresentationDistance(){
        return presDist;
    }

    public int getIndex() {
        return index;
    }

    public Converters getConverter() {
        return converter;
    }

    public String resultToString(){
        String dis = contDist != null ? contDist.toString() : "-";
        dis += "(" + (presDist != null ? presDist.toString() : "-") + ")";
        return dis;
    }

    @Override
    public String toString(){
        return converter.name() + ":" + index + "=" + resultToString();
    }
}
