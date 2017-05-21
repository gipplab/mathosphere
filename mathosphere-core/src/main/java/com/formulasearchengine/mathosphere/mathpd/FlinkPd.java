package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathosphere.mathpd.cli.FlinkPdCommandConfig;
import com.formulasearchengine.mathosphere.mathpd.contracts.PreprocessedExtractedMathPDDocumentMapper;
import com.formulasearchengine.mathosphere.mathpd.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mathpd.pojos.ExtractedMathPDDocument;
import com.formulasearchengine.mathosphere.mlp.contracts.CreateCandidatesMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.flink.api.common.functions.CrossFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.io.TextOutputFormat.TextFormatter;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.operators.GroupReduceOperator;
import org.apache.flink.api.java.operators.ReduceOperator;
import org.apache.flink.api.java.operators.SortPartitionOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.tuple.Tuple7;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.apache.flink.core.fs.FileSystem.WriteMode.OVERWRITE;

public class FlinkPd {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkPd.class);
    private static final int NUMBER_OF_PARTITIONS = -1; // if -1 then partitioning is disabled and it will just be one document merge (all snippets into one doc)
    private static DecimalFormat decimalFormat = new DecimalFormat("0.0");

    public static void main(String[] args) throws Exception {
        FlinkPdCommandConfig config = FlinkPdCommandConfig.from(args);
        run(config);
    }


    /**
     * This function takes math pd snippets and converts them to single documents (by merging all snippets belonging to the same document)
     *
     * @param extractedMathPdSnippets
     * @return
     */
    private static DataSet<ExtractedMathPDDocument> aggregateSnippetsToPartitions(FlatMapOperator<String, ExtractedMathPDDocument> extractedMathPdSnippets) {
        DataSet<ExtractedMathPDDocument> extractedMathPdDocuments = extractedMathPdSnippets
                .groupBy("title")
                .reduceGroup((GroupReduceFunction<ExtractedMathPDDocument, ExtractedMathPDDocument>) (iterable, collector) -> {
                    final List<ExtractedMathPDDocument> sortedNamesAndSnippets = new ArrayList<>();
                    for (ExtractedMathPDDocument nameAndSnippet : iterable) {
                        sortedNamesAndSnippets.add(nameAndSnippet);
                    }
                    //LOGGER.warn("sorting {} entries", sortedNamesAndSnippets.size());
                    Collections.sort(sortedNamesAndSnippets, (o1, o2) -> o1.getPage().compareTo(o2.getPage()));

                    final List<List<ExtractedMathPDDocument>> partitions = CollectionUtils.partition(sortedNamesAndSnippets, NUMBER_OF_PARTITIONS);
                    List<Tuple3<String, String, String>> partitionFirstEntrysTitle = new ArrayList<>();
                    for (List<ExtractedMathPDDocument> partition : partitions) {
                        partitionFirstEntrysTitle.add(new Tuple3<>(
                                partition.get(0).getTitle(),
                                partition.get(0).getName(),
                                partition.get(0).getPage()));
                    }
                    final List<List<ExtractedMathPDDocument>> overlappingPartitions = CollectionUtils.overlapInPercent(partitions, 0.25, 0.25);

                    //LOGGER.warn("overlappingPartitions number = {}", overlappingPartitions.size());

                    int i = 0;
                    for (List<ExtractedMathPDDocument> overlappingPartition : overlappingPartitions) {
                        //LOGGER.warn("merging partition with {} entries", overlappingPartition.size());
                        final ExtractedMathPDDocument mergedPartition = mergeToOne(overlappingPartition);

                        // we need to overwrite these properties to avoid duplicates later. the duplicates were introduced during creating overlapping partitions.
                        Tuple3<String, String, String> firstOriginalEntry = partitionFirstEntrysTitle.get(i++);
                        //mergedPartition.setField(firstOriginalEntry.f0, 0);
                        mergedPartition.setTitle(firstOriginalEntry.f0);
                        mergedPartition.setName(firstOriginalEntry.f1);
                        mergedPartition.setPage(firstOriginalEntry.f2);

                        collector.collect(mergedPartition);
                        //LOGGER.warn(mergedPartition.f0);
                    }
                });

        return extractedMathPdDocuments;
    }

    private static ExtractedMathPDDocument mergeToOne(List<ExtractedMathPDDocument> list) {
        final List<HashMap<String, Double>> allHistogramsCi = new ArrayList<>();
        final List<HashMap<String, Double>> allHistogramsCn = new ArrayList<>();
        final List<HashMap<String, Double>> allHistogramsCsymbol = new ArrayList<>();
        final List<HashMap<String, Double>> allHistogramsBvar = new ArrayList<>();
        String mainString = null;
        ExtractedMathPDDocument mainDoc = null;

        for (ExtractedMathPDDocument nameAndSnippet : list) {
            final String name = nameAndSnippet.getTitle();
            if (mainDoc == null) {
                mainDoc = nameAndSnippet;
                mainString = name;
            }

            allHistogramsCi.add(nameAndSnippet.getHistogramCi());
            allHistogramsCn.add(nameAndSnippet.getHistogramCn());
            allHistogramsCsymbol.add(nameAndSnippet.getHistogramCsymbol());
            allHistogramsBvar.add(nameAndSnippet.getHistogramBvar());
            mainDoc.text += nameAndSnippet.text;
        }

        mainDoc.setHistogramCi(Distances.histogramsPlus(allHistogramsCi));
        mainDoc.setHistogramCn(Distances.histogramsPlus(allHistogramsCn));
        mainDoc.setHistogramCsymbol(Distances.histogramsPlus(allHistogramsCsymbol));
        mainDoc.setHistogramBvar(Distances.histogramsPlus(allHistogramsBvar));

        return mainDoc;
    }

    private static DataSet<ExtractedMathPDDocument> aggregateSnippets(FlatMapOperator<String, ExtractedMathPDDocument> extractedMathPdSnippets) {
        if (NUMBER_OF_PARTITIONS >= 0) {
            return aggregateSnippetsToPartitions(extractedMathPdSnippets);
        } else if (NUMBER_OF_PARTITIONS == -1) {
            return aggregateSnippetsToSingleDocs(extractedMathPdSnippets);
        } else {
            throw new RuntimeException("illegal state: NUMBER_OF_PARTITIONS");
        }
    }

    /**
     * This function takes math pd snippets and converts them to single documents (by merging all snippets belonging to the same document)
     *
     * @param extractedMathPdSnippets
     * @return
     */
    private static DataSet<ExtractedMathPDDocument> aggregateSnippetsToSingleDocs(FlatMapOperator<String, ExtractedMathPDDocument> extractedMathPdSnippets) {
        DataSet<ExtractedMathPDDocument> ds = extractedMathPdSnippets;
        ReduceOperator<ExtractedMathPDDocument> extractedMathPdDocuments = ds
                .groupBy(new SelectTitle())
                .reduce((ReduceFunction<ExtractedMathPDDocument>) (t0, t1) -> {
                    t1.mergeOtherIntoThis(t0);
                    t1.setText("removed");
                    //LOGGER.info("merged {} into {}", new Object[]{t1.f0, t0.f0});
                    return t1;
                });

        return extractedMathPdDocuments;
    }

    public static void run(FlinkPdCommandConfig config) throws Exception {
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        final String preprocessedSourcesFiles = config.getDataset() + "_preprocessed";
        String preprocessedRefsFiles = config.getRef() + "_preprocessed";
        if (preprocessedRefsFiles.equals(preprocessedSourcesFiles)) {
            preprocessedRefsFiles += "2";
        }
        if (config.isText()) {

            DataSource<String> source = readWikiDump(config, env);
            DataSource<String> refs = readRefs(config, env);
            final FlatMapOperator<String, ExtractedMathPDDocument>
                    extractedMathPdSnippetsSources = source.flatMap(new TextExtractorMapper(true, true));
            // now for the refs
            final FlatMapOperator<String, ExtractedMathPDDocument>
                    extractedMathPdSnippetsRefs = refs.flatMap(new TextExtractorMapper(false, true));
            extractedMathPdSnippetsSources
                    .crossWithTiny(extractedMathPdSnippetsRefs)
                    .with(
                            (CrossFunction<ExtractedMathPDDocument, ExtractedMathPDDocument, Tuple3<String, String, Double>>)
                                    (cand, ref) -> {
                                        final CosineDistance similarity = new CosineDistance();
                                        return new Tuple3<>(cand.getTitle(), ref.getTitle(),
                                                similarity.apply(cand.getText(), ref.getPlainText()));
                                    })
                    .returns(new TypeHint<Tuple3<String, String, Double>>() {
                    })
                    .writeAsCsv(config.getOutputDir(), OVERWRITE);
        } else {
            if (config.isPreProcessingMode()) {
                DataSource<String> source = readWikiDump(config, env);
                DataSource<String> refs = readRefs(config, env);

                final FlatMapOperator<String, ExtractedMathPDDocument>
                        extractedMathPdSnippetsSources = source.flatMap(new TextExtractorMapper(true));

                // first, merge all pages of one doc to one doc
                DataSet<ExtractedMathPDDocument>
                        extractedMathPdDocumentsSources = aggregateSnippets(extractedMathPdSnippetsSources);

                // write to disk
                LOGGER.info("writing preprocessed input to disk at {}", preprocessedRefsFiles);
                extractedMathPdDocumentsSources
                        .writeAsFormattedText(
                                preprocessedSourcesFiles,
                                OVERWRITE,
                                (TextFormatter<ExtractedMathPDDocument>)
                                        PreprocessedExtractedMathPDDocumentMapper::getFormattedWritableText);

                // now for the refs
                final FlatMapOperator<String, ExtractedMathPDDocument>
                        extractedMathPdSnippetsRefs = refs.flatMap(new TextExtractorMapper(false));

                // first, merge all pages of one doc to one doc
                final DataSet<ExtractedMathPDDocument>
                        extractedMathPdDocumentsRefs = aggregateSnippets(extractedMathPdSnippetsRefs);

                // write to disk
                LOGGER.info("writing preprocesssed refs to disk at {}", preprocessedRefsFiles);
                extractedMathPdDocumentsRefs
                        .writeAsFormattedText(
                                preprocessedRefsFiles,
                                OVERWRITE,
                                (TextFormatter<ExtractedMathPDDocument>)
                                        PreprocessedExtractedMathPDDocumentMapper::getFormattedWritableText);
            } else {
                final DataSet<ExtractedMathPDDocument>
                        extractedMathPdDocumentsSources = readPreprocessedFile(preprocessedSourcesFiles, env)
                        .flatMap(new PreprocessedExtractedMathPDDocumentMapper());
                final DataSet<ExtractedMathPDDocument> extractedMathPdDocumentsRefs
                        = readPreprocessedFile(preprocessedRefsFiles, env).flatMap(new PreprocessedExtractedMathPDDocumentMapper());

                GroupReduceOperator<Tuple2<
                        Tuple2<String, ExtractedMathPDDocument>,
                        Tuple3<String, String, Double>>,
                        Tuple2<String, ExtractedMathPDDocument>> extractedMathPDDocsWithTFIDF = null;


                SortPartitionOperator distancesAndSectionPairs =
                        extractedMathPdDocumentsSources
                                .cross(extractedMathPdDocumentsRefs)
                                .map((MapFunction<
                                        Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument>,
                                        Tuple7<String, String, Double, Double, Double, Double, Double>>)
                                        d -> {
                                            if (d.f0 == null || d.f1 == null) {
                                                return null;
                                            }

                                            // Tuple4 contains (if cosine is used, the term distance actually means similarity, i.e.,
                                            // -1=opposite, 0=unrelated, 1=same doc
                                            // 1) total distance (accumulated distance of all others) - makes no sense in case of cosine distance
                                            // 2) numbers
                                            // 3) operators
                                            // 4) identifiers
                                            // 5) bound variables
                                            Tuple4<Double, Double, Double, Double> distanceAllFeatures;

                                            distanceAllFeatures = Distances.distanceRelativeAllFeatures(d.f0, d.f1);


                                            return new Tuple7<>(
                                                    d.f0.getId(),
                                                    d.f1.getId(),
                                                    Math.abs(distanceAllFeatures.f0) + Math.abs(distanceAllFeatures.f1)
                                                            + Math.abs(distanceAllFeatures.f2)
                                                            + Math.abs(distanceAllFeatures.f3),
                                                    distanceAllFeatures.f0,
                                                    distanceAllFeatures.f1,
                                                    distanceAllFeatures.f2,
                                                    distanceAllFeatures.f3
                                            );
                                        })
                                .returns(new TypeHint<Tuple7<String, String, Double, Double, Double, Double, Double>>() {
                                })
                                .sortPartition(1, Order.ASCENDING);
                distancesAndSectionPairs.writeAsCsv(config.getOutputDir(), OVERWRITE);

                // also merge all partitions together of all document pairs, by taking the min distance in any field
                final DataSet<Tuple7<String, String, Double, Double, Double, Double, Double>> minDistancesOfRemergedDocs = distancesAndSectionPairs
                        .map(new MapFunction<Tuple7<String, String, Double, Double, Double, Double, Double>, Tuple7<String, String, Double, Double, Double, Double, Double>>() {
                            @Override
                            public Tuple7<String, String, Double, Double, Double, Double, Double> map(Tuple7<String, String, Double, Double, Double, Double, Double> stringStringDoubleDoubleDoubleDoubleDoubleTuple7) throws Exception {
                                String id0 = stringStringDoubleDoubleDoubleDoubleDoubleTuple7.f0;
                                String id1 = stringStringDoubleDoubleDoubleDoubleDoubleTuple7.f1;
                                id0 = id0.substring(0, id0.lastIndexOf("/"));
                                id1 = id1.substring(0, id1.lastIndexOf("/"));

                                return new Tuple7<>(id0, id1, stringStringDoubleDoubleDoubleDoubleDoubleTuple7.f2,
                                        stringStringDoubleDoubleDoubleDoubleDoubleTuple7.f3,
                                        stringStringDoubleDoubleDoubleDoubleDoubleTuple7.f4,
                                        stringStringDoubleDoubleDoubleDoubleDoubleTuple7.f5,
                                        stringStringDoubleDoubleDoubleDoubleDoubleTuple7.f6);
                            }
                        })
                        .groupBy(0, 1)
                        .reduceGroup(new GroupReduceFunction<Tuple7<String, String, Double, Double, Double, Double, Double>, Tuple7<String, String, Double, Double, Double, Double, Double>>() {
                            @Override
                            public void reduce(Iterable<Tuple7<String, String, Double, Double, Double, Double, Double>> iterable, Collector<Tuple7<String, String, Double, Double, Double, Double, Double>> collector) throws Exception {
                                double f2 = Double.MAX_VALUE, f3 = Double.MAX_VALUE, f4 = Double.MAX_VALUE, f5 = Double.MAX_VALUE, f6 = Double.MAX_VALUE;
                                String s0 = null, s1 = null;
                                for (Tuple7<String, String, Double, Double, Double, Double, Double> cur : iterable) {
                                    if (s0 == null) {
                                        s0 = cur.f0;
                                    }
                                    if (s1 == null) {
                                        s1 = cur.f1;
                                    }
                                    f2 = Math.min(f2, cur.f2);
                                    f3 = Math.min(f2, cur.f3);
                                    f4 = Math.min(f2, cur.f4);
                                    f5 = Math.min(f2, cur.f5);
                                    f6 = Math.min(f2, cur.f6);
                                }
                                collector.collect(new Tuple7<>(
                                        s0, s1, f2, f3, f4, f5, f6
                                ));
                            }
                        });
                minDistancesOfRemergedDocs.writeAsCsv(
                        config.getOutputDir() + "_remergedbymindist", OVERWRITE);

                // we can now use the distances and section pairs dataset to aggregate the distances on document level in distance bins
                //noinspection Convert2Lambda
                DataSet binnedDistancesForPairs =
                        distancesAndSectionPairs
                                .reduceGroup(new GroupReduceFunction<
                                        Tuple7<String, String, Double, Double, Double, Double, Double>,
                                        Tuple5<String, String, Double, Double, Double>>() {
                                    @Override
                                    public void reduce(Iterable<Tuple7<String, String, Double, Double, Double, Double, Double>> iterable, Collector<Tuple5<String, String, Double, Double, Double>> collector) throws Exception {
                                        // histogram will contain as a key a tuple2 of the names of the two documents from the pair; and the bin
                                        // the value will be the frequency of that bin in that pair of documents
                                        final HashMap<Tuple4<String, String, Double, Double>, Double> histogramPairOfNameAndBinWithFrequency = new HashMap<>();
                                        final HashMap<Tuple2<String, String>, Double> histogramPairOfNameWithFrequency = new HashMap<>();

                                        for (Tuple7<String, String, Double, Double, Double, Double, Double> curPairWithDistances : iterable) {
                                            final String id0 = curPairWithDistances.f0;
                                            final String id1 = curPairWithDistances.f1;
                                            final String name0 = ExtractedMathPDDocument.getNameFromId(id0);
                                            final String name1 = ExtractedMathPDDocument.getNameFromId(id1);

                                            double distance = curPairWithDistances.f2
                                                    / 4.0; // take the accumulated distance and normalize it

                                            // the key3
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
                                            histogramPairOfNameWithFrequency.put(keyName,
                                                    histogramPairOfNameWithFrequency.getOrDefault(keyName, 0.0) + 1.0);
                                        }

                                        for (Tuple4<String, String, Double, Double> key : histogramPairOfNameAndBinWithFrequency.keySet()) {
                                            collector.collect(new Tuple5<>(key.f0, key.f1, key.f2, key.f3,
                                                    histogramPairOfNameAndBinWithFrequency.get(key)
                                                            / histogramPairOfNameWithFrequency.get(new Tuple2<>(key.f0, key.f1))));
                                        }
                                    }
                                })
                                .sortPartition(0, Order.ASCENDING)
                                .sortPartition(1, Order.ASCENDING);
                binnedDistancesForPairs.writeAsCsv(config.getOutputDir() + "_binned", OVERWRITE);
            }
        }
        env.execute(String.format("MathPD(IS_MODE_PREPROCESSING=%b)", config.isPreProcessingMode()));
    }

    private static double getBinBoundary(double value, double binWidth, boolean isLower) {
        double flooredDivision = Math.floor(value / binWidth);
        double binBoundary;

        if (isLower) {
            binBoundary = binWidth * flooredDivision;
        } else {
            binBoundary = binWidth * (flooredDivision + 1);
        }

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

    public static DataSource<String> readPreprocessedFile(String pathname, ExecutionEnvironment env) {
        Path filePath = new Path(pathname);
        TextInputFormat inp = new TextInputFormat(filePath);
        inp.setCharsetName("UTF-8");
        // env.read
        return env.readFile(inp, pathname);
    }

    public WikiDocumentOutput outDocFromText(FlinkPdCommandConfig config, String input) throws Exception {
        final TextAnnotatorMapper textAnnotatorMapper = new TextAnnotatorMapper(config);
        textAnnotatorMapper.open(null);
        final CreateCandidatesMapper candidatesMapper = new CreateCandidatesMapper(config);

        final ParsedWikiDocument parsedWikiDocument = textAnnotatorMapper.parse(input);
        return candidatesMapper.map(parsedWikiDocument);
    }

    public String runFromText(FlinkPdCommandConfig config, String input) throws Exception {
        final JsonSerializerMapper<Object> serializerMapper = new JsonSerializerMapper<>();
        return serializerMapper.map(outDocFromText(config, input));
    }

    public static class SelectTitle implements KeySelector<ExtractedMathPDDocument, String> {
        @Override
        public String getKey(ExtractedMathPDDocument w) {
            return w.title;
        }
    }
}
