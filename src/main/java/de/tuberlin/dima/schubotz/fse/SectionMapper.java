package de.tuberlin.dima.schubotz.fse;


import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.configuration.Configuration;
import eu.stratosphere.util.Collector;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SectionMapper extends FlatMapFunction<String, SectionTuple> {
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
    private Map<String,Node> formulae;
    private Map<String, String> keywords;
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
        formulae = new HashMap<>();
        keywords = new HashMap<>();
        Collection<Query> queries = getRuntimeContext().getBroadcastVariable("Queries");
        for (Query query : queries) {
            for (Map.Entry<String, String> formula : query.formulae.entrySet()) {
                Node node = XMLHelper.String2Doc(formula.getValue(),false);
                formulae.put(query.name+formula.getKey(),node);
            }
            for (Map.Entry<String, String> keyword : query.keywords.entrySet()) {
                String[] tokens = keyword.getValue().toLowerCase().split("\\W+"); //What does this match?
                Integer i = 0;
                for (String token : tokens) {
                    i++;
                    keywords.put(token, query.name + keyword.getKey() + i.toString());
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
    public void flatMap(String value, Collector<SectionTuple> out) throws Exception {
        SectionTuple sectionTuple = new SectionTuple();
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
        /* Formula counter */
        Document doc = XMLHelper.String2Doc(lines[1], false);
        String plainText = Jsoup.parse(lines[1]).text();
        String[] tokens = plainText.toLowerCase().split("\\W+");
        Integer j = 0;
        for (String token : tokens) {
            j++;
            if (keywords.containsKey(token)) {
                System.out.println("match for keyword " + j.toString() + token + keywords.get(token));
            }
        }
        NodeList MathMLElements = XMLHelper.getElementsB(doc, "//math");
        for (int i = 0; i < MathMLElements.getLength(); i++) {
            for (Map.Entry<String, Node> entry : formulae.entrySet()) {
                Map<String, Node> qvars = null;
                if ( XMLHelper.compareNode(entry.getValue(),MathMLElements.item(i),true,qvars) ) {
                    HitTuple hitTuple = new HitTuple();
                    if (qvars != null){
                        explicitDataSet<QVarTuple> qvarDataSet = new explicitDataSet<>();
                        for (Map.Entry<String, Node> nodeEntry : qvars.entrySet()) {
                            QVarTuple tuple = new QVarTuple();
                            tuple.setQVar(nodeEntry.getKey());
                            tuple.setXRef(nodeEntry.getValue().getAttributes().getNamedItem("id").getNodeValue());
                            qvarDataSet.add(tuple);
                        }
                    }
                    hitTuple.setQueryID(entry.getKey());
                    hitTuple.setScore(100.);
                    hitTuple.setXref(docID);
                    out.collect(sectionTuple);
                }
            }
        }
    }
}
