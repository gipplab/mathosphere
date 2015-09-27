package mlp;

import mlp.contracts.CreateCandidatesMapper;
import mlp.contracts.JsonSerializerMapper;
import mlp.contracts.TextAnnotatorMapper;
import mlp.contracts.TextExtractorMapper;
import mlp.pojos.ParsedWikiDocument;
import mlp.pojos.WikiDocumentOutput;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.core.fs.Path;

public class RussianMlpRelationFinder {

  public static void main(String[] args) throws Exception {
    String[] params = {"-in", "c:/tmp/mlp/ru", "-out", "c:/tmp/mlp/ru-out", "--language", "ru"};
    Config config = Config.from(params);

    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    DataSource<String> source = readWikiDump(config, env);
    DataSet<ParsedWikiDocument> documents =
      source.flatMap(new TextExtractorMapper())
        .map(new TextAnnotatorMapper(config));

    DataSet<WikiDocumentOutput> result = documents.map(new CreateCandidatesMapper(config));
    result.map(new JsonSerializerMapper<>())
      .writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);

    env.execute("Relation Finder");
  }

  public static DataSource<String> readWikiDump(Config config, ExecutionEnvironment env) {
    Path filePath = new Path(config.getDataset());
    TextInputFormat inp = new TextInputFormat(filePath);
    inp.setCharsetName("UTF-8");
    inp.setDelimiter("</page>");
    return env.readFile(inp, config.getDataset());
  }


}
