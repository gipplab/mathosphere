package mlp;

import mlp.contracts.CreateCandidatesMapper;
import mlp.contracts.PatternMatcherMapper;
import mlp.contracts.TextAnnotatorMapper;
import mlp.contracts.TextExtractorMapper;
import mlp.pojos.Relation;
import mlp.pojos.WikiDocument;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.Path;

public class RelationFinder {

    public static void main(String[] args) throws Exception {
        Config config = Config.test();

        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<String> source = readWikiDump(config, env);
        DataSet<WikiDocument> documents = source.flatMap(new TextExtractorMapper())
                                                .map(new TextAnnotatorMapper(config.getModel()));

//        DataSet<Relation> foundRelations = 
//                        documents.flatMap(new CreateCandidatesMapper(config))
//                                 .filter(rel -> rel.getScore() > config.getThreshold());

        documents.flatMap(new PatternMatcherMapper()).print();

        env.execute("Relation Finder");
    }

    public static DataSource<String> readWikiDump(Config config, ExecutionEnvironment env) {
        Path filePath = new Path(config.getDataset());
        TextInputFormat wikiDocumentEmitter = new TextInputFormat(filePath);
        wikiDocumentEmitter.setCharsetName("UTF-8");
        wikiDocumentEmitter.setDelimiter("</page>");
        return env.readFile(wikiDocumentEmitter, config.getDataset());
    }

    
}
