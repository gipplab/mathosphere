package mlp.contracts;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import mlp.pojos.Relation;
import mlp.pojos.Sentence;
import mlp.pojos.WikiDocument;
import mlp.pojos.Word;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

public class PatternMatcherMapper implements FlatMapFunction<WikiDocument, Relation> {

    @Override
    public void flatMap(WikiDocument doc, Collector<Relation> out) throws Exception {
        List<Sentence> sentences = doc.getSentences();
        for (Sentence sentence : sentences) {
            Set<String> identifiers = sentence.getIdentifiers();
            for (String identifier : identifiers) {
                List<Pattern> patterns = generatePatterns(identifier);
                for (Pattern pattern : patterns) {
                    Match match = findPattern(sentence.getWords(), pattern);
                    if (match == null) {
                        continue;
                    }

                    Relation relation = new Relation();
                    relation.setDocumentTitle(doc.getTitle());
                    relation.setIdentifier(identifier);
                    relation.setIdentifierPosition(0); // who cares
                    relation.setWord(match.definition);
                    relation.setWordPosition(match.definitionPosition);
                    relation.setScore(1.0d);
                    relation.setSentence(sentence);
                    out.collect(relation);
                }
            }
        }
    }

    public static List<Pattern> generatePatterns(String identifier) {
        List<Pattern> patterns = Arrays.asList(
                p(identifier, Pattern.DEFINITION),
                p(identifier, "is", Pattern.DEFINITION),
                p(identifier, "is", "the", Pattern.DEFINITION),
                p(identifier, "denote", Pattern.DEFINITION),
                p(identifier, "denote", "the", Pattern.DEFINITION),
                p(identifier, "denotes", Pattern.DEFINITION),
                p(identifier, "denotes", "the", Pattern.DEFINITION),
                p("let", identifier, "be", Pattern.DEFINITION),
                p("let", identifier, "be", "the", Pattern.DEFINITION),
                p(Pattern.DEFINITION, "is", "denoted", "by", identifier),
                p(Pattern.DEFINITION, "are", "denoted", "by", identifier));
        return patterns;
    }

    public static Match findPattern(List<Word> sentence, Pattern input) {
        List<String> pattern = input.pattern;
        for (int wordIdx = 0; wordIdx < sentence.size() - pattern.size() + 1; wordIdx++) {
            boolean success = true;
            Word definition = null;
            int definitionIdx = -1;

            for (int pieceIdx = 0; pieceIdx < pattern.size(); pieceIdx++) {
                Word word = sentence.get(wordIdx + pieceIdx);
                String piece = pattern.get(pieceIdx);

                if (Pattern.DEFINITION.equals(piece)) {
                    if (word.posTag.matches("(NN[PS]{0,2}|NP\\+?|NN\\+|LNK)")) {
                        definition = word;
                        definitionIdx = pieceIdx;
                    } else {
                        success = false;
                        break;
                    }
                } else {
                    String lowerCase = word.toLowerCase();
                    if (!Objects.equals(lowerCase, piece)) {
                        success = false;
                        break;
                    }
                }
            }

            if (success && definition != null) {
                return new Match(definition, wordIdx, wordIdx + definitionIdx);
            }
        }

        return null;
    }

    public static Pattern p(String... pattern) {
        return new Pattern(Arrays.asList(pattern));
    }

    public static class Pattern {
        public static final String DEFINITION = "%DEFINITION%";
        private List<String> pattern;

        public static Pattern of(String... pattern) {
            return new Pattern(Arrays.asList(pattern));
        }

        public Pattern(List<String> pattern) {
            this.pattern = pattern;
        }

        @Override
        public String toString() {
            return pattern.toString();
        }
    }

    public static class Match {
        private Word definition;
        private int patternPosition;
        private int definitionPosition;

        public Match(Word definition, int patternPosition, int definitionPosition) {
            this.definition = definition;
            this.patternPosition = patternPosition;
            this.definitionPosition = definitionPosition;
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public int hashCode() {
            return Objects.hash(definition, patternPosition, definitionPosition);
        }

        @Override
        public String toString() {
            return "Match [definition=" + definition + ", patternPosition=" + patternPosition
                    + ", definitionPosition=" + definitionPosition + "]";
        }
    }

}
