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
package cc.clabs.stratosphere.mlp.io;

import cc.clabs.stratosphere.mlp.types.WikiDocument;
import cc.clabs.stratosphere.mlp.utils.StringUtils;
import eu.stratosphere.api.java.record.io.TextInputFormat;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.types.Record;
import eu.stratosphere.types.StringValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rob
 */
public class WikiDocumentEmitter extends TextInputFormat {

    @Override
    public void configure (Configuration parameter) {
    	parameter.setString(DEFAULT_CHARSET_NAME, "UTF-8");
        super.configure( parameter );
        this.setDelimiter("</page>");
    }
    
    /* (non-Javadoc)
     * @see eu.stratosphere.pact.common.io.DelimitedInputFormat#readRecord(eu.stratosphere.pact.common.type.Record, byte[], int)
     */
    @Override
    public boolean readRecord(Record target, byte[] bytes, int offset, int numBytes) {
     
        super.readRecord( target, bytes, offset, numBytes );
        String content = target.getField( 0, StringValue.class ).getValue();
        
        Matcher m;
        Pattern titleRegexp = Pattern.compile( "(?:<title>)(.*?)(?:</title>)" );
        Pattern nsRegexp = Pattern.compile( "(?:<ns>)(.*?)(?:</ns>)" );
        Pattern idRegexp = Pattern.compile( "(?:<revision>.*?<id>)(\\d+)(?:</id>)", Pattern.DOTALL );
        Pattern textRegexp = Pattern.compile( "(?:<text.*?>)(.*?)(?:</text>)", Pattern.DOTALL );
        
        // parse title
        m = titleRegexp.matcher( content ); if ( !m.find() ) return false;
        String title = m.group( 1 );
        // parse namespace
        m = nsRegexp.matcher( content ); if ( !m.find() ) return false;
        Integer ns = Integer.parseInt( m.group( 1 ) );
        // parse revision id
        m = idRegexp.matcher( content ); if ( !m.find() ) return false;
        Integer id = Integer.parseInt( m.group( 1 ) );
        // parse text
        m = textRegexp.matcher( content ); if ( !m.find() ) return false;
        String text = StringUtils.unescapeEntities( m.group( 1 ) );
        
        // otherwise create a WikiDocument object from the xml
        WikiDocument doc = new WikiDocument();
        doc.setId( id );
        doc.setTitle( title );
        doc.setNS( ns );
        doc.setText( text );
        
        // skip docs from namespaces other than 0
        if ( doc.getNS() != 0 ) return false;
        
        target.setField( 0, doc );
        return true;
    }

}