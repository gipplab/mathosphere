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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mlp.Config;
import mlp.types.Identifiers;
import mlp.types.Relation;
import mlp.types.Sentence;
import mlp.types.WikiDocument;
import mlp.types.Word;

import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.types.StringValue;
import org.apache.flink.util.Collector;

import com.google.common.collect.Iterables;

/**
 * @author rob
 */
public class CandidateEmitter implements
        CoGroupFunction<Tuple2<String, WikiDocument>, 
                        Tuple3<String, Sentence, Double>, 
                        Tuple2<String, Relation>> {

    private final static List<String> blacklist = Arrays.asList("behavior", "infinity", "sum", "other", "=",
            "|", "·", "≥", "≤", "≠", "lim", "ƒ", "×", "/", "\\", "-", "function", "functions", "equation",
            "equations", "solution", "solutions", "result", "results");

    private double alpha;
    private double beta;
    private double gamma;

    private Identifiers identifiers;
    private String title;

    public CandidateEmitter(double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    public CandidateEmitter(Config config) {
        this(config.getAlpha(), config.getBeta(), config.getGamma());
    }

    @Override
    public void coGroup(Iterable<Tuple2<String, WikiDocument>> left,
            Iterable<Tuple3<String, Sentence, Double>> right, Collector<Tuple2<String, Relation>> out)
            throws Exception {
        WikiDocument doc = Iterables.get(left, 0).f1;
        title = doc.getTitle();
        identifiers = doc.getKnownIdentifiers();

        List<Sentence> sentences = new ArrayList<>();
        for (Tuple3<String, Sentence, Double> sentence : right) {
            sentences.add(sentence.f1.clone());
        }

        for (StringValue identifier : identifiers) {
            List<Tuple2<String, Relation>> candiates = generateCandidates(sentences, identifier.getValue());
            candiates.forEach(tuple -> out.collect(tuple));
        }
    }

    private List<Tuple2<String, Relation>> generateCandidates(List<Sentence> sentences, String identifier) {
        List<Tuple2<String, Relation>> candidates = new ArrayList<>();

        List<Sentence> candidateSentences = new ArrayList<>();
        for (Sentence sentence : sentences) {
            if (!sentence.containsWord(identifier)) {
                continue;
            }
            candidateSentences.add(sentence);
        }

        Map<String, Integer> ω = generateFrequencies(sentences);
        int Ω = Collections.max(ω.values());

        int index = -1; // will be zero on the first loop
        for (Sentence sentence : candidateSentences) {
            index += 1;
            List<Integer> positions = sentence.getWordPosition(identifier);
            Integer position = -1; // will be zero on the first loop
            for (Word word : sentence) {
                position += 1;
                if (filterWord(word)) {
                    continue;
                }

                int pmin = getMinimumDistancePosition(position, positions);
                int Δ = Math.abs(pmin - position);
                int ω0 = ω.get(word.getWord().toLowerCase());
                double score = getScore(Δ, ω0, Ω, index);

                Relation relation = new Relation();
                relation.setScore(score);
                relation.setIdentifier(identifier);
                relation.setWordPosition(position);
                relation.setIdentifierPosition(pmin);
                relation.setSentence(sentence);
                relation.setTitle(title);

                candidates.add(new Tuple2<>(title, relation));
            }
        }

        return candidates;
    }

    private Map<String, Integer> generateFrequencies(List<Sentence> sentences) {
        Map<String, Integer> ω = new HashMap<>();
        String w;

        for (Sentence sentence : sentences) {
            for (Word word : sentence) {
                w = word.getWord().toLowerCase();
                // only count words we're interested in
                if (filterWord(word)) {
                    continue;
                }
                int count = ω.containsKey(w) ? ω.get(w) + 1 : 1;
                ω.put(w, count);
            }
        }

        return ω;
    }

    private int getMinimumDistancePosition(int pos0, List<Integer> positions) {
        int Δ, Δmin = Integer.MAX_VALUE, min = positions.get(0);
        for (Integer pos1 : positions) {
            Δ = pos1 - pos0;
            if (Δmin > Math.abs(Δ)) {
                Δmin = Math.abs(Δ);
                min = pos1;
            }
        }
        return min;
    }

    private Double getScore(Integer Δ, Integer ω, Integer Ω, Integer x) {
        Double dist = gaussian((double) Δ, Math.sqrt(Math.pow(5d, 2d) / (2d * Math.log(2))));
        Double seq = gaussian((double) x, Math.sqrt(Math.pow(3d, 2d) / (2d * Math.log(2))));
        Double freq = (double) ω / (double) Ω;
        return (alpha * dist + beta * seq + gamma * freq) / (alpha + beta + gamma);
    }

    /**
     * Returns the value of the gaussian function at x. σ is the standard deviation.
     */
    private Double gaussian(Double x, Double σ) {
        return Math.exp(-Math.pow(x, 2d) / (2d * Math.pow(σ, 2d)));
    }

    private boolean filterWord(Word word) {
        // skip the identifier words
        return identifiers.containsIdentifier(word.getWord()) ||
        // skip blacklisted words
                blacklist.contains(word.getWord()) ||
                // we're only interested in nouns, entities and links
                !word.getTag().matches("NN[PS]{0,2}|NP\\+?|NN\\+|LNK");
    }

}
