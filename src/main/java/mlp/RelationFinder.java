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
package mlp;

import mlp.contracts.AccuracyEvaluator;
import mlp.contracts.CandidateEmitter;
import mlp.contracts.DocumentProcessor;
import mlp.contracts.FilterCandidates;
import mlp.contracts.PatternMatcher;
import mlp.contracts.SentenceEmitter;
import mlp.types.Relation;
import mlp.types.Sentence;
import mlp.types.WikiDocument;

import org.apache.flink.api.common.ProgramDescription;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.core.fs.Path;
import org.apache.flink.types.Record;

public class RelationFinder implements ProgramDescription {

    public void run(String[] args) throws Exception {
//        Config config = Config.from(args);
        Config config = Config.test();

        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        Path filePath = new Path(config.getDataset());
        TextInputFormat wikiDocumentEmitter = new TextInputFormat(filePath);
        wikiDocumentEmitter.setCharsetName("UTF-8");
        wikiDocumentEmitter.setDelimiter("</page>");
        DataSource<String> source = env.readFile(wikiDocumentEmitter, config.getDataset());
        DataSet<Tuple2<String, WikiDocument>> documents = source.flatMap(new DocumentProcessor());
        documents.print();

//        DataSet<Tuple3<String, Sentence, Double>> sentences = documents.flatMap(new SentenceEmitter(config
//                .getModel()));
//
//        DataSet<Tuple2<String, Relation>> candidates = documents.coGroup(sentences).where(0).equalTo(0)
//                .sortSecondGroup(2, Order.ASCENDING)
//                .with(new CandidateEmitter(config));
//
//        DataSet<Tuple2<String, Relation>> filter = candidates.filter(new FilterCandidates(config
//                .getThreshold()));
//
//        DataSet<Tuple2<String, Relation>> patterns = documents.coGroup(sentences).where(0).equalTo(0)
//                .with(new PatternMatcher());
//
//        DataSet<Record> evaluator = patterns.coGroup(candidates).where(0).equalTo(0)
//                .with(new AccuracyEvaluator());
//
//        documents.writeAsText(config.getOutputdir() + "/pages.jsonp", WriteMode.OVERWRITE);
//        filter.writeAsText(config.getOutputdir() + "/relations.jsonp", WriteMode.OVERWRITE);
//        evaluator.writeAsText(config.getOutputdir() + "/evaluation.csv", WriteMode.OVERWRITE);

        env.execute("Relation Finder");
    }

    @Override
    public String getDescription() {
        return "Parameters: [DATASET] [OUTPUT] [MODEL] [ALPHA] [BETA] [GAMMA] [THRESHOLD]";
    }

    public static void main(String[] args) throws Exception {
        new RelationFinder().run(args);
    }

}
