package com.formulasearchengine.mathosphere.mlp.text;

import com.alexeygrigorev.rseq.*;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikidataLink;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

import static com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils.deLinkify;

/**
 * @author extended for MOIs by Andre Greiner-Petter
 */
public class PatternMatcher {
  private List<Pattern<Word>> patterns;

  public PatternMatcher(List<Pattern<Word>> patterns) {
    this.patterns = patterns;
  }

  public List<IdentifierMatch> match(List<Word> words, ParsedWikiDocument doc) {
    List<IdentifierMatch> result = Lists.newArrayList();
    for (int i = 0; i < patterns.size(); i++) {
      Pattern<Word> pattern = patterns.get(i);
      List<Match<Word>> matches = pattern.find(words);
      for (Match<Word> match : matches) {
        String id = match.getVariable("identifier").getWord();
        String def = deLinkify(match.getVariable("definition"), doc);
        int position = i;
        result.add(new IdentifierMatch(id, def, position));
      }
    }
    return result;
  }

  public Set<Word> match(List<Word> words) {
    Set<Word> matchedDefiniens = new HashSet<>();
    for ( Pattern<Word> pattern : patterns ) {
      List<Match<Word>> matches = pattern.find(words);
      for ( Match<Word> match : matches ) {
        Word matchedDefinition = match.getVariable("definition");
        matchedDefiniens.add( matchedDefinition );
      }
    }
    return matchedDefiniens;
  }

  public static PatternMatcher generateMOIPatternMatcher(MathTag... formulae) {
    if ( formulae == null ) return null;
    return generateMOIPatternMatcher( Arrays.asList(formulae) );
  }

  /**
   * The following pattern matcher is a copy of the patterns published in
   * <url>https://www.gipp.com/wp-content/papercite-data/pdf/schubotz2017.pdf</url>
   * but updated for MOI.
   *
   * It only implements the rules 1, 2, 5-6 via two replacement rules.
   * The reason is simple. First those rules are the only one that can be applied directly to MOI
   * and second, these rules performed best.
   *
   * You must use {@link #match(List, ParsedWikiDocument)}
   *
   * @param formulae the math tags you want to match
   * @return the pattern matcher
   * @author Andre
   */
  public static PatternMatcher generateMOIPatternMatcher(Collection<MathTag> formulae) {
    String[] formulaePlaceholders = formulae.stream().map(MathTag::placeholder).toArray(String[]::new);
    List<Pattern<Word>> patterns = Arrays.asList(
            // 1. <definiens> <identifier>
            Pattern.create(
                    PosTagger.pos( PosTag.DEFINIEN_REGEX ).captureAs("definition"),
                    PosTagger.txtIn(formulaePlaceholders).captureAs("moi")
            ),
            // 2, 5-6. <identifier> DT? <definiens>
            Pattern.create(
                    PosTagger.txtIn(formulaePlaceholders).captureAs("moi"),
                    PosTagger.txtIn( "is", "are" ).optional(),
                    PosTagger.pos( PosTag.DETERMINER ).optional(),
                    PosTagger.pos( PosTag.DEFINIEN_REGEX ).captureAs("definition")
            )
    );
    return new PatternMatcher(patterns);
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
    Matcher<Word> definition = posRegExp("("+PosTag.DEFINIEN_REGEX+")").captureAs("definition");

    List<Pattern<Word>> patterns = Arrays.asList(
      //0
      Pattern.create(definition, identifier),
      //1
      Pattern.create(identifier, definition),
      //2
      Pattern.create(identifier, isOrAre, definition),
      //3
      Pattern.create(identifier, isOrAre, the, definition),
      //4
      Pattern.create(let, identifier, be, definition),
      //5
      Pattern.create(let, identifier, be, the, definition),
      //6
      Pattern.create(definition, isOrAre, denoted, by, identifier),
      //7
      Pattern.create(identifier, denotes, definition),
      //8
      Pattern.create(identifier, denotes, the, definition)
    );
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
    private int position;

    public int getPosition() {
      return position;
    }

    /**
     * Constructor.
     *
     * @param identifier the identifier.
     * @param definition the definiens candidate.
     * @param position   the position of the pattern which this was found.
     */
    public IdentifierMatch(String identifier, String definition, int position) {
      this.identifier = identifier;
      this.definition = definition;
      this.position = position;
    }

    public String getIdentifier() {
      return identifier;
    }

    public String getDefinition() {
      return definition;
    }
  }
}
