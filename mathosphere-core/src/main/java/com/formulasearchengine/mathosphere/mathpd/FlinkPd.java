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
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.operators.GroupReduceOperator;
import org.apache.flink.api.java.tuple.*;
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

    private static void collectElementFrequencies(HashMap<String, Double> histogramOfDimension, String dimension, Collector<Tuple3<String, String, Double>> collector) {
        for (String key : histogramOfDimension.keySet()) {
            collector.collect(new Tuple3<>(dimension, key, histogramOfDimension.get(key)));
        }
    }

    private static void convertAbsoluteHistogramToTFIDFHistogram(ExtractedMathPDDocument doc, ExtractedMathPDDocument tfidfDoc, String dimensionName, String elementName, double df) {
        HashMap<String, Double> histogramIn = null;
        HashMap<String, Double> histogramOut = null;

        switch (dimensionName) {
            case "bvar":
                histogramIn = doc.getHistogramBvar();
                histogramOut = tfidfDoc.getHistogramBvar();
                break;
            case "ci":
                histogramIn = doc.getHistogramCi();
                histogramOut = tfidfDoc.getHistogramCi();
                break;
            case "cn":
                histogramIn = doc.getHistogramCn();
                histogramOut = tfidfDoc.getHistogramCn();
                break;
            case "csymbol":
                histogramIn = doc.getHistogramCsymbol();
                histogramOut = tfidfDoc.getHistogramCsymbol();
                break;
            default:
                throw new RuntimeException("unknown dimension");
        }

        histogramOut.put(elementName, histogramIn.getOrDefault(elementName, 0.0) / (df + 0.00000000000000000001));
    }

    public static void run(FlinkPdCommandConfig config) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<String> source = readWikiDump(config, env);
        DataSource<String> refs = readRefs(config, env);

        final FlatMapOperator<String, Tuple2<String, ExtractedMathPDDocument>> extractedMathPdDocuments = source.flatMap(new TextExtractorMapper());

        //noinspection Convert2Lambda
        final GroupReduceOperator<Tuple3<String, String, Double>, Tuple3<String, String, Double>> corpusWideElementFrequenciesByDimension = extractedMathPdDocuments
                .flatMap(new FlatMapFunction<Tuple2<String, ExtractedMathPDDocument>, Tuple3<String, String, Double>>() {
                    @Override
                    public void flatMap(Tuple2<String, ExtractedMathPDDocument> stringExtractedMathPDDocumentTuple2, Collector<Tuple3<String, String, Double>> collector) throws Exception {
                        final ExtractedMathPDDocument curDoc = stringExtractedMathPDDocumentTuple2.f1;
                        collectElementFrequencies(curDoc.getHistogramBvar(), "bvar", collector);
                        collectElementFrequencies(curDoc.getHistogramCi(), "ci", collector);
                        collectElementFrequencies(curDoc.getHistogramCn(), "cn", collector);
                        collectElementFrequencies(curDoc.getHistogramCsymbol(), "csymbol", collector);
                    }
                })
                .groupBy(0, 1)
                .reduceGroup(new GroupReduceFunction<Tuple3<String, String, Double>, Tuple3<String, String, Double>>() {
                    @Override
                    public void reduce(Iterable<Tuple3<String, String, Double>> iterable, Collector<Tuple3<String, String, Double>> collector) throws Exception {
                        final HashMap<Tuple2<String, String>, Double> freqsInCorpus = new HashMap<>();
                        for (Tuple3<String, String, Double> i : iterable) {
                            final Tuple2<String, String> key = new Tuple2<>(i.f0, i.f1);
                            freqsInCorpus.put(key, freqsInCorpus.getOrDefault(key, 0.0) + i.f2);
                        }

                        for (Tuple2<String, String> key : freqsInCorpus.keySet()) {
                            collector.collect(new Tuple3<>(key.f0, key.f1, freqsInCorpus.get(key)));
                        }
                    }
                });
        // at this point we have in corpusWideElementFrequenciesByDimension the DF over all documents for each element in all dimensions
        corpusWideElementFrequenciesByDimension.writeAsCsv(config.getOutputDir() + "_DF");

        // now convert the absolute histograms into tfidf histograms
        final GroupReduceOperator<Tuple2<Tuple2<String, ExtractedMathPDDocument>, Tuple3<String, String, Double>>, Tuple2<String, ExtractedMathPDDocument>> extractedMathPDDocsWithTFIDF =
                extractedMathPdDocuments
                        .cross(corpusWideElementFrequenciesByDimension)
                        .reduceGroup(new GroupReduceFunction<Tuple2<Tuple2<String, ExtractedMathPDDocument>, Tuple3<String, String, Double>>, Tuple2<String, ExtractedMathPDDocument>>() {
                            @Override
                            public void reduce(Iterable<Tuple2<Tuple2<String, ExtractedMathPDDocument>, Tuple3<String, String, Double>>> iterable,
                                               Collector<Tuple2<String, ExtractedMathPDDocument>> collector) throws Exception {
                                final HashMap<String, ExtractedMathPDDocument> tfidfDocs = new HashMap<>();

                                for (Tuple2<Tuple2<String, ExtractedMathPDDocument>, Tuple3<String, String, Double>> pair : iterable) {
                                    final ExtractedMathPDDocument curDoc = pair.f0.f1;
                                    final String name = curDoc.getName();
                                    final Tuple3<String, String, Double> curIDFTriple = pair.f1;

                                    // get to tfidf doc
                                    ExtractedMathPDDocument curTfidfDoc = tfidfDocs.get(name);
                                    if (curTfidfDoc == null) {
                                        curTfidfDoc = new ExtractedMathPDDocument();
                                        tfidfDocs.put(name, curTfidfDoc);
                                    }

                                    convertAbsoluteHistogramToTFIDFHistogram(curDoc, curTfidfDoc, curIDFTriple.f0, curIDFTriple.f1, curIDFTriple.f2);
                                }

                                for (String name : tfidfDocs.keySet()) {
                                    collector.collect(new Tuple2<>(name, tfidfDocs.get(name)));
                                }
                            }
                        });
        extractedMathPDDocsWithTFIDF.writeAsCsv(config.getOutputDir() + "_TFIDF");

        DataSet distancesAndSectionPairs =
                extractedMathPDDocsWithTFIDF
                        .groupBy(0)
                        .reduceGroup(new GroupReduceFunction<Tuple2<String, ExtractedMathPDDocument>, ExtractedMathPDDocument>() {
                            @Override
                            public void reduce(Iterable<Tuple2<String, ExtractedMathPDDocument>> iterable, Collector<ExtractedMathPDDocument> collector) throws Exception {
                                ExtractedMathPDDocument tmpDoc = null;
                                for (Tuple2<String, ExtractedMathPDDocument> i : iterable) {
                                    if (tmpDoc == null) {
                                        tmpDoc = i.f1;
                                    } else {
                                        tmpDoc.mergeOtherIntoThis(i.f1);
                                    }
                                }
                                collector.collect(tmpDoc);
                            }
                        })
                        .cross(refs.flatMap(new TextExtractorMapper())
                                .groupBy(0)
                                .reduceGroup(new GroupReduceFunction<Tuple2<String, ExtractedMathPDDocument>, ExtractedMathPDDocument>() {
                                    @Override
                                    public void reduce(Iterable<Tuple2<String, ExtractedMathPDDocument>> iterable, Collector<ExtractedMathPDDocument> collector) throws Exception {
                                        ExtractedMathPDDocument tmpDoc = null;
                                        for (Tuple2<String, ExtractedMathPDDocument> i : iterable) {
                                            if (tmpDoc == null) {
                                                tmpDoc = i.f1;
                                            } else {
                                                tmpDoc.mergeOtherIntoThis(i.f1);
                                            }
                                        }
                                        collector.collect(tmpDoc);
                                    }
                                })
                        )
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

                                    // WARNING: Currently the cosine distance is just the relative distance!!! TODO
                                    final Tuple4<Double, Double, Double, Double> distanceAbsoluteAllFeatures = Distances.distanceCosineAllFeatures(i.f0, i.f1);
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
