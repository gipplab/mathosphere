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
package cc.clabs.stratosphere.mlp.tests;

import cc.clabs.stratosphere.mlp.types.Sentence;
import cc.clabs.stratosphere.mlp.types.Word;

import cc.clabs.stratosphere.mlp.utils.SentenceUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rob
 */
public class SentenceUtilsTest {
    
    @Test
    public void joinsByPatterns () {
        
        Sentence sentence = new Sentence();
        sentence.add( new Word( "my", "PP" ) );
        sentence.add( new Word( "fantastic", "JJ" ) );
        sentence.add( new Word( "cat", "NNS" ) );
        sentence.add( new Word( "keyboards", "NNP" ) );
        sentence.add( new Word( "!", "SYM" ) );
        
        Sentence s1 = SentenceUtils.joinByTagPattern( sentence, "NN +", "ENTITY" );
        assertEquals( "Sequences of nouns should be joined together", "cat keyboards", s1.get( 2 ).getWord() );
        assertEquals( "Joined noun sequences sould have the tag ENTITY", "ENTITY", s1.get( 2 ).getTag() );

        Sentence s2 = SentenceUtils.joinByTagPattern( sentence, "PP * NN", "TRIPLE" );
        assertEquals( "Wildcard chars are not greedy", "my fantastic cat", s2.get( 0 ).getWord() );
        
    }
}
