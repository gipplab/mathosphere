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
import cc.clabs.stratosphere.mlp.types.Word;
import cc.clabs.stratosphere.mlp.types.WikiDocument;
import eu.stratosphere.api.java.record.functions.CoGroupFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;
import java.util.Collections;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author rob
 */
public class CandidateEmitter extends CoGroupFunction{

    private static final Log LOG = LogFactory.getLog( CandidateEmitter.class );

    private StringValue title = null;

    private Identifiers identifiers = null;

    private final static List<String> blacklist = Arrays.asList(
        "behavior", "infinity", "sum", "other",
        "=", "|", "·", "≥", "≤", "≠", "lim", "ƒ",
        "×", "/", "\\", "-",
        "function", "functions",
        "equation", "equations",
        "solution", "solutions",
        "result", "results"
    );

    private Double α;
    private Double β;
    private Double γ;

    @Override
    public void open(Configuration parameter) throws Exception {
      super.open( parameter );
      α = Double.parseDouble( parameter.getString( "α", "1" ) );
      β  = Double.parseDouble( parameter.getString( "β", "1" ) );
      γ = Double.parseDouble( parameter.getString( "γ", "1" ) );
    }


    @Override
    public void coGroup(Iterator<Record> left, Iterator<Record> right, Collector<Record> collector) throws Exception {
        // populating identifier list
        // we'll allways get one record from the left,
        // therefore, we don't need to iterate through
        // left
        identifiers = left.next().getField( 1, WikiDocument.class ).getKnownIdentifiers();
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
            // emit the generated candidate sentences
            for ( Record candidate : generateCandidates( sentences, identifier.getValue() ) ) {
                collector.collect( candidate );
            }
        }
    }

    /**
     * 
     * @param sentences
     * @return 
     */
    private HashMap<String,Integer> generateFrequencies( ArrayList<Sentence> sentences ) {
        HashMap<String,Integer> ω = new HashMap<>();
        Integer count = 0; String w;
        /*                         _
         *                        / |
         *   ____ ___ ___ ______  - |
         *  /  ._|   ) __|  __  ) | |
         * ( () ) | |> _) | || |  | |
         *  \__/   \_)___)|_||_|  |_|
         * calculate the word frequencies for all sentences
         */
        for ( Sentence sentence : sentences ) {
            for ( Word word : sentence ) {
                w = word.getWord().toLowerCase();
                // only count words we're interested in
                if ( filterWord( word ) ) continue;
                count = ω.containsKey( w ) ?
                    ω.get( w ) + 1 : 1;
                ω.put( w, count );
            }
        }
        return ω;
    }

    /**
     *
     * @param sentences
     * @param identifier
     * @return
     */
    private ArrayList<Record> generateCandidates( ArrayList<Sentence> sentences, String identifier  ) {
        ArrayList<Record> candidates = new ArrayList<>();
        ArrayList<Sentence> candidate_sentences = new ArrayList<>();
        for ( Sentence sentence : sentences ) {
            // only analyse sentences that contain the identifier
            if ( !sentence.containsWord( identifier ) ) continue;
            candidate_sentences.add( sentence );
        }
        // calculate the token frequencies for all words in the candidate sentences
        HashMap<String,Integer> ω = generateFrequencies( sentences );
        Integer Ω = Collections.max( ω.values() );
        /*                        ____
         *                       (___ \
         *  ____ ___ ___ ______    __) )
         * /  ._|   ) __|  __  )  / __/
         *( () ) | |> _) | || |  | |___
         * \__/   \_)___)|_||_|  |_____)
         * the kernel step
         */
        Integer index = -1; // will be zero on the first loop
        for ( Sentence sentence : candidate_sentences ) {
            index += 1;
            ArrayList<Integer> positions = sentence.getWordPosition( identifier );
            Integer position = -1; // will be zero on the first loop
            for ( Word word : sentence ) {
                position += 1;
                if ( filterWord( word ) ) continue;
                Integer pmin = getMinimumDistancePosition( position, positions );
                Integer Δ = Math.abs(  pmin - position );
                Integer ω0 = ω.get( word.getWord().toLowerCase() );
                Double score = getScore( Δ, ω0, Ω, index );
                // create a relation object
                Relation relation = new Relation();
                relation.setScore( score );
                relation.setIdentifier( identifier );
                relation.setWordPosition( position );
                relation.setIdentifierPosition( pmin );
                relation.setSentence( sentence );
                relation.setTitle( title );
                // emit the relation
                Record record = new Record();
                record.setField( 0, title );
                record.setField( 1, relation );
                candidates.add( record );
            }
        }
        return candidates;
    }


    /**
     *
     * @param pos
     * @param positions
     * @return
     */
    private Integer getMinimumDistancePosition( Integer pos0, ArrayList<Integer> positions ) {
        Integer Δ, Δmin = Integer.MAX_VALUE,
                min = positions.get( 0 );
        for ( Integer pos1 : positions ) {
            Δ = pos1 - pos0;
            if ( Δmin > Math.abs( Δ ) ) {
                Δmin = Math.abs( Δ );
                min = pos1;
            }
        }
        return min;
    }


    /**
     *
     * @param Δ
     * @param ω
     * @param Ω
     * @param x
     * @return
     */
    private Double getScore( Integer Δ, Integer ω, Integer Ω, Integer x ) {
        Double dist = gaussian( (double) Δ, Math.sqrt( Math.pow( 5d, 2d ) / ( 2d * Math.log( 2 ) ) )  );
        Double seq = gaussian( (double) x, Math.sqrt( Math.pow( 3d, 2d ) / ( 2d * Math.log( 2 ) ) ) );
        Double freq = (double) ω / (double) Ω;
        return ( α * dist + β * seq + γ * freq ) / ( α + β + γ );
    }


    /**
     * Returns the value of the gaussian function
     * at x. σ is the standard deviation.
     *
     * @param x
     * @param σ
     * @return
     */
    private Double gaussian( Double x, Double σ ) {
        return Math.exp(
            - Math.pow( x, 2d ) /
            ( 2d * Math.pow( σ, 2d ) )
        );
    }


    /**
     *
     * @param word
     * @return
     */
    private boolean filterWord( Word word ) {
               // skip the identifier words
        return identifiers.containsIdentifier( word.getWord() ) ||
               // skip blacklisted words
               blacklist.contains( word.getWord() ) ||
               // we're only interested in nouns, entities and links
               !word.getTag().matches( "NN[PS]{0,2}|NP\\+?|NN\\+|LNK" );
    }


}
