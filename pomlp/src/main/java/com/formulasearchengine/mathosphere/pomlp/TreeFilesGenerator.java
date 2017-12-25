package com.formulasearchengine.mathosphere.pomlp;

import com.formulasearchengine.mathosphere.pomlp.convertor.MathParser;
import com.formulasearchengine.mathosphere.pomlp.convertor.ParseAndExportable;
import com.formulasearchengine.mathosphere.pomlp.convertor.SnuggleTexConverter;
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

    private MathParser pomParser;

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
                conv.parser.init();
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
            Path outputF = converter.subPath.resolve(number+ converter.fileEnding );
            converter.parser.parseToFile( tex, outputF );
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
                    generate(i, bean, c);
                }
            } catch ( IOException e ){
                LOG.error("SKIP: " + i, e);
            }

        }
    }

    private enum Converters{
        POM("pom", ".xml", new MathParser()),
        SnuggleTeX("snuggletex", ".mml", new SnuggleTexConverter());

        private final String name;
        private final String fileEnding;
        private final ParseAndExportable parser;
        private Path subPath;

        Converters( String name, String fileEnding, ParseAndExportable parser ){
            this.name = name;
            this.fileEnding = fileEnding;
            this.parser = parser;
        }

        public Path initSubPath( Path baseDir ){
            subPath = baseDir.resolve(name);
            return subPath;
        }
    }

    public static void main(String[] args) throws Exception{
        TreeFilesGenerator gen = new TreeFilesGenerator();
        gen.init();
        gen.generateAllSubs();
    }
}
