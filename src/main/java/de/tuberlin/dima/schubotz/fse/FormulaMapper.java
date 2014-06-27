package de.tuberlin.dima.schubotz.fse;


import com.google.common.collect.Multiset;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple4;
import eu.stratosphere.api.java.tuple.Tuple7;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FormulaMapper extends FlatMapFunction<Tuple4<String, Integer, Integer, StringValue>, Tuple7<String, Integer, Integer, String, String, Double, StringValue>> {
	private final Tuple7<String, Integer, Integer, String, String, Double, StringValue> result = new Tuple7<>();
	private explicitDataSet<Tuple4<String, String, Node, Multiset<String>>> formulae;

	@Override
	public void open (Configuration parameters) throws Exception {
		//Setup formulae, keywords from queries
		formulae = new explicitDataSet<>();
		Collection<Query> queries = getRuntimeContext().getBroadcastVariable( "Queries" );
		for ( Query query : queries ) {
			for ( Map.Entry<String, String> formula : query.formulae.entrySet() ) {
				Node node = XMLHelper.String2Doc( formula.getValue(), false );
				formulae.add( new Tuple4<>( query.name, formula.getKey(), node, XMLHelper.getIdentifiersFrom( formula.getValue() ) ) );
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
			for ( Tuple4<String, String, Node, Multiset<String>> entry : formulae ) {
				Map<String, Node> qvars = new HashMap<>(  );
				Multiset<String> actualElements = XMLHelper.getIdentifiersFromCmml( XMLHelper.getElementB( MathMLElements.item( i ), "./semantics/annotation-xml" ) );
				double score =0.;
				if( actualElements.size() > 0 )
					score = XMLHelper.calulateBagScore( entry.f3, actualElements );
				if ( score>0.  ) {
					score += XMLHelper.cacluateSimilarityScore( entry.f2, MathMLElements.item( i ), qvars );
					//System.out.println( "Found result for " + entry.f0 + "(" + entry.f1 + ") in " + value.f0 );
					ResultTuple resultTuple = new ResultTuple();
					HitTuple hitTuple = new HitTuple();
					FormulaTuple formulaTuple = new FormulaTuple();
					formulaTuple.setScore( score );
					String formulaID = MathMLElements.item( i ).getAttributes().getNamedItem( "id" ).getNodeValue();
					formulaTuple.setXRef( formulaID );
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
					resultTuple.setFor( value.getField( 0 )+"_"+value.getField( 1 )+"_"+value.getField( 2 )+".xhtml");

					hitTuple.addFormula( formulaTuple );
					hitTuple.setQueryID( entry.f1 );
					hitTuple.setScore( score );
					resultTuple.addHit( hitTuple );
					for ( int j = 0; j < 3; j++ ) {
						result.setField( value.getField( j ), j );
					}
					result.f3 = resultTuple.getFor();
					result.f4 = resultTuple.getFor(); //hitTuple.getXRef();
					result.f5 = hitTuple.getScore();
					result.f6 = new StringValue( hitTuple.toString() );
					out.collect( result );
				}
			}
		}
	}
}
