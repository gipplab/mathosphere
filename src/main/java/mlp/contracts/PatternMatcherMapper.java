package mlp.contracts;

import java.util.List;
import java.util.Set;

import mlp.pojos.IndentifiersRepresentation;
import mlp.pojos.Relation;
import mlp.pojos.Sentence;
import mlp.pojos.WikiDocument;
import mlp.text.PatternMatcher;
import mlp.text.PatternMatcher.IdentifierMatch;

import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class PatternMatcherMapper implements MapFunction<WikiDocument, IndentifiersRepresentation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternMatcherMapper.class);

    @Override
    public IndentifiersRepresentation map(WikiDocument doc) throws Exception {
        List<Relation> foundRelations = Lists.newArrayList();
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
                relation.setIdentifier(match.getIdentifier());
                relation.setDefinition(match.getDefinition());
                // relation.setSentence(sentence);
                relation.setScore(1.0d);

                LOGGER.debug("found match {}", relation);
                foundRelations.add(relation);
            }
        }

        LOGGER.info("extracted {} relations from {}", foundRelations.size(), doc.getTitle());
        return new IndentifiersRepresentation(doc.getTitle(), foundRelations, doc.getIdentifiers());
    }

}
