package de.tuberlin.dima.schubotz.fse.mappers.dbMapper;

import de.tuberlin.dima.schubotz.fse.mappers.preprocess.DataPreprocessTemplate;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;


public class CountFormulae extends DataPreprocessTemplate<RawDataTuple,Tuple2<String,Integer>> {

    /**
     * Takes in cleaned document, outputs tuple
     * containing all document data extracted.
     * @param in RawDataTuple
     * @param out DataTuple of document
     */
	@Override
	public void flatMap(RawDataTuple in, Collector<Tuple2<String,Integer>> out) {
        docID = in.getNamedField(RawDataTuple.fields.ID);
        data = in.getNamedField(RawDataTuple.fields.rawData);
		setDoc();
//		if( setMath() ) {
//			Integer count = mathElements.size();
//			out.collect( new Tuple2<>( docID,count ) );
//		} else {
//			out.collect( new Tuple2<>( docID, 0 ) );
//		}
		setMath();
	}

}
