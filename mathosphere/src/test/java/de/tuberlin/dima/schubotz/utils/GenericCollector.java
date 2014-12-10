package de.tuberlin.dima.schubotz.utils;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jimmy on 9/14/2014.
 */
public class GenericCollector<T> implements Collector<T> {
    List<T> dataList = new ArrayList<>();
    @Override
    public void collect(T record) {
        dataList.add(record);
    }

    public Iterable<T> getDatalist() {
        return dataList;
    }

    @Override
    public void close() {
    }
}
