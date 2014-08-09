package de.tuberlin.dima.schubotz.fse.settings;

import com.google.common.collect.HashMultiset;
import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import eu.stratosphere.api.java.DataSet;

/**
 * Created by Jimmy on 8/9/2014.
 */
public class DataStorage {
    private DataSet<String> querySet;
    private DataSet<String> dataRawSet;
    private DataSet<ResultTuple> resultSet;
    private HashMultiset<String> keywordSet;
    private HashMultiset<String> latexSet;

    private DataSet<DataTuple> dataTupleSet;

    public DataSet<DataTuple> getDataTupleSet() {
        return dataTupleSet;
    }

    public void setDataTupleSet(DataSet<DataTuple> dataTupleSet) {
        this.dataTupleSet = dataTupleSet;
    }

    public void setQuerySet(DataSet<String> set) {
        querySet = set;
    }
    public void setDataRawSet(DataSet<String> set) {
        dataRawSet = set;
    }
    public void setResultSet(DataSet<ResultTuple> set) {
        resultSet = set;
    }
    public DataSet<String> getDataRawSet() {
        return dataRawSet;
    }
    public DataSet<ResultTuple> getResultSet() {
        return resultSet;
    }
    public DataSet<String> getQuerySet() {
        return querySet;
    }

    public void setLatexSet(HashMultiset<String> latexSet) {
        this.latexSet = latexSet;
    }

    public void setKeywordSet(HashMultiset<String> keywordSet) {

        this.keywordSet = keywordSet;
    }

    public HashMultiset<String> getLatexSet() {

        return latexSet;
    }

    public HashMultiset<String> getKeywordSet() {

        return keywordSet;
    }
}
