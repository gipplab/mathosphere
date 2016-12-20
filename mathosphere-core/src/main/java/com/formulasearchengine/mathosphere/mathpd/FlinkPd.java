package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathosphere.mathpd.cli.FlinkPdCommandConfig;
import com.formulasearchengine.mathosphere.mathpd.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import com.formulasearchengine.mathosphere.mlp.contracts.CreateCandidatesMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.tuple.Tuple6;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.Collector;

import java.text.DecimalFormat;
import java.util.HashMap;

public class FlinkPd {
    protected static final Log LOG = LogFactory.getLog(FlinkPd.class);
    private static DecimalFormat decimalFormat = new DecimalFormat("0.0");

    public static void main(String[] args) throws Exception {
        FlinkPdCommandConfig config = FlinkPdCommandConfig.from(args);
        run(config);
    }

    private static String generateIdPair(String id1, String id2) {
        return id1 + "-" + id2;
    }

    private static String getIdFromIdPair(String idPair, int index) {
        return idPair.split("-")[index];
    }

    public static void run(FlinkPdCommandConfig config) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<String> source = readWikiDump(config, env);
        DataSource<String> refs = readRefs(config, env);

        // TODO: cross product or reduce function can be enhanced by leaving out all duplicates of the matrix because d(a,b) == d(b,a)

        //noinspection Convert2Lambda
        DataSet distancesAndSectionPairs =
                source.flatMap(new TextExtractorMapper())
                        .cross(refs.flatMap(new TextExtractorMapper()))
                        .reduceGroup(new GroupReduceFunction<Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument>, Tuple6<Double, Double, Double, Double, Double, String>>() {
                            @Override
                            public void reduce(Iterable<Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument>> iterable, Collector<Tuple6<Double, Double, Double, Double, Double, String>> collector) throws Exception {
                                for (Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument> i : iterable) {
                                    if (i.f0 == null || i.f1 == null)
                                        continue;

                                    // skip one diagonal half of the matrix
                                    if (!i.f0.getId().contains("Original"))
                                        continue;

                                    // only check Original against Plagiarism (not against other Originals)
                                    if (!i.f1.getId().contains("Plagiarism"))
                                        continue;

                                    final Tuple4<Double, Double, Double, Double> distanceAbsoluteAllFeatures = Distances.distanceRelativeAllFeatures(i.f0, i.f1);
                                    final Tuple6<Double, Double, Double, Double, Double, String> resultLine = new Tuple6<>(
                                            distanceAbsoluteAllFeatures.f0 + distanceAbsoluteAllFeatures.f1 + distanceAbsoluteAllFeatures.f2 + distanceAbsoluteAllFeatures.f3,
                                            distanceAbsoluteAllFeatures.f0,
                                            distanceAbsoluteAllFeatures.f1,
                                            distanceAbsoluteAllFeatures.f2,
                                            distanceAbsoluteAllFeatures.f3,
                                            generateIdPair(i.f0.getId(), i.f1.getId()));

                                    collector.collect(resultLine);
                                }
                            }
                        })
                        .sortPartition(1, Order.ASCENDING);
        distancesAndSectionPairs.writeAsCsv(config.getOutputDir(), WriteMode.OVERWRITE);

