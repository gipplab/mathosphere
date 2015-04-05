///*        __
// *        \ \
// *   _   _ \ \  ______
// *  | | | | > \(  __  )
// *  | |_| |/ ^ \| || |
// *  | ._,_/_/ \_\_||_|
// *  | |
// *  |_|
// *
// * ----------------------------------------------------------------------------
// * "THE BEER-WARE LICENSE" (Revision 42):
// * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
// * can do whatever you want with this stuff. If we meet some day, and you think
// * this stuff is worth it, you can buy me a beer in return.
// * ----------------------------------------------------------------------------
// */
//package mlp.contracts_old;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import mlp.types.Relation;
//
//import org.apache.flink.api.common.functions.CoGroupFunction;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.types.DoubleValue;
//import org.apache.flink.types.Record;
//import org.apache.flink.types.StringValue;
//import org.apache.flink.util.Collector;
//
///**
// * @author rob
// */
//public class AccuracyEvaluator implements
//        CoGroupFunction<Tuple2<String, Relation>, Tuple2<String, Relation>, Record> {
//
//    @Override
//    public void coGroup(Iterable<Tuple2<String, Relation>> left, Iterable<Tuple2<String, Relation>> right,
//            Collector<Record> out) throws Exception {
//        // Relations from PatternMatcher
//        Map<String, List<Relation>> PM = new HashMap<>();
//        String title = "";
//
//        for (Tuple2<String, Relation> patternRel : left) {
//            title = patternRel.f0;
//
//            // we need to clone the sentence objects, because of reused objects
//            Relation relation = patternRel.f1.clone();
//
//            String identifier = relation.getIdentifier().getValue();
//            if (!PM.containsKey(identifier)) {
//                PM.put(identifier, new ArrayList<Relation>());
//            }
//            PM.get(identifier).add(relation);
//        }
//
//        // right: Relations from CandidateEmitter
//        Map<String, List<Relation>> MLP = new HashMap<>();
//        for (Tuple2<String, Relation> mlpRel : right) {
//            title = mlpRel.f0;
//
//            // we need to clone the sentence objects, because of reused objects
//            Relation relation = mlpRel.f1.clone();
//
//            String identifier = relation.getIdentifier().getValue();
//            if (!MLP.containsKey(identifier)) {
//                MLP.put(identifier, new ArrayList<Relation>());
//            }
//            MLP.get(identifier).add(relation);
//        }
//
//        int numRelationsPM = 0;
//        int numRelationsMLP = 0;
//        int numBest = 0;
//        int above90 = 0;
//        int above80 = 0;
//        int above70 = 0;
//        int above60 = 0;
//
//        for (Entry<String, List<Relation>> set : MLP.entrySet()) {
//            String id = set.getKey();
//            List<Relation> relationsPM = set.getValue();
//
//            // add number of relations found by PM
//            numRelationsPM += relationsPM.size();
//
//            // has the identifier been found in MLP?
//            if (MLP.containsKey(id)) {
//                List<Relation> relationsMLP = MLP.get(id);
//                Collections.sort(relationsMLP, new RelationComparator());
//                Collections.reverse(relationsMLP);
//
//                for (Relation rPM : relationsPM) {
//                    // best matches are equal?
//                    if (relationsMLP.get(0).getDefinitionWord().equals(rPM.getDefinitionWord())) {
//                        numBest++;
//                    }
//
//                    // iterate through the MLP relation
//                    for (Relation rMLP : relationsMLP) {
//                        // only match equal definitions
//                        if (!rMLP.getDefinitionWord().equals(rPM.getDefinitionWord())) {
//                            // GOTO FAIL!
//                            continue;
//                        }
//
//                        numRelationsMLP++;
//
//                        // and gather some statistics
//                        double score = rMLP.getScore();
//                        if (score >= 0.9d) {
//                            above90++;
//                        }
//                        if (score >= 0.8d) {
//                            above80++;
//                        }
//                        if (score >= 0.7d) {
//                            above70++;
//                        }
//                        if (score >= 0.6d) {
//                            above60++;
//                        }
//
//                        // thus we're only interested in the best matches
//                        // we can break the iteration
//                        break;
//                    }
//                }
//            } else {
//                // MLP did not find this one :(
//            }
//        }
//
//        Record record = new Record();
//
//        // detection rate (overall, best, 90, 80, 70, ...)
//        record.setField(0, new DoubleValue(numRelationsMLP * 1.0 / numRelationsPM));
//        record.setField(1, new DoubleValue(numBest * 1.0 / numRelationsPM));
//        record.setField(2, new DoubleValue(above90 * 1.0 / numRelationsPM));
//        record.setField(3, new DoubleValue(above80 * 1.0 / numRelationsPM));
//        record.setField(4, new DoubleValue(above70 * 1.0 / numRelationsPM));
//        record.setField(5, new DoubleValue(above60 * 1.0 / numRelationsPM));
//
//        record.setField(6, new StringValue(title));
//
//        out.collect(record);
//    }
//
//    public static class RelationComparator implements Comparator<Relation> {
//        @Override
//        public int compare(Relation r1, Relation r2) {
//            return Double.compare(r1.getScore(), r2.getScore());
//        }
//    }
//}
