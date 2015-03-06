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
package mlp.contracts;

import java.util.ArrayList;
import java.util.List;

import mlp.types.Identifiers;
import mlp.types.Relation;
import mlp.types.Sentence;
import mlp.types.WikiDocument;
import mlp.utils.SentenceUtils;
import mlp.utils.SentenceUtils.Tuple;

import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.types.StringValue;
import org.apache.flink.util.Collector;

import com.google.common.collect.Iterables;

/**
 * @author rob
 */
public class PatternMatcher implements 
        CoGroupFunction<Tuple2<String, WikiDocument>, 
                        Tuple3<String, Sentence, Double>, 
                        Tuple2<String, Relation>> {

    private String title = null;

    @Override
    public void coGroup(Iterable<Tuple2<String, WikiDocument>> left,
            Iterable<Tuple3<String, Sentence, Double>> right, Collector<Tuple2<String, Relation>> out)
            throws Exception {
        // left: Doc
        // right: Sentences

        // populating identifier list we'll allways get one record from the left, therefore, we don't
        // need to iterate through left

        WikiDocument doc = Iterables.get(left, 0).f1;
        title = doc.getTitle();
        Identifiers identifiers = doc.getKnownIdentifiers();

        List<Sentence> sentences = new ArrayList<>();
        for (Tuple3<String, Sentence, Double> sentence : right) {
            sentences.add(sentence.f1.clone());
        }

        for (StringValue identifier : identifiers) {
            String id = identifier.getValue();
            ArrayList<Tuple<String, Tuple<Integer, Integer>>> patterns = new ArrayList<>();

            // is
            patterns.add(createPattern("%identifier%	is	%definition%", id, 0, 2));
            // is the
            patterns.add(createPattern("%identifier%	is	the	%definition%", id, 0, 3));
            // let be
            patterns.add(createPattern("let	%identifier%	be	the	%definition%", id, 1, 4));
            // denoted by
            patterns.add(createPattern("%definition%	is|are	denoted	by	%identifier%", id, 4, 0));
            // denotes
            patterns.add(createPattern("%identifier%	denotes	(DT)	%identifier%", id, 0, 3));
            // zero
            patterns.add(createPattern("%definition%	%identifier%", id, 1, 0));

            for (Sentence sentence : sentences) {
                // only care about sentences the identifier is contained in
                if (!sentence.containsWord(identifier))
                    continue;
                // search for each pattern
                for (Tuple<String, Tuple<Integer, Integer>> pattern : patterns) {
                    String patternstring = pattern.first;
                    Integer iOffset = pattern.second.first;
                    Integer dOffset = pattern.second.second;
                    Integer index = SentenceUtils.findByPattern(sentence, patternstring);
                    if (index >= 0) {
                        // pattern found
                        Relation relation = new Relation();

                        relation.setIdentifier(identifier);
                        relation.setIdentifierPosition(index + iOffset);
                        relation.setWordPosition(index + dOffset);
                        relation.setScore(1.0d);
                        relation.setSentence(sentence);
                        relation.setTitle(title);

                        out.collect(new Tuple2<>(title, relation));
                    }
                }

            }
        }

    }

    private Tuple<String, Tuple<Integer, Integer>> createPattern(String pattern, String identifier,
            Integer iOffset, Integer dOffset) {
        String definition = "(NN[PS]{0,2}|NP\\+?|NN\\+|LNK)";
        pattern = pattern.replaceAll("%identifier%", identifier);
        pattern = pattern.replaceAll("%definition%", definition);
        return new Tuple<>(pattern, new Tuple<>(iOffset, dOffset));
    }

}
