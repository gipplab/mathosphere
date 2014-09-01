package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.types.DataTuple;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.ExtractHelper;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.regex.Pattern;

public class DataPreprocess extends FlatMapFunction<RawDataTuple, DataTuple> {
	private final Pattern WORD_SPLIT;
	private final String STR_SPLIT;

	private static final SafeLogWrapper LOG = new SafeLogWrapper(DataPreprocess.class);
	
	public DataPreprocess(Pattern WORD_SPLIT, String STR_SPLIT) {
		this.WORD_SPLIT = WORD_SPLIT;
		this.STR_SPLIT = STR_SPLIT;
	}

    /**
     * Takes in cleaned document, outputs tuple
     * containing all document data extracted.
     * @param in RawDataTuple
     * @param out DataTuple of document
     */
	@Override
	public void flatMap(RawDataTuple in, Collector<DataTuple> out) {
        final String docID = in.getNamedField(RawDataTuple.fields.ID);
        final String data = in.getNamedField(RawDataTuple.fields.rawData);
        final Document doc;

        try {
            doc = Jsoup.parse(data, "", Parser.xmlParser());
        } catch (final RuntimeException e) {
            LOG.warn("Unable to parse XML data document: ", data, e);
            return;
        }

        //Math and latex extraction
        final Elements MathElements = doc.select("math, m|math");
        if (MathElements.isEmpty()) {
            LOG.warn("Unable to find math tags in data document: ", data);
            return;
        }
        final StringBuilder outputLatex = new StringBuilder();
        final StringBuilder cmml = new StringBuilder();
        final StringBuilder pmml = new StringBuilder();
        for (final Element MathElement : MathElements) {
            ExtractHelper.processMathElement(MathElement, docID, outputLatex, cmml, pmml, STR_SPLIT);
        }

        //Delete all math so they don't show up in keyword list
        MathElements.remove();

        // Keyword list generation
        // WARNING: THIS IS RELIANT ON ALL NON RELEVANT TEXT (SUCH AS
        // TITLE TEXT) BEING STRIPPED ALREADY
        final String plainText = doc.text();
        final StringBuilder keywordList = new StringBuilder();
        final String[] tokens = WORD_SPLIT.split(plainText.toLowerCase());
 		for (final String token : tokens) {
            if (keywordList.length() == 0) {
                keywordList.append(token);
            } else {
                keywordList.append(STR_SPLIT);
                keywordList.append(token);
            }
        }

        //Stringify then output
        final String outputLatexStr = outputLatex.toString();
        final String cmmlStr = cmml.toString();
        final String pmmlStr = pmml.toString();
        if (outputLatexStr.isEmpty() && cmmlStr.isEmpty() && pmmlStr.isEmpty()) {
            LOG.info(docID, " has no math.");
        } else {
            out.collect(new DataTuple(
                    docID, outputLatex.toString(), keywordList.toString(), cmml.toString(), pmml.toString()));
        }
        LOG.info(docID, " complete!");
	}
}
