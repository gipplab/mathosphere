package com.formulasearchengine.mathosphere.pomlp.xml;

import com.formulasearchengine.mathosphere.pomlp.GoldStandardLoader;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import net.sf.saxon.expr.flwor.Tuple;
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
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andre Greiner-Petter
 */
public class DLMFWebLoader {
    public static final Logger LOG = LogManager.getLogger(DLMFWebLoader.class.getName());

    public static final String EXIT_CODE = "--quit--";

    private GoldStandardLoader gouldi;

    private Path outputFile;

    private HashSet<String> loadedSet;

    private LinkedList<URI> websitesList;
    private LinkedBlockingQueue<SimpleInfoHolder> rawLoadedWebsitesQueue;
    private LinkedBlockingQueue<SimpleInfoHolder> postProcessedWebsitesQueue;

    private RestTemplate restTemplate;

    private static final int min = 101, max = 103;

    public DLMFWebLoader(){
        gouldi = GoldStandardLoader.getInstance();
    }

    public void init() throws IOException, URISyntaxException {
        LOG.info("Init multithreaded loader process for DLMF.");
        gouldi.initLocally();

        String gouldiPath = ConfigLoader.CONFIG.getProperty( ConfigLoader.GOULDI_LOCAL_PATH );
        outputFile = Paths
                .get( gouldiPath )
                .resolve("..")
                .resolve("dlmfSource")
                .resolve("dlmf-small.xml");

        if ( !Files.exists(outputFile) ) Files.createFile(outputFile);

        loadedSet = new HashSet<>();

        websitesList = new LinkedList<>();
        rawLoadedWebsitesQueue = new LinkedBlockingQueue<>();
        postProcessedWebsitesQueue = new LinkedBlockingQueue<>();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout( 2000 );
        factory.setReadTimeout( 5000 );

        restTemplate = new RestTemplate(factory);

        JsonGouldiBean currBean;
        URL url;
        for ( int idx = min; idx <= max; idx++ ){
            currBean = gouldi.getGouldiJson(idx);
            url = new URL(currBean.getUri());
            URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), null);

