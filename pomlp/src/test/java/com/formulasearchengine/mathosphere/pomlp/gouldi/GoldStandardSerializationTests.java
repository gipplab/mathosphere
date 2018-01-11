package com.formulasearchengine.mathosphere.pomlp.gouldi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Properties;

import com.formulasearchengine.mathosphere.pomlp.util.GoldUtils;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Andre Greiner-Petter
 */
public class GoldStandardSerializationTests {
    private static final Logger LOG = LogManager.getLogger( GoldStandardSerializationTests.class.getName() );

    private static Path folderPath;
    private static Path tmpOutput;

    @BeforeAll
    public static void init(){
        LOG.debug("Start goldstandard tests.");
        Properties props = ConfigLoader.CONFIG;
        String goldPath = props.getProperty( ConfigLoader.GOULDI_LOCAL_PATH );
        folderPath = Paths.get(goldPath);

        String tmpDir = System.getProperty("java.io.tmpdir");
        LOG.debug("Create tmp directory in " + tmpDir);
        tmpOutput = Paths.get( tmpDir ).resolve("goldstandard");
        try {
            Files.createDirectory( tmpOutput );
        } catch (IOException e) {
            LOG.error("Cannot created temp directory.");
        }
    }

    @Test
    public void loadTest(){
        try {
            JsonGouldiBean gold = GoldUtils.readGoldFile( folderPath.resolve("1.json") );
            assertEquals( "Van_der_Waerden's_theorem", gold.getTitle() );
            assertTrue( gold.getCheck().isTree() );
            LinkedList<JsonGouldiIdentifierDefinienBean> list = gold.getDefinitions().getIdentifierDefiniens();
            String definitionTags = "W|k|\\\\varepsilon";
            assertEquals( 3, list.size() );
            for ( int i = 0; i < list.size(); i++ )
                assertTrue( list.get(i).getName().matches(definitionTags) );
        } catch ( Exception e ){
            fail("Process thrown an exception during test.", e);
        }
    }

    @Test
    public void specialDefinitionsReloadTest(){
        try {
            JsonGouldiBean gold = GoldUtils.readGoldFile( folderPath.resolve("13.json") );
            reloadAssertions( gold );

            Path tmpP = tmpOutput.resolve( "reloadTest.json" );
            GoldUtils.writeGoldFile( tmpP, gold );
            JsonGouldiBean goldNew = GoldUtils.readGoldFile( tmpP );
            reloadAssertions( goldNew );
        } catch ( Exception e ){
            fail("Process thrown an exception during test.", e);
        }
    }

    private void reloadAssertions( JsonGouldiBean bean ){
        LinkedList<JsonGouldiIdentifierDefinienBean> list =
                bean.getDefinitions().getIdentifierDefiniens();
        assertEquals( 2, list.size() );
        JsonGouldiIdentifierDefinienBean beanCase;
        if ( list.get(0).getName().equals("s_{V}") )
            beanCase = list.get(0);
        else beanCase = list.get(1);

        assertEquals( 1, beanCase.getDefiniens().length );
        assertTrue( beanCase.getDefiniens()[0] instanceof JsonGouldiTextDefinienBean );
    }

    @Test
    public void loadAndWriteTest(){
        try {
            JsonGouldiBean gold = GoldUtils.readGoldFile( folderPath.resolve("2.json") );
            gold.setMathTex("WRONG");
            boolean oldQIDCheck = gold.getCheck().isQid();
            gold.getCheck().setQid( !oldQIDCheck );
            Path tmpP = tmpOutput.resolve( "loadAndWriteTest.json" );
            GoldUtils.writeGoldFile( tmpP, gold );
            JsonGouldiBean gouldNew = GoldUtils.readGoldFile( tmpP );
            assertEquals( !oldQIDCheck, gouldNew.getCheck().isQid() );
            assertEquals( "WRONG", gouldNew.getMathTex() );
        } catch ( Exception e ){
            fail("Exception during writing test.", e);
        }
    }

    @AfterAll
    public static void finish(){
        try {
            LOG.debug("Tests finished. Delete temp directory for tests.");
            FileUtils.deleteDirectory( tmpOutput.toFile() );
        } catch (IOException e) {
            LOG.error("Cannot deleted tmp directory.", e);
        }
    }
}
