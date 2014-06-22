package de.tuberlin.dima.schubotz.fse;


import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import org.w3c.dom.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Moritz on 20.06.2014.
 */
public class ArticleMapper extends MapFunction<String,Tuple2<String,Integer>> {
    /** The Constant mathpattern. */
    final static Pattern mathpattern = Pattern
            .compile("(<m:math|<math) (?:xmlns=\"http://www.w3.org/1998/Math/MathML\" )?id=\"(.*?)\"( alttext=\"(.*?)\")?(.*?)(</math>|</m:math>)");
    final static String FILENAME_INDICATOR = "Filename";
    final static Pattern filnamePattern = Pattern
            .compile("<ARXIVFILESPLIT\\\\n"+FILENAME_INDICATOR + "=\"\\./\\d+/(\\d+)\\.(\\d+)/\\1.\\2_(\\d+)_(\\d+)\\.xhtml\">");
    /** The Constant GRP_ID. */
    public final static int GRP_ID = 2;

    /** The Constant GRP_TEX. */
    public final static int GRP_TEX = 4;
    /** The Constant FILENAME_INDICATOR. */


    /**
     * The core method of the MapFunction. Takes an element from the input data set and transforms
     * it into another element.
     *
     * @param value The input value.
     * @return The value produced by the map function from the input value.
     * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
     *                   to fail and may trigger recovery.
     */
    @Override
    public Tuple2<String, Integer> map(String value) throws Exception {
        String[] lines = value.trim().split("\\n", 2);
        if (lines.length < 2)
            return new Tuple2<>("",0);
        Matcher matcher = filnamePattern.matcher(lines[0]);
        String docID = null;
        if (matcher.find()) {
            docID = matcher.group(0);
        }
        Document doc = XMLHelper.String2Doc(lines[1], false);

        int mathCount = XMLHelper.getElementsB(doc,"//math").getLength();
        return new Tuple2<>(docID,mathCount);
    }
}
