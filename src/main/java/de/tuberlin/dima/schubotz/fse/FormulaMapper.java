package de.tuberlin.dima.schubotz.fse;


import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.api.java.tuple.Tuple4;
import eu.stratosphere.api.java.tuple.Tuple7;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.Map;

public class FormulaMapper extends FlatMapFunction<Tuple4<String, Integer, Integer, StringValue>, Tuple7<String, Integer, Integer, String, String, Double, StringValue>> {
	private final Tuple7<String, Integer, Integer, String, String, Double, StringValue> result = new Tuple7<>();
	private explicitDataSet<Tuple3<String, String, Node>> formulae;

	@Override
	public void open (Configuration parameters) throws Exception {
		//Setup formulae, keywords from queries
		formulae = new explicitDataSet<>();
		Collection<Query> queries = getRuntimeContext().getBroadcastVariable( "Queries" );
		for ( Query query : queries ) {
			for ( Map.Entry<String, String> formula : query.formulae.entrySet() ) {
				Node node = XMLHelper.String2Doc( formula.getValue(), false );
				formulae.add( new Tuple3<>( query.name, formula.getKey(), node ) );
			}
		}
		super.open( parameters );
	}

	/**
	 * The core method of the FlatMapFunction. Takes an element from the input data set and transforms
	 * it into zero, one, or more elements.
	 *
	 * @param value The input value.
	 * @param out   The collector for for emitting result values.
	 * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
	 *                   to fail and may trigger recovery.
	 */
	@Override
	public void flatMap (Tuple4<String, Integer, Integer, StringValue> value, Collector<Tuple7<String, Integer, Integer, String, String, Double, StringValue>> out) throws Exception {
		if ( value == null || value.f3.getValue().length()<10 )
			return;
		Document doc = XMLHelper.String2Doc( value.f3.getValue(), false );
		//System.out.println("processing:"+value.f3.getValue());
		NodeList MathMLElements = XMLHelper.getElementsB( doc, "//math" );
		for ( int i = 0; i < MathMLElements.getLength(); i++ ) {
			for ( Tuple3<String, String, Node> entry : formulae ) {
				Map<String, Node> qvars = null;
				if ( XMLHelper.compareNode( entry.f2, MathMLElements.item( i ), false, qvars ) ) {
					System.out.println("Found result for "+entry.f0+"("+entry.f1+") in " + value.f0 );
					ResultTuple resultTuple = new ResultTuple();
					HitTuple hitTuple = new HitTuple();
					FormulaTuple formulaTuple = new FormulaTuple();
					String formulaID = MathMLElements.item( i ).getAttributes().getNamedItem( "id" ).getNodeValue();
					formulaTuple.setXRef(  formulaID );
					if ( qvars != null ) {
						explicitDataSet<QVarTuple> qvarDataSet = new explicitDataSet<>();
						for ( Map.Entry<String, Node> nodeEntry : qvars.entrySet() ) {
							QVarTuple tuple = new QVarTuple();
							tuple.setQVar( nodeEntry.getKey() );
							tuple.setXRef( nodeEntry.getValue().getAttributes().getNamedItem( "id" ).getNodeValue() );
							qvarDataSet.add( tuple );
						}
						formulaTuple.setQVar( qvarDataSet );
					}
					hitTuple.addFormula(formulaTuple);
					hitTuple.setQueryID( entry.f1 );
					hitTuple.setScore( 100. );
					resultTuple.addHit( hitTuple );
					for ( int j = 0; j < 3; j++ ) {
						result.setField( value.getField( j ), j );
					}
					result.f3 = resultTuple.getFor();
					result.f4 = hitTuple.getXRef();
					result.f5 = hitTuple.getScore();
					result.f6 = new StringValue( hitTuple.toString() );
					out.collect( result );
				}
			}
		}
	}
}
