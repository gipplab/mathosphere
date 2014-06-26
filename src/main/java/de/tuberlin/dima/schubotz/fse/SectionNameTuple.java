package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple3;

/**
 * Created by Moritz on 25.06.2014.
 */
public class SectionNameTuple extends Tuple3<String,Integer,Integer> {
    public enum fields{
        id,version,section_number
    }

    public SectionNameTuple(String i, int i1, int i2) {
        super(i,i1,i2);
    }
}
