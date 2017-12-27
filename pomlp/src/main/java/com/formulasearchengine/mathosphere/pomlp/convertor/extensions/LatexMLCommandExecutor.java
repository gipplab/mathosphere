package com.formulasearchengine.mathosphere.pomlp.convertor.extensions;

import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLServiceResponse;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Andre Greiner-Petter
 */
public class LatexMLCommandExecutor {

    private static final Logger LOG = LogManager.getLogger( LatexMLCommandExecutor.class.getName() );

    public static final long DEFAULT_TIMEOUT = 2000L;

    private ProcessBuilder pb;

    public LatexMLCommandExecutor( String... args ){
        this.pb = new ProcessBuilder( args );
    }

    public LatexMLCommandExecutor( List<String> args ) {
        this.pb = new ProcessBuilder( args );
    }

    public LaTeXMLServiceResponse exec( long timeoutMs ) {
        return exec( timeoutMs, TimeUnit.MILLISECONDS );
    }

    public LaTeXMLServiceResponse exec( long timeoutMs, Level logLevel ){
        return exec( timeoutMs, TimeUnit.MILLISECONDS, logLevel );
    }

    public LaTeXMLServiceResponse exec( long timeout, TimeUnit unit ) {
        return exec( timeout, unit, Level.DEBUG );
    }

    public LaTeXMLServiceResponse exec(long timeout, TimeUnit unit, Level logLevel ) {
        LaTeXMLServiceResponse response = new LaTeXMLServiceResponse();
        try {
            Process process = pb.start();
            logErrorStream( process.getErrorStream(), logLevel );
            response.setResult(buildFromStream( process.getInputStream() ));
            process.waitFor( timeout, unit );
            response.setStatusCode( process.exitValue() );
            response.setStatus("OK");
            safetyExit( process );
            process.destroy();
        } catch ( InterruptedException ie ){
            LOG.warn("Process exceeded timeout -> return null.", ie);
            return null;
        } catch ( IOException ioe ){
            LOG.error("Cannot execute LaTeXML!", ioe);
            response.setStatus( "Error in LaTeXML: " + ioe.getMessage() );
        }

        return response;
    }

    private void logErrorStream(InputStream err, Level logLevel){
        try ( BufferedReader br = new BufferedReader( new InputStreamReader(err) ) ){
            String line;
            while ( (line = br.readLine()) != null )
                if (!line.isEmpty())
                    LOG.log(logLevel, "LaTeXML - " + line);
            LOG.trace("Finished sub-process logging.");
        } catch ( IOException ioe ){
            LOG.error("Error while reading from error stream.", ioe);
        }
    }

    private String buildFromStream( InputStream in ){
        StringBuilder builder = new StringBuilder();
        try ( InputStreamReader br = new InputStreamReader(in) ){
            builder.append(IOUtils.toString( br ));
        } catch ( IOException ioe ){
            LOG.error("Cannot build result from stream.", ioe);
        }
        return builder.toString();
    }

    private void safetyExit( Process process ) throws IOException {
        process.getErrorStream().close();
        process.getInputStream().close();
        process.getOutputStream().close();
    }
}
