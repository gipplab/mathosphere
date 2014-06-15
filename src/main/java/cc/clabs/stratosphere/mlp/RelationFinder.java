/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package cc.clabs.stratosphere.mlp;

import cc.clabs.stratosphere.mlp.contracts.*;
import cc.clabs.stratosphere.mlp.io.WikiDocumentEmitter;
import cc.clabs.stratosphere.mlp.types.Relation;
import cc.clabs.stratosphere.mlp.types.WikiDocument;
import eu.stratosphere.api.common.Plan;
import eu.stratosphere.api.common.Program;
import eu.stratosphere.api.common.ProgramDescription;
import eu.stratosphere.api.common.operators.Order;
import eu.stratosphere.api.common.operators.Ordering;
import eu.stratosphere.api.java.record.io.CsvOutputFormat;
import eu.stratosphere.api.java.record.operators.*;
import eu.stratosphere.client.LocalExecutor;
import eu.stratosphere.types.DoubleValue;
import eu.stratosphere.types.StringValue;

import java.util.ArrayList;
import java.util.Collection;

public class RelationFinder implements Program, ProgramDescription {

    /**
    * {@inheritDoc}
    */
    @Override
    public Plan getPlan( String... args ) {
        // parse job parameters
        String dataset = args[0];
        String outputdir = args[1];
        String model = args[2];
        
        String alpha = args[3];
        String beta  = args[4];
        String gamma = args[5];
        String threshold = args[6];

        
        FileDataSource source = new FileDataSource( WikiDocumentEmitter.class, dataset, "Dumps" );
        
        MapOperator doc = MapOperator
                .builder( DocumentProcessor.class )
                .name( "Processing Documents" )
                .input( source )
                .build();

        
        MapOperator sentences = MapOperator
                .builder( SentenceEmitter.class )
                .name( "Sentences" )
                .input( doc )
                .build();
        sentences.setParameter( "POS-MODEL", model );
        
        CoGroupOperator candidates = CoGroupOperator
                .builder( CandidateEmitter.class, StringValue.class, 0, 0 )
                .name( "Candidates" )
                .input1( doc )
                .input2( sentences )
                .build();
        // order sentences by their position within the document
        candidates.setGroupOrderForInputTwo( new Ordering( 2, DoubleValue.class, Order.ASCENDING ) );
        // set the weighting factors
        candidates.setParameter( "α", alpha );
        candidates.setParameter( "β", beta );
        candidates.setParameter( "γ", gamma );


        ReduceOperator filter = ReduceOperator
                .builder( FilterCandidates.class, StringValue.class, 0)
                .name( "Filter" )
                .input( candidates )
                .build();
        // order candidates by the identifier
        filter.setGroupOrder( new Ordering( 1, Relation.class, Order.ASCENDING ) );
        // sets the minimum threshold for a candidate's score
        filter.setParameter( "THRESHOLD", threshold );

        
        CoGroupOperator patterns = CoGroupOperator
                .builder( PatternMatcher.class, StringValue.class, 0, 0 )
                .name( "Pattern Matcher" )
                .input1( doc  )
                .input2( sentences )
                .build();
        
        CoGroupOperator evaluator = CoGroupOperator
                .builder( AccuracyEvaluator.class, StringValue.class, 0, 0 )
                .name( "Accuracy Evaluator" )
                .input1( patterns )
                .input2(candidates )
                .build();
        
        ReduceOperator aggregator = ReduceOperator
                .builder( AccuracyAggregator.class, StringValue.class, 0 )
                .name( "Accuracy Aggregator" )
                .input( evaluator )
                .build();
        
        FileDataSink out1 = new FileDataSink( CsvOutputFormat.class, outputdir+"/pages.jsonp", doc, "Output Pages" );
        CsvOutputFormat.configureRecordFormat( out1 )
                .recordDelimiter( '\n' )
                .fieldDelimiter( ' ' )
                .field( WikiDocument.class, 1 );
        // needs to be set to one
        out1.setParameter( "pact.output.record.num-fields", 1 );
        
        FileDataSink out2 = new FileDataSink( CsvOutputFormat.class, outputdir+"/relations.jsonp", filter, "Output Relations" );
        CsvOutputFormat.configureRecordFormat( out2 )
                .recordDelimiter( '\n' )
                .fieldDelimiter( ' ' )
                .field( Relation.class, 0 );
        
        FileDataSink out3 = new FileDataSink( CsvOutputFormat.class, outputdir+"/evaluation.csv", evaluator, "Output Evaluation" );
        CsvOutputFormat.configureRecordFormat( out3 )
                .recordDelimiter( '\n' )
                .fieldDelimiter( '\t' )
                .field( DoubleValue.class, 0 )
                .field( DoubleValue.class, 1 )
                .field( DoubleValue.class, 2 )
                .field( DoubleValue.class, 3 )
                .field( DoubleValue.class, 4 )
                .field( DoubleValue.class, 5 )
                .field( StringValue.class, 6 );
        
        Collection<FileDataSink> sinks = new ArrayList<>();
        sinks.add( out1 );
        sinks.add( out2 );
        sinks.add( out3 );
        return new Plan( sinks, "Relation Finder" );
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getDescription() {
        return "Parameters: [DATASET] [OUTPUT] [MODEL] [ALPHA] [BETA] [GAMMA] [THRESHOLD]";
    }
    
    public static void main(String[] args) throws Exception {
        RelationFinder rf = new RelationFinder();
        Plan plan = rf.getPlan( args );
        LocalExecutor.execute(plan);
    }
    
}