        // we can now use the distances and section pairs dataset to aggregate the distances on document level in distance bins
        //noinspection Convert2Lambda
        DataSet binnedDistancesForPairs =
                distancesAndSectionPairs
                        .reduceGroup(new GroupReduceFunction<
                                Tuple6<Double, Double, Double, Double, Double, String>,
                                Tuple5<String, String, Double, Double, Double>>() {
                            @Override
                            public void reduce(Iterable<Tuple6<Double, Double, Double, Double, Double, String>> iterable, Collector<Tuple5<String, String, Double, Double, Double>> collector) throws Exception {
                                // histogram will contain as a key a tuple2 of the names of the two documents from the pair; and the bin
                                // the value will be the frequency of that bin in that pair of documents
                                final HashMap<Tuple4<String, String, Double, Double>, Double> histogramPairOfNameAndBinWithFrequency = new HashMap<>();
                                final HashMap<Tuple2<String, String>, Double> histogramPairOfNameWithFrequency = new HashMap<>();

                                for (Tuple6<Double, Double, Double, Double, Double, String> curPairWithDistances : iterable) {
                                    final String idPair = curPairWithDistances.f5;
                                    final String id0 = FlinkPd.getIdFromIdPair(idPair, 0);
                                    final String id1 = FlinkPd.getIdFromIdPair(idPair, 1);
                                    final String name0 = ExtractedMathPDDocument.getNameFromId(id0);
                                    final String name1 = ExtractedMathPDDocument.getNameFromId(id1);

                                    double distance = curPairWithDistances.f0 / 4.0; // take the accumulated distance and normalize it

                                    // the key
                                    final Tuple4<String, String, Double, Double> key =
                                            new Tuple4<>(
                                                    name0,
                                                    name1,
                                                    getBinBoundary(distance, 0.2, true),
                                                    getBinBoundary(distance, 0.2, false));
                                    final Tuple2<String, String> keyName = new Tuple2<String, String>(name0, name1);

                                    // look up if something has been stored under this key
                                    Double frequencyOfCurKey = histogramPairOfNameAndBinWithFrequency.getOrDefault(key, 0.0);
                                    histogramPairOfNameAndBinWithFrequency.put(key, frequencyOfCurKey + 1.0);

                                    // also update the pair's total frequency
                                    histogramPairOfNameWithFrequency.put(keyName, histogramPairOfNameWithFrequency.getOrDefault(keyName, 0.0) + 1.0);
                                }

                                for (Tuple4<String, String, Double, Double> key : histogramPairOfNameAndBinWithFrequency.keySet()) {
                                    collector.collect(new Tuple5<>(key.f0, key.f1, key.f2, key.f3, histogramPairOfNameAndBinWithFrequency.get(key) / histogramPairOfNameWithFrequency.get(new Tuple2<>(key.f0, key.f1))));
                                }
                            }
                        })
                        .sortPartition(0, Order.ASCENDING)
                        .sortPartition(1, Order.ASCENDING);
        binnedDistancesForPairs.writeAsCsv(config.getOutputDir() + "_binned", WriteMode.OVERWRITE);

        final int parallelism = config.getParallelism();
        if (parallelism > 0) {
            env.setParallelism(parallelism);
        }
        env.execute("Relation Finder");
    }

    private static double getBinBoundary(double value, double binWidth, boolean isLower) {
        double flooredDivision = Math.floor(value / binWidth);
        double binBoundary = Double.NaN;

        if (isLower)
            binBoundary = binWidth * flooredDivision;
        else
            binBoundary = binWidth * (flooredDivision + 1);

        return Double.valueOf(decimalFormat.format(binBoundary));
    }

    public static DataSource<String> readWikiDump(FlinkPdCommandConfig config, ExecutionEnvironment env) {
        Path filePath = new Path(config.getDataset());
        TextInputFormat inp = new TextInputFormat(filePath);
        inp.setCharsetName("UTF-8");
        inp.setDelimiter("</ARXIVFILESPLIT>");
        return env.readFile(inp, config.getDataset());
    }

    public static DataSource<String> readRefs(FlinkPdCommandConfig config, ExecutionEnvironment env) {
        Path filePath = new Path(config.getRef());
        TextInputFormat inp = new TextInputFormat(filePath);
        inp.setCharsetName("UTF-8");
        inp.setDelimiter("</ARXIVFILESPLIT>");
        return env.readFile(inp, config.getRef());
    }

    public String runFromText(FlinkPdCommandConfig config, String input) throws Exception {
        final JsonSerializerMapper<Object> serializerMapper = new JsonSerializerMapper<>();
        return serializerMapper.map(outDocFromText(config, input));
    }

    public WikiDocumentOutput outDocFromText(FlinkPdCommandConfig config, String input) throws Exception {
        final TextAnnotatorMapper textAnnotatorMapper = new TextAnnotatorMapper(config);
        textAnnotatorMapper.open(null);
        final CreateCandidatesMapper candidatesMapper = new CreateCandidatesMapper(config);

        final ParsedWikiDocument parsedWikiDocument = textAnnotatorMapper.parse(input);
        return candidatesMapper.map(parsedWikiDocument);
    }


}
