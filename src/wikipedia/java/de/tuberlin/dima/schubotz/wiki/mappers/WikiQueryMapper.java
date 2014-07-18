package de.tuberlin.dima.schubotz.wiki.mappers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.tuberlin.dima.schubotz.common.utils.ExtractHelper;
import de.tuberlin.dima.schubotz.common.utils.XMLHelper;
import de.tuberlin.dima.schubotz.fse.MainProgram;
import de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.util.Collector;


@SuppressWarnings("serial")
public class WikiQueryMapper extends FlatMapFunction<String,WikiQueryTuple>{
	Log LOG = LogFactory.getLog(WikiQueryMapper.class);
	/**
	 * See {@link MainProgram#STR_SPLIT}
	 */
	String STR_SPLIT;
	
	/**
	 * @param STR_SPLIT {@link MainProgram#STR_SPLIT} sent as parameter to ensure serializability
	 */
	@SuppressWarnings("hiding")
	public WikiQueryMapper(String STR_SPLIT) {
		this.STR_SPLIT = STR_SPLIT;
	}
	
	/**
	 * Takes in a query, parses and outputs {@link de.tuberlin.dima.schubotz.wiki.types.WikiQueryTuple}
	 */
	@Override
	public void flatMap(String in, Collector<WikiQueryTuple> out) {
		Document doc;
		Element main;
		try {
			doc = Jsoup.parse(in);
			main = doc.getElementsByTag("num").first(); //title is in <num>
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unable to parse XML in query: " + in);
			}
			return;
		}
		
		String queryID = main.text();
		if (queryID == null) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Null query id, assigning this_was_null: " + in);
			}
			queryID = "this_was_null";
		}
		
		//only one math and one semantics per query, so extract semantics children directly
		Elements MMLElements = doc.select("m:math.m:semantics").first().children(); 
		Elements PmmlElements = new Elements();
		Elements CmmlElements = new Elements();
		Elements LatexElements = new Elements();
		String encoding;
		//All queries are well formed: 1) Content MML 2) annotation-PMML 3) annotation-TEX
		//Any element not under annotation tag is Content MML
		for (Element curElement : MMLElements) { 
			if (curElement.tagName().equals("m:annotation-xml")) {
				encoding = curElement.attr("encoding");
				if (encoding.equals("MathML-Presentation")) { 
					PmmlElements.add(curElement);
				} else if (encoding.equals("application/x-tex")) {
					LatexElements.add(curElement);
				}
			} else {   
				CmmlElements.add(curElement);
			}
		}
		String latex = ExtractHelper.extractLatex(LatexElements, STR_SPLIT);
		
		Document cmml = ExtractHelper.extractCanonicalizedDoc(CmmlElements);
		Document pmml = ExtractHelper.extractCanonicalizedDoc(PmmlElements);
		
		try {
			out.collect(new WikiQueryTuple(queryID,latex,cmml,pmml));
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Extract helper failed on query: " + in);
			}
		}
	}
	
}
