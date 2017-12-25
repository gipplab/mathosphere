package com.formulasearchengine.mathosphere.pomlp.comparison;

import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.formulasearchengine.mathosphere.pomlp.util.Constants.NL;


/**
 * @author Andre Greiner-Petter
 */
public class CSVResultWriter {
    private static final Logger LOG = LogManager.getLogger( CSVResultWriter.class.getName() );

    public static final String CSV_SEP = ",";

    private Info[] results;

    public static final Path RESULT_PATH = Paths.get("results");

    public CSVResultWriter( int max ){
        results = new Info[max];
        for ( int i = 0; i < max; i++ )
            results[i] = new Info();
    }

    public void addResult( ComparisonResult result ){
        results[result.getIndex()-1].all[result.getConverter().getPosition()] = result;
    }

    public void writeToFile( Path output ) throws IOException {
        Path filePath = RESULT_PATH.resolve("latest.csv");
        LOG.info("Write results to " + filePath.toAbsolutePath());
        if (!Files.exists(filePath)) Files.createFile(filePath);

        String header = "GOLD" + CSV_SEP;
        for ( Converters c : Converters.values() )
            header += c.name() + CSV_SEP;
        header = header.substring( 0, header.length()-CSV_SEP.length() );

        try ( BufferedWriter w = Files.newBufferedWriter(filePath) ){
            w.write( header + NL );

            for ( int i = 0; i < results.length; i++ ){
                w.write( (i+1) + CSV_SEP );
                w.write( results[i].toString() + NL );
            }
        } catch ( IOException ioe ){
            LOG.error("Cannot write result file.", ioe);
        }
    }

    private class Info {
        private ComparisonResult[] all = new ComparisonResult[Converters.values().length];

        @Override
        public String toString(){
            String str = "";
            for ( int i = 0; i < all.length; i++ ){
                str += all[i] != null ? all[i].resultToString() : " ";
                if ( i < all.length-1 ) str += CSV_SEP;
            }
            return str;
        }
    }
}
