package com.formulasearchengine.mathosphere.pomlp.convertor.extensions;

import com.formulasearchengine.mathosphere.pomlp.convertor.Converters;
import com.formulasearchengine.mathosphere.pomlp.convertor.Parser;
import com.formulasearchengine.mathosphere.pomlp.util.POMLoader;
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
public class CommandExecutor {

    private static final Logger LOG = LogManager.getLogger( CommandExecutor.class.getName() );

    public static final long DEFAULT_TIMEOUT = 2000L;

    private ProcessBuilder pb;

    private final String serviceName;

    public CommandExecutor(String serviceName, String... args ){
        this.pb = new ProcessBuilder( args );
        this.serviceName = serviceName;
    }

    public CommandExecutor(String serviceName, List<String> args ) {
        this.pb = new ProcessBuilder( args );
        this.serviceName = serviceName;
    }

    public NativeResponse exec( long timeoutMs ) {
        return exec( timeoutMs, TimeUnit.MILLISECONDS );
    }

    public NativeResponse exec( long timeoutMs, Level logLevel ){
        return exec( timeoutMs, TimeUnit.MILLISECONDS, logLevel );
    }

    public NativeResponse exec( long timeout, TimeUnit unit ) {
        return exec( timeout, unit, Level.DEBUG );
    }

    public NativeResponse exec( long timeout, TimeUnit unit, Level logLevel ) {
        int exitCode = 1;
        try {
            Process process = pb.start();
            logErrorStream( process.getErrorStream(), logLevel );
            String result = buildFromStream( process.getInputStream() );
            process.waitFor( timeout, unit );
            exitCode = process.exitValue();
            if ( exitCode != 0 ) throw new IOException( "Execution Fail!" );
            safetyExit( process );
            process.destroy();
            return new NativeResponse( result );
        } catch ( InterruptedException ie ){
            LOG.warn(serviceName + " - Process exceeded timeout -> return null.", ie);
            return null;
        } catch ( IOException ioe ){
            LOG.error("Cannot execute " + serviceName, ioe);
            return new NativeResponse(exitCode, exitCode + "-Error in " + serviceName + ": " + ioe.getMessage(), ioe );
        }
    }

    private void logErrorStream(InputStream err, Level logLevel){
        try ( BufferedReader br = new BufferedReader( new InputStreamReader(err) ) ){
            String line;
            while ( (line = br.readLine()) != null )
                if (!line.isEmpty())
                    LOG.log(logLevel, serviceName + " - " + line);
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
