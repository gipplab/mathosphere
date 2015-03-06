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
import cc.clabs.stratosphere.mlp.types.Sentence;
import cc.clabs.stratosphere.mlp.types.Word;
import cc.clabs.stratosphere.mlp.types.WikiDocument;
import cc.clabs.stratosphere.mlp.utils.SentenceUtils;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import edu.stanford.nlp.util.CoreMap;
import eu.stratosphere.api.java.record.functions.MapFunction;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.types.DoubleValue;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;

import java.util.List;
import java.util.Properties;

/**
 *
 * @author rob
 */
public class SentenceEmitter extends MapFunction {

    /**
     * Stanford NLP Core related stuff
     */
    StanfordCoreNLP pipeline;
    Properties props = new Properties();
    
    /**
     *
     */
    private final Record target = new Record();


    @Override
    public void open(Configuration parameter) throws Exception {
      super.open( parameter );

      props.put( "annotators", "tokenize, ssplit, pos" );
      props.put( "pos.model", parameter.getString( "POS-MODEL", "models/wsj-0-18-left3words-distsim.tagger" ) );
      props.put( "tokenize.options",
          "untokenizable=firstKeep"+
          ",strictTreebank3=true"+
          ",ptb3Escaping=true"+
          ",escapeForwardSlashAsterisk=false"
      );
      props.put( "ssplit.newlineIsSentenceBreak", "two" );
      pipeline = new StanfordCoreNLP( props );
    }


    @Override
    public void map( Record record, Collector<Record> collector ) throws Exception {
        target.clear();
        // field 0 remains the same (title of the document)
        target.setField( 0, record.getField( 0, StringValue.class ) );
        WikiDocument doc = record.getField( 1, WikiDocument.class );
        Identifiers identifiers = doc.getKnownIdentifiers();
        String plaintext = doc.getPlainText();
        // create an empty Annotation just with the given plaintext
        Annotation document = new Annotation( plaintext );
        // run all annotators
        pipeline.annotate( document );
        List<CoreMap> sentences = (List<CoreMap>) document.get( SentencesAnnotation.class );
        Integer position = -1;
        for(CoreMap sentence: sentences) {
          position += 1;
          Sentence ps = new Sentence();
          for ( CoreLabel token: sentence.get( TokensAnnotation.class ) ) {
            String word = token.get( TextAnnotation.class );
            // set the pos tag manually if the word
            // is contained in the list of known identifiers
            String pos = ( identifiers.containsIdentifier( word ) ) ?
                    "ID" : token.get( PartOfSpeechAnnotation.class );
            ps.add( new Word( word, pos ) );
          }
          // parse tree of the current sentence
          //Tree tree = sentence.get( TreeAnnotation.class );
          // dependency graph of the current sentence
          //SemanticGraph dependencies = sentence.get( CollapsedCCProcessedDependenciesAnnotation.class );
          // postprocess the sentence
          ps = postprocessSentence( ps );
          // emit the final sentence
          target.setField( 1, ps );
          target.setField( 2, new DoubleValue( (double) position / (double) sentences.size() ) );
          collector.collect( target );
        }
    }
    
    private Sentence postprocessSentence(Sentence sentence) {
        // links
        sentence = SentenceUtils.joinByTagPattern( sentence, "`` * ''", "LNK" );
        // trim links
        sentence = SentenceUtils.replaceAllByTag( sentence, "LNK", "^\\s*[\"`']+\\s*|\\s*[\"`']+\\s*$", "" );
        // change tag type for formulas
        sentence = SentenceUtils.replaceAllByPattern( sentence, "FORMULA", "MATH" );
        // for some reason some signs will be tagged JJ or NN, need to fix this
        sentence = SentenceUtils.replaceAllByPattern( sentence, "<|=|>|≥|≤|\\|\\/|\\[|\\]", "SYM" );
        // aggregate noun phrases
        sentence = SentenceUtils.joinByTagPattern( sentence, "NN +", "NN+" );
        sentence = SentenceUtils.joinByTagPattern( sentence, "JJ NN", "NP" );
        sentence = SentenceUtils.joinByTagPattern( sentence, "JJ NN+", "NP+" );
        return sentence;
    }
}
