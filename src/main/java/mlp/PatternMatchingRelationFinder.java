package mlp;

import mlp.contracts.JsonSerializerMapper;
import mlp.contracts.PatternMatcherMapper;
import mlp.contracts.TextAnnotatorMapper;
import mlp.contracts.TextExtractorMapper;
import mlp.pojos.IndentifiersRepresentation;
import mlp.pojos.WikiDocument;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.core.fs.Path;

public class PatternMatchingRelationFinder {

    public static void main(String[] args) throws Exception {
        Config config = Config.test();

        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<String> source = readWikiDump(config, env);
        DataSet<WikiDocument> documents = source.flatMap(new TextExtractorMapper())
                                                .map(new TextAnnotatorMapper(config.getModel()));

        DataSet<IndentifiersRepresentation> relations = documents.map(new PatternMatcherMapper());
        relations.map(new JsonSerializerMapper<>())
                 .writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);

        env.execute("Pattern Matcher Relation Finder");
    }

    public static DataSource<String> readWikiDump(Config config, ExecutionEnvironment env) {
        Path filePath = new Path(config.getDataset());
        TextInputFormat inp = new TextInputFormat(filePath);
        inp.setCharsetName("UTF-8");
        inp.setDelimiter("</page>");
        return env.readFile(inp, config.getDataset());
    }

}
