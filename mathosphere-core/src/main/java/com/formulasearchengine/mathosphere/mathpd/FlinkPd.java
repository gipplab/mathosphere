package com.formulasearchengine.mathosphere.mathpd;

import com.formulasearchengine.mathmlquerygenerator.xmlhelper.NonWhitespaceNodeList;
import com.formulasearchengine.mathosphere.mathpd.cli.FlinkPdCommandConfig;
import com.formulasearchengine.mathosphere.mathpd.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mathpd.pojos.ArxivDocument;
import com.formulasearchengine.mathosphere.mlp.contracts.CreateCandidatesMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.google.common.collect.Multiset;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeutils.base.array.StringArraySerializer;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.Collector;
import org.w3c.dom.Node;

public class FlinkPd {

    public static void main(String[] args) throws Exception {
        FlinkPdCommandConfig config = FlinkPdCommandConfig.from(args);
        run(config);
    }

    public static void run(FlinkPdCommandConfig config) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<String> source = readWikiDump(config, env);
        DataSource<String> refs = readRefs(config, env);
        source.flatMap(new TextExtractorMapper()).cross(refs.flatMap(new TextExtractorMapper()))
                .reduceGroup(new GroupReduceFunction<Tuple2<ArxivDocument, ArxivDocument>, Tuple2<Integer, String>>() {
                    @Override
                    public void reduce(Iterable<Tuple2<ArxivDocument, ArxivDocument>> iterable, Collector<Tuple2<Integer, String>> collector) throws Exception {
                        for (Tuple2<ArxivDocument, ArxivDocument> i : iterable) {
                            NonWhitespaceNodeList allMathExpressionsF0 = i.f1.getMathTags();
                            for(Node n : allMathExpressionsF0){
                                Multiset<String> contentNodesF0 = i.f1.getCElements(n);
                                System.out.println(contentNodesF0.toString());
                            }
                            System.out.println("cur comparison is finished now "+i.f0.title+" " +i.f1.title);

                            final Multiset<String> elements = i.f1.getCElements();
                            elements.removeAll(i.f0.getCElements());
                            collector.collect(new Tuple2(elements.size(), i.f1.title + "-" + i.f0.title));
                        }
                    }
                })
                .writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);
        final int parallelism = config.getParallelism();
        if (parallelism > 0) {
            env.setParallelism(parallelism);
        }
        env.execute("Relation Finder");
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


}
