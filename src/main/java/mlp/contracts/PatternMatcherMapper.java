package mlp.contracts;

import java.util.List;
import java.util.Set;

import mlp.pojos.Relation;
import mlp.pojos.Sentence;
import mlp.pojos.WikiDocument;
import mlp.text.PatternMatcher;
import mlp.text.PatternMatcher.IdentifierMatch;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternMatcherMapper implements FlatMapFunction<WikiDocument, Relation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternMatcherMapper.class);

    @Override
    public void flatMap(WikiDocument doc, Collector<Relation> out) throws Exception {
        List<Sentence> sentences = doc.getSentences();
        for (Sentence sentence : sentences) {
            if (!sentence.getIdentifiers().isEmpty()) {
                LOGGER.debug("sentence {}", sentence);
            }

            Set<String> identifiers = sentence.getIdentifiers();
            PatternMatcher matcher = PatternMatcher.generatePatterns(identifiers);
            List<IdentifierMatch> foundMatches = matcher.match(sentence.getWords());

            for (IdentifierMatch match : foundMatches) {
                Relation relation = new Relation();
                relation.setDocumentTitle(doc.getTitle());
                relation.setIdentifier(match.getIdentifier().getWord());
                relation.setWord(match.getDefinition());
                relation.setSentence(sentence);
                relation.setScore(1.0d);

                LOGGER.debug("found match {}", relation);
                out.collect(relation);
            }
        }
    }

}
