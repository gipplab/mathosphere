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
//import mlp.types.Relation;
//
//import org.apache.flink.api.common.functions.FilterFunction;
//import org.apache.flink.api.java.tuple.Tuple2;
//
///**
// * @author rob
// */
//public class FilterCandidates implements FilterFunction<Tuple2<String, Relation>> {
//
//    private double threshold;
//
//    public FilterCandidates(double threshold) {
//        this.threshold = threshold;
//    }
//
//    @Override
//    public boolean filter(Tuple2<String, Relation> value) throws Exception {
//        return value.f1.getScore() >= threshold;
//    }
//
//}