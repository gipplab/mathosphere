package de.tuberlin.dima.schubotz.fse;


import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

import net.htmlparser.jericho.*;

/**
 * Created by Moritz on 20.06.2014.
 */
public class ArticleMapper extends FlatMapFunction<String, Article> {
    /**
     * The Constant GRP_ID.
     */
    public final static int GRP_ID = 2;
    /**
     * The Constant GRP_TEX.
     */
    public final static int GRP_TEX = 4;
    /** The Constant mathpattern. */
    final static Pattern mathpattern = Pattern
            .compile("(<m:math|<math) (?:xmlns=\"http://www.w3.org/1998/Math/MathML\" )?id=\"(.*?)\"( alttext=\"(.*?)\")?(.*?)(</math>|</m:math>)");
    final static String FILENAME_INDICATOR = "Filename";
    final static Pattern filnamePattern = Pattern
            .compile("<ARXIVFILESPLIT\\\\n" + FILENAME_INDICATOR + "=\"\\./\\d+/(\\d+)\\.(\\d+)/\\1.\\2_(\\d+)_(\\d+)\\.xhtml\">");

    /** The Constant FILENAME_INDICATOR. */

    public static String getPlainText(InputStream is) throws IOException, MalformedURLException {
    	//Using Jericho HTMLParser
    	Source source=new Source(is);
    	source.fullSequentialParse();
    	TextExtractor textExtractor=new TextExtractor(source);
        return textExtractor.setIncludeAttributes(false).toString();
    }
    

    public String observationsFromMml(Node mml) {
        return "";

    }
    /**
     * The core method of the MapFunction. Takes an element from the input data set and transforms
     * it into another element.
     *
     * @param value The input value.
     * @return The value produced by the map function from the input value.
     * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
     *                   to fail and may trigger recovery.
     */
    public Tuple2<String, Integer> map(String value) throws Exception { //demo, returns DocumentID and number of formulae
        String[] lines = value.trim().split("\\n", 2);
        if (lines.length < 2)
            return new Tuple2<>("",0);
        Matcher matcher = filnamePattern.matcher(lines[0]);
        String docID = null;
        if (matcher.find()) {
            docID = matcher.group(0);
        }
        Document doc = XMLHelper.String2Doc(lines[1], false);
        NodeList MathMLElements = XMLHelper.getElementsB(doc, "//math");
        for (int i = 0; i < MathMLElements.getLength(); i++) {
            observationsFromMml(MathMLElements.item(i));
        }

        int mathCount = XMLHelper.getElementsB(doc,"//math").getLength();
        return new Tuple2<>(docID, mathCount);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        Collection<Query> queries = getRuntimeContext().getBroadcastVariable("Queries");
        for (Query query : queries) { 
        	for (Map.Entry<String, String> formula : query.formulae.entrySet()) {
        		Node node = XMLHelper.String2Doc(formula.getValue(),false);
        		//query.formulae.put(query.name+formula.getKey(),node);
        		//up to 4 keywords. no null keywords
        	}
        	// TODO:implement similar loop for keywords
        }

        super.open(parameters);
    }

    /**
     * The core method of the FlatMapFunction. Takes an element from the input data set and transforms
     * it into zero, one, or more elements.
     *
     * @param value The input value.
     * @param out   The collector for for emitting result values.
     * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
     *                   to fail and may trigger recovery.
     */
    @Override
    public void flatMap(String value, Collector<Article> out) throws Exception {
    	//returns filename, document contents
        String[] lines = value.trim().split("\\n", 2);
        if (lines.length < 2)
            return;
        Matcher matcher = filnamePattern.matcher(lines[0]);
        String docID = null;
        if (matcher.find()) {
            docID = matcher.group(0);
        }
        out.collect(new Article(docID, lines[1]));
        /* Formula counter
        Document doc = XMLHelper.String2Doc(lines[1], false);
        NodeList MathMLElements = XMLHelper.getElementsB(doc, "//math");
        for (int i = 0; i < MathMLElements.getLength(); i++) {
            observationsFromMml(MathMLElements.item(i));
        }

        int mathCount = XMLHelper.getElementsB(doc, "//math").getLength();
        */
    }
}
