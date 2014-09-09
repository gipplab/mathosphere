package de.tuberlin.dima.schubotz.fse.mappers;

import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.fse.types.ResultTuple;
import de.tuberlin.dima.schubotz.fse.utils.CMMLInfo;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import net.sf.saxon.s9api.XQueryExecutable;
import org.apache.flink.api.java.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Takes in each document, compares it to each query and maps a score in the form of a {@link de.tuberlin.dima.schubotz.fse.types.ResultTuple}
 */
public class QueryDepthMatch extends RichFlatMapFunction<DataTuple, ResultTuple> {
	/**
	 * QueryTuple dataset taken from broadcast variable in {@link de.tuberlin.dima.schubotz.fse.mappers.QueryDepthMatch#open}
	 */
    private Collection<DataTuple> queries;
	private Map<String,XQueryExecutable> cQueries = new HashMap();

	private static final SafeLogWrapper LOG = new SafeLogWrapper(QueryDepthMatch.class);

	@Override
	public void open(Configuration parameters) throws Exception {
		queries = getRuntimeContext().getBroadcastVariable("Queries");
		for (final DataTuple query : queries) {
/*            try {
                cmml=new CMMLInfo(annotationXML.toString());
                cmml.getXQueryString();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }*/
			String mml= query.getNamedField( DataTuple.fields.mml );
			CMMLInfo cmml = new CMMLInfo( mml );
			cQueries.put( query.getNamedField( DataTuple.fields.docID ), cmml.getXQuery() );
		}
	}

	/**
	 * The core method of the MapFunction. Takes an element from the input data set and transforms
	 * it into another element.
	 *
	 * @param in The input value.
	 * @return The value produced by the map function from the input value. 
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap(DataTuple in,Collector<ResultTuple> out) {


		for ( Map.Entry<String, XQueryExecutable> query : cQueries.entrySet() ) {
			try {
				CMMLInfo cmmlInfo = new CMMLInfo( in.getNamedField( DataTuple.fields.mml ) );
				Integer depth = cmmlInfo.getDepth( query.getValue() );
				if (depth != null){
					out.collect(new ResultTuple(
						query.getKey(),
						in.getNamedField(DataTuple.fields.docID),
						new Double( depth)));
				}
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}




	}

}
