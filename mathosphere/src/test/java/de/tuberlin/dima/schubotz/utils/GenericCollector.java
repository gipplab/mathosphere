package de.tuberlin.dima.schubotz.utils;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.flink.util.Collector;

import java.util.ArrayList;

/**
 * Created by Jimmy on 9/14/2014.
 */
public class GenericCollector<T> implements Collector<T> {
    ArrayList<T> dataList = new ArrayList<>();
    @Override
    public void collect(T record) {
        dataList.add(record);
    }

    public ArrayList<T> getDatalist() {
        return dataList;
    }

    @Override
    public void close() {
    }
}
