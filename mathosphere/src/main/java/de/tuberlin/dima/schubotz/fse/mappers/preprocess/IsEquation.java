package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.CMMLInfo;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.jsoup.nodes.Element;

public class IsEquation extends DataPreprocessTemplate<Tuple3<Boolean,String,String>> {
    private static final SafeLogWrapper LOG = new SafeLogWrapper(IsEquation.class);

	@Override
	public void flatMap(RawDataTuple in, Collector<Tuple3<Boolean,String,String>> out) {
        docID = in.getNamedField(RawDataTuple.fields.ID);
        data = in.getNamedField(RawDataTuple.fields.rawData);
		setDoc();
		if( setMath() ) {
			for ( Element mathElement : mathElements ) {
				String id = mathElement.attr( "id" );
                Boolean equation= isEquation(mathElement.toString());
                if (equation != null){
                    out.collect( new Tuple3<>(equation, docID,id)  );
                }
            }
		}
	}
    private Boolean isEquation(String MathML){
        try {
            CMMLInfo cmml = new CMMLInfo(MathML);
            return cmml.isEquation();
        } catch (Exception e) {
            return null;
        }
    }

}
