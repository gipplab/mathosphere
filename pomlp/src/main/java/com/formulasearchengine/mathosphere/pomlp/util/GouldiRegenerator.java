package com.formulasearchengine.mathosphere.pomlp.util;

import com.formulasearchengine.mathmltools.mml.elements.MathDoc;
import com.formulasearchengine.mathmltools.mml.elements.MathDocWrapper;
import com.formulasearchengine.mathosphere.pomlp.GoldStandardLoader;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiBean;
import com.formulasearchengine.mathosphere.pomlp.gouldi.JsonGouldiCheckBean;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import com.formulasearchengine.mathosphere.pomlp.util.config.LatexMLConfig;
import com.formulasearchengine.nativetools.CommandExecutor;
import com.formulasearchengine.nativetools.NativeResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * @author Andre Greiner-Petter
 */
public class GouldiRegenerator {

    private static final Logger LOG = LogManager.getLogger(GouldiRegenerator.class.getName());

    private Path outputPath;
    private GoldStandardLoader loader;
    private int max;
    private LinkedList<String> arguments;
    private Path workingDir;

    public GouldiRegenerator( Path outputPath ){
        this.outputPath = outputPath;
        this.loader = GoldStandardLoader.getInstance();
    }

    /**
     * Will load all gold files to cache.
     */
    public void init(){
        loader.initLocally();
        String maxNumStr = ConfigLoader.CONFIG.getProperty( ConfigLoader.GOULDI_MAXIMUM_NUM );
        max = Integer.parseInt( maxNumStr );
        arguments = new LinkedList<>(LatexMLConfig.asList( LatexMLConfig.SEMANTIC_CONFIG ));

        // TODO need a path to drmf folder to use macros...
        String homeDirStr = System.getProperty( "user.home" );
        workingDir = Paths
                .get( homeDirStr )
                .resolve("Projects")
                .resolve("DRMF");
    }

    /**
     * Will change all cached gold files MML.
     */
    public void regenerateAllMML(){
        for ( int i = 1; i <= max; i++){
            try {
                JsonGouldiBean bean = loader.getGouldiJson( i );
                regenerateMMLViaLatexML( bean );
                postLatexmlProcessing( bean );
                augmentSingleGouldiEntry( bean );
                JsonGouldiCheckBean checker = bean.getCheck();
                checker.setQid( false );
                checker.setTree( false );
                bean.setCheck( checker );
                //GoldUtils.writeGoldFile( outputPath.resolve( i+".json" ), bean );
                LOG.info("Successfully augmented gouldi entry with QID: " + i);
            } catch ( Exception e ){
                LOG.warn("Cannot augment gould entry with QID: " + i, e);
            }
        }
        LOG.info("Regenerated every MML from all gouldi entries.");
    }

    public void regenerateMMLViaLatexML( JsonGouldiBean bean ) {
        String semanticTex = bean.getSemanticTex(); // manually semantified
        arguments.addLast( "literal:" + semanticTex  );

        CommandExecutor executor = new CommandExecutor( "LaTeXML", arguments);
        executor.setWorkingDirectoryForProcess( workingDir );
        NativeResponse response = executor.execWithoutTimeout( Level.DEBUG );
        if ( response.getResult() != null ){
            bean.setMml( response.getResult() );
            LOG.debug("Regenerated MML with LaTeXML.");
        } else {
            LOG.warn("Something went wrong during LaTeXML native process.", response.getThrowedException());
        }

        arguments.removeLast();
    }

    public void postLatexmlProcessing( JsonGouldiBean bean ){
        String latexmml = bean.getMml();
        latexmml = latexmml.replaceAll("xmlns:m=\"http://www.w3.org/1998/Math/MathML\"","");
        bean.setMml(latexmml);
    }

    public void augmentSingleGouldiEntry(JsonGouldiBean bean) throws Exception {
        LOG.debug("Start augment new generated gouldi entry.");
        final MathDocWrapper math = new MathDocWrapper(MathDoc.tryFixHeader(bean.getMml()));
        math.fixGoldCd();
        math.changeTeXAnnotation( bean.getOriginalTex() );
        bean.setMml( math.toString() );
    }
}
