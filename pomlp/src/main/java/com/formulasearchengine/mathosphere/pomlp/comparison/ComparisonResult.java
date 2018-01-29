package com.formulasearchengine.mathosphere.pomlp.comparison;

import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import com.sun.corba.se.spi.servicecontext.UEInfoServiceContext;

/**
 * @author Andre Greiner-Petter
 */
public class ComparisonResult {

    public static final String[] HEADER = new String[]{
            "-Struc-Total-Dist",
            "-Struc-Cont-Dist",
            "-Struc-Pres-Dist",
            "-Histo-Abs-Dist",
            "-Histo-Rel-Dist",
            "-Histo-Cos-Dist",
            "-Histo-EarthMov-Dist",
            "-Content-Histo-Abs-Dist",
            "-Content-Histo-Rel-Dist",
            "-Content-Histo-Cos-Dist",
            "-Content-Histo-EarthMov-Dist"
    };

    private final Converters converter;
    private final int index;

    private Double
            strucTotalDistance,
            strucContentDistance,
            strucPresentationDistance;

    private Double
            histoAbsoluteDist,
            histoRelativeDist,
            histoCosineDist,
            histoEarthMoverDist;

    private Double
            conthistoAbsoluteDist,
            conthistoRelativeDist,
            conthistoCosineDist,
            conthistoEarthMoverDist;

    public ComparisonResult(int index, Converters conv ){
        this.index = index;
        this.converter = conv;
    }

    public static String[] personalHeader( Converters conv ){
        String[] header = new String[HEADER.length];
        for ( int i = 0; i < HEADER.length; i++ )
            header[i] = conv.name() + HEADER[i];
        return header;
    }

    public Double[] getResults(){
        return new Double[]{
                strucTotalDistance,
                strucContentDistance,
                strucPresentationDistance,
                histoAbsoluteDist,
                histoRelativeDist,
                histoCosineDist,
                histoEarthMoverDist,
                conthistoAbsoluteDist,
                conthistoRelativeDist,
                conthistoCosineDist,
                conthistoEarthMoverDist
        };
    }

    public Converters getConverter() {
        return converter;
    }

    public int getIndex() {
        return index;
    }

    public Double getStrucTotalDistance() {
        return strucTotalDistance;
    }

    public void setStrucTotalDistance(Double strucTotalDistance) {
        this.strucTotalDistance = strucTotalDistance;
    }

    public Double getStrucContentDistance() {
        return strucContentDistance;
    }

    public void setStrucContentDistance(Double strucContentDistance) {
        this.strucContentDistance = strucContentDistance;
    }

    public Double getStrucPresentationDistance() {
        return strucPresentationDistance;
    }

    public void setStrucPresentationDistance(Double strucPresentationDistance) {
        this.strucPresentationDistance = strucPresentationDistance;
    }

    public Double getHistoAbsoluteDist() {
        return histoAbsoluteDist;
    }

    public void setHistoAbsoluteDist(Double histoAbsoluteDist) {
        this.histoAbsoluteDist = histoAbsoluteDist;
    }

    public Double getHistoRelativeDist() {
        return histoRelativeDist;
    }

    public void setHistoRelativeDist(Double histoRelativeDist) {
        this.histoRelativeDist = histoRelativeDist;
    }

    public Double getHistoCosineDist() {
        return histoCosineDist;
    }

    public void setHistoCosineDist(Double histoCosineDist) {
        this.histoCosineDist = histoCosineDist;
    }

    public Double getHistoEarthMoverDist() {
        return histoEarthMoverDist;
    }

    public void setHistoEarthMoverDist(Double histoEarthMoverDist) {
        this.histoEarthMoverDist = histoEarthMoverDist;
    }

    public Double getConthistoAbsoluteDist() {
        return conthistoAbsoluteDist;
    }

    public void setConthistoAbsoluteDist(Double conthistoAbsoluteDist) {
        this.conthistoAbsoluteDist = conthistoAbsoluteDist;
    }

    public Double getConthistoRelativeDist() {
        return conthistoRelativeDist;
    }

    public void setConthistoRelativeDist(Double conthistoRelativeDist) {
        this.conthistoRelativeDist = conthistoRelativeDist;
    }

    public Double getConthistoCosineDist() {
        return conthistoCosineDist;
    }

    public void setConthistoCosineDist(Double conthistoCosineDist) {
        this.conthistoCosineDist = conthistoCosineDist;
    }

    public Double getConthistoEarthMoverDist() {
        return conthistoEarthMoverDist;
    }

    public void setConthistoEarthMoverDist(Double conthistoEarthMoverDist) {
        this.conthistoEarthMoverDist = conthistoEarthMoverDist;
    }
}
