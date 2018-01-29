package com.formulasearchengine.mathosphere.pomlp.gouldi;

import com.formulasearchengine.mathmlsim.similarity.MathPlag;
import com.formulasearchengine.mathmlsim.similarity.result.Match;
import com.formulasearchengine.mathmlsim.similarity.result.SubMatch;
import com.formulasearchengine.mathmltools.mml.elements.MathDoc;
import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;
import com.formulasearchengine.mathosphere.mathpd.Distances;
import com.formulasearchengine.mathosphere.pomlp.GoldStandardLoader;
import com.formulasearchengine.mathosphere.pomlp.comparison.RTEDTreeComparator;
import com.formulasearchengine.mathosphere.pomlp.convertor.LatexmlGenerator;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import com.formulasearchengine.mathosphere.pomlp.util.config.LatexMLConfig;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import com.formulasearchengine.mathosphere.utils.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Andre Greiner-Petter
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SemantificationImprovementsTests {

    private static final Logger LOG = LogManager.getLogger( SemantificationImprovementsTests.class.getName() );

    public static final String NL = System.lineSeparator();

    private JsonGouldiBean gold;
    private RTEDTreeComparator RTED;
    private LatexmlGenerator latexml;
    private Node goldTree, goldContent, goldPresentation;
    private Document goldDoc;
    private String goldDocString;

    private LinkedList<String> semanticArgs;

    private LinkedList<String> results;

    private String lastTree;

    private HashMap<String, Double> goldContentHistogramm;

    @BeforeAll
    public void init() throws Exception {
        GoldStandardLoader goldLoader = GoldStandardLoader.getInstance();
        goldLoader.initLocally();

        latexml = new LatexmlGenerator();
        latexml.init();
        semanticArgs = new LinkedList<>(LatexMLConfig.asList(LatexMLConfig.SEMANTIC_CONFIG));
        latexml.redirectLatex(Paths.get( System.getProperty("user.home"), "Projects", "DRMF" ));

        RTED = new RTEDTreeComparator();

        gold = goldLoader.getGouldiJson( 101 );
        MathMLDocumentReader mmlDocReader = new MathMLDocumentReader( gold.getMml() );
        goldDoc = mmlDocReader.getDocument();
        goldTree = goldDoc.getDocumentElement();
        goldContent = mmlDocReader.getContentNode();
        goldPresentation = mmlDocReader.getPresentationNode();

        goldDocString = Utility.documentToString( goldDoc, true );

        goldContentHistogramm = Distances.contentElementsToHistogram( mmlDocReader.getAllContentNodes() );

        results = new LinkedList<>();
    }

    @AfterAll
    public void end() {
        System.out.println("Final Results!");
        Collections.sort( results );
        results.stream().forEach(System.out::println);

        System.out.println( "SEE DIFFERENCE BETWEEN BEST TREES" );
        System.out.println( "NOT Gouldi" );
        System.out.println( lastTree );
        System.out.println( "Gouldi" );
        System.out.println( goldDocString );
    }

    @Test
    public void straightTest() throws Exception {
        MathMLDocumentReader docReader = parseToMMLDocReader( gold.getCorrectedTex() );
        writeResults( 1, "Straight", docReader );
    }

    @Test
    public void straightPreProcessingTest() throws Exception {
        MathMLDocumentReader docReader = parseToMMLDocReader(preprocess( gold.getCorrectedTex() ));
        writeResults( 2, "Pre Processed", docReader );
    }

    @Test
    public void normalizedTest() throws Exception {
        String in = preprocess( gold.getCorrectedTex() );
        MathMLDocumentReader docReader = parseToMMLDocReader( in );
        String canonicalizedString = Utility.documentToString( docReader.getDocument(), true );

        MathDoc mathDoc = new MathDoc( canonicalizedString );
        mathDoc.fixGoldCd();
        mathDoc.changeTeXAnnotation( gold.getOriginalTex() );
        docReader = new MathMLDocumentReader( mathDoc.getDom() );
        writeResults( 3, "Pre- & Post Processed", docReader );
    }

    @Test
    public void semantifiedTest() throws Exception {
        String rawGold = gold.getCorrectedTex();
        String knownSemantic = rawGold.replaceAll( "f", "\\\\wf{Q11348}{f}" );

        semanticArgs.addLast( "literal:" + knownSemantic );
        String almostNiceMML = latexml.parseToString( semanticArgs, knownSemantic );
        LOG.debug( "Native code calls finished." );
        semanticArgs.removeLast();

        almostNiceMML = MathDoc.tryFixHeader( almostNiceMML );
        LOG.debug( "Fixed header." );
        MathDoc mathDoc = new MathDoc( almostNiceMML );
        LOG.debug( "Parsed to MathDoc." );
        mathDoc.fixGoldCd();
        LOG.debug( "Fixed gold cd." );
        mathDoc.changeTeXAnnotation( gold.getOriginalTex() );
        LOG.debug( "Changed tex annotation." );
        MathMLDocumentReader docReader = new MathMLDocumentReader( mathDoc.getDom() );
        LOG.debug( "Parsed to MathMLDocumentReader." );

        writeResults( 4, "Semantified", docReader );
    }

    private String preprocess( String in ){
        return Utility.latexPreProcessing( in );
    }

    private MathMLDocumentReader parseToMMLDocReader( String input ){
        semanticArgs.add("literal:" + input);
        String docStr = latexml.parseToString( semanticArgs, input );
        semanticArgs.removeLast();
        docStr = MathDoc.tryFixHeader( docStr );
        return new MathMLDocumentReader( docStr );
    }

    private void writeResults( int id, String message, MathMLDocumentReader docReader ) throws Exception {
        writeTreeComparison(
                id,
                message,
                docReader.getDocument(),
                docReader.getDocument().getDocumentElement(),
                docReader.getContentNode(),
                docReader.getPresentationNode()
        );
    }

    private void writeTreeComparison(int id, String header, Document dom, Node tree, Node content, Node presentation ) {
        StringBuilder out = new StringBuilder();
        double totalDistance = RTED.computeDistance( goldTree, tree );
        double contentDistance = RTED.computeDistance( goldContent, content );
        double presentationDistance = RTED.computeDistance( goldPresentation, presentation );

        out.append(id)
                .append("------ ").append(header).append(" ------").append(NL)
                .append("    Complete Tree: ").append(totalDistance).append(NL)
                .append("     Content Tree: ").append(contentDistance).append(NL)
                .append("Presentation Tree: ").append(presentationDistance).append(NL);

        try {
            HashMap<String, Double> contentHistogram = Distances.contentElementsToHistogram(XMLHelper.getElementsB(content, "*//child::*"));
            double absoluteDist = Distances.computeAbsoluteDistance( contentHistogram, goldContentHistogramm );
            double relativeDist = Distances.computeRelativeDistance( contentHistogram, goldContentHistogramm );
            double cosineDist   = Distances.computeCosineDistance(   contentHistogram, goldContentHistogramm );
            double earthMoverDist = Distances.computeEarthMoverAbsoluteDistance( contentHistogram, goldContentHistogramm );

            out
                    .append("  ++++++ Content Distances +++++ ").append(NL)
                    .append("Absolute Distance: ").append(absoluteDist).append(NL)
                    .append("Relative Distance: ").append(relativeDist).append(NL)
                    .append("  Cosine Distance: ").append(cosineDist).append(NL)
                    .append("Earth Mover Dist.: ").append(earthMoverDist).append(NL);
        } catch ( Exception e ){
            LOG.warn("Content Distances Error.", e);
        }

        String compareTree = Utility.documentToString( dom, true );
        try {
            Map<String, Object> distances = MathPlag.compareOriginalFactors( goldDocString, compareTree );
            out.append( "  ++++++ MathPlag Distance Check ++++++ " ).append(NL);
            for ( String name : distances.keySet() ){
                out.append( name ).append(": ").append( distances.get(name) ).append(NL);
            }


            List<Match> distList = MathPlag.compareSimilarMathML(
                    MathDoc.tryFixHeader(goldDocString),
                    MathDoc.tryFixHeader(compareTree)
            );
            out.append( "  ++++++ MathPlag Similarity Distance ++++++ " ).append(NL);
            addMatchOverview( out, distList );
            out.append(NL);

            distList = MathPlag.compareIdenticalMathML(
                    MathDoc.tryFixHeader(goldDocString),
                    MathDoc.tryFixHeader(compareTree)
            );
            out.append( "  ++++++ MathPlag Identical Distance ++++++ " ).append(NL);
            addMatchOverview( out, distList );
            out.append( distList.toString() );
        } catch ( ParserConfigurationException | XPathExpressionException | IOException e ){
            LOG.warn("Deeper content distance error from MathPlag.", e);
        }



        if ( id == 4 ) lastTree = compareTree;
        results.add(out.append(NL).toString());
    }

    private void addMatchOverview( StringBuilder out, List<Match> distList ){
        while ( !distList.isEmpty() ){
            Match m = distList.remove(0);
            out.append( "Match: [ID:" + m.getId() + "; Depth:" + m.getDepth() + "; Coverage:" + m.getCoverage() + "]").append(NL);
            for ( SubMatch sm : m.getMatches() ){
                out.append( "Sub-" + sm.getId() + " (" + sm.getType() + "): " );
                out.append( "[Assessment:" + sm.getAssessment() + "; " );
                out.append( "Depth:" + sm.getDepth() + "; ");
                out.append( "Coverage:" + sm.getCoverage() + "]").append(NL);
            }
            out.append( "<<< ").append(NL);
        }
        out.append(NL);
    }
}
