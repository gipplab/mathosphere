package com.formulasearchengine.mathosphere.pomlp.xml;

import com.formulasearchengine.mathosphere.pomlp.GoldStandardLoader;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.xml.transform.Source;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * @author Andre Greiner-Petter
 */
public class DLMFWebLoader {
    public static final Logger LOG = LogManager.getLogger(DLMFWebLoader.class.getName());

    private HashSet<String> loaded;

    private GoldStandardLoader gouldi;
    private RestTemplate rest;

    boolean done;

    private Path outputFile;
    private Writer writer;

    public DLMFWebLoader(){
        gouldi = GoldStandardLoader.getInstance();
        loaded = new HashSet<>();
        done = false;
    }

    public int init() throws IOException {
        LOG.info("Init gold standard locally.");
        gouldi.initLocally();

        String gouldiPath = ConfigLoader.CONFIG.getProperty( ConfigLoader.GOULDI_LOCAL_PATH );
        outputFile = Paths
                .get( gouldiPath )
                .resolve("..")
                .resolve("dlmfSource")
                .resolve("dlmf-small.xml");
        Scanner sc = new Scanner(System.in);
        LOG.info("Writing to " + outputFile.toAbsolutePath() + " - correct? (y / n)");
        String in = sc.next();
        while ( true ) {
            if ( in.matches("(n|no)") ) {
                LOG.info("You said NO!");
                return -1;
            }
            else if ( in.matches("(y|yes)") ) {
                LOG.info("You said YES!");
                break;
            }
            System.out.println("What? Say y for yes or n for no.");
        }

        if ( !Files.exists(outputFile) ) Files.createFile(outputFile);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout( 2000 );
        factory.setReadTimeout( 5000 );

        rest = new RestTemplate(factory);
        writer = new Writer( outputFile );
        return 0;
    }

    private void loadPage( URL url, int gouldiID ) throws URISyntaxException {
        if ( loaded.contains(url.getPath()) ){
            LOG.info("SKIP ("+gouldiID+") - Already loaded path " + url.getPath());
            return;
        }

        LOG.info( gouldiID + "- GET-Request: " + url.toString());
        loaded.add( url.getPath() );
        URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), null);
        LOG.info( gouldiID + "- Wait for response..." );
        String result = rest.getForObject( uri, String.class );
        LOG.info( gouldiID + "- Successfully loaded.");
        try {
            writer.write(result);
        } catch (IOException ioe){
            LOG.error( gouldiID + "- Cannot write to file.", ioe);
        }
    }

    private int min = 101, max = 104;

    private void startProcess() throws IOException {
        LOG.info("Start writing process.");
        writer.init();

        JsonGouldiBean currBean;
        URL currURL;
        for ( int idx = min; idx <= max; idx++ ){
            currBean = gouldi.getGouldiJson(idx);
            try {
                currURL = new URL(currBean.getUri());
                loadPage( currURL, idx );
            } catch ( Exception e ){
                LOG.warn("Cannot parse URI -> Skip" + idx, e);
            }
        }

        writer.end();
    }

    public static void main(String[] args) throws Exception {
        final DLMFWebLoader loader = new DLMFWebLoader();
        LOG.info("Init loader.");
        int i = loader.init();
        if ( i != 0 ) return;
        LOG.info("Start loader...");
        loader.startProcess();
    }

    private class Writer {
        private final String NL = System.lineSeparator();

        private Path file;

        private BufferedWriter writer;

        public Writer( Path file ){
            this.file = file;
        }

        private void init() throws IOException {
            writer = Files.newBufferedWriter( file );
            LOG.info("Write mother root.");
            writer.write("<root>");
        }

        private void end() throws IOException {
            LOG.info("Write end root.");
            writer.write("</root>");
            writer.close();
        }

        private void write( String file ) throws IOException {
            LOG.info("Write next page.");
            writer.write("<page><text>" + NL);
            writer.write(file);
            writer.write("</text></page>" + NL);
            LOG.info("Done writing single page.");
        }
    }

}
