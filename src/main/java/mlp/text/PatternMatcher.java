package mlp.text;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import mlp.pojos.Word;

import com.google.common.collect.Lists;
import com.itshared.rseq.BeanMatchers;
import com.itshared.rseq.EnhancedMatcher;
import com.itshared.rseq.Match;
import com.itshared.rseq.Matcher;
import com.itshared.rseq.Pattern;

public class PatternMatcher {
    private List<Pattern<Word>> patterns;

    public PatternMatcher(List<Pattern<Word>> patterns) {
        this.patterns = patterns;
    }

    public List<IdentifierMatch> match(List<Word> words) {
        List<IdentifierMatch> result = Lists.newArrayList();
        for (Pattern<Word> pattern : patterns) {
            List<Match<Word>> matches = pattern.find(words);
            for (Match<Word> match : matches) {
                Word id = match.getVariable("identifier");
                Word def = match.getVariable("definition");
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

    private static EnhancedMatcher<Word> word(String word) {
        return BeanMatchers.eq(Word.class, "word", word);
    }

    private static EnhancedMatcher<Word> pos(String pos) {
        return BeanMatchers.eq(Word.class, "posTag", pos);
    }

    private static EnhancedMatcher<Word> posRegExp(String regexp) {
        return BeanMatchers.regex(Word.class, "posTag", regexp);
    }

    public static class IdentifierMatch {
        private Word identifier;
        private Word definition;

        public IdentifierMatch(Word identifier, Word definition) {
            this.identifier = identifier;
            this.definition = definition;
        }

        public Word getIdentifier() {
            return identifier;
        }

        public Word getDefinition() {
            return definition;
        }
    }

}
