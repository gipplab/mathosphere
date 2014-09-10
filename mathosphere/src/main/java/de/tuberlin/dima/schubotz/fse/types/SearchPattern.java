package de.tuberlin.dima.schubotz.fse.types;

import com.google.common.collect.Multiset;
import net.sf.saxon.s9api.XQueryExecutable;
import org.apache.flink.api.java.tuple.Tuple7;
import org.w3c.dom.Node;

/**
 * Created by mas9 on 9/12/14.
 */
public class SearchPattern extends Tuple7<Integer,String,Node,XQueryExecutable,XQueryExecutable,XQueryExecutable,Multiset> {
    public SearchPattern(RawSearchPattern rawSearchPattern) {
        for (int i = 0; i < rawSearchPattern.getArity(); i++) {
            setField(rawSearchPattern.getField(i),i);
        }
    }

    public <T> T getNamedField(fields field) {
        return (T) getField( field.ordinal() );
    }

    public <T> void setNamedField(fields field, T value) {
        setField(value, field.ordinal());
    }

    public enum fields {
        queryNumber, formulaID, mathNode, // Inherited from RawSearchPattern
        xQuery,xCdQuery,xDtQuery,tokens
    }
    public Node getMath(){
        return getNamedField(fields.mathNode);
    }
    public void setQuery(fields field, XQueryExecutable query){
        setNamedField(field,query);
    }
}
