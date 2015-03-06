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
package cc.clabs.stratosphere.mlp.utils;

import cc.clabs.stratosphere.mlp.types.Sentence;
import cc.clabs.stratosphere.mlp.types.Word;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author rob
 */
public class SentenceUtils {
    
    private static final Log LOG = LogFactory.getLog( SentenceUtils.class );

    
    public final static String WILDCARD = "*";
    public final static String MORE = "+";
        
    public static Sentence replaceAllByPattern( Sentence sentence, String regex, String withTag ) {
        for ( Word word: sentence )
            if ( word.getWord().matches( regex ) )
                word.setTag( withTag );
        return sentence;
    }
    
    public static Sentence replaceAllByTag( Sentence sentence, String tag, String regex, String replacement ) {
        for ( Word word : sentence )
            // skip other words than those with a specific tag
            if ( word.getTag().equals( tag ) ) {
                String text = word.getWord();
                text = text.replaceAll( regex, replacement );
                word.setWord( text );
            }
        return sentence;
    }

    /**
     * Joins a set of PactWords, described by a pattern,
     * together with a given tag into a new Word within
     * the sentence. A pattern is a string containing tags
     * and wildcards where every part is separated by a space
     * character.
     * 
     * @param sentence  the sentence
     * @param pattern   a string containing the pattern
     * @param withTag   tag given to new PactWords
     * 
     * @return  a new sentence with the joined PactWords
     */
    public static Sentence joinByTagPattern( Sentence sentence, String pattern, String withTag ) {
        
        Sentence result = new Sentence();
        String[] tags = pattern.trim().split( " " );
        
        // nothing to compare to
        if (  tags.length == 0 ) return sentence;
        // pattern can never be matched
        if ( tags.length > sentence.size() ) return sentence;        
        
        
        int ptr = 0;
        boolean isRangeTag = false;
        String curTag = "", nexTag = "", prevTag = "", poo = "\uD83D\uDCA9";
        ArrayList<Word> candidates = new ArrayList<>();
        
        // for every word …
        for ( Word word : sentence ) {
            // get the tags form tags array
            prevTag = ( ptr - 1 >= 0 ) ? tags[ ptr - 1 ] : poo;
            curTag = ( ptr < tags.length ) ? tags[ ptr ] : poo;
            nexTag = ( ptr + 1 < tags.length  ) ? tags[ ptr + 1 ] : poo;
            // is range tag?
            isRangeTag = curTag.equals( WILDCARD ) || curTag.equals( MORE );
            if ( isRangeTag ) {
                // current tag is wildcard symbol
                if ( curTag.equals( WILDCARD ) ) {
                    // when the tag of the next word matches
                    if ( word.getTag().contains( nexTag ) ) {
                        candidates.add( word );
                        result.add( new Word( joinPactWords( candidates ), withTag ) );
                        candidates.clear();
                        ptr += 1;
                    // otherwise add the current word to the concat
                    } else {
                        // concatenate the current word
                        candidates.add( word );
                    }                
                }
                // current tag is a more symbol
                else if ( curTag.equals( MORE ) ) {
                    // when the tag of the next word matches
                    if ( word.getTag().contains( nexTag ) ) {
                        candidates.add( word );
                        result.add( new Word( joinPactWords( candidates ), withTag ) );
                        candidates.clear();
                        ptr += 2;
                    // otherwise check if the previous tag matches
                    } else if ( word.getTag().contains( prevTag ) ) {
                        candidates.add( word );
                    // when nothing matches flush everything to the results
                    } else {
                        // at least two hits need to be found
                        if ( candidates.size() > 1 ) {
                            result.add( new Word( joinPactWords( candidates ), withTag ) );
                        } else {
                            result.addAll( candidates );
                        }
                        candidates.clear();
                        result.add( word );
                        ptr = 0;
                    }
                }
            }
            // simple tag
            else {
                // the current tag matches the tag of the word
                if ( word.getTag().contains( curTag ) ) {
                    // concatenate the current word
                    candidates.add( word );
                    // and increment the position of pattern array
                    ptr += 1;
                // current tag doesn't match
                } else {
                    if ( !candidates.isEmpty() ) {
                        result.addAll( candidates );
                        candidates.clear();
                    }
                    ptr = 0;
                    result.add( word );
                }
                // reset the position if needed
                if ( ptr >= tags.length ) {
                    result.add( new Word( joinPactWords( candidates ), withTag ) );
                    candidates.clear();
                    ptr = 0;
                }
            }
        }
        // deal with a possible remainders
        if ( !candidates.isEmpty() ) {
            // join candidates together if last tag was ranged
            if ( isRangeTag ) {
                result.add( new Word( joinPactWords( candidates ), withTag ) );
            // otherwise simply add candidates to the results
            } else {
                result.addAll( candidates );
            }
            candidates.clear();
        }
        return result;
    }
    
    public static class Tuple<X, Y> { 
        public final X first; 
        public final Y second; 
        public Tuple(X x, Y y) { 
          this.first = x; 
          this.second = y; 
        } 
    }
    /**
     * 
     * @param sentence
     * @param pattern
     * @return 
     */
    public static Integer findByPattern( Sentence sentence, String patternstring ) {
        String[] parts = patternstring.split( "\t" );
        ArrayList<Tuple<String,Pattern>> test = new ArrayList<>();
        for ( String part : parts ) {
            String type = ( part.matches( "^\\(.*?\\)$" ) ) ? "tag" : "word";
            part = part.replaceAll( "^\\(|\\)$", "" );
            Pattern p = Pattern.compile( "\\A" + part + "\\z", Pattern.CASE_INSENSITIVE );
            test.add( new Tuple( type, p ) );
        }
        // quickwins ;)
        if ( test.isEmpty() ) return -1;
        if ( sentence.size() < test.size() ) return -1;
        
        String type, word;
        Word pWord;
        Pattern pattern;
        Matcher m;
        for (Integer i = 0, j = 0; i < sentence.size(); i += 1 ) {
           if ( j < test.size() ) {
               type = test.get( j ).first;
               pattern = test.get( j ).second;
               pWord = sentence.get( i );
               word = ( type.equals( "tag" ) ) ? pWord.getTag() : pWord.getWord();
               m = pattern.matcher( word );
               if ( m.find() ) {
                   // found
                   j += 1;
               } else {
                   // reset
                   i -= j;
                   j = 0;
               }
           }
           else {
               // found match
               return i-j;
           }
        }
        // nothing found
        return -1;
    }
    
    /**
     * 
     * @param candidates
     * @return 
     */
    private static String joinPactWords( List<Word> candidates ) {
        String result = "";
        for( Word word : candidates )
            result += word.getWord() + " ";
        return result.trim();
    }
    
}
