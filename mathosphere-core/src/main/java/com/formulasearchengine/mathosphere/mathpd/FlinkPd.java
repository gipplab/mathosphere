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
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple6;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.Collector;

public class FlinkPd {
    protected static final Log LOG = LogFactory.getLog(FlinkPd.class);

    public static void main(String[] args) throws Exception {
        FlinkPdCommandConfig config = FlinkPdCommandConfig.from(args);
        run(config);
    }

    public static void run(FlinkPdCommandConfig config) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<String> source = readWikiDump(config, env);
        DataSource<String> refs = readRefs(config, env);

        // TODO: cross product or reduce function can be enhanced by leaving out all duplicates of the matrix because d(a,b) == d(b,a)

        //noinspection Convert2Lambda
        source.flatMap(new TextExtractorMapper()).cross(refs.flatMap(new TextExtractorMapper()))
                .reduceGroup(new GroupReduceFunction<Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument>, Tuple6<Double, Double, Double, Double, Double, String>>() {
                    @Override
                    public void reduce(Iterable<Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument>> iterable, Collector<Tuple6<Double, Double, Double, Double, Double, String>> collector) throws Exception {
                        for (Tuple2<ExtractedMathPDDocument, ExtractedMathPDDocument> i : iterable) {
                            if (i.f0 == null || i.f1 == null)
                                continue;

                            if (i.f0.getId().contains("Plagiarism") && i.f1.getId().contains("Original") ||
                                    i.f0.getId().contains("Original") && i.f1.getId().contains("Plagiarism")) {

                                final Tuple4<Double, Double, Double, Double> distanceAbsoluteAllFeatures = Distances.distanceRelativeAllFeatures(i.f0, i.f1);
                                final Tuple6<Double, Double, Double, Double, Double, String> resultLine = new Tuple6<>(
                                        distanceAbsoluteAllFeatures.f0 + distanceAbsoluteAllFeatures.f1 + distanceAbsoluteAllFeatures.f2 + distanceAbsoluteAllFeatures.f3,
                                        distanceAbsoluteAllFeatures.f0,
                                        distanceAbsoluteAllFeatures.f1,
                                        distanceAbsoluteAllFeatures.f2,
                                        distanceAbsoluteAllFeatures.f3,
                                        i.f0.getId() + "-" + i.f1.getId());

                                collector.collect(resultLine);
                            }
                        }
                    }
                })
                //.writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);
                .writeAsCsv(config.getOutputDir(), WriteMode.OVERWRITE);
        final int parallelism = config.getParallelism();
        if (parallelism > 0) {
            env.setParallelism(parallelism);
        }
        env.execute("Relation Finder");
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
