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

import cc.clabs.stratosphere.mlp.types.WikiDocument;
import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.common.stubs.MapStub;
import eu.stratosphere.pact.common.stubs.StubAnnotation.ConstantFields;
import eu.stratosphere.pact.common.stubs.StubAnnotation.OutCardBounds;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.type.base.PactInteger;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rob
 */
@ConstantFields(fields={})
@OutCardBounds(lowerBound=0, upperBound=OutCardBounds.UNBOUNDED)
public class SentenceEmitter extends MapStub {
    
    MaxentTagger tagger = null;
    
    private static final Log LOG = LogFactory.getLog( SentenceEmitter.class );
    
    // initialize reusable mutable objects
    private final PactRecord output = new PactRecord();
    private final PactString text = new PactString();
    private final PactInteger id = new PactInteger();
    private WikiDocument page = null;
   

    @Override
    public void open(Configuration parameter) throws Exception {
      super.open( parameter );
      String model = parameter.getString( "POS-MODEL", "models/wsj-0-18-left3words-distsim.tagger");
      tagger = new MaxentTagger( model );
    }
    
    @Override
    public void map( PactRecord record, Collector<PactRecord> collector ) {
        page = (WikiDocument) record.getField( 0, WikiDocument.class );
        // skip pages from namespaces other than
        if ( page.getNS() != 0 ) return;
        String body = page.getPlainText();
        List<List<HasWord>> sentences = MaxentTagger.tokenizeText( new StringReader( body ) );
        for (List<HasWord> sentence : sentences) {
            ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
            text.setValue( Sentence.listToString(tSentence, false) );
            output.setField( 0, id );
            output.setField( 0, text );
            collector.collect( output );
        }
    }
}
