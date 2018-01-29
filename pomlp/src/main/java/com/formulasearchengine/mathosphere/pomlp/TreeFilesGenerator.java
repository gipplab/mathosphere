package com.formulasearchengine.mathosphere.pomlp;

import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import com.formulasearchengine.mathosphere.pomlp.convertor.Parser;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.util.POMLoader;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import com.formulasearchengine.nativetools.CommandExecutor;
import com.formulasearchengine.nativetools.NativeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.formulasearchengine.mathosphere.pomlp.util.Constants.NL;

public class TreeFilesGenerator {
    private static final Logger LOG = LogManager.getLogger( TreeFilesGenerator.class.getName() );

    public static final String DATA_SUB_DIR = "generated";

    private GoldStandardLoader loader = GoldStandardLoader.getInstance();
    private int maxNumber;

    private static final Duration[] TIMER = new Duration[Converters.values().length];
    private static final int[] EXCEPTIONS = new int[Converters.values().length];

    private Path gouldiLibPath;

    private TreeFilesGenerator(){}

    private void init() throws IOException {
        LOG.info("Load gold standard.");
        maxNumber = loader.initLocally();
        String pathStr = ConfigLoader.CONFIG.getProperty( ConfigLoader.GOULDI_LOCAL_PATH );
        gouldiLibPath = Paths.get( pathStr );

        if ( !Files.exists( gouldiLibPath ) ){
            LOG.error("Cannot find Gouldi-Data file: " + gouldiLibPath.toAbsolutePath().toString());
            return;
        }

        gouldiLibPath.resolve( DATA_SUB_DIR );
        if ( !Files.exists(gouldiLibPath) ) Files.createDirectory(gouldiLibPath);

        LOG.debug("Gouldi-Lib-Path: " + gouldiLibPath.toAbsolutePath().toString());
        LOG.info("Init convertors...");
        for ( Converters conv : Converters.values() ){
            LOG.debug("Init: " + conv.name());
            Path subPath = conv.initSubPath( gouldiLibPath );
            if (!Files.exists(subPath)){
                LOG.info("Create directory " + subPath.toString());
                Files.createDirectory( subPath );
            }
            try{
                if ( !conv.skip() ) conv.getParser().init();
            } catch ( Exception e ){
                LOG.error( "Cannot initiate " + conv.name() + " converter!" );
            }
        }

        LOG.info("Files generator is ready to use.");
    }


    public void generate( int number, JsonGouldiBean bean, Converters converter ) throws Exception
    {
        try {
            LOG.info("Generate [" + number + ": " + converter.name()+"]!");
            String tex = bean.getCorrectedTex();
            Path outputF = converter.getSubPath().resolve(number+ converter.fileEnding() );
            converter.getParser().parseToFile( tex, outputF );
        } catch ( Exception e ){
            LOG.error("Cannot generate file: " + number + " via " + converter.name(),e);
            throw e;
        }
    }

    public void generate( int number, Converters converter ) throws Exception {
        JsonGouldiBean bean = loader.getGouldiJson(number);
        LOG.debug("Start generating");
        generate(number, bean, converter);
    }

    /**
     * Generates all third party MMLs at once
     */
    public void generateAllSubs(){
        ExecutorService service = Executors.newSingleThreadExecutor();

        for ( Converters converter : Converters.values() ){
            if ( converter.skip() ) continue;
            LOG.info("Register new service for " + converter.name());

            service.submit(() -> {
                Thread.currentThread().setName("Thread-Converter-" + converter.name());
                LOG.info("Start converting all files. Start timer.");
                Instant start = Instant.now();

                for ( int i = 1; i <= maxNumber; i++ ){
                    try {
                        generate( i, converter );
                    } catch ( Exception e ){
                        LOG.error("Well done.", e);
                        EXCEPTIONS[converter.getPosition()]++;
                    }
                }

                Instant stop = Instant.now();
                Duration duration = Duration.between( start, stop );
                LOG.info("Done, converted all files. Total time: {}ms.", duration.toMillis());
                TIMER[converter.getPosition()] = duration;
            });
        }

        try {
            LOG.info("Shutdown thread pool from new requests.");
            service.shutdown();
            LOG.info("Wait at most 10 minutes to finish all processes.");
            service.awaitTermination( 10, TimeUnit.MINUTES );
        } catch ( InterruptedException ie ){
            LOG.error("Interrupted exception, looks like the service needed longer than 10 minutes to finish all tasks.", ie);
        }

        LOG.info("Done, all files were created. Write overview.");
        Path outputPath = Paths.get( "results", "generation-overview.csv" );
        StringBuilder strB = new StringBuilder();
        strB.append("Converter, Duration, Exceptions").append( NL );
        for ( Converters conv : Converters.values() ){
            if ( conv.skip() ) continue;
            strB
                    .append(conv.name()).append(", ")
                    .append(TIMER[conv.getPosition()]).append(", ")
                    .append(EXCEPTIONS[conv.getPosition()]).append(NL);
        }
        try {
            Files.write( outputPath, strB.toString().getBytes() );
        } catch ( IOException ioe ){
            LOG.error("Cannot create overview file.", ioe);
        }
    }

    public static void main(String[] args) throws Exception{
        // TODO: WILL GENERATE AND OVERWRITE ALL MML FILES BY THIRD PARTY TOOLS
        // TODO: DO NOT START IF YOU DON'T KNOW WHAT YOU ARE DOING HERE!
        preStartCommandCheck();
        TreeFilesGenerator gen = new TreeFilesGenerator();
        gen.init();
        gen.generateAllSubs();
    }

    /**
     * Checks the
     */
    public static void preStartCommandCheck(){
        Parser p;
        CommandExecutor executor;
        NativeResponse res;
        LOG.info("Check availability of native programs.");
        for (Converters convs : Converters.values()){
            p = convs.getParser();
            if ( p != null && p.getNativeCommand() != null ){
                executor = new CommandExecutor( "DefinitionCheck", "which", p.getNativeCommand() );
                res = executor.exec( 100 );
                if ( res.getStatusCode() != 0 ){
                    convs.setSkipMode( true );
                    LOG.info(convs.name() + " not available => Deactivate!");
                } else {
                    LOG.debug("Successfully checked " + convs.name());
                }
            }
        }

        try {
            LOG.info("Check POM-Tagger!");
            POMLoader pom = new POMLoader();
            pom.init();
            pom.parse("a"); // test pars
        } catch ( Exception e ){
            LOG.info("Cannot call POM-Tagger: " + e.getMessage());
            LOG.trace("Reason: ", e);
            Converters.POM.setSkipMode(true);
        }
    }
}
