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
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.io.TextOutputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.operators.GroupReduceOperator;
import org.apache.flink.api.java.tuple.*;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

public class FlinkPd {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkPd.class);
    private static final boolean IS_MODE_PREPROCESSING = false;
    private static final int NUMBER_OF_PARTITIONS = 10; // if -1 then partitioning is disabled and it will just be one document merge (all snippets into one doc)
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
    private static DataSet<Tuple2<String, ExtractedMathPDDocument>> aggregateSnippetsToPartitions(FlatMapOperator<String, Tuple2<String, ExtractedMathPDDocument>> extractedMathPdSnippets) {
        DataSet<Tuple2<String, ExtractedMathPDDocument>> extractedMathPdDocuments = extractedMathPdSnippets
                .groupBy(0)
                .reduceGroup(new GroupReduceFunction<Tuple2<String, ExtractedMathPDDocument>, Tuple2<String, ExtractedMathPDDocument>>() {
                    @Override
                    public void reduce(Iterable<Tuple2<String, ExtractedMathPDDocument>> iterable, Collector<Tuple2<String, ExtractedMathPDDocument>> collector) throws Exception {
                        final List<Tuple2<String, ExtractedMathPDDocument>> sortedNamesAndSnippets = new ArrayList<>();
                        for (Tuple2<String, ExtractedMathPDDocument> nameAndSnippet : iterable) {
                            sortedNamesAndSnippets.add(nameAndSnippet);
                        }
                        LOGGER.info("sorting {} entries", sortedNamesAndSnippets.size());
                        Collections.sort(sortedNamesAndSnippets, new Comparator<Tuple2<String, ExtractedMathPDDocument>>() {
                            @Override
                            public int compare(Tuple2<String, ExtractedMathPDDocument> o1, Tuple2<String, ExtractedMathPDDocument> o2) {
                                return o1.f0.compareTo(o2.f0);
                            }
                        });

                        final List<List<Tuple2<String, ExtractedMathPDDocument>>> partitions = CollectionUtils.partition(sortedNamesAndSnippets, 10);
                        final List<List<Tuple2<String, ExtractedMathPDDocument>>> overlappingPartitions = CollectionUtils.overlap(partitions, 25, 25);

                        for (List<Tuple2<String, ExtractedMathPDDocument>> overlappingPartition : overlappingPartitions) {
                            LOGGER.info("merging partition with {} entries", overlappingPartition.size());
                            final Tuple2<String, ExtractedMathPDDocument> mergedPartition = mergeToOne(overlappingPartition);
                            collector.collect(mergedPartition);
                        }
                    }
                });

        return extractedMathPdDocuments;
    }

    private static Tuple2<String, ExtractedMathPDDocument> mergeToOne(List<Tuple2<String, ExtractedMathPDDocument>> list) {
        final List<HashMap<String, Double>> allHistogramsCi = new ArrayList<>();
        final List<HashMap<String, Double>> allHistogramsCn = new ArrayList<>();
        final List<HashMap<String, Double>> allHistogramsCsymbol = new ArrayList<>();
        final List<HashMap<String, Double>> allHistogramsBvar = new ArrayList<>();
        String mainString = null;
        ExtractedMathPDDocument mainDoc = null;

        for (Tuple2<String, ExtractedMathPDDocument> nameAndSnippet : list) {
            final String name = nameAndSnippet.f0;
            final ExtractedMathPDDocument snippet = nameAndSnippet.f1;
            if (mainDoc == null) {
                mainDoc = snippet;
                mainString = name;
            }

            allHistogramsCi.add(snippet.getHistogramCi());
            allHistogramsCn.add(snippet.getHistogramCn());
            allHistogramsCsymbol.add(snippet.getHistogramCsymbol());
            allHistogramsBvar.add(snippet.getHistogramBvar());
        }

        mainDoc.setHistogramCi(Distances.histogramsPlus(allHistogramsCi));
        mainDoc.setHistogramCn(Distances.histogramsPlus(allHistogramsCn));
        mainDoc.setHistogramCsymbol(Distances.histogramsPlus(allHistogramsCsymbol));
        mainDoc.setHistogramBvar(Distances.histogramsPlus(allHistogramsBvar));

        return new Tuple2<>(mainString, mainDoc);
    }

    private static DataSet<Tuple2<String, ExtractedMathPDDocument>> aggregateSnippets(FlatMapOperator<String, Tuple2<String, ExtractedMathPDDocument>> extractedMathPdSnippets) {
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
    private static DataSet<Tuple2<String, ExtractedMathPDDocument>> aggregateSnippetsToSingleDocs(FlatMapOperator<String, Tuple2<String, ExtractedMathPDDocument>> extractedMathPdSnippets) {
        DataSet<Tuple2<String, ExtractedMathPDDocument>> extractedMathPdDocuments = extractedMathPdSnippets
                .groupBy(0)
                .reduce(new ReduceFunction<Tuple2<String, ExtractedMathPDDocument>>() {
                    @Override
                    public Tuple2<String, ExtractedMathPDDocument> reduce(Tuple2<String, ExtractedMathPDDocument> t0, Tuple2<String, ExtractedMathPDDocument> t1) throws Exception {
                        t1.f1.mergeOtherIntoThis(t0.f1);
                        t1.f1.setText("removed");
                        LOGGER.info("merged {} into {}", new Object[]{t1.f0, t0.f0});
                        return t1;
                    }
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

        if (IS_MODE_PREPROCESSING) {
            DataSource<String> source = readWikiDump(config, env);
            DataSource<String> refs = readRefs(config, env);

            final FlatMapOperator<String, Tuple2<String, ExtractedMathPDDocument>> extractedMathPdSnippetsSources = source.flatMap(new TextExtractorMapper());

            // first, merge all pages of one doc to one doc
            DataSet<Tuple2<String, ExtractedMathPDDocument>> extractedMathPdDocumentsSources = aggregateSnippets(extractedMathPdSnippetsSources);

            // write to disk
            LOGGER.info("writing preprocesssed input to disk at {}", preprocessedRefsFiles);
            extractedMathPdDocumentsSources.writeAsFormattedText(preprocessedSourcesFiles,
                    new TextOutputFormat.TextFormatter<Tuple2<String, ExtractedMathPDDocument>>() {
                        @Override
                        public String format(Tuple2<String, ExtractedMathPDDocument> stringExtractedMathPDDocumentTuple2) {
                            return PreprocessedExtractedMathPDDocumentMapper.getFormattedWritableText(stringExtractedMathPDDocumentTuple2.f1);
                        }
                    });

            // now for the refs
            final FlatMapOperator<String, Tuple2<String, ExtractedMathPDDocument>> extractedMathPdSnippetsRefs = refs.flatMap(new TextExtractorMapper());

            // first, merge all pages of one doc to one doc
            final DataSet<Tuple2<String, ExtractedMathPDDocument>> extractedMathPdDocumentsRefs = aggregateSnippets(extractedMathPdSnippetsRefs);

            // write to disk
            LOGGER.info("writing preprocesssed refs to disk at {}", preprocessedRefsFiles);
            extractedMathPdDocumentsRefs.writeAsFormattedText(preprocessedRefsFiles,
                    new TextOutputFormat.TextFormatter<Tuple2<String, ExtractedMathPDDocument>>() {
                        @Override
                        public String format(Tuple2<String, ExtractedMathPDDocument> stringExtractedMathPDDocumentTuple2) {
                            return PreprocessedExtractedMathPDDocumentMapper.getFormattedWritableText(stringExtractedMathPDDocumentTuple2.f1);
                        }
                    });
        } else {
            final DataSet<Tuple2<String, ExtractedMathPDDocument>> extractedMathPdDocumentsSources = readPreprocessedFile(preprocessedSourcesFiles, env).flatMap(new PreprocessedExtractedMathPDDocumentMapper());
            final DataSet<Tuple2<String, ExtractedMathPDDocument>> extractedMathPdDocumentsRefs = readPreprocessedFile(preprocessedRefsFiles, env).flatMap(new PreprocessedExtractedMathPDDocumentMapper());

            GroupReduceOperator<Tuple2<Tuple2<String, ExtractedMathPDDocument>, Tuple3<String, String, Double>>, Tuple2<String, ExtractedMathPDDocument>> extractedMathPDDocsWithTFIDF = null;


            DataSet distancesAndSectionPairs =
                    extractedMathPdDocumentsSources
                            /*.groupBy(0)
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
                            })*/
                            .map(new MapFunction<Tuple2<String, ExtractedMathPDDocument>, ExtractedMathPDDocument>() {
                                @Override
                                public ExtractedMathPDDocument map(Tuple2<String, ExtractedMathPDDocument> stringExtractedMathPDDocumentTuple2) throws Exception {
                                    return stringExtractedMathPDDocumentTuple2.f1;
                                }
                            })
                            .cross(extractedMathPdDocumentsRefs
                                            .map(new MapFunction<Tuple2<String, ExtractedMathPDDocument>, ExtractedMathPDDocument>() {
                                                @Override
                                                public ExtractedMathPDDocument map(Tuple2<String, ExtractedMathPDDocument> stringExtractedMathPDDocumentTuple2) throws Exception {
                                                    return stringExtractedMathPDDocumentTuple2.f1;
                                                }
                                            })
                                    /*.groupBy(0)
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
                                    })*/
                            )
                            .reduceGroup(new GroupReduceFunction<Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument>, Tuple7<String, String, Double, Double, Double, Double, Double>>() {
                                @Override
                                public void reduce(Iterable<Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument>> iterable, Collector<Tuple7<String, String, Double, Double, Double, Double, Double>> collector) throws Exception {
                                    for (Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument> i : iterable) {
                                        if (i.f0 == null || i.f1 == null)
                                            continue;

                                        // skip one diagonal half of the matrix
                                        //if (!i.f0.getId().contains("Original"))
                                        //    continue;

                                        // only check Original against Plagiarism (not against other Originals)
                                        //if (!i.f1.getId().contains("Plagiarism"))
                                        //    continue;

                                        // Tuple4 contains (if cosine is used, the term distance actually means similarity, i.e.,
                                        // -1=opposite, 0=unrelated, 1=same doc
                                        // 1) total distance (accumulated distance of all others) - makes no sense in case of cosine distance
                                        // 2) numbers
                                        // 3) operators
                                        // 4) identifiers
                                        // 5) bound variables
                                        Tuple4<Double, Double, Double, Double> distanceAllFeatures;

                                        distanceAllFeatures = Distances.distanceRelativeAllFeatures(i.f0, i.f1);


                                        final Tuple7<String, String, Double, Double, Double, Double, Double> resultLine = new Tuple7<>(
                                                i.f0.getId(),
                                                i.f1.getId(),
                                                Math.abs(distanceAllFeatures.f0) + Math.abs(distanceAllFeatures.f1) + Math.abs(distanceAllFeatures.f2) + Math.abs(distanceAllFeatures.f3),
                                                distanceAllFeatures.f0,
                                                distanceAllFeatures.f1,
                                                distanceAllFeatures.f2,
                                                distanceAllFeatures.f3
                                        );

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

                                        double distance = curPairWithDistances.f2 / 4.0; // take the accumulated distance and normalize it

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
        }

        env.execute(String.format("MathPD(IS_MODE_PREPROCESSING=%b)", config.isPreProcessingMode()));
    }

    private static double getBinBoundary(double value, double binWidth, boolean isLower) {
        double flooredDivision = Math.floor(value / binWidth);
        double binBoundary;

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

    public static DataSource<String> readPreprocessedFile(String pathname, ExecutionEnvironment env) {
        Path filePath = new Path(pathname);
        TextInputFormat inp = new TextInputFormat(filePath);
        inp.setCharsetName("UTF-8");
        // env.read
        return env.readFile(inp, pathname);
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
