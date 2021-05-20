package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienListConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.CreateCandidatesMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.WikiTextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.WikiTextPageExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextOutputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.core.fs.FileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A combination of the {@link RelationExtractor} and {@link MachineLearningModelGenerator}.
 * The idea is to provide a program that can extract and list all identifier-definien
 * pairs without using a gold standard for comparison.
 *
 * @author Andre Greiner-Petter
 */
public class MachineLearningRelationExtractor {

    private static final Logger LOG = LogManager.getLogger( MachineLearningRelationExtractor.class.getName() );

    private static final String NL = System.lineSeparator();

    private static final String OUTPUT_FILE_NAME = "extractions.csv";

    public static void start( MachineLearningDefinienListConfig config ){
        LOG.info("Start machine learning approach for listing identifier-definien pairs");
        // first, create a flink environment
        ExecutionEnvironment flinkEnv = ExecutionEnvironment.getExecutionEnvironment();
        flinkEnv.setParallelism( config.getParallelism() );

        LOG.debug("Read wikidump via flink");
        DataSource<String> dataSource = FlinkMlpRelationFinder.readWikiDump( config, flinkEnv );

        LOG.debug("Parse documents via flink");
        FlatMapOperator<String, RawWikiDocument> mapOperator = dataSource.flatMap(new WikiTextPageExtractorMapper());

        LOG.debug("Open text annotator mapper");
        WikiTextAnnotatorMapper annotatorMapper = new WikiTextAnnotatorMapper(config);
        // ML approach doesn't create PosTagger here ... strange, so I will use it now.
        annotatorMapper.open(null);
        DataSet<ParsedWikiDocument> parsedDocuments = mapOperator.map( annotatorMapper );

        LOG.debug("Create feature Extractor without Gouldi");
        CreateCandidatesMapper candidatesMapper = new CreateCandidatesMapper(config);
        DataSet<WikiDocumentOutput> outputDataSet = parsedDocuments.map( candidatesMapper );

        LOG.debug("Map to output format.");
        RelationMapper outputMapper = new RelationMapper();
        DataSet<LinkedList<String[]>> outputs = outputDataSet.map(outputMapper);

        Path outputPath = Paths.get(config.getOutputDir(), OUTPUT_FILE_NAME);
        LOG.info("Write output file " + outputPath.toString() );
        outputs.writeAsFormattedText(
            outputPath.toString(),
            FileSystem.WriteMode.OVERWRITE,
            new OutputFormatter()
        ).setParallelism(1);

        try {
            flinkEnv.execute();
        } catch (Exception e) {
            LOG.error("Error due execution of flink process.", e);
        }
    }

    private static class RelationMapper implements MapFunction<WikiDocumentOutput, LinkedList<String[]>> {
        @Override
        public LinkedList<String[]> map(WikiDocumentOutput wikiDocumentOutput) {
            LinkedList<String[]> relationArray = new LinkedList<>();
            relationArray.add( new String[]{ wikiDocumentOutput.getTitle() } );

            List<Relation> relations = wikiDocumentOutput.getRelations();
            for (Relation r : relations) {
                String[] record = {r.getIdentifier(), r.getDefinition(), Double.toString(r.getScore())};
                relationArray.add( record );
            }

            return relationArray;
        }
    }

    private static class OutputFormatter implements TextOutputFormat.TextFormatter<LinkedList<String[]>> {
        @Override
        public String format(LinkedList<String[]> in) {
            StringBuffer buffer = new StringBuffer( in.size()*3 );
            buffer.append( in.removeFirst()[0] ).append(NL);

            while ( !in.isEmpty() ){
                String tmp = Arrays.toString(in.removeFirst());
                buffer.append(tmp.substring(1, tmp.length() - 1)).append(NL);
            }

            return buffer.append(NL).toString();
        }
    }

