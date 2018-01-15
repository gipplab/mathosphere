package com.formulasearchengine.mathosphere.pomlp.xml;

import com.formulasearchengine.mathosphere.pomlp.GoldStandardLoader;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.util.GoldUtils;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import net.sf.saxon.expr.flwor.Tuple;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
import java.util.HashMap;
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
    private Path outputCSVTranslations;
    private Path baseGouldiPath;

    private HashMap<String, Integer> loadedSet;
    private HashMap<Integer, Integer> crossNames;
    private String[] nameMap;

    private LinkedList<SimpleInfoHolder> websitesList;
    private LinkedBlockingQueue<SimpleInfoHolder> rawLoadedWebsitesQueue;
    private LinkedBlockingQueue<SimpleInfoHolder> postProcessedWebsitesQueue;


    private RestTemplate restTemplate;

    private static final int min = 101, max = 200;

    public DLMFWebLoader(){
        gouldi = GoldStandardLoader.getInstance();
    }

    public void init() throws IOException, URISyntaxException {
        LOG.info("Init multithreaded loader process for DLMF.");
        gouldi.initLocally();

        String gouldiPath = ConfigLoader.CONFIG.getProperty( ConfigLoader.GOULDI_LOCAL_PATH );
        baseGouldiPath = Paths.get(gouldiPath);

        Path base = Paths.get("..")
                .resolve("lib")
                .resolve("GoUldI")
                .resolve("dlmfSource");

        outputFile = base.resolve("dlmf-complete.xml");
        outputCSVTranslations = base.resolve("name-translations.csv");

        if ( !Files.exists(outputFile) ) Files.createFile(outputFile);


        websitesList = new LinkedList<>();
        rawLoadedWebsitesQueue = new LinkedBlockingQueue<>();
        postProcessedWebsitesQueue = new LinkedBlockingQueue<>();

        loadedSet = new HashMap<>();
        crossNames = new HashMap<>();
        nameMap = new String[max+1];

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

            if ( !loadedSet.keySet().contains( uri.toString() ) ){
                loadedSet.put(uri.toString(), idx);
                websitesList.add( new SimpleInfoHolder( uri, null, idx ) );
            } else {
                LOG.debug("Skip ID " + idx + " to avoid duplicated page loading.");
                crossNames.put( idx, loadedSet.get(uri.toString()) ); // set cross referencing
                LOG.info("Cross reference: qID {} uses the same website as qID {}.", idx, loadedSet.get(uri.toString()));
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
            rawLoadedWebsitesQueue.add(new SimpleInfoHolder(null, EXIT_CODE, -1));
            postProcessorPool.awaitTermination( 2, TimeUnit.MINUTES );
            postProcessedWebsitesQueue.add(new SimpleInfoHolder(null, EXIT_CODE, -1));

            LOG.info("Done, all process finished.");
            LOG.info("Resolve cross referencing.");
            for ( Integer idxCross : crossNames.keySet() ){
                nameMap[ idxCross ] = nameMap[ crossNames.get(idxCross) ];
            }
            LOG.info("Done cross referencing, every number between 101-200 should has a title now.");
            LOG.info("Write CSV file.");
            writeCsvNameFile();
            LOG.info("Done. Update gold files!");
            writeGouldiNames();
        } catch (InterruptedException e) {
            LOG.error("Cannot wait until end of termination.", e);
        }
    }

    private void writeCsvNameFile(){
        try (
                BufferedWriter bf = Files.newBufferedWriter( outputCSVTranslations );
                CSVPrinter csvPrinter = new CSVPrinter(bf, CSVFormat.RFC4180.withHeader(
                        "QID", "NAME"
                ))
        ){
            for ( int i = 101; i <= max; i++ ){
                csvPrinter.printRecord( i, nameMap[i] );
            }
            csvPrinter.flush();
        } catch ( IOException ioe ){
            LOG.error("Cannot write csv file for name references.", ioe);
        }
    }

    private void writeGouldiNames(){
        for ( int i = 101; i <= max; i++ ){
            try {
                JsonGouldiBean bean = gouldi.getGouldiJson( i );
                if ( bean.getTitle() != null )
                    bean.set( "specific_title", bean.getTitle() );
                bean.setTitle( nameMap[i] );
                GoldUtils.writeGoldFile( baseGouldiPath.resolve( i+".json" ), bean );
            } catch ( Exception e ){
                LOG.warn("Cannot update name of qID " + i, e);
            }
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
        private Integer qid;

        public SimpleInfoHolder(URI uri, String input, Integer qid){
            this.uri = uri;
            this.input = input;
            this.qid = qid;
        }
    }

    private class WebsiteLoader implements Runnable {
        private final Logger LOG = LogManager.getLogger(WebsiteLoader.class.getName());

        private LinkedBlockingQueue<SimpleInfoHolder> rawWebPageQueue;
        private final SimpleInfoHolder info;
        private final RestTemplate rest;

        public WebsiteLoader( RestTemplate rest, SimpleInfoHolder info, LinkedBlockingQueue rawWebPageQueue){
            this.rawWebPageQueue = rawWebPageQueue;
            this.info = info;
            this.rest = rest;
        }

        @Override
        public void run() {
            try {
                LOG.info( "Wait for response: " + info.uri.toString() );
                String webPage = rest.getForObject( info.uri, String.class );
                rawWebPageQueue.put( new SimpleInfoHolder(info.uri, webPage, info.qid) );
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
        private static final int TITLE_IDX = 3;
        private static final String TAGS =
                "<link.+?>\r?\n?|" +
                        "<script.+</script>\r?\n?|" +
                        "(<body)|" +
                        "(</body>)|" +
                        "<title>(.*?)</title>|" +
                        "<a((?!<math).)*</a>\r?\n?|" +
                        "<dt>(?:" +
                            "See also|" +
                            "Permalink|"+
                            "Encodings|"+
                            "Notes|"    +
                            "Referenced"+
                        ").+?</dd>\r?\n?";
        private Pattern pattern;

        public PostProcessor( LinkedBlockingQueue rawWebPageQueue, LinkedBlockingQueue processedPageQueue ){
            this.rawWebPageQueue = rawWebPageQueue;
            this.processedPageQueue = processedPageQueue;
            this.pattern = Pattern.compile( TAGS, Pattern.DOTALL );
        }

        private String postProcess(SimpleInfoHolder info){
            StringBuffer buffer = new StringBuffer();
            Matcher matcher = pattern.matcher( info.input );
            while( matcher.find() ){
                if ( matcher.group(OPEN_BODY_IDX) != null ){
//                    LOG.trace("Found body start, wrap it by text block. " + matcher.group(0));
                    matcher.appendReplacement( buffer, "<text><body" );
                }
                else if ( matcher.group(CLOSE_BODY_IDX) != null ){
//                    LOG.trace("Found body end, close text wrapping." + matcher.group(0));
                    matcher.appendReplacement( buffer, "</body></text>" );
                }
                else if ( matcher.group(TITLE_IDX) != null ){
                    String title = matcher.group(TITLE_IDX);
                    LOG.debug("Extract name '{}' for qID: {}.", title, info.qid);
                    matcher.appendReplacement( buffer, "<title>" + title + "</title>" );
                    nameMap[info.qid] = title.replaceAll(" ", "_");
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
                    info.input = postProcess(info);
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
