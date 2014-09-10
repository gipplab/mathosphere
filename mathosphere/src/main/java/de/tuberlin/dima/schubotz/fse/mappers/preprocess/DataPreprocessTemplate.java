package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.mappers.preprocess.DataPreprocess;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import org.apache.flink.api.java.functions.RichFlatMapFunction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public abstract class DataPreprocessTemplate<K,T> extends RichFlatMapFunction< K,  T> {
	protected static final SafeLogWrapper LOG = new SafeLogWrapper(DataPreprocess.class);
	public String docID;
	public String data;
	public Elements mathElements;
	public Document doc;

	public boolean setDoc() {
		try {
		    doc = Jsoup.parse( data, "", Parser.xmlParser() );
			return true;
		} catch (final RuntimeException e) {
		    LOG.warn("Unable to parse XML data document(" + docID + "): ", data, e);
			return  false;
		}
	}

	public boolean setMath(){
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
