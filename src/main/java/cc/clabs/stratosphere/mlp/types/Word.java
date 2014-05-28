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

import edu.stanford.nlp.ling.TaggedWord;
import eu.stratosphere.types.Key;
import eu.stratosphere.types.StringValue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author rob
 */
public class Word implements Key {
    
    
    /*
     * 
     */
    private StringValue word = new StringValue();
    
    /*
     * 
     */
    private StringValue tag = new StringValue();
    
    /**
     * default constructor
     */
    public Word() { }
    
    
    /**
     * 
     * @param word
     * @param tag 
     */
    public Word( final String word, final String tag ) {
        setWord( word );
        setTag( tag );
    }
    
    /**
     * Constructor for Word. Replaces some odd conversions
     * from the Stanford Tagger.
     * 
     * @param word a TaggedWord (@see edu.stanford.nlp.ling.TaggedWord)
     */
    public Word( TaggedWord word ) {
        setWord( word.value() );
        setTag( word.tag() );
    }
    
    /**
     * Returns this Word as a TaggedWord from the Stanford
     * NLP Project (@see edu.stanford.nlp.ling.TaggedWord).
     * 
     * @return a TaggedWord
     */
    public TaggedWord getTaggedWord() {
        return new TaggedWord( word.getValue(), tag.getValue() );
    }
    
    /**
     * Returns the string representation of the word.
     * 
     * @return the string representation of the word
     */
    public String getWord() {
        return word.getValue();
    }
    
    /**
     * Sets the value of the word.
     * 
     * @param string the string representation of the word
     */
    public final void setWord( final String string ) {
        String v = string;
        switch ( v ) {
            case "-LRB-":
                v = "(";
                break;
            case "-RRB-":
                v = ")";
                break;
            case "-LCB-":
                v = "{";
                break;
            case "-RCB-":
                v = "}";
                break;
            case "-LSB-":
                v = "[";
                break;
            case "-RSB-":
                v = "]";
                break;
            case "``":
                v = "\"";
                break;
            case "''":
                v = "\"";
                break;
            case "--":
                v = "-";
                break;
        }
        word.setValue( v );
    }
    
    /**
     * Returns the assigned tag value.
     * 
     * @return the assigned tag value
     */
    public String getTag() {
        return tag.getValue();
    }
    
    /**
     * Sets the tag to a given string value.
     * 
     * @param string a given string value
     */
    public final void setTag( final String string ) {
        String t = string;
        switch ( t ) {
            case "``":
                t = "\"";
                break;
            case "''":
                t = "\"";
                break;
        }
        tag.setValue( string );
    }

    @Override
    public void write( final DataOutput out ) throws IOException {
        word.write( out );
        tag.write( out );
    }

    @Override
    public void read( final DataInput in ) throws IOException {
        word.read( in );
        tag.read( in );
    }

    @Override
    public int compareTo( Key o ) {
        Word other = (Word) o;
        if (  this.word.equals( other.word ) &&  this.tag.equals( other.tag ) )
            return 0;
        else
            return this.word.compareTo( other.word );
    }
    
    @Override
    public String toString() {
        return toJSON().toString();
        // return this.getWord() + "(" + this.getTag() + ")";
    }

    
    public JSONObject toJSON () {
        Map<String, Object> json = new HashMap<>();
        json.put( "word", getWord() );
        json.put( "tag", getTag() );
        return new JSONObject( json );
    }
}
