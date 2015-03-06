package mlp.contracts;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.util.Collector;

/**
 * Collector implementation for testing
 */
public class ListCollector<T> implements Collector<T> {

    private final List<T> list = new ArrayList<>();

    public ListCollector() {
    }

    @Override
    public void collect(T record) {
        list.add(record);
    }

    @Override
    public void close() {
        // no op
    }

    public List<T> getList() {
        return list;
    }
}