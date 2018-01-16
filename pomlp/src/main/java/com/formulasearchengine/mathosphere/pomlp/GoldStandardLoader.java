package com.formulasearchengine.mathosphere.pomlp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.util.GoldUtils;
import com.formulasearchengine.mathosphere.pomlp.util.GouldiRegenerator;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import com.formulasearchengine.mathosphere.pomlp.util.rest.GitHubFileResponse;
import com.formulasearchengine.mathosphere.pomlp.util.rest.RESTPathBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class GoldStandardLoader {
    private static final Logger LOG = LogManager.getLogger(GoldStandardLoader.class.getName());

    // Time out is 5sec
    public static final int CONN_TIME_OUT = 5 * 1000;

    // Time out is 2sec
    public static final int READ_TIME_OUT = 2 * 1000;

    //
    public static final int PARALLEL_READING_TIMEOUT = 90;
    public static final TimeUnit PARALLEL_READING_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private String gitHubApiURL;

    private Properties props;

    private RestTemplate rest;

    private int max;

    private boolean local = false;

    private static JsonGouldiBean[] gouldi;

    /**
     * @throws RuntimeException if the configurations cannot be loaded from config.properties
     */
    private GoldStandardLoader() {
    }

    private static final GoldStandardLoader loader = new GoldStandardLoader();

    public static GoldStandardLoader getInstance() {
        return loader;
    }

    public void init() {
        props = ConfigLoader.CONFIG;
        max = Integer.parseInt(props.getProperty(ConfigLoader.GOULDI_MAXIMUM_NUM));

        String repo = props.getProperty(ConfigLoader.GITHUB_REPO_NAME);
        String owner = props.getProperty(ConfigLoader.GITHUB_REPO_OWNER);
        String path = props.getProperty(ConfigLoader.GITHUB_REPO_PATH);
        String githubLink = props.getProperty(ConfigLoader.GITHUB_URL);

        if (repo == null || owner == null || githubLink == null) {
            LOG.info("Cannot find GitHub access -> switch to local initialization.");
            initLocally();
            return;
        }

        LOG.debug("Load all github properties.");
        gitHubApiURL = new RESTPathBuilder(githubLink)
                .setGithubContent(owner, repo)
                .setInnerPath(path)
                .getURL();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONN_TIME_OUT);
        factory.setReadTimeout(READ_TIME_OUT);

        rest = new RestTemplate(factory);
    }

    public int initLocally() {
        props = ConfigLoader.CONFIG;
        max = Integer.parseInt(props.getProperty(ConfigLoader.GOULDI_MAXIMUM_NUM));

        String goldPath = props.getProperty(ConfigLoader.GOULDI_LOCAL_PATH);
        Path path = Paths.get(goldPath);
        gouldi = new JsonGouldiBean[max];

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        for (int i = 1; i <= max; i++) {
            executor.execute(new JSONReader(path, i));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(PARALLEL_READING_TIMEOUT, PARALLEL_READING_TIMEOUT_UNIT);
        } catch (InterruptedException ie) {
            LOG.warn("Executor service exceeds timeouts to read files. It maybe didn't properly load all gouldi-files.");
        }

        this.local = true;
        return max;
    }

    private class JSONReader implements Runnable {
        private final Logger IN_LOG = LogManager.getLogger(JSONReader.class.getName());

        private Path path;
        private int number;

        public JSONReader(Path goldPath, int number) {
            this.path = goldPath;
            this.number = number;
        }

        @Override
        public void run() {
            try {
                Path p = path.resolve(number + ".json");
                gouldi[number - 1] = GoldUtils.readGoldFile(p);
            } catch (Exception e) {
                IN_LOG.error("Parallel process cannot parse " + path.toString() + number + ".json - " + e.getMessage(), e);
            }
        }
    }

    public GitHubFileResponse getResponseFromGouldiRequest(int number) {
        String file = number + ".json";
        return rest.getForObject(
                gitHubApiURL + RESTPathBuilder.BIND + file,
                GitHubFileResponse.class
        );
    }

    public JsonGouldiBean getGouldiJson(int number)
            throws IOException {
        if (local) {
            LOG.trace("Local mode. Get Json: " + number);
            return gouldi[number - 1];
        }

        GitHubFileResponse response = getResponseFromGouldiRequest(number);
        // TODO handle response codes here
        return response.getJsonBeanFromContent();
    }

    public static void main(String[] args) {
        // TODO: DO NOT USE THIS MAIN, IF YOU DON'T KNOW WHAT YOU ARE DOING!
        // it's just a main for manually executions and changes constantly

        // regenerate all gold files once!
        //String goldPath = ConfigLoader.CONFIG.getProperty(ConfigLoader.GOULDI_LOCAL_PATH);
        //Path outputPath = Paths.get( goldPath );
        Path outputPath = Paths.get("outputTmp");
        GouldiRegenerator regenerator = new GouldiRegenerator(outputPath);
        regenerator.init();
        regenerator.regenerateAllMML();
    }
}
