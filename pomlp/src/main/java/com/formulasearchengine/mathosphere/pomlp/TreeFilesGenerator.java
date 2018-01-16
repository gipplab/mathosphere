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
        for ( int i = 1; i <= maxNumber; i++ ){
            try {
                JsonGouldiBean bean = loader.getGouldiJson(i);
                LOG.info("Start pre-processing of tex input.");
                bean.setOriginalTex( Utility.latexPreProcessing(bean.getOriginalTex()) );
                // use all available converters
                for ( Converters c : Converters.values() ){
                    if (!c.skip()) {
                        generate(i, bean, c);
                    }
                }
            } catch ( IOException e ){
                LOG.error("SKIP: " + i, e);
            }
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
