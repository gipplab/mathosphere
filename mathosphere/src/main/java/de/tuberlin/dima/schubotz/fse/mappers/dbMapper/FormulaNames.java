package de.tuberlin.dima.schubotz.fse.mappers.dbMapper;

import de.tuberlin.dima.schubotz.fse.mappers.preprocess.DataPreprocessTemplate;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.jsoup.nodes.Element;


public class FormulaNames extends DataPreprocessTemplate<RawDataTuple,Tuple2<String,String>> {

	@Override
	public void flatMap(RawDataTuple in, Collector<Tuple2<String,String>> out) {
        docID = in.getNamedField(RawDataTuple.fields.ID);
        data = in.getNamedField(RawDataTuple.fields.rawData);
		setDoc();
		if( setMath() ) {
			for ( Element mathElement : mathElements ) {
				String id = mathElement.attr( "id" );
				out.collect( new Tuple2<>( docID,id ) );
			}
		}
	}

}
