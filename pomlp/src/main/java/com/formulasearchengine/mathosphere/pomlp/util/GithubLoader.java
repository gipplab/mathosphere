package com.formulasearchengine.mathosphere.pomlp.util;

import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import com.formulasearchengine.mathosphere.pomlp.util.config.PathBuilder;
import com.formulasearchengine.mathosphere.pomlp.util.rest.GitHubFileResponse;
import com.formulasearchengine.mathosphere.pomlp.util.rest.RESTPathBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

public class GithubLoader {
    private static final Logger LOG = LogManager.getLogger( GithubLoader.class.getName() );

    // Time out is 5sec
    public static final int CONN_TIME_OUT = 5 * 1000;

    // Time out is 2sec
    public static final int READ_TIME_OUT = 2 * 1000;

    private String gitHubApiURL;

    private RestTemplate rest;

    /**
     *
     * @throws RuntimeException if the configurations cannot be loaded from config.properties
     */
    public GithubLoader() throws RuntimeException {
        try {
            Properties props = ConfigLoader.loadConfiguration();
            String repo = props.getProperty( ConfigLoader.GITHUB_REPO_NAME );
            String owner = props.getProperty( ConfigLoader.GITHUB_REPO_OWNER );
            String path = props.getProperty( ConfigLoader.GITHUB_REPO_PATH );
            String githubLink = props.getProperty( ConfigLoader.GITHUB_URL );
            LOG.debug("Load all github properties.");
            gitHubApiURL = new RESTPathBuilder( githubLink )
                    .setGithubContent(owner, repo)
                    .setInnerPath(path)
                    .getURL();
        } catch ( FileNotFoundException e ){
            throw new RuntimeException( "Cannot instantiate GithubLoader.", e );
        }
    }

    public void init() throws Exception {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout( CONN_TIME_OUT );
        factory.setReadTimeout( READ_TIME_OUT );

        rest = new RestTemplate(factory);
    }

    public static void main(String[] args) throws Exception {
        GithubLoader loader = new GithubLoader();
        loader.init();
        GitHubFileResponse file = loader.rest.getForObject( loader.gitHubApiURL+"/19.json", GitHubFileResponse.class );
        LOG.info("ENCODED_CONTENT: " + file.getContent());
        LOG.info("CONTENT: " + file.getDecodedContent());

//        JsonGouldiBean bean = file.getJsonBeanFromContent();
//        LOG.info("Hmm: " + bean.getDefinitions().get("Z"));

//        String file = loader.rest.getForObject( loader.gitHubApiURL+"/1.json", String.class );
//        LOG.info("Joa: " + System.lineSeparator()+file);
    }
}
