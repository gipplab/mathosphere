package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple4;

public class Hit extends Tuple4<String, String, String, Double>{
    public final static int SCORE_POS=3;
    public final static int XREF_POS=1;
    public final static int JUSTIFICATION_POS=2;
    public final static int QUERYID_POS=0;

    public Double getScore() {
        return f3;
    }

    public String getXref() {
        return f1;
    }

    public String getJustification() {
        return f2;
    }

    public String getQueryID() {
        return f0;
    }

    public void setScore(Double score) {
        this.f3 = score;
    }

    public void setXref(String xref) {
        this.f1 = xref;
    }

    public void setJustification(String justification) {
        this.f2 = justification;
    }

    public void setQueryID(String queryID) {
        this.f0 = queryID;
    }




}
