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

import cc.clabs.stratosphere.mlp.types.Identifiers;
import cc.clabs.stratosphere.mlp.types.Relation;
import cc.clabs.stratosphere.mlp.types.Sentence;
import cc.clabs.stratosphere.mlp.types.WikiDocument;
import cc.clabs.stratosphere.mlp.utils.SentenceUtils;
import cc.clabs.stratosphere.mlp.utils.SentenceUtils.Tuple;
import eu.stratosphere.api.java.record.functions.CoGroupFunction;

import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author rob
 */
public class PatternMatcher extends CoGroupFunction{

    private static final Log LOG = LogFactory.getLog( PatternMatcher.class );
    private StringValue title = null;
    
    @Override
    public void coGroup(Iterator<Record> left, Iterator<Record> right, Collector<Record> collector) throws Exception {
        // left: Doc
        // right: Sentences
        // populating identifier list
        // we'll allways get one record from the left,
        // therefore, we don't need to iterate through
        // left
        Identifiers identifiers = left.next().getField( 1, WikiDocument.class ).getKnownIdentifiers();
        // populating sentences list
        ArrayList<Sentence> sentences =  new ArrayList<>();
        while ( right.hasNext() ) {
            Record next = right.next();
            // title should always be the same
            title = next.getField( 0, StringValue.class );
            // we need to clone the sentence objects, because of reused objects
            sentences.add( (Sentence) next.getField( 1, Sentence.class ).clone() );
        }
        
        for ( StringValue identifier : identifiers ) {
            String id = identifier.getValue();
            ArrayList<Tuple<String,Tuple<Integer,Integer>>> patterns = new ArrayList<>();
            
            // is
            patterns.add( createPattern( "%identifier%	is	%definition%", id, 0, 2 ) );
            // is the
            patterns.add( createPattern( "%identifier%	is	the	%definition%", id, 0, 3 ) );
            // let be
            patterns.add( createPattern( "let	%identifier%	be	the	%definition%", id, 1, 4 ) );
            // denoted by
            patterns.add( createPattern( "%definition%	is|are	denoted	by	%identifier%", id, 4, 0 ) );
            // denotes
            patterns.add( createPattern( "%identifier%	denotes	(DT)	%identifier%", id, 0, 3 ) );
            // zero
            patterns.add( createPattern( "%definition%	%identifier%", id, 1, 0 ) );

            for ( Sentence sentence : sentences ) {
                // only care about sentences the identifier is contained in
                if ( !sentence.containsWord( identifier ) ) continue;
                // search for each pattern
                for ( Tuple<String,Tuple<Integer,Integer>> pattern : patterns ) {
                    String patternstring = pattern.first;
                    Integer iOffset = pattern.second.first;
                    Integer dOffset = pattern.second.second;
                    Integer index = SentenceUtils.findByPattern( sentence, patternstring );
                    if ( index >= 0 ) {
                        // pattern found
                        Relation relation = new Relation();
                        System.out.println( sentence);
                        relation.setIdentifier( identifier );
                        relation.setIdentifierPosition( index + iOffset );
                        relation.setWordPosition( index + dOffset );
                        relation.setScore( 1d );
                        relation.setSentence( sentence );
                        relation.setTitle( title );
                        // emit relation
                        Record record = new Record();
                        record.setField( 0, title );
                        record.setField( 1, relation );
                        collector.collect( record );
                    }
                }
                
                
            }
        }
   
    }
    
    /**
     * 
     * @param pattern
     * @param identifier
     * @param iOffset
     * @param dOffset
     * @return 
     */
    private Tuple<String,Tuple<Integer,Integer>> createPattern( String pattern, String identifier, Integer iOffset, Integer dOffset ) {
        String definition = "(NN[PS]{0,2}|NP\\+?|NN\\+|LNK)";
        pattern = pattern.replaceAll( "%identifier%", identifier );
        pattern = pattern.replaceAll( "%definition%", definition );
        return new Tuple( pattern, new Tuple( iOffset, dOffset ) );
    }


}