            if ( !loadedSet.contains( uri.toString() ) ){
                loadedSet.add(uri.toString());
                websitesList.add( uri );
            } else {
                LOG.debug("Skip ID " + idx + " to avoid duplicated page loading.");
            }
        }
    }

    private void startProcess() {
        LOG.info("Fill thread pools in reverse order.");
        LOG.info("Start writer process.");
        new Thread(
                new Writer( outputFile, postProcessedWebsitesQueue ),
                "WriterProcess"
        ).start();

        LOG.info("Init 2 post processors.");
        ExecutorService postProcessorPool = Executors.newFixedThreadPool(2);
        postProcessorPool.submit(new PostProcessor( rawLoadedWebsitesQueue, postProcessedWebsitesQueue ));
        postProcessorPool.submit(new PostProcessor( rawLoadedWebsitesQueue, postProcessedWebsitesQueue ));

        LOG.info("Load websites.");
        ExecutorService pageLoaderPool = Executors.newFixedThreadPool(6);
        while ( !websitesList.isEmpty() ){
            pageLoaderPool.submit(
                    new WebsiteLoader(
                            restTemplate,
                            websitesList.removeFirst(),
                            rawLoadedWebsitesQueue
                            )
            );
        }

        try {
            LOG.info("Process running. Wait at most 10 minutes until send stop signal to all process.");
            pageLoaderPool.shutdown();
            postProcessorPool.shutdown();

            pageLoaderPool.awaitTermination( 10, TimeUnit.MINUTES );
            LOG.info("Loading process finished. Inform others about the end of the process.");
            rawLoadedWebsitesQueue.add(new SimpleInfoHolder(null, EXIT_CODE));
            postProcessorPool.awaitTermination( 2, TimeUnit.MINUTES );
            postProcessedWebsitesQueue.add(new SimpleInfoHolder(null, EXIT_CODE));
        } catch (InterruptedException e) {
            LOG.error("Cannot wait until end of termination.", e);
        }
    }

    public static void main(String[] args) throws Exception {
        DLMFWebLoader loader = new DLMFWebLoader();
        LOG.info("Init loader.");
        loader.init();
        LOG.info("Start loader...");
        loader.startProcess();
    }

    private class SimpleInfoHolder {
        private URI uri;
        private String input;

        public SimpleInfoHolder(URI uri, String input){
            this.uri = uri;
            this.input = input;
        }
    }

    private class WebsiteLoader implements Runnable {
        private final Logger LOG = LogManager.getLogger(WebsiteLoader.class.getName());

        private LinkedBlockingQueue<SimpleInfoHolder> rawWebPageQueue;
        private final URI uri;
        private final RestTemplate rest;

        public WebsiteLoader( RestTemplate rest, URI uri, LinkedBlockingQueue rawWebPageQueue){
            this.rawWebPageQueue = rawWebPageQueue;
            this.uri = uri;
            this.rest = rest;
        }

        @Override
        public void run() {
            try {
                LOG.info( "Wait for response: " + uri.toString() );
                String webPage = rest.getForObject( uri, String.class );
                rawWebPageQueue.put( new SimpleInfoHolder(uri, webPage) );
            } catch ( Exception e ){
                LOG.error("Cannot load websites.", e);
            }
        }
    }

    private class PostProcessor implements Runnable {

        private final Logger LOG = LogManager.getLogger(PostProcessor.class.getName());
        private LinkedBlockingQueue<SimpleInfoHolder> rawWebPageQueue;
        private LinkedBlockingQueue<SimpleInfoHolder> processedPageQueue;

        private static final int OPEN_BODY_IDX = 1;
        private static final int CLOSE_BODY_IDX = 2;
        private static final String TAGS = "<link.+?>\r?\n?|<script.+</script>\r?\n?|(<body)|(</body>)|<a((?!<math).)*</a>\r?\n?";
        private Pattern pattern;

        public PostProcessor( LinkedBlockingQueue rawWebPageQueue, LinkedBlockingQueue processedPageQueue ){
            this.rawWebPageQueue = rawWebPageQueue;
            this.processedPageQueue = processedPageQueue;
            this.pattern = Pattern.compile( TAGS );
        }

        private String postProcess(String in){
            StringBuffer buffer = new StringBuffer();
            Matcher matcher = pattern.matcher( in );
            while( matcher.find() ){
                if ( matcher.group(OPEN_BODY_IDX) != null ){
//                    LOG.trace("Found body start, wrap it by text block. " + matcher.group(0));
                    matcher.appendReplacement( buffer, "<text><body" );
                }
                else if ( matcher.group(CLOSE_BODY_IDX) != null ){
//                    LOG.trace("Found body end, close text wrapping." + matcher.group(0));
                    matcher.appendReplacement( buffer, "</body></text>" );
                }
                else {
//                    LOG.trace("Found something else, delete: " + matcher.group(0));
                    matcher.appendReplacement( buffer, "" );
                }
            }
            matcher.appendTail(buffer);
            return buffer.toString();
        }

        @Override
        public void run() {
            LOG.info("Start post processor.");

            try {
                SimpleInfoHolder info;
                while( !( info = rawWebPageQueue.take()).input.equals(EXIT_CODE) ){
                    LOG.info("Start post processing: " + info.uri.toString());
                    info.input = postProcess(info.input);
                    LOG.info("Finished post processing: " + info.uri.toString());
                    processedPageQueue.put( info );
                }

                // put EXIT code back to queue for other waiting services.
                rawWebPageQueue.put( info );
                LOG.info("Post processor received exit signal - done!");
            } catch ( InterruptedException ie ){
                LOG.error("Cannot post process, interrupted queue process.", ie);
            }
        }
    }

    private class Writer implements Runnable {
        private final Logger LOG = LogManager.getLogger(Writer.class.getName());
        private final String NL = System.lineSeparator();

        private Path file;

        private BufferedWriter writer;
        private LinkedBlockingQueue<SimpleInfoHolder> processedQueue;

        public Writer( Path file, LinkedBlockingQueue processedQueue ){
            this.file = file;
            this.processedQueue = processedQueue;
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

        private void write( String processedPage ) throws IOException {
            LOG.info("Write next page.");
            writer.write("<page>" + NL);
            writer.write(processedPage);
            writer.write("</page>" + NL);
            LOG.info("Done writing single page.");
        }

        @Override
        public void run() {
            try {
                LOG.info("Start writing process...");
                init();

                SimpleInfoHolder info;
                while ( !(info = this.processedQueue.take()).input.equals(EXIT_CODE) ){
                    LOG.info("Write next page: " + info.uri);
                    write( info.input );
                }

                // put exit code back to queue for other waiting services
                processedQueue.put(info);

                LOG.info("Received exit code. Finish writing process.");
                end();
            } catch ( IOException ioe ){
                LOG.error("Cannot write output file!", ioe);
            } catch ( InterruptedException ie ){
                LOG.error("Interrupted reading from processed queue.", ie);
            }
        }
    }

}
