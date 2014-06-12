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
package cc.clabs.stratosphere.mlp.types;

import eu.stratosphere.types.ListValue;
import eu.stratosphere.types.StringValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author rob
 */
public class Sentence extends ListValue<Word> implements Cloneable {
    
    /**
     * 
     * @param word
     * @return 
     */
    public ArrayList<Integer> getWordPosition( String word ) {
        ArrayList<Integer> positions = new ArrayList<>();
        String token;
        Integer pos = -1;
        Iterator<Word> it = this.iterator();
        while ( it.hasNext() ) {
            pos += 1;
            token = it.next().getWord();
            if ( token.equals( word ) )
                positions.add( pos );
        }
        return positions;        
    }
    
    
    /**
     *
     * @return 
     */
    public boolean containsWord( String word ) {
        return !getWordPosition( word ).isEmpty();
    }

    /**
     * 
     * @param word
     * @return 
     */
    public boolean containsWord( StringValue word ) {
        return containsWord( word.getValue() );
    }
    
    /**
     * 
     * @param word
     * @return 
     */
    public boolean containsWord( Word word ) {
        return containsWord( word.getWord() );
    }
    
  
    
    @Override
    public Object clone() {
        Sentence obj = new Sentence();
        obj.addAll( this );
        return obj;
    }
    
    @Override
    public String toString() {
        return toJSON().toString();
        // String buffer = "";
        // Iterator<PactWord> it = this.iterator();
        // while ( it.hasNext() ) buffer += it.next().toString() + " ";
        // return buffer;
    }
    
    public JSONObject toJSON () {
        JSONArray words = new JSONArray();
        Iterator<Word> it = this.iterator();
        while ( it.hasNext() ) words.put( it.next().toJSON() );
        Map<String,Object> json = new HashMap<>();
        json.put( "words", words );
        return new JSONObject( json );
    }
    
}
