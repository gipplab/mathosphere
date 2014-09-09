package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;

public class FormulaToDB extends DataPreprocessTemplate<RawDataTuple,Tuple3<String,String,String>> {

	@Override
	public void flatMap(RawDataTuple in, Collector out) {
        docID = in.getNamedField(RawDataTuple.fields.ID);
        data = in.getNamedField(RawDataTuple.fields.rawData);
		setDoc();
		if( setMath() ) {
			for ( Element mathElement : mathElements ) {
				String id = mathElement.attr( "id" );
                doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
                doc.outputSettings().prettyPrint(false);
				out.collect( new Tuple3<>( docID,id, mathElement.toString() ) );
			}
		}
	}

}
