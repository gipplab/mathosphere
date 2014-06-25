package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.tuple.Tuple;

import java.util.HashMap;
import java.util.HashSet;

public class explicitDataSet<T extends Tuple> extends HashSet<T> {
    HashMap<?,T> getAsHashMap(Integer keyFieldNo){
        HashMap<Object, T> map = new HashMap<>();
        for (T h : this) {
            map.put(h.getField(keyFieldNo),h);
        }
        return map;
    }

}
