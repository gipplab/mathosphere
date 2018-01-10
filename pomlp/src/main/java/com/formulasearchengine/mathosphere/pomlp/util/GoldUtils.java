package com.formulasearchengine.mathosphere.pomlp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andre Greiner-Petter
 */
public class GoldUtils {
    private static final Logger LOG = LogManager.getLogger( GoldUtils.class.getName() );

    public static JsonGouldiBean readGoldFile( Path pathToSingleGoldFile ) throws IOException {
        File f = pathToSingleGoldFile.toFile();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue( f, JsonGouldiBean.class );
    }

    public static void writeGoldFile( Path outputPath, JsonGouldiBean goldEntry ) throws IOException{
        try { Files.createFile( outputPath ); }
        catch ( FileAlreadyExistsException e ){
            LOG.warn("File already exists!");
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue( outputPath.toFile(), goldEntry );
    }
}
