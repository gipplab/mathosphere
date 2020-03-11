package com.formulasearchengine.mathosphere.pomlp;

import com.formulasearchengine.mathmltools.nativetools.CommandExecutor;
import com.formulasearchengine.mathmltools.nativetools.NativeResponse;
import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import com.formulasearchengine.mathosphere.pomlp.convertor.Parser;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.util.POMLoader;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TreeFilesGenerator {
    private static final Logger LOG = LogManager.getLogger( TreeFilesGenerator.class.getName() );

    public static final String DATA_SUB_DIR = "generated";

    private GoldStandardLoader loader = GoldStandardLoader.getInstance();
    private int maxNumber;

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


    public void generate( int number, JsonGouldiBean bean, Converters converter )
    {
        try {
            LOG.info("Generate [" + number + ": " + converter.name()+"]!");
            String tex = bean.getOriginalTex();
            //tex = Utility.latexPreProcessing(tex);
            Path outputF = converter.getSubPath().resolve(number+ converter.fileEnding() );
            converter.getParser().parseToFile( tex, outputF );
        } catch ( Exception e ){
            LOG.error("Cannot generate file: " + number + " via " + converter.name(),e);
        }
    }

    public void generate( int number, Converters converter ) throws Exception{
        JsonGouldiBean bean = loader.getGouldiJson(number);
        generate(number, bean, converter);
    }

    /**
     * Generates all third party MMLs at once
     */
    public void generateAllSubs(){
//        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(16);

        // each number 1-300
        for ( int i = 1; i <= maxNumber; i++ ){
            try {
                JsonGouldiBean bean = loader.getGouldiJson(i);
                LOG.info("Start pre-processing of tex input.");
                bean.setMathTex( Utility.latexPreProcessing(bean.getOriginalTex()) );
                // each converter which is available
                for ( Converters c : Converters.values() ){
                    final int index = i;
                    if (!c.skip()) {
//                        executor.submit( () -> generate(index, bean, c) );
                        generate(index, bean, c);
                    }
                }
            } catch ( IOException e ){
                LOG.error("SKIP: " + i, e);
            }
        }

//        LOG.info("Shutdown thread pool. All threads in queue.");
//        executor.shutdown();
//        LOG.info("Wait for termination now.");
//        try {
//            executor.awaitTermination( 10, TimeUnit.MINUTES );
//            LOG.info("Finished all threads in thread pool.");
//        } catch ( InterruptedException ie ){
//            LOG.error( "Waited 10 Minutes but still the thread pool is not terminated.", ie );
//            LOG.error( "Shutdown now!" );
//            executor.shutdownNow();
//        }
    }

    public static void main(String[] args) throws Exception{
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
