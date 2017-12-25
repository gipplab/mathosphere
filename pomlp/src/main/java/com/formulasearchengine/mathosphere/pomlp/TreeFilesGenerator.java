package com.formulasearchengine.mathosphere.pomlp;

import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
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
                conv.getParser().init();
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
            Path outputF = converter.getSubPath().resolve(number+ converter.fileEnding() );
            converter.getParser().parseToFile( tex, outputF );
        } catch ( Exception e ){
            LOG.error("Cannot generate file: " + number + " via " + converter.name());
        }
    }

    public void generate( int number, Converters converter ) throws Exception{
        JsonGouldiBean bean = loader.getGouldiJson(number);
        generate(number, bean, converter);
    }

    public void generatePomXMLs(){
        for ( int i = 1; i <= maxNumber; i++ ){
            try {
                JsonGouldiBean bean = loader.getGouldiJson(i);
                generate( i, bean, Converters.POM );
            } catch ( Exception e ){
                LOG.error("SKIPPED ->Cannot write POM-XML " + i + "<- SKIPPED", e);
            }
        }
    }

    public void generateSnuggleTexMMLs(){
        for ( int i = 1; i <= maxNumber; i++ ){
            try {
                JsonGouldiBean bean = loader.getGouldiJson(i);
                generate( i, bean, Converters.SnuggleTeX );
            } catch ( Exception e ){
                LOG.error("SKIPPED ->Cannot write POM-XML " + i + "<- SKIPPED", e);
            }
        }
    }

    /**
     * Generates all third party MMLs at once
     */
    public void generateAllSubs(){
        for ( int i = 1; i <= maxNumber; i++ ){
            try {
                JsonGouldiBean bean = loader.getGouldiJson(i);
                for ( Converters c : Converters.values() ){
                    if (!c.skip()) generate(i, bean, c);
                }
            } catch ( IOException e ){
                LOG.error("SKIP: " + i, e);
            }

        }
    }

    public static void main(String[] args) throws Exception{
        TreeFilesGenerator gen = new TreeFilesGenerator();
        gen.init();
        gen.generateAllSubs();
    }
}
