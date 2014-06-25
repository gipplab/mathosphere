package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.tuple.Tuple5;
import eu.stratosphere.types.DoubleValue;

/**
 * Schema according to @url{https://svn.mathweb.org/repos/NTCIR-Math/topics/ntcir11/lib/NTCIR11-results.rnc}
 * formula = element formula {id.att & score.att & xref.att & for.att & qvar*}
 */
public class FormulaTuple extends Tuple5<Integer, String, String, DoubleValue, explicitDataSet<QVarTuple>> {
    public DataSet<QVarTuple> getQVar() {
        return getField(fields.qvar.ordinal());
    }

    public void setQVar(DataSet<QVarTuple> QVar) {
        setField(QVar, fields.qvar.ordinal());
    }

    public Integer getId() {
        return getField(fields.id.ordinal());
    }

    public void setId(Integer id) {
        setField(id, fields.id.ordinal());
    }

    public String getFor() {
        return getField(fields.aFor.ordinal());
    }

    public void setFor(String aFor) {
        setField(aFor, fields.aFor.ordinal());
    }

    public String getXRef() {
        return getField(fields.xref.ordinal());
    }

    public void setXRef(String XRef) {
        setField(XRef, fields.xref.ordinal());
    }

    public Double getScore() {
        DoubleValue field = getField(fields.id.ordinal());
        return field.getValue();
    }

    public void setScore(Double score) {
        DoubleValue field = new DoubleValue(score);
        setField(field, fields.score.ordinal());
    }

    public enum fields {
        id, aFor, xref, score, qvar
    }

}
