package de.tuberlin.dima.schubotz.fse.mappers.dbMapper;

import de.tuberlin.dima.schubotz.fse.mappers.preprocess.DataPreprocessTemplate;
import de.tuberlin.dima.schubotz.fse.types.DatabaseTuple;
import de.tuberlin.dima.schubotz.fse.utils.CMMLInfo;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class ToStrict extends DataPreprocessTemplate<DatabaseTuple, Tuple3<Integer,String,String>> {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(ToStrict.class);

	@Override
	public void flatMap(DatabaseTuple in, Collector<Tuple3<Integer,String,String>> out) {
        if( in.getNamedField(DatabaseTuple.fields.isEquation) != null){
            final String math = in.getNamedField(DatabaseTuple.fields.value);
            try {
                final CMMLInfo cmml = new CMMLInfo(math);
                out.collect( new Tuple3<>(
                        (Integer) in.getNamedField(DatabaseTuple.fields.fId),
                        cmml.toStrictCmml().toString(),
                        "" ));
            } catch (IOException|ParserConfigurationException|TransformerException e){
                LOG.warn("Unable to parse cmml: ", math, e);
            }
        }
	}

}
