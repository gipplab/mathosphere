package de.tuberlin.dima.schubotz.fse;


import com.google.common.collect.HashMultiset;
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
	public void flatMap (Tuple4<String, Integer, Integer, StringValue> value, Collector<Tuple7<String, Integer, Integer, String, String, Double, StringValue>> out) {
		NodeList mathMLElements;
		if ( value == null || value.f3.getValue().length() < 10 )
			return;
		try {
			Document doc = XMLHelper.String2Doc( value.f3.getValue(), false );
			//System.out.println("processing:"+value.f3.getValue());
			mathMLElements = XMLHelper.getElementsB( doc, "//math" );
			// TODO: This loop must loop over queries not formulae
			for ( Tuple4<String, String, Node, Multiset<String>> entry : formulae ) {
				Double globalScore=0.;
				String filename = value.getField( 0 ) + "_" + value.getField( 1 ) + "_" + value.getField( 2 ) + ".xhtml";
				ResultTuple resultTuple = new ResultTuple();
				HitTuple hitTuple = new HitTuple();
				for ( int i = 0; i < mathMLElements.getLength(); i++ ) {
					Map<String, Node> qvars = new HashMap<>();
					Multiset<String> actualElements;
					try {
						actualElements = XMLHelper.getIdentifiersFromCmml( XMLHelper.getElementB( mathMLElements.item( i ), "//semantics/annotation-xml" ) );
					} catch ( Exception e ) {
						actualElements = HashMultiset.create();
						e.printStackTrace();
					}
					double score = 0.;
					if ( actualElements.size() > 0 )
						score = XMLHelper.calulateBagScore( entry.f3, actualElements );
					if ( score > 0. ) {
						score += XMLHelper.cacluateSimilarityScore( entry.f2, mathMLElements.item( i ), qvars );
						//System.out.println( "Found result for " + entry.f0 + "(" + entry.f1 + ") in " + value.f0 );
						FormulaTuple formulaTuple = new FormulaTuple();
						formulaTuple.setScore( score );
						String formulaID = mathMLElements.item( i ).getAttributes().getNamedItem( "id" ).getNodeValue();
						formulaTuple.setXRef( filename + "#" + formulaID );
						formulaTuple.setFor( entry.f1 );
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
						resultTuple.setFor( entry.f0 );

						hitTuple.addFormula( formulaTuple );
						hitTuple.setQueryID( entry.f1 );
						hitTuple.setScore( globalScore );
						hitTuple.setXref( filename );
						resultTuple.addHit( hitTuple );

					}
					globalScore += score;
				}
				if(globalScore>0.){
					for ( int j = 0; j < 3; j++ ) {
						result.setField( value.getField( j ), j );
					}
					result.f3 = resultTuple.getFor();
					result.f4 = filename;//resultTuple.getFor(); //hitTuple.getXRef();
					result.f5 = globalScore;
					result.f6 = new StringValue( hitTuple.toString() );
					out.collect( result );
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			return;
		}
	}


}
