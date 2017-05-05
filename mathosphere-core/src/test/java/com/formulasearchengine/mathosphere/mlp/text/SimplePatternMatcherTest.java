package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Leo on 21.01.2017.
 */
public class SimplePatternMatcherTest {
  private ParsedWikiDocument doc;
  private Set<String> identifiers;
  private SimplePatternMatcher simplePatternMatcher;

  @Before
  public void setup() throws Exception {
    doc = new ParsedWikiDocument();
    identifiers = new HashSet<>();
    identifiers.add("a");
    //decoy identifier
    identifiers.add("b");
    simplePatternMatcher = SimplePatternMatcher.generatePatterns(identifiers);
  }

  @Test
  public void testMatch() {
    List<Word> words = new ArrayList<>();
    //definiens candidate 1
    words.add(new Word("bar", "NN"));
    words.add(new Word("a", "ID"));
    //no noun phrase or similar
    words.add(new Word("foo", "VB"));
    //link - definiens candidate 2
    words.add(new Word("car bartz", "LNK"));
    //too short word
    words.add(new Word("at", "NNP"));
    Sentence s = new Sentence(words, identifiers, null);
    Collection<Relation> relations = simplePatternMatcher.match(s, doc);
    Assert.assertEquals(2, relations.size());
    List<Relation> sorted = new ArrayList<>(relations);
    //sort by definiens
    Collections.sort(sorted, (r1, r2) -> r1.getDefinition().compareTo(r2.getDefinition()));
    //first
    Assert.assertEquals("a", sorted.get(0).getIdentifier());
    Assert.assertEquals("[[car bartz]]", sorted.get(0).getDefinition());
    Assert.assertEquals(1, sorted.get(0).getIdentifierPosition());
    Assert.assertEquals(3, sorted.get(0).getWordPosition());
    //second
    Assert.assertEquals("a", sorted.get(1).getIdentifier());
    Assert.assertEquals("bar", sorted.get(1).getDefinition());
    Assert.assertEquals(1, sorted.get(1).getIdentifierPosition());
    Assert.assertEquals(0, sorted.get(1).getWordPosition());
  }
}