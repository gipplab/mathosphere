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

import eu.stratosphere.types.DoubleValue;
import eu.stratosphere.types.IntValue;
import eu.stratosphere.types.StringValue;
import eu.stratosphere.types.Key;
import org.json.JSONObject;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rob
 */
public class Relation implements Key<Object>, Cloneable {

    private DoubleValue score = new DoubleValue();
    private IntValue iposition = new IntValue();
    private IntValue wposition = new IntValue();
    private StringValue identifier = new StringValue();
    private Sentence sentence = new Sentence();
    private StringValue title = new StringValue();
    
    /**
     * 
     * @return 
     */
    public DoubleValue getScore() {
        return score;
    }
    
    
    /**
     * 
     * @param score 
     */
    public void setScore( DoubleValue score ) {
        this.score = score;
    }
    
    
    /**
     * 
     * @param score 
     */
    public void setScore( Double score ) {
        this.score = new DoubleValue( score );
    }
    
    
    public String getDefinitionWord() {
        return ( wposition != null ) ?
            sentence.get( wposition.getValue() ).getWord() :
            null;
    }
    
    
    /**
     * 
     * @return 
     */
    public StringValue getIdentifier() {
        return identifier;
    }
    
    
    /**
     * 
     * @param identifier 
     */
    public void setIdentifier( StringValue identifier ) {
        this.identifier = identifier;
    }
    
    
    /**
     * 
     * @param identifier 
     */
    public void setIdentifier( String identifier ) {
        setIdentifier( new StringValue( identifier ) );
    }
    
    
    /**
     * 
     * @return 
     */
    public IntValue getIdentifierPosition() {
        return iposition;
    }
    
    
    /**
     * 
     * @param position 
     */
    public void setIdentifierPosition( IntValue position ) {
        this.iposition = position;
    }
    
    
    /**
     * 
     * @param position 
     */
    public void setIdentifierPosition( Integer position ) {
        setIdentifierPosition( new IntValue( position ) );
    }
    
    
    /**
     * 
     * @return 
     */
    public IntValue getWordPosition() {
        return wposition;
    }
    
    
    /**
     * 
     * @param position 
     */
    public void setWordPosition( IntValue position ) {
        this.wposition = position;
    }
    
    
    /**
     * 
     * @param position 
     */
    public void setWordPosition( Integer position ) {
        setWordPosition( new IntValue( position ) );
    }
    
    
    /**
     * 
     * @return 
     */
    public Sentence getSentence() {
        return sentence;
    }
    
    
    /**
     * 
     * @param sentence 
     */
    public void setSentence( Sentence sentence ) {
        this.sentence = sentence;
    }

    
    /**
     * 
     * @return 
     */
    public StringValue getTitle () {
        return this.title;
    }
    
    public void setTitle( StringValue title ) {
        this.title = title;
    }
    
    public void setTitle( String title ) {
        this.title = new StringValue( title );
    }
    
    @Override
    public void write( DataOutput out ) throws IOException {
        iposition.write( out );
        identifier.write( out );
        wposition.write( out );
        sentence.write( out );
        score.write( out );
        title.write( out );
    }

    @Override
    public void read( DataInput in ) throws IOException {
        iposition.read( in );
        identifier.read( in );
        wposition.read( in );
        sentence.read( in );
        score.read( in );
        title.read( in );
    }


    @Override
    public String toString() {
        String s = toJSON().toString();
        if ( s == null ) {
            return "";
        } else {
            return s;
        }
        //String word = ((Word) sentence.get( wposition.getValue() )).getWord();
        //return String.format( "%-18.18s | %-2.2s | %-5f | %-18.18s | %s",
        //    title.getValue(),
        //    identifier.getValue(),
        //    score.getValue(),
        //    word,
        //    sentence.toString() );
    }
    
    public JSONObject toJSON () {
        String word;
        try {
            word = ((Word) sentence.get( wposition.getValue() )).getWord();
        } catch ( Exception e ) {
            word = "";
        }
        Map<String,Object> json = new HashMap<>();
        json.put( "page", title.getValue() );
        json.put( "identifier", identifier.getValue() );
        json.put( "score", score.getValue() );
        json.put( "word", word );
        json.put( "identifier_position", iposition.getValue() );
        json.put( "definition_position", wposition.getValue() );
        json.put( "sentence", sentence.toJSON() );
        return new JSONObject( json );
    }
    
    @Override
    public Object clone() {
        Relation obj = new Relation();
        obj.setIdentifier( new StringValue( identifier.getValue() ) );
        obj.setIdentifierPosition( new IntValue( iposition.getValue() ) ) ;
        obj.setScore( new DoubleValue( score.getValue() ) );
        obj.setSentence( (Sentence) sentence.clone() );
        obj.setTitle( new StringValue( title.getValue() ) );
        obj.setWordPosition( new IntValue( wposition.getValue() ) );
        return obj;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p/>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p/>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p/>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p/>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p/>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Object o) {
        Relation other = (Relation) o;
        // only and only if the identifier and the
        // sentences are equal, consider the relations
        // natural order as equal.
        if ( identifier.equals(other.getIdentifier() ) )
            if ( this.sentence.equals( other.getSentence() ) )
                return 0;
        // otherwise use the ordering derived from the
        // identifer
        return identifier.compareTo( other.identifier );
    }
}
