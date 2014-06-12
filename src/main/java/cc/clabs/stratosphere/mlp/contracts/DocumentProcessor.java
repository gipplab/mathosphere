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
import cc.clabs.stratosphere.mlp.utils.StringUtils;
import eu.stratosphere.api.java.record.functions.MapFunction;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.util.Collector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rob
 */
public class DocumentProcessor extends MapFunction {
        
    private static final Log LOG = LogFactory.getLog( DocumentProcessor.class );
    
    private final StringValue plaintext = new StringValue();
    private final Identifiers list = new Identifiers();
    private final StringValue docTitle = new StringValue();
    private final Record target = new Record();
   
    @Override
    public void map( Record record, Collector<Record> collector ) {

            String content = record.getField( 0, StringValue.class ).getValue();

            Matcher m;
            Pattern titleRegexp = Pattern.compile( "(?:<title>)(.*?)(?:</title>)" );
            Pattern nsRegexp = Pattern.compile( "(?:<ns>)(.*?)(?:</ns>)" );
            Pattern idRegexp = Pattern.compile( "(?:<revision>.*?<id>)(\\d+)(?:</id>)", Pattern.DOTALL );
            Pattern textRegexp = Pattern.compile( "(?:<text.*?>)(.*?)(?:</text>)", Pattern.DOTALL );

            // parse title
            m = titleRegexp.matcher( content ); if ( !m.find() ) return;
            String title = m.group( 1 );
            // parse namespace
            m = nsRegexp.matcher( content ); if ( !m.find() ) return;
            Integer ns = Integer.parseInt( m.group( 1 ) );
            // parse revision id
            m = idRegexp.matcher( content ); if ( !m.find() ) return;
            Integer id = Integer.parseInt( m.group( 1 ) );
            // parse text
            m = textRegexp.matcher( content ); if ( !m.find() ) return;
            String text = StringUtils.unescapeEntities(m.group(1));

            // otherwise create a WikiDocument object from the xml
            WikiDocument doc = new WikiDocument();
            doc.setId( id );
            doc.setTitle( title );
            doc.setNS( ns );
            doc.setText( text );

            // skip docs from namespaces other than 0
            if ( doc.getNS() != 0 ) return;
        
        // populate the list of known identifiers
        list.clear();
        for ( StringValue var : doc.getKnownIdentifiers() )
            list.add( var );

        // generate a plaintext version of the document
        plaintext.setValue( doc.getPlainText() );

        LOG.info( "Analyzed Page '"+ doc.getTitle() +"' (id: "+ doc.getId() +"), found identifiers: " + list.toString() );
        
        // set the id
        docTitle.setValue( doc.getTitle() );
                
        // finally emit all parts
        target.clear();
        target.setField( 0, docTitle );
        target.setField( 1, doc );
        collector.collect( target );   
    }
}