    /*
    private static PrintWriter createPrinter( FlinkMlpCommandConfig config ) {
        if (StringUtils.isNotBlank(config.getOutputDir())) {
            Path outFilePath = Paths.get( config.getOutputDir() ).resolve( OUTPUT_FILE_NAME );
            try {
                return new PrintWriter(new FileOutputStream( outFilePath.toString(), false ));
            } catch ( FileNotFoundException ioe ){
                LOG.error("Cannot write to file. Switch to console mode.", ioe);
                return new PrintWriter(System.out);
            }
        }

        LOG.info("No output directory specified -> printing to console.");
        return new PrintWriter(System.out);
    }*/

    /*
    public static int counter = 1;

    private static DataSet<WikiDocumentOutput> writeMLPResults( final FlinkMlpCommandConfig flinkConfig, DataSet<WikiDocumentOutput> dataSetWikiOuts ){
        return dataSetWikiOuts.map(
                (MapFunction<WikiDocumentOutput,WikiDocumentOutput>) wikiDocumentOutput
                        -> {
                    LOG.debug("Create not printer task and write current results of MLP to files.");
                    try (PrintWriter pw = createPrintWriter(flinkConfig)) {
                        LOG.info("Write WikiDocumentOutput information " + counter);
                        List<Relation> relations = wikiDocumentOutput.getRelations();
                        CSVPrinter printer = CSVFormat.DEFAULT.withRecordSeparator( System.lineSeparator() ).print(pw);
                        for (Relation r : relations) {
                            String[] record = {r.getIdentifier(), r.getDefinition(), Double.toString(r.getScore())};
                            printer.printRecord(record);
                        }
                        printer.flush();
                        pw.flush();
                    } catch ( IOException ioe ){
                        LOG.error("Cannot write results from the MLP process.", ioe);
                    }
                    return wikiDocumentOutput;
                });
    }

    private static PrintWriter createPrintWriter(FlinkMlpCommandConfig flinkConfig) throws IOException {
        Path outputDir = Paths.get(flinkConfig.getOutputDir());
        if (!Files.exists(outputDir) )
            Files.createDirectory(outputDir);

        Path outputF = outputDir.resolve("OutputFromMLP-" + (counter++) + ".csv");
        if (!Files.exists(outputF) )
            Files.createFile( outputF );

        return new PrintWriter(outputF.toFile());
    }
    */

    /*
    SimpleFeatureExtractorMapper featureExtractorMapper = new SimpleFeatureExtractorMapper(config, null);
        DataSet<WikiDocumentOutput> outputDocuments = parsedDocuments.map(featureExtractorMapper);

        try {
            LOG.debug("Reduce groups by machine learning weka api");
            WekaLearner learner = new WekaLearner(config);
            DataSet<EvaluationResult> evaluationResults = outputDocuments.reduceGroup( learner );

            LOG.debug("Write results to the tmp.txt output file.");
            evaluationResults
                    .map( new JsonSerializerMapper<>() )
                    .writeAsText(
                            config.getOutputDir() + File.separator + "tmp.txt",
                            FileSystem.WriteMode.OVERWRITE
                    );

            LOG.info("Execute flink environment");
            flinkEnv.execute();
        } catch ( Exception e ){
            LOG.error("Cannot execute flink environment.", e);
        }
     */

    /*
    LOG.info("Find corresponding gold ideas, just for the weka learner");
        try {
            final ArrayList<GoldEntry> gold = (new Evaluator()).readGoldEntries(new File(config.getGoldFile()));
            outputDocuments = outputDocuments.map(new MapFunction<WikiDocumentOutput, WikiDocumentOutput>() {
                @Override
                public WikiDocumentOutput map(WikiDocumentOutput wikiDocumentOutput) {
                    try{
                        GoldEntry entry = GoldUtil.getGoldEntryByTitle( gold, wikiDocumentOutput.getTitle() );
                        LOG.info("Found gold entry by title: " + entry.getqID());
                        wikiDocumentOutput.setqId(entry.getqID());
                    } catch ( Exception e ){
                        LOG.warn("Cannot find qID for " + wikiDocumentOutput.getTitle(), e);
                    }
                    return wikiDocumentOutput;
                }
            });
        } catch ( IOException ioe ){
            LOG.error("Cannot add gold qID to each wiki document.");
        }
     */
}
