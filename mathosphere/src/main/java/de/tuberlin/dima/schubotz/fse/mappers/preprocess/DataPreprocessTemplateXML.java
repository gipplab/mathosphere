package de.tuberlin.dima.schubotz.fse.mappers.preprocess;

import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import de.tuberlin.dima.schubotz.fse.utils.XMLHelper;
import org.apache.flink.api.java.functions.RichFlatMapFunction;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


/**
 * Created by Moritz on 06.09.2014.
 */
public abstract class DataPreprocessTemplateXML<T> extends RichFlatMapFunction<RawDataTuple, T> {
	protected static final SafeLogWrapper LOG = new SafeLogWrapper(DataPreprocess.class);
	protected String docID;
	protected String data;
	Document doc;
	NodeList mathElements;

	protected boolean setDoc () {
		try {
			doc = XMLHelper.String2Doc( data,true );
			return true;
		} catch (final IOException|ParserConfigurationException e) {
		    LOG.warn("Unable to parse XML data document(" + docID + "): ", data, e);
			return  false;
		}
	}

	protected boolean setMath(){
		if( doc == null){
			return false;
		}
		mathElements = doc.getElementsByTagNameNS( "http://www.w3.org/1998/Math/MathML", "math" );
		if ( mathElements.getLength() == 0) {
			LOG.warn("Unable to find math tags in data document(\" + docID + \"): ", data);
			return false;
		}
		return true;
	}
}
