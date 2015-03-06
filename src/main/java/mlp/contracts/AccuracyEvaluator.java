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
package cc.clabs.stratosphere.mlp.contracts;

import cc.clabs.stratosphere.mlp.types.Relation;
import eu.stratosphere.api.java.record.functions.CoGroupFunction;
import eu.stratosphere.types.DoubleValue;

import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author rob
 */
public class AccuracyEvaluator extends CoGroupFunction{

    private static final Log LOG = LogFactory.getLog( AccuracyEvaluator.class );

    @Override
    public void coGroup(Iterator<Record> left, Iterator<Record> right, Collector<Record> collector) throws Exception {
        StringValue title = new StringValue();
        
        // Relations from PatternMatcher
        HashMap<String,ArrayList<Relation>> PM = new HashMap<>();
        while ( left.hasNext() ) {
            Record next = left.next();
            // will always be the same
            title = next.getField( 0, StringValue.class );
            // we need to clone the sentence objects, because of reused objects
            Relation relation = (Relation) next.getField( 1, Relation.class ).clone();
            String identifier = relation.getIdentifier().getValue();
            if ( !PM.containsKey( identifier ) ) {
                PM.put( identifier, new ArrayList<Relation>() );
            }
            PM.get( identifier ).add( relation );
        }
        // right: Relations from CandidateEmitter
        HashMap<String,ArrayList<Relation>> MLP = new HashMap<>();
        while ( right.hasNext() ) {
            Record next = right.next();
            // we need to clone the sentence objects, because of reused objects
            Relation relation = (Relation) next.getField( 1, Relation.class ).clone();
            String identifier = relation.getIdentifier().getValue();
            if ( !MLP.containsKey( identifier ) ) {
                MLP.put( identifier, new ArrayList<Relation>() );
            }
            MLP.get( identifier ).add( relation );
        }
        
        Double numRelationsPM = 0d;
        Double numRelationsMLP = 0d;
        Double numBest = 0d;
        Double above90 = 0d;
        Double above80 = 0d;
        Double above70 = 0d;
        Double above60 = 0d;
        
        for ( Entry<String,ArrayList<Relation>> set : PM.entrySet() ) {
            String id = set.getKey();
            ArrayList<Relation> relationsPM = set.getValue();
            // add number of relations found by PM
            numRelationsPM += (double) relationsPM.size();
            // has the identifier been found in MLP?
            if ( MLP.containsKey( id ) ) {
                ArrayList<Relation> relationsMLP = MLP.get( id );
                // sort by score
                Collections.sort( relationsMLP, new RelationComparator() );
                // descending
                Collections.reverse( relationsMLP );
                // for each relation from PM
                for ( Relation rPM : relationsPM ) {
                    String word = rPM.getDefinitionWord();
                    // best matches are equal?
                    if ( relationsMLP.get( 0 ).getDefinitionWord().equals( rPM.getDefinitionWord() ) ) {
                        numBest += 1d;
                    }
                    // iterate through the MLP relation
                    for ( Relation rMLP : relationsMLP ) {
                        // only match equal definitions
                        if ( !rMLP.getDefinitionWord().equals( rPM.getDefinitionWord() ) )
                            // GOTO FAIL!
                            continue;
                        numRelationsMLP += 1d;
                        // and gather some statistics
                        Double score = rMLP.getScore().getValue();
                        if ( score >= 0.9d ) above90 += 1d;
                        if ( score >= 0.8d ) above80 += 1d;
                        if ( score >= 0.7d ) above70 += 1d;
                        if ( score >= 0.6d ) above60 += 1d;
                        // thus we're only interested in the best matches
                        // we can break the iteration
                        break;
                    }
                }
                
            } else {
                // MLP did not find this one :(
            }
        }
        
        Record record = new Record();
        // detection rate (overall)
        record.setField( 0, new DoubleValue( numRelationsMLP / numRelationsPM ) );
        // detection rate (best)
        record.setField( 1, new DoubleValue( numBest / numRelationsPM ) );
        // detection rate (90)
        record.setField( 2, new DoubleValue( above90 / numRelationsPM ) );
        // detection rate (80)
        record.setField( 3, new DoubleValue( above80 / numRelationsPM ) );
        // detection rate (70)
        record.setField( 4, new DoubleValue( above70 / numRelationsPM ) );
        // detection rate (60)
        record.setField( 5, new DoubleValue( above60 / numRelationsPM ) );
        // title
        record.setField( 6, title );
        collector.collect( record );
        
    }
    
    
    public class RelationComparator implements Comparator<Relation> {
        @Override
        public int compare(Relation r1, Relation r2) {
            return r1.getScore().compareTo( r2.getScore() );
        }
    }


}
