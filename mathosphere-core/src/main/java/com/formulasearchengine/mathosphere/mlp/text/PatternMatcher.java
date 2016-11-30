package com.formulasearchengine.mathosphere.mlp.text;

import com.alexeygrigorev.rseq.*;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikidataLink;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PatternMatcher {
  private List<Pattern<Word>> patterns;

  public PatternMatcher(List<Pattern<Word>> patterns) {
    this.patterns = patterns;
  }

  public List<IdentifierMatch> match(List<Word> words, ParsedWikiDocument doc) {
    List<IdentifierMatch> result = Lists.newArrayList();
    for (Pattern<Word> pattern : patterns) {
      List<Match<Word>> matches = pattern.find(words);
      for (Match<Word> match : matches) {
        String id = match.getVariable("identifier").getWord();
        String def = deLinkify(match.getVariable("definition"), doc);
        result.add(new IdentifierMatch(id, def));
      }
    }
    return result;
  }

  public static PatternMatcher generatePatterns(Set<String> identifiers) {
    Matcher<Word> isOrAre = word("is").or(word("are"));
    Matcher<Word> let = word("let");
    Matcher<Word> be = word("be");
    Matcher<Word> by = word("by");
    Matcher<Word> denotes = word("denotes").or(word("denote"));
    Matcher<Word> denoted = word("denoted");

    Matcher<Word> the = pos("DT");

    Matcher<Word> identifier = BeanMatchers.in(Word.class, "word", identifiers).captureAs("identifier");
    Matcher<Word> definition = posRegExp("(NN[PS]{0,2}|NP\\+?|NN\\+|LNK)").captureAs("definition");

    List<Pattern<Word>> patterns = Arrays.asList(
        Pattern.create(definition, identifier),
        Pattern.create(identifier, definition),
        Pattern.create(identifier, denotes, definition),
        Pattern.create(identifier, denotes, the, definition),
        Pattern.create(identifier, isOrAre, definition),
        Pattern.create(identifier, isOrAre, the, definition),
        Pattern.create(identifier, isOrAre, denoted, by, definition),
        Pattern.create(identifier, isOrAre, denoted, by, the, definition),
        Pattern.create(let, identifier, be, denoted, by, definition),
        Pattern.create(let, identifier, be, denoted, by, the, definition));

    return new PatternMatcher(patterns);

  }

  public static XMatcher<Word> word(String word) {
    return BeanMatchers.eq(Word.class, "word", word);
  }

  public static XMatcher<Word> pos(String pos) {
    return BeanMatchers.eq(Word.class, "posTag", pos);
  }

  public static XMatcher<Word> posRegExp(String regexp) {
    return BeanMatchers.regex(Word.class, "posTag", regexp);
  }

  public static class IdentifierMatch {
    private String identifier;
    private String definition;

    public IdentifierMatch(String identifier, String definition) {
      this.identifier = identifier;
      this.definition = definition;
    }

    public String getIdentifier() {
      return identifier;
    }

    public String getDefinition() {
      return definition;
    }
  }

  private String deLinkify(Word word, ParsedWikiDocument doc) {
    String definition;
    if (word.getPosTag().equals(PosTag.LINK)) {
      String hash = word.getWord().replaceAll("^LINK_", "");
      WikidataLink link = doc.getLinkMap().get(hash);
      if (link != null) {
        definition = "[[" + link.getContent() + "]]";
      } else {
        definition = "[[" + word.getWord() + "]]";
      }
    } else {
      definition = word.getWord();
    }
    return definition;
  }

}
