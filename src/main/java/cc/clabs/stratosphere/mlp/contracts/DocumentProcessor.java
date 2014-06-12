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
import cc.clabs.stratosphere.mlp.types.WikiDocument;
import eu.stratosphere.api.java.record.functions.MapFunction;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author rob
 */
public class DocumentProcessor extends MapFunction {
        
    private static final Log LOG = LogFactory.getLog( DocumentProcessor.class );
    
    private final StringValue plaintext = new StringValue();
    private final Identifiers list = new Identifiers();
    private final StringValue title = new StringValue();
    private final Record target = new Record();
   
    @Override
    public void map( Record record, Collector<Record> collector ) {
        
        WikiDocument doc = (WikiDocument) record.getField( 0, WikiDocument.class );
        
        // populate the list of known identifiers
        list.clear();
        for ( StringValue var : doc.getKnownIdentifiers() )
            list.add( var );

        // generate a plaintext version of the document
        plaintext.setValue( doc.getPlainText() );

        LOG.info( "Analyzed Page '"+ doc.getTitle() +"' (id: "+ doc.getId() +"), found identifiers: " + list.toString() );
        
        // set the id
        title.setValue( doc.getTitle() );
                
        // finally emit all parts
        target.clear();
        target.setField( 0, title );
        target.setField( 1, doc );
        collector.collect( target );   
    }
}








