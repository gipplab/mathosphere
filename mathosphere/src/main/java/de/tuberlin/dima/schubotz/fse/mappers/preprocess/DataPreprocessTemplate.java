package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 * Created by Moritz on 06.09.2014.
 */
public abstract class DataPreprocessTemplate<T> extends FlatMapFunction<RawDataTuple, T> {
	protected static final SafeLogWrapper LOG = new SafeLogWrapper(DataPreprocess.class);
	protected String docID;
	protected String data;
	protected Elements mathElements;
	Document doc;

	protected boolean setDoc () {
		try {
		    doc = Jsoup.parse( data, "", Parser.xmlParser() );
			return true;
		} catch (final RuntimeException e) {
		    LOG.warn("Unable to parse XML data document(" + docID + "): ", data, e);
			return  false;
		}
	}

	protected boolean setMath(){
		if( doc == null){
			return false;
		}
		mathElements = doc.select("math, m|math");
		if ( mathElements.isEmpty()) {
			LOG.warn("Unable to find math tags in data document(\" + docID + \"): ", data);
			return false;
		}
		return true;
	}
}
