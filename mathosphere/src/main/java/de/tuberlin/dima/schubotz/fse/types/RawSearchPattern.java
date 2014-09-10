package de.tuberlin.dima.schubotz.fse.types;

import org.apache.flink.api.java.tuple.Tuple3;
import org.w3c.dom.Node;

/**
 * Created by mas9 on 9/12/14.
 */
public class RawSearchPattern extends Tuple3<Integer,String,Node> {
    public RawSearchPattern(Integer qId, String fid, Node math) {
        setNamedField(fields.queryNumber,qId);
        setNamedField(fields.formulaID,fid);
        setNamedField(fields.mathNode,math);
    }

    public <T> T getNamedField(fields field) {
        return (T) getField( field.ordinal() );
    }

    public <T> void setNamedField(fields field, T value) {
        setField(value, field.ordinal());
    }

    public enum fields {
        queryNumber, formulaID, mathNode
    }
    public Node getMath(){
        return getNamedField(fields.mathNode);
    }
}
