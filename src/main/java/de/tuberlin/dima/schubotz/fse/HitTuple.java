package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple5;

/**
 * Schema according to @url{https://svn.mathweb.org/repos/NTCIR-Math/topics/ntcir11/lib/NTCIR11-results.rnc}
 * hit = element hit {id.att & score.att & xref.att & rank.att& formula*}
 */
public class HitTuple extends Tuple5<String, String, String, Double, explicitDataSet<FormulaTuple>> {
    public String getQueryID() {
        return getField(fields.id.ordinal());
    }

    public void setQueryID(String queryID) {
        setField(queryID, fields.id.ordinal());
    }

    public String getXref() {
        return getField(fields.xref.ordinal());
    }

    public void setXref(String xref) {
        setField(xref, fields.xref.ordinal());
    }

    public Double getScore() {
        return getField(fields.score.ordinal());
    }

    public void setScore(Double score) {
        setField(score, fields.score.ordinal());
    }

    public enum fields {
        id, xref, score, rank, formula
    }
}
