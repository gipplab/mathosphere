package de.tuberlin.dima.schubotz.fse.settings;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import org.apache.flink.api.java.DataSet;

import java.io.Serializable;

/**
 * Created by Jimmy on 8/9/2014.
 */
public class DataStorage implements Serializable {
    private DataSet<RawDataTuple> querySet;
    private DataSet<RawDataTuple> dataSet;
    private DataSet<ResultTuple> resultSet;
    private HashMultiset<String> keywordSet;
    private HashMultiset<String> latexSet;

    private DataSet<DataTuple> dataTupleSet;
    private DataSet<DataTuple> queryTupleSet;

    public DataSet<DataTuple> getQueryTupleSet() {
        return queryTupleSet;
    }
    public void setQueryTupleSet(DataSet<DataTuple> queryTupleSet) {
        this.queryTupleSet = queryTupleSet;
    }
    public DataSet<DataTuple> getDataTupleSet() {
        return dataTupleSet;
    }
    public void setDataTupleSet(DataSet<DataTuple> dataTupleSet) {
        this.dataTupleSet = dataTupleSet;
    }
    public void setQuerySet(DataSet<RawDataTuple> set) {
        querySet = set;
    }
    public void setDataSet(DataSet<RawDataTuple> set){
        dataSet = set;
    }
    public void setResultSet(DataSet<ResultTuple> set) {
        resultSet = set;
    }
    public DataSet<RawDataTuple> getDataSet() {
        return dataSet;
    }
    public DataSet<ResultTuple> getResultSet() {
        return resultSet;
    }
    public DataSet<RawDataTuple> getQuerySet() {
        return querySet;
    }
    public void setLatexSet(HashMultiset<String> latexSet) {
        this.latexSet = HashMultiset.create(latexSet);
    }
    public void setKeywordSet(HashMultiset<String> keywordSet) {
        this.keywordSet = HashMultiset.create(keywordSet);
    }
    public HashMultiset<String> getLatexSet() {
        return HashMultiset.create(latexSet);
    }
    public HashMultiset<String> getKeywordSet() {
        return HashMultiset.create(keywordSet);
    }
}
