package de.tuberlin.dima.schubotz.fse;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.io.TextInputFormat;
import eu.stratosphere.api.java.operators.DataSource;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.typeutils.BasicTypeInfo;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.core.fs.Path;
import eu.stratosphere.util.Collector;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class KeywordMapper extends FlatMapFunction<String, KeyWordTuple> {
    private Map<String, String> keywords;
    
    final static String FILENAME_INDICATOR = "Filename";
    final static Pattern filnamePattern = Pattern
            .compile("<ARXIVFILESPLIT\\\\n" + FILENAME_INDICATOR + "=\"\\./\\d+/(\\d+)\\.(\\d+)/\\1.\\2_(\\d+)_(\\d+)\\.xhtml\">");
    
    
    @Override
    public void open(Configuration parameters) throws Exception {
    	//Setup formulae, keywords from queries
        keywords = new HashMap<>();
        Collection<Query> queries = getRuntimeContext().getBroadcastVariable("Queries");
        for (Query query : queries) {
            for (Map.Entry<String, String> keyword : query.keywords.entrySet()) {
                String[] tokens = keyword.getValue().toLowerCase().split("\\W+"); //match all non word characters non-greedily repeated
                Integer i = 0;
                for (String token : tokens) {
                    i++; //Handle repeating keywords
                    keywords.put(token, query.name + keyword.getKey() + i.toString());//flipped because we are searching by token
                }
            }
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
    public void flatMap(String value, Collector<KeyWordTuple> out) throws Exception {
        SectionTuple sectionTuple = new SectionTuple();
		//Split into lines 0: ARXIVFILENAME, 1: HTML
        String[] lines = value.trim().split("\\n", 2);
        if (lines.length < 2)
            return;
        Matcher matcher = filnamePattern.matcher(lines[0]);
        String docID = null;
        if (matcher.find()) {

            SectionNameTuple sectionNameTuple = new SectionNameTuple(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))
            );
        }
        //Extract plaintext from article
        Document doc = XMLHelper.String2Doc(lines[1], false);
        String plainText = Jsoup.parse(lines[1]).text();
        String[] tokens = plainText.toLowerCase().split("\\W+");
        Integer j = 0;
        
        //TODO: Implement keyword results
        //Keywords: keyword, id
        for (String token : tokens) {
            j++;
            if (keywords.containsKey(token)) {
            	//add to keyword tuple list for reduction later
//DEBUG OUTPUT 
                System.out.println("match for keyword " + j.toString() + token + keywords.get(token));
            }
        }
    }

}
